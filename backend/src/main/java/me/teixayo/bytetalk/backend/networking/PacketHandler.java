package me.teixayo.bytetalk.backend.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacketType;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.user.User;
import me.teixayo.bytetalk.backend.user.UserConnection;
import me.teixayo.bytetalk.backend.user.UserManager;
import org.json.JSONObject;

@Slf4j
public class PacketHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;

    private User user;
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
        // Handle bad request
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
    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("Unsupported frame type: %s", frame.getClass().getName()));
        }
        String requestText = ((TextWebSocketFrame) frame).text();
        JSONObject jsonObject = new JSONObject(requestText);
        String type = jsonObject.getString("type");

        if(ctx.channel().attr(ChannelInitializer.getState()).get()== ClientStateType.IN_LOGIN) {
            if(type.equals("Login")) {
                String name = jsonObject.getString("name");
                String token =  jsonObject.getString("token");

                User user = Server.getInstance().getUserService().getUserByUserName(name);

                if(user==null) {
                    handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                    return;
                }
//                if(!user.getToken().equals(token)) {
//                    handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
//                    return;
//                }
                ctx.channel().attr(ChannelInitializer.getState()).set(ClientStateType.AFTER_LOGIN);

                user.setUserConnection(new UserConnection(ctx,user));
                this.user = user;
                UserManager.getInstance().addUser(user);
                user.sendPacket(ServerPacketType.SuccessLogin.createPacket(
                        "description", "You successfully logged in"));
                return;
            }
            if(type.equals("CreateUser")) {
                String name = jsonObject.getString("name");
//                if (!Server.getInstance().getUserService().isUserExists(name)) {
                    String token = Server.getInstance().getUserService().saveUser(name);
                    JSONObject output = new JSONObject();
                    output.put("type", "GetToken");
                    output.put("token", token);

                    ctx.channel().writeAndFlush(
                            new TextWebSocketFrame(output.toString()));
//                }
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

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate error page if response status code is not OK
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close connection if necessary
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


}
