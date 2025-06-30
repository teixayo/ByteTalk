package me.teixayo.bytetalk.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacketType;
import me.teixayo.bytetalk.backend.protocol.server.StatusCodes;
import org.json.JSONObject;

import java.net.URI;
import java.util.Scanner;

@Slf4j
public class NettyWebSocketExample {

    public static String password;
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

                Scanner scanner = new Scanner(System.in);
                if(scanner.hasNextLine()){
                    if (scanner.nextLine().equals("1")) {
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("type", "CreateUser");

                        jsonObject.put("name", "test");
                        jsonObject.put("password", scanner.nextLine());
                        password = jsonObject.getString("password");
                        ch.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));


                        Thread.sleep(1000);


                        ch.writeAndFlush(new TextWebSocketFrame(
                                ClientPacketType.RequestBulkMessage.createPacket(
                                                "time", String.valueOf(System.currentTimeMillis()))
                                        .getData().toString()));

                        Thread.sleep(5000);
                        while (scanner.hasNextLine()) {
                            ch.writeAndFlush(new TextWebSocketFrame(ClientPacketType.SendMessage.createPacket(
                                    "content", scanner.nextLine()).getData().toString()));
                        }
                    } else {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "Login");
                        jsonObject.put("name", "test");
                        jsonObject.put("password", scanner.nextLine());
                        ch.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));

                        Thread.sleep(1000);
                        ch.writeAndFlush(new TextWebSocketFrame(
                                ClientPacketType.RequestBulkMessage.createPacket(
                                                "time", String.valueOf(System.currentTimeMillis()))
                                        .getData().toString()));

                        Thread.sleep(5000);
                        while (scanner.hasNextLine()) {
                            ch.writeAndFlush(new TextWebSocketFrame(ClientPacketType.SendMessage.createPacket(
                                    "content", scanner.nextLine()).getData().toString()));
                        }

                    }
                }
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
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
                System.out.println("Ping");
                return;
            } else if (frame instanceof CloseWebSocketFrame) {
                System.out.println("Client received closing");
                ch.close();
            } else {
                System.out.println("Client received: " + ((TextWebSocketFrame) frame).text());
                JSONObject jsonObject = new JSONObject(((TextWebSocketFrame) frame).text());
                String type = jsonObject.getString("type");
                if(type.equals("Status") && jsonObject.getInt("code")== StatusCodes.SUCCESS.getStatusCode()) {
                    jsonObject.put("type","Login");
                    jsonObject.put("name", "test");
                    jsonObject.put("password",password);
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
