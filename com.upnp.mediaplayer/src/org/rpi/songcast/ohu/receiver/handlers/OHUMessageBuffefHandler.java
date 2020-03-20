package org.rpi.songcast.ohu.receiver.handlers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageAudio;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHUMessageBuffefHandler extends MessageToMessageDecoder<OHUMessageAudio> {

	//Map<Integer, OHUMessageAudio> map = new TreeMap<Integer, OHUMessageAudio>();
	Logger log = Logger.getLogger(this.getClass());
	int lastHandledMessage = 0;
	int countMissedFrames = 0;
	int maxConsecutivesMisses = 0;
	int maxBufferSize = 0;

	@Override
	protected void decode(ChannelHandlerContext ctx, OHUMessageAudio msg, List<Object> out) throws Exception {
		//map.put(msg.getFrameNumber(), msg);

		//if (map.size() > 5) {
		//	OHUMessageAudio a = map.values().stream().findFirst().get();
			int frameId = msg.getFrameNumber();
		//	map.remove(frameId);
			if (frameId - lastHandledMessage != 1) {
				//boolean isInList = map.containsKey(lastHandledMessage + 1);
				int missed = ((frameId - lastHandledMessage) - 1);
				countMissedFrames += missed;
				if(missed > maxConsecutivesMisses) {
					maxConsecutivesMisses = missed;
				}
				
				//log.debug("Missed a Frame: " + frameId + " Last Frame: " + lastHandledMessage + "  " + ((frameId - lastHandledMessage) - 1) + " IsInBuffer: " + isInList);
			}
			
			int bufferSize = msg.getData().readableBytes();
			if(bufferSize > maxBufferSize) {
				maxBufferSize = bufferSize;
			}

			
			if(frameId % 1000 == 0) {
				log.debug("FrameCount: " + frameId + " Missed Frames: " + countMissedFrames + " Max Consecutive Missed Frames: " + maxConsecutivesMisses + " MaxBufferSize: " + maxBufferSize);
				countMissedFrames = 0;
				maxConsecutivesMisses = 0;
				maxBufferSize = 0;
			}
			
						
			lastHandledMessage = frameId;
			out.add(msg);
		//}
	}

}
