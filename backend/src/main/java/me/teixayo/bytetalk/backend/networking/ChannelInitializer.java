package me.teixayo.bytetalk.backend.networking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.protocol.client.ClientStateType;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

    @Getter
    private static final AttributeKey<ClientStateType> state = AttributeKey.newInstance("State");

    private static SslContext sslContent = null;
    static {
        if(Server.getInstance().getConfig().isSslToggleUsingKeys()) {
            try {

                sslContent = SslContextBuilder.forServer(
                                Server.getInstance().getConfig().getSslCertifiateFile(),
                                Server.getInstance().getConfig().getSslPrivateKeyFile())
                        .build();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initChannel(Channel channel) {

        ChannelPipeline pipeline = channel.pipeline();
        if (sslContent != null) {
            pipeline.addLast("ssl", sslContent.newHandler(channel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new IdleStateHandler(0, Server.getInstance().getConfig().getMaxTimeOut() / 2, 0, TimeUnit.SECONDS))
                .addLast(new PacketHandler());
        channel.attr(state).set(ClientStateType.IN_LOGIN);
    }

}
