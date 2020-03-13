package org.rpi.songcast.ohu.sender.mpd;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

public class MPDStreamerConnectorHandler extends SimpleChannelInboundHandler<HttpObject> {

	private Logger log = Logger.getLogger(this.getClass());

	public MPDStreamerConnectorHandler() {
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			
			String nl = System.getProperty("line.separator");
			
			StringBuilder sb = new StringBuilder();
			sb.append("MPD Response: ");
			sb.append(nl);
			sb.append("Status: " + response.status());
			sb.append(nl);
			sb.append("Version: " + response.protocolVersion());
			sb.append(nl);
			



			if (!response.headers().isEmpty()) {
				for (CharSequence name : response.headers().names()) {
					for (CharSequence value : response.headers().getAll(name)) {
						sb.append("HEADER: " + name + " = " + value);
						sb.append(nl);
					}
				}
			}
			
			log.debug(sb.toString());

			if (HttpUtil.isTransferEncodingChunked(response)) {
				log.debug("CHUNKED CONTENT Start");
			} else {
				log.debug("CONTENT Start");
			}
		}
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;

			ByteBuf buffer = httpContent.content();
			int capacity = buffer.readableBytes();
			// System.err.println(capacity);

			// For some reason skimming off the first 5bytes is the only way to
			// get decent audio.
			// Definition of WAV File here:
			// http://www.topherlee.com/software/pcm-tut-wavformat.html
			int skim = 1;
			if (capacity > skim) {
				//String riff = buffer.readCharSequence(skim, CharsetUtil.UTF_8).toString();
				byte[] test = new byte[skim];
				httpContent.content().readBytes(test, 0, skim);
				//System.err.println("RIFF: " + test);
			}
			
			byte[] bytes = new byte[buffer.readableBytes() ];
			buffer.readBytes(bytes);

			OHUSenderAudioResponse a = new OHUSenderAudioResponse( bytes);
			MPDStreamerController.getInstance().addSoundByte(a);

			if (httpContent instanceof LastHttpContent) {
				log.debug(" END OF CONTENT");
				MPDStreamerController.getInstance().setFinished(true);
				ctx.close();
			}
		}
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("MPDConnector Connection Error: ", cause);
		ctx.close();
	}
}