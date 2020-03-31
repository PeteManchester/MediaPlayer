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

@Deprecated
public class MPDStreamerConnectorHandler extends SimpleChannelInboundHandler<HttpObject> {

	private Logger log = Logger.getLogger(this.getClass());
	private int maxBufferSize = 0;
	private int count = 0;


	public MPDStreamerConnectorHandler() {
		log.debug("MPDStreamerConnectorHandler: " + count);
		count++;
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

			if (capacity > maxBufferSize) {
				maxBufferSize = capacity;
			}
			// System.err.println(capacity);

			// For some reason skimming off the first 1bytes is the only way to
			// get decent audio.
			// Definition of WAV File here:
			// http://www.topherlee.com/software/pcm-tut-wavformat.html
			int skim = 1;
			if (capacity > skim && skim > 0) {
				byte[] test = new byte[skim];

			}

				OHUSenderAudioResponse a = new OHUSenderAudioResponse(buffer);


			if (httpContent instanceof LastHttpContent) {
				log.debug(" END OF CONTENT");
				MPDStreamerController.getInstance().stop();
				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("Error MPDConnectorHandler: ", cause);
		ctx.close();
	}
}