package me.teixayo.bytetalk.backend.networking;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;
import io.netty.util.concurrent.FastThreadLocalThread;
import lombok.Getter;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

@Getter
public enum TransportType {
    IO_URING(IoUringServerSocketChannel::new, IoUringSocketChannel::new, IoUringIoHandler::newFactory),
    EPOLL(EpollServerSocketChannel::new, EpollSocketChannel::new, EpollIoHandler::newFactory),
    KQUEUE(KQueueServerSocketChannel::new, KQueueSocketChannel::new, KQueueIoHandler::newFactory),
    NIO(NioServerSocketChannel::new, NioSocketChannel::new, NioIoHandler::newFactory);

    private final ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory;
    private final ChannelFactory<? extends SocketChannel> socketChannelFactory;
    private final Supplier<IoHandlerFactory> ioHandlerFactorySupplier;
    private final String name;
    TransportType(ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory, ChannelFactory<? extends SocketChannel> socketChannelFactory, Supplier<IoHandlerFactory> ioHandlerFactorySupplier) {
        this.serverSocketChannelFactory = serverSocketChannelFactory;
        this.socketChannelFactory = socketChannelFactory;
        this.ioHandlerFactorySupplier = ioHandlerFactorySupplier;
        this.name = this.name().toLowerCase();
    }

    private static ThreadFactory createThreadFactory() {
        return runnable -> new FastThreadLocalThread(runnable, "Netty Worker");
    }
    public EventLoopGroup createEventLoopGroup() {
        return new MultiThreadIoEventLoopGroup(
                0, createThreadFactory(), this.ioHandlerFactorySupplier.get());
    }

    public static TransportType bestTransportType() {
        if(IoUring.isAvailable()) return IO_URING;
        if (Epoll.isAvailable()) return EPOLL;
        if (KQueue.isAvailable()) return KQUEUE;
        return NIO;
    }


}
