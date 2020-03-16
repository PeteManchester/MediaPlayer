package org.rpi.airplay.audio;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class AudioBuffer extends MessageToMessageDecoder<AirPlayAudioHolder> {

	private int last_sequence = 0;
	private Logger log = Logger.getLogger(this.getClass());
	
	private Map<Integer,AirPlayAudioHolder> map = new TreeMap<Integer, AirPlayAudioHolder>();

	@Override
	protected void decode(ChannelHandlerContext ctx, AirPlayAudioHolder msg, List<Object> out) throws Exception {
		
		map.put(msg.getFrameId(), msg);		
		if (map.size() > 5) {
			AirPlayAudioHolder a = map.values().stream().findFirst().get();
			int sequence = a.getFrameId();
			map.remove(sequence);
			if (sequence - last_sequence != 1) {
				boolean isInList = map.containsKey(last_sequence + 1);
				log.debug("Missed a Frame: " + sequence + " Last Frame: " + last_sequence + "     " + ((sequence - last_sequence) - 1) + " IsInBuffer: " + isInList);				
			}
			last_sequence = sequence;
			out.add(a.getBuf());
		}		
	}

}
