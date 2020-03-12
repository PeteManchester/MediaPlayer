package org.rpi.songcast.ohu.sender.mpd.copy;

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
import io.netty.util.CharsetUtil;

public class MPDStreamerConnectorHandler extends SimpleChannelInboundHandler<HttpObject> {

	private Logger log = Logger.getLogger(this.getClass());

	int frameCount = 0;

	public MPDStreamerConnectorHandler() {
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;

			// response.protocolVersion()
			// System.err.println("STATUS: " + response.status());
			// System.err.println("VERSION: " + response.protocolVersion());
			// System.err.println();

			if (!response.headers().isEmpty()) {
				for (CharSequence name : response.headers().names()) {
					for (CharSequence value : response.headers().getAll(name)) {
						log.debug("HEADER: " + name + " = " + value);
					}
				}
				System.err.println();
			}

			if (HttpUtil.isTransferEncodingChunked(response)) {
				log.debug("CHUNKED CONTENT {");
			} else {
				log.debug("CONTENT {");
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

			OHUSenderAudioResponse a = new OHUSenderAudioResponse(frameCount, bytes);
			frameCount++;
			if(frameCount == Integer.MAX_VALUE) {
				log.debug("FrameCount reached Max Value");
				frameCount = 0;
			}
			if (frameCount % 1000 == 0) {
				log.debug("Frame: " + frameCount);
			}
			MPDStreamerController.getInstance().addSoundByte(a, frameCount);

			if (httpContent instanceof LastHttpContent) {
				log.debug("} END OF CONTENT");
				ctx.close();
			}
		}
	}

	private byte[] converting(byte[] value) {
		final int length = value.length;
		byte[] res = new byte[length];
		for (int i = 0; i < length; i++) {
			res[length - i - 1] = value[i];
		}
		return res;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("MPDConnector Connection Error: ", cause);
		// cause.printStackTrace();
		ctx.close();
	}
}