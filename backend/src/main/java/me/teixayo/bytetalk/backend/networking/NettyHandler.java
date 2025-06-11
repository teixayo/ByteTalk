package me.teixayo.bytetalk.backend.networking;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHandler {

    public int threads = Runtime.getRuntime().availableProcessors();
    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(1048576,
            2097152);

    private EventLoopGroup workerGroup;
    private ChannelInitializer channelInitializer;

    public NettyHandler(String address, int port) {
        Thread thread = new Thread(() -> {

            TransportType transportType = TransportType.bestTransportType();
            channelInitializer = new ChannelInitializer();
            log.info("Using {} threads for Netty based {}", threads, transportType.name());
            try {
                workerGroup = transportType.createEventLoopGroup();

                ServerBootstrap bootstrap = new ServerBootstrap()
                        .channelFactory(transportType.getServerSocketChannelFactory())
                        .group(workerGroup)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(channelInitializer)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                        .childOption(ChannelOption.IP_TOS, 0x18)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.TCP_FASTOPEN, 3)
                        .localAddress(new InetSocketAddress(address, port));
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
                            log.error("Can't bind to {}", address, future.cause());
                        }).sync().channel().closeFuture().sync();

            } catch (InterruptedException e) {
                workerGroup.shutdownGracefully();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        thread.setName("NettyHandler");
        thread.start();
    }
}
