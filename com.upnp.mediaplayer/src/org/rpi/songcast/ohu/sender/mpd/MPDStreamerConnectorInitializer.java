package org.rpi.songcast.ohu.sender.mpd;
import org.rpi.songcast.ohu.receiver.OHULeakCatcher;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.ssl.SslContext;

public class MPDStreamerConnectorInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public MPDStreamerConnectorInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
    	
    	int chunkSize = 1764;
        ChannelPipeline p = ch.pipeline();

        // Enable HTTPS if necessary.
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        //The FrameBuffer will ensure that each chunk is 1764 (441 * 4)
        p.addLast("MPDStreamerBuffer",new MPDStreamerBuffer());
        
        //p.addLast(new HttpClientCodec(chunkSize, 8192, chunkSize));
        
        //Just use the Encoder so that we can get the raw content.
        p.addLast("MPDHttpEnconder",new HttpRequestEncoder());

        // Remove the following line if you don't want automatic content decompression.
        //p.addLast(new HttpContentDecompressor());

        // Uncomment the following line if you don't want to handle HttpContents.
        //p.addLast(new HttpObjectAggregator(1048576));
        
        //p.addLast(new MPDStreamerConnectorHandler());
        
        //Package the raw bytes to a Songcast Audio Message
        p.addLast("MPDStreamerMessageHandler",new MPDStreamerMessageHandler());
        p.addLast("MPDLeakCatcher", new OHULeakCatcher());
    }
}