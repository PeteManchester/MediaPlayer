package org.rpi.songcast.ohu.sender.mpd;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;

public class MPDStreamerMessageHandler extends SimpleChannelInboundHandler<DefaultHttpContent> {

	private Logger log = Logger.getLogger(this.getClass());
	private int iCount = 0;
	private int iCountAudio =0;
	private int iMaxSize = 0;
	private boolean isFirstTime = true;
	private ZonedDateTime now = ZonedDateTime.now() ;


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpContent msg) throws Exception {

		if(isFirstTime) {
			log.debug("First Bytes: " + msg.content().readableBytes());
			String type = msg.content().readCharSequence(4, Charset.forName("utf-8")).toString();
			int fileSize = msg.content().readIntLE();
			String headerType = msg.content().readCharSequence(4, Charset.forName("utf-8")).toString();
			String fmt = msg.content().readCharSequence(4, Charset.forName("utf-8")).toString();
			int formatLength = msg.content().readIntLE();
			int typeFormat = msg.content().readShortLE();
			int numberChannels = msg.content().readShortLE();
			int sampleRate = msg.content().readIntLE();
			int calc = msg.content().readIntLE();
			int bitsPerSample = msg.content().readShortLE();
			int bs = msg.content().readShortLE();
			String data = msg.content().readCharSequence(4, Charset.forName("utf-8")).toString();
			int fileSizeData = msg.content().readIntLE();
			
			log.debug("Wav File Header: " + type + " FileSize: " + fileSize + " HeaderType: " + headerType + " Format: " + fmt + " FormatLength: " + formatLength + " FormatType: " + typeFormat
					+ " NumberChannels: " + numberChannels + " SampleRate: " + sampleRate + " " + calc + " BitsPerSample: " + bitsPerSample + " " + bs + " " + data + " FileSize: " + fileSizeData);
			
			isFirstTime = false;
			return;
		}
		
		
		int size = msg.content().readableBytes();
		if (size > iMaxSize) {
			iMaxSize = size;
		}
		
		MPDStreamerController.getInstance().addSoundByte(msg.content());
		
		if (iCount % 1000 == 0) {
			ZonedDateTime latest = ZonedDateTime.now();
			int seconds = (int) now.until(latest, ChronoUnit.MILLIS);
			now = latest;			
			log.debug("HttpContent. Count: " + iCount + " MaxSize: " + iMaxSize + " AudioCount: " + iCountAudio + " Time: " + seconds);
			iMaxSize = 0;
			iCountAudio =0;

		}
		iCount++;
	}



	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("Error MPDStreamerMessageHandler Error: " + cause);
		cause.printStackTrace();
		ctx.close();
	}

}
