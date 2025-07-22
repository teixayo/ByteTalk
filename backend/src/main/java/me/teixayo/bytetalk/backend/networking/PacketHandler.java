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
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacketType;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.protocol.server.StatusCodes;
import me.teixayo.bytetalk.backend.security.Crypto;
import me.teixayo.bytetalk.backend.security.EncryptionUtils;
import me.teixayo.bytetalk.backend.service.channel.Channel;
import me.teixayo.bytetalk.backend.user.User;
import me.teixayo.bytetalk.backend.user.UserConnection;
import me.teixayo.bytetalk.backend.user.UserManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PacketHandler extends SimpleChannelInboundHandler<Object> {
    private static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;
    private User user;

    private ScheduledFuture<?> pingFuture;
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
        if (Server.getInstance().getConfig().isSslToggleUsingWSS()) {
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
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }


        String jwtToken = req.headers().get(HttpHeaderNames.COOKIE);
        User user = null;
        if (jwtToken != null) {
            jwtToken = jwtToken.replace("token=","");
            DecodedJWT jwt = EncryptionUtils.decryptToken(jwtToken);
            if (jwt == null) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
                log.info("Disconnected {} cause of invalid token", socketAddress.getAddress().getHostAddress());
                return;
            }
            String name = jwt.getSubject();
            if (name == null) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
                return;
            }
            user = Server.getInstance().getUserService().getUserByUserName(name);
            if (user == null) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
                log.info("Disconnected {} cause of invalid user", socketAddress.getAddress().getHostAddress());
                return;
            }
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }
        handshaker.handshake(ctx.channel(), req);
        ctx.channel().attr(ChannelInitializer.getHandshake()).set(handshaker);

        if(user!=null) {
            loggedIn(ctx, true, user);
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
                String name = jsonObject.getString("username");
                User user = Server.getInstance().getUserService().getUserByUserName(name);
                if (user == null) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.NOT_SUCCESS_LOGIN_WITH_PASSWORD.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid user", socketAddress.getAddress().getHostAddress());
                    return;
                }
                String password = Crypto.encryptSHA256(jsonObject.getString("password"));
                if (!user.getPassword().equals(password)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.NOT_SUCCESS_LOGIN_WITH_PASSWORD.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid password", socketAddress.getAddress().getHostAddress());
                    return;
                }
                loggedIn(ctx, false, user);
            }
            if (type.equals("CreateUser")) {
                String name = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                if (!EncryptionUtils.isValidName(name)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INVALID_USER.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid name", socketAddress.getAddress().getHostAddress());
                    return;
                }
                if (!EncryptionUtils.isValidPassword(password)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.INVALID_PASSWORD.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of invalid password", socketAddress.getAddress().getHostAddress());
                    return;
                }
                if (Server.getInstance().getUserService().isUserExists(name)) {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.USER_EXISTS.createPacket().getData().toString()));
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                    log.info("Disconnected {} cause of using created exists names for signup", socketAddress.getAddress().getHostAddress());
                    return;
                }
                log.info("User {} Created", name);
                password = Crypto.encryptSHA256(password);
                Server.getInstance().getUserService().saveUser(name, password);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(StatusCodes.SUCCESS_SIGNUP.createPacket().getData().toString()));
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
            log.info("Disconnected {} cause of using bad packets", socketAddress.getAddress().getHostAddress());
        }
    }

    private void loggedIn(ChannelHandlerContext ctx, boolean useToken, User user) {
        ctx.channel().attr(ChannelInitializer.getState()).set(ClientStateType.AFTER_LOGIN);

        user.setUserConnection(new UserConnection(ctx, user));
        this.user = user;
        UserManager.getInstance().addUser(user);
        if (useToken) {
            user.sendPacket(StatusCodes.SUCCESS_LOGIN_WITH_TOKEN.createPacket());
        } else {
            user.sendPacket(StatusCodes.SUCCESS_LOGIN_WITH_PASSWORD.createPacket());
        }
        if (EncryptionUtils.getJWT(user.getName()) == null) {
            user.sendPacket(ServerPacketType.LoginToken.createPacket("token",
                    EncryptionUtils.createLoginJWT(user.getName())));
            log.info("Created token for {}", user.getName());
        }
        JSONArray usersArray = new JSONArray();
        for (Channel privateChannel : Server.getInstance().getChannelService().getUserPrivateChannels(user.getId())) {
            JSONObject jsonObject = new JSONObject();

            Optional<Long> targetUserID = privateChannel.getMembers().stream().filter(id -> id != user.getId()).findFirst();
            if(targetUserID.isEmpty()) continue;

            User targetUser = Server.getInstance().getUserService().getUserById(targetUserID.get());

            jsonObject.put("name", targetUser.getName());
            jsonObject.put("creationDate", privateChannel.getCreationDate());
            usersArray.put(jsonObject);
        }
        JSONObject jsonObject1 = new JSONObject();

        jsonObject1.put("channels", usersArray);
        user.sendPacket(ServerPacketType.UserPrivateChannels.createPacket(jsonObject1));

        log.info("User {} logged in", user.getName());
    }

    @SneakyThrows
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        pingFuture = ctx.executor().scheduleAtFixedRate(() -> {
                    if (!ctx.channel().isActive()) return;
                    ctx.writeAndFlush(new PingWebSocketFrame());
                }
                , 0, Server.getInstance().getConfig().getMaxTimeOut() / 2, TimeUnit.SECONDS);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (pingFuture != null) {
            pingFuture.cancel(false);
        }
        super.channelInactive(ctx);
    }
}
