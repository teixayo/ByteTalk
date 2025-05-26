package me.teixayo.bytetalk.backend.networking;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

    private String websocketPath;
    @Getter
    private static AttributeKey<ClientStateType> state = AttributeKey.newInstance("State");
    public ChannelInitializer(String websocketPath) {
        this.websocketPath = websocketPath;
    }
    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new PacketHandler());
        channel.attr(state).set(ClientStateType.IN_LOGIN);
    }

}
