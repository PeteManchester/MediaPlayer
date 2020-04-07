package org.rpi.songcast.ohu.sender.mpd;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;

public class MPDStreamerConnectorInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public MPDStreamerConnectorInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {

        ChannelPipeline p = ch.pipeline();

        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        
        p.addLast(new HttpClientCodec(8192 , 8192, 8192 ));
     
        p.addLast("MPDStreamerMessageHandler",new MPDStreamerMessageHandler());
        p.addLast("MPDStreamerDefaultHttpResponse", new MPDStreamerDefaultHttpResponseHandler());
        p.addLast("MPDLeakCatcher", new MPDStreamerLeakCatcher());
    }
}