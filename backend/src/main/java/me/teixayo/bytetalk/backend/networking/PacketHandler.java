package me.teixayo.bytetalk.backend.networking;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacketType;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.protocol.server.StatusCodes;
import me.teixayo.bytetalk.backend.security.EncryptionUtils;
import me.teixayo.bytetalk.backend.user.User;
import me.teixayo.bytetalk.backend.user.UserConnection;
import me.teixayo.bytetalk.backend.user.UserManager;
import org.json.JSONObject;

import java.net.InetSocketAddress;

@Slf4j
public class PacketHandler extends SimpleChannelInboundHandler<Object> {
    private static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;
    private User user;

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String protocol = "ws";
        if (req.headers().get(HttpHeaderNames.ORIGIN).startsWith("https")) {
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
    }

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(new PingWebSocketFrame());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            if (user != null) user.getUserConnection().keepAlive();
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("Unsupported frame type: %s", frame.getClass().getName()));
        }
        String requestText = ((TextWebSocketFrame) frame).text();
        JSONObject jsonObject = new JSONObject(requestText);
        String type = jsonObject.getString("type");

        if (ctx.channel().attr(ChannelInitializer.getState()).get() == ClientStateType.IN_LOGIN) {
            if (type.equals("Login")) {
                String name = jsonObject.getString("name");

                User user = Server.getInstance().getUserService().getUserByUserName(name);

                if (user == null) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INCORRECT_USER_OR_PASSWORD.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid user",socketAddress.getAddress().getHostAddress());
                    return;
                }
                if (jsonObject.has("token")) {
                    String token = jsonObject.getString("token");
                    DecodedJWT jwt = EncryptionUtils.getJWT(name);
                    if (jwt == null || !jwt.getToken().equals(token)) {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INCORRECT_USER_OR_PASSWORD.createPacket().getData().toString()));
                        handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                        log.info("Disconnected {} cause of invalid token",socketAddress.getAddress().getHostAddress());
                        return;
                    }
                } else {
                    String password = EncryptionUtils.encrypt(jsonObject.getString("password"));
                    if (!user.getPassword().equals(password)) {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INCORRECT_USER_OR_PASSWORD.createPacket().getData().toString()));
                        handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                        log.info("Disconnected {} cause of invalid password",socketAddress.getAddress().getHostAddress());
                        return;
                    }
                }
                ctx.channel().attr(ChannelInitializer.getState()).set(ClientStateType.AFTER_LOGIN);

                user.setUserConnection(new UserConnection(ctx, user));
                this.user = user;
                UserManager.getInstance().addUser(user);
                user.sendPacket(StatusCodes.SUCCESS.createPacket());
                if (EncryptionUtils.getJWT(name) == null) {
                    user.sendPacket(ServerPacketType.LoginToken.createPacket("token",
                            EncryptionUtils.createLoginJWT(name)));
                }
                user.sendMessages(Server.getInstance().getCacheService().loadLastestMessages());
                log.info("User {} logged in", name);
                return;
            }
            if (type.equals("CreateUser")) {
                String name = jsonObject.getString("name");
                String password = jsonObject.getString("password");
                if (!EncryptionUtils.isValidName(name)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INVALID_USER.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid name",socketAddress.getAddress().getHostAddress());
                    return;
                }
                if (!EncryptionUtils.isValidPassword(password)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INVALID_PASSWORD.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid password",socketAddress.getAddress().getHostAddress());
                    return;
                }
                if (Server.getInstance().getUserService().isUserExists(name)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.USER_EXISTS.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of using created names for signup",socketAddress.getAddress().getHostAddress());
                    return;
                }
                log.info("User {} Created", name);
                password = EncryptionUtils.encrypt(password);
                Server.getInstance().getUserService().saveUser(name, password);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.SUCCESS.createPacket().getData().toString()));
            }
        } else {
            ClientPacketType packetType;
            try {
                packetType = ClientPacketType.valueOf(type);
            } catch (IllegalArgumentException exception) {
                user.getUserConnection().disconnect();
                return;
            }
            ClientPacket packet = packetType.createPacket(jsonObject);
            user.getUserConnection().handleClientPacket(packet);
        }
    }


}
