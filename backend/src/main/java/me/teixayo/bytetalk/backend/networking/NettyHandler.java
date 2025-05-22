package me.teixayo.bytetalk.backend.networking;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class NettyHandler {
    private static final Logger LOGGER = LogManager.getLogger(NettyHandler.class);

    public int threads = Runtime.getRuntime().availableProcessors();
    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(1 << 20,
            1 << 21);

    private EventLoopGroup workerGroup;
    private ServerChannelInitializer serverChannelInitializer;

    public NettyHandler(String address, int port,String websocketPat) {
        TransportType transportType = TransportType.bestTransportType();
        serverChannelInitializer = new ServerChannelInitializer(websocketPat);
        LOGGER.info("Using " + threads + " threads for Netty based " + transportType.name());
        try {
            workerGroup = transportType.createEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap()
                    .channelFactory(transportType.getServerSocketChannelFactory())
                    .group(workerGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(serverChannelInitializer)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                    .childOption(ChannelOption.IP_TOS, 0x18)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.TCP_FASTOPEN, 3)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .localAddress(new InetSocketAddress(address, port));
            if(transportType!=TransportType.NIO) {
                bootstrap.option(UnixChannelOption.SO_REUSEPORT, true);

            }

            bootstrap.bind()
                    .addListener((ChannelFutureListener) future -> {
                        Channel channel = future.channel();
                        if (future.isSuccess()) {
                            LOGGER.info("Listening on {}", channel.localAddress());
                            return;
                        }
                        LOGGER.error("Can't bind to {}", address, future.cause());
                    }).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
