package me.teixayo.bytetalk.backend.networking;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHandler {
    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(
            Server.getInstance().getConfig().getNetworkingWriteBufferWaterMarkLow(),
            Server.getInstance().getConfig().getNetworkingWriteBufferWaterMarkHigh());
    private EventLoopGroup workerGroup;
    private ChannelInitializer channelInitializer;

    public NettyHandler() {
        Thread thread = new Thread(() -> {

            TransportType transportType = Server.getInstance().getConfig().getNetworkingTransportType();
            channelInitializer = new ChannelInitializer();
            log.info("Using {} threads for Netty based {}", NettyRuntime.availableProcessors() * 2, transportType.name());
            try {
                workerGroup = transportType.createEventLoopGroup();

                ServerBootstrap bootstrap = new ServerBootstrap()
                        .channelFactory(transportType.getServerSocketChannelFactory())
                        .group(workerGroup)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(channelInitializer)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                        .childOption(ChannelOption.IP_TOS, 0x18)
                        .localAddress(new InetSocketAddress(Server.getInstance().getConfig().getNetworkingIp(), Server.getInstance().getConfig().getNetworkingPort()));
                if (Server.getInstance().getConfig().isNetworkingTcpFastOpen()) {
                    bootstrap.option(ChannelOption.TCP_FASTOPEN, 3);
                }

                if (Server.getInstance().getConfig().isNetworkingTcpNoDelay()) {
                    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                }
                if (transportType != TransportType.NIO) {
                    bootstrap.option(UnixChannelOption.SO_REUSEPORT, true);
                }
                bootstrap.bind()
                        .addListener((ChannelFutureListener) future -> {
                            Channel channel = future.channel();
                            if (future.isSuccess()) {
                                log.info("Listening on {}", channel.localAddress());
                                return;
                            }
                            log.error("Can't bind to {}", Server.getInstance().getConfig().getNetworkingIp(), future.cause());
                        }).sync().channel().closeFuture().sync();

            } catch (InterruptedException e) {
                workerGroup.shutdownGracefully();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        thread.setName("Netty");
        thread.start();
    }
}
