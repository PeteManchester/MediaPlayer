package org.rpi.songcast.ohu.sender.mpd;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageAudio;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

public class MPDStreamerConnectorHandler extends SimpleChannelInboundHandler<HttpObject> {

	private Logger log = Logger.getLogger(this.getClass());
	private int maxBufferSize = 0;
	private int count = 0;

	//private ByteBuf pool = Unpooled.buffer();

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
				// String riff = buffer.readCharSequence(skim,
				// CharsetUtil.UTF_8).toString();
				byte[] test = new byte[skim];
				// httpContent.content().readBytes(test, 0, skim);
				//buffer.readBytes(test, 0, skim);
				// System.err.println("RIFF: " + test);
			}

			/*
			 * byte[] bytes = new byte[buffer.readableBytes() ];
			 * buffer.readBytes(bytes);
			 * 
			 * count++; if(count % 1000 == 0) { log.debug("MPD MaxBufferSize: "
			 * + maxBufferSize); maxBufferSize = 0; }
			 */

			//pool.writeBytes(buffer);

			//int size = 1764;
			//while (pool.readableBytes() > size) {
				//ByteBuf t = pool.readBytes(size);
				// log.debug("ReadableSize: " + t.readableBytes());
			//log.debug("AudioSize: " + buffer.readableBytes());
				OHUSenderAudioResponse a = new OHUSenderAudioResponse(buffer);
				//OHUMessageAudio test = new	OHUMessageAudio(a.getBuffer().retain(), false);
				//log.debug("Size: " + a.getBuffer().retain().readableBytes() + " " + test.getAudio().length);
				MPDStreamerController.getInstance().addSoundByte(a);

			//}

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