package me.teixayo.bytetalk.backend.networking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.AttributeKey;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;

public class ServerChannelInitializer extends ChannelInitializer<Channel> {

    private String websocketPath;
    public static AttributeKey<ClientStateType> state = AttributeKey.newInstance("State");
    public ServerChannelInitializer(String websocketPath) {
        this.websocketPath = websocketPath;
    }
    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketClientHandler());
        channel.attr(state).set(ClientStateType.IN_LOGIN);
    }

}
