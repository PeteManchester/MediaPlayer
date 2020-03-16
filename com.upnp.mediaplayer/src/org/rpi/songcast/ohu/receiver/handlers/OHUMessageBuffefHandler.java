package org.rpi.songcast.ohu.receiver.handlers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageAudio;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHUMessageBuffefHandler extends MessageToMessageDecoder<OHUMessageAudio> {

	Map<Integer, OHUMessageAudio> map = new TreeMap<Integer, OHUMessageAudio>();
	Logger log = Logger.getLogger(this.getClass());
	int lastHandledMessage = 0;

	@Override
	protected void decode(ChannelHandlerContext ctx, OHUMessageAudio msg, List<Object> out) throws Exception {
		map.put(msg.getFrameNumber(), msg);

		if (map.size() > 5) {
			OHUMessageAudio a = map.values().stream().findFirst().get();
			int frameId = a.getFrameNumber();
			map.remove(frameId);
			if (frameId - lastHandledMessage != 1) {
				boolean isInList = map.containsKey(lastHandledMessage + 1);
				log.debug("Frame is out of sequence: Expected Frame: " + lastHandledMessage + 1 + " IsInList: " + isInList);				
			}
			lastHandledMessage = frameId;
			out.add(a);
		}
	}

}
