package me.teixayo.bytetalk.backend.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.CharsetUtil;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacketType;
import org.json.JSONObject;

import java.net.URI;
import java.util.Scanner;

/**
 * Combined server and client example for Netty WebSocket
 */
public class NettyWebSocketExample {
    // Server-side handler as before
    public static class NettyWebSocketHandler extends SimpleChannelInboundHandler<Object> {
        private static final String WEBSOCKET_PATH = "/websocket";
        private WebSocketServerHandshaker handshaker;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            if (!req.decoderResult().isSuccess() || !"websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
                sendHttpResponse(ctx, req,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
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

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass().getName());
            }
            String requestText = ((TextWebSocketFrame) frame).text();
            System.out.println("Server received: " + requestText);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(requestText));
        }

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
            String origin = req.headers().get(HttpHeaderNames.ORIGIN, "");
            if (origin.startsWith("https")) {
                protocol = "wss";
            }
            return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        }
    }

    // Client-side implementation
    public static class NettyWebSocketClient {
        public static void main(String[] args) throws Exception {
            URI uri = new URI("ws://0.0.0.0:25565/websocket");
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                final WebSocketClientHandler handler = new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new HttpClientCodec());
                                p.addLast(new HttpObjectAggregator(8192));
                                p.addLast(WebSocketClientCompressionHandler.INSTANCE);
                                p.addLast(handler);
                            }
                        });

                Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
                handler.handshakeFuture().sync();

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("type", "CreateUser");
                jsonObject.put("name", "test");
                ch.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));


                Thread.sleep(1000);
                Scanner scanner = new Scanner(System.in);

                ch.writeAndFlush(new TextWebSocketFrame(
                        ClientPacketType.RequestBulkMessage.createPacket(
                        "time", String.valueOf(System.currentTimeMillis()))
                                .getData().toString()));
                while (scanner.hasNextLine()) {
                    ch.writeAndFlush(new TextWebSocketFrame(ClientPacketType.SendMessage.createPacket(
                            "content", scanner.nextLine()).getData().toString()));
                }
                // Wait to receive echo
                ch.closeFuture().sync();
            } finally {
                group.shutdownGracefully();
            }
        }
    }

    /**
     * Handler for client messages and handshake
     */
    public static class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return handshakeFuture;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel ch = ctx.channel();
            if (!handshaker.isHandshakeComplete()) {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
                return;
            }
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse: " + response.status());
            }
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof PongWebSocketFrame) {
                System.out.println("Client received Pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                System.out.println("Client received closing");
                ch.close();
            } else {
                System.out.println("Client received: " + ((TextWebSocketFrame) frame).text());
                JSONObject jsonObject = new JSONObject(((TextWebSocketFrame) frame).text());
                String type = jsonObject.getString("type");
                if(type.equals("GetToken")) {
                    String token = jsonObject.getString("token");

                    jsonObject.put("type","Login");
                    jsonObject.put("name", "test");
                    jsonObject.put("token",token);
                    ch.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));
                } else {
                    System.out.println(jsonObject.toString());
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }
}
