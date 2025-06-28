package me.teixayo.bytetalk.backend.networking;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;

import java.util.concurrent.TimeUnit;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

    @Getter
    private static AttributeKey<ClientStateType> state = AttributeKey.newInstance("State");

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                .addLast(new PacketHandler());
        channel.attr(state).set(ClientStateType.IN_LOGIN);
    }

}
