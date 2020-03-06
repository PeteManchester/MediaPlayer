package org.rpi.songcast.ohu.receiver;

/**
 * OHUMessageAudioHandler
 * Handles the OHUMessageAudio
 * Create an Player and passes the sound bytes
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IJavaSoundPlayer;
import org.rpi.java.sound.JavaSoundPlayerBasic;
import org.rpi.java.sound.JavaSoundPlayerLatency;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
public class OHUMessageAudioHandler extends SimpleChannelInboundHandler<OHUMessageAudio> {

	private Logger log = Logger.getLogger(this.getClass());
	private IJavaSoundPlayer player = null;
	private AudioInformation audioInformation = null;
	private Thread threadPlayer = null;
	private int lastFrameNumber = 0;
	private int maxRepairQueueFrames = 50;
	private boolean isRepairing = false;
	private Vector<OHUMessageAudio> repairQueue = new Vector<OHUMessageAudio>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageAudio msg) throws Exception {
		if (msg instanceof OHUMessageAudio) {
			try {
				if (player != null) {
					AudioInformation ai = msg.getAudioInformation();
					if (ai != null && !ai.compare(audioInformation)) {
						player.createSoundLine(ai);
						setAudioInformation(ai);
					}
					ai = null;
					
					if (isRepairing) {
						isRepairing = repair(msg);
						return;
					}
					
					int diff = msg.getFrameNumber() - lastFrameNumber;
					
					if (diff == 1) {
						// Next frame is the one we expected, send it to the player
						player.put(msg);
						lastFrameNumber = msg.getFrameNumber();
					} else if (diff < 1) {
						// Next frame is older than last played, discard it (repair not applicable)
						log.debug("Songcast: Frame is out of sequence but too old to repair: " + msg.getFrameNumber() + " (previous frame number: " + lastFrameNumber + ")");
					} else {
						// Next frame is newer than we expected (out of sequence), begin repair
						isRepairing = repairBegin(msg);
					}
					
				} else {
					
					if (Config.getInstance().isSongcastLatencyEnabled()) {
						// With Latency
						player = new JavaSoundPlayerLatency();
						threadPlayer = new Thread(player, "SongcastPlayerJavaSoundLatency");
					} else {
						player = new JavaSoundPlayerBasic();
						threadPlayer = new Thread(player, "SongcastPlayerJavaSound");
					}
					threadPlayer = new Thread(player, "SongcastPlayerJavaSound");
					threadPlayer.start();
					setAudioInformation(msg.getAudioInformation());
					player.createSoundLine(audioInformation);
					player.put(msg);
					lastFrameNumber = msg.getFrameNumber();
				}
				// msg.getData().release();
			} catch (Exception e) {
				log.error("Error Handling Audio Message", e);
			}
		}
	}

	public boolean repairBegin(OHUMessageAudio msg) {
		log.debug("Songcast: Out of sequence frame, beginning repair on frame " + msg.getFrameNumber() + " (previous frame number: " + lastFrameNumber + ")");
		repairInsertFirst(msg);
		return true;
	}
	
	public boolean repair(OHUMessageAudio msg) {
		log.fatal("Songcast: Repair recevied frame " + msg.getFrameNumber());
		int diff = msg.getFrameNumber() - lastFrameNumber;
		
		if (diff == 1) {
			// This is the next expected frame, send it to the player
			log.debug("Songcast: Repair sending frame " + msg.getFrameNumber() + " directly");
			player.put(msg);
			lastFrameNumber = msg.getFrameNumber();
			// Now check if the first waiting frame in the repair queue is the next one we need
			while (!repairIsEmpty() && repairFirstFrameNumber() == lastFrameNumber + 1) {
				// If so, pull it out of the queue and send it
				OHUMessageAudio nextMsg = repairFirst();
				log.debug("Songcast: Sending frame " + nextMsg.getFrameNumber() + " from repair queue");
				player.put(nextMsg);
				lastFrameNumber = nextMsg.getFrameNumber();
				// Then check for more
				if (repairIsEmpty()) {
					// If the queue is empty, we're done
					log.debug("Songcast: Repair process complete");
					return false;
				}
				// If the queue isn't empty, continue testing
			}
			// When the first frame in the queue is not the one we need, return to processing incoming frames
			return true;
			
		} else if (diff < 1) {
			// Frame is older than last played, discard it and return to processing incoming frames
			log.debug("Songcast: Frame is too old to repair: " + msg.getFrameNumber() + " (previous frame number: " + lastFrameNumber + ")");
			return true;
			
		} else {
			// This frame is not the next expected one, but we'll need it later, so it needs
			// to go into the queue; compare it to the first waiting frame to find out where
			diff = msg.getFrameNumber() - repairFirstFrameNumber();
			
			if (diff < 0) {
				// It's older than the current first waiting frame, so it goes at the front of the queue
				if (repairSlotsUsed() == maxRepairQueueFrames) {
					// If the queue is full, clear it and quit the repair process
					log.debug("Songcast: All available repair slots used ("+ repairSlotsUsed() +") ");
					repairReset();
					player.put(msg);
					lastFrameNumber = msg.getFrameNumber();
					return false;
				}
				repairInsertFirst(msg);
				return true;
				
			} else if (diff == 0) {
				// It's a duplicate of the current first waiting frame, discard it
				log.debug("Songcast: Frame " + msg.getFrameNumber() + " is already in the repair queue, discarding");
				return true;
				
			} else {
				// It's newer than the first waiting frame, so it goes into the backlog
				// Compare it to the last frame in the queue
				diff = msg.getFrameNumber() - repairLastFrameNumber();
				if (diff > 0) {
					// It's newer than the last frame, so just add it to the end
					if (repairSlotsUsed() == maxRepairQueueFrames) {
						// If the queue is full, clear it and quit the repair process
						log.debug("Songcast: All available repair slots used ("+ repairSlotsUsed() +") ");
						repairReset();
						player.put(msg);
						lastFrameNumber = msg.getFrameNumber();
						return false;
					}
					repairInsertLast(msg);
					return true;
					
				} else if (diff == 0) {
					// It's a duplicate of the last waiting frame, discard it
					log.debug("Songcast: Frame " + msg.getFrameNumber() + " is already in the repair queue, discarding");
					return true;
					
				} else {
					// It has to go somewhere in the middle of the queue
					for (int i = 0; i < repairSlotsUsed(); i++) {
						diff = msg.getFrameNumber() - repairMiddleFrameNumber(i);
						if (diff < 0) {
							// The frame is older than the one at position i, insert it here
							if (repairSlotsUsed() == maxRepairQueueFrames) {
								// If the queue is full, clear it and quit the repair process
								log.debug("Songcast: All available repair slots used ("+ repairSlotsUsed() +") ");
								repairReset();
								player.put(msg);
								lastFrameNumber = msg.getFrameNumber();
								return false;
							}
							repairInsertMiddle(i, msg);
							break;
						} else if (diff == 0) {
							log.debug("Songcast: Frame " + msg.getFrameNumber() + " is already in the repair queue, discarding");
							break;
						}
					}
					return true;
				}
			}
		}
	}

	public void repairReset() {
		repairReset(true);
	}
	
	public void repairReset(boolean send_on_flush) {
		if (send_on_flush) {
			log.debug("Songcast: Flushing the repair queue");
			while (!repairIsEmpty()) {
				// Just send all the frames in the repair queue as they are
				OHUMessageAudio nextMsg = repairFirst();
				log.debug("Songcast: Sending frame " + nextMsg.getFrameNumber() + " from repair queue");
				player.put(nextMsg);
				lastFrameNumber = nextMsg.getFrameNumber();
			}
		}
		log.debug("Songcast: Resetting repair process");
		try {
			repairQueue.removeAllElements();
		} catch (Exception e) {
			log.error("Unable to reset the repair queue");
		}
	}

	
	public synchronized OHUMessageAudio repairFirst() {
		if (repairFirstFrameNumber() != null)
			return repairQueue.remove(0);
		return null;
	}
	
	public synchronized void repairInsertFirst(OHUMessageAudio msg) {
		try {
			log.debug("Songcast: Adding frame " + msg.getFrameNumber() + " to front of repair queue");
			repairQueue.add(0, msg);
		} catch (Exception e) {
			log.error("Songcast: Unable to add audio frame to front of repair queue");
		}
	}
	
	public synchronized void repairInsertLast(OHUMessageAudio msg) {
		try {
			log.debug("Songcast: Adding frame " + msg.getFrameNumber() + " to end of repair queue");
			repairQueue.addElement(msg);
		} catch (Exception e) {
			log.error("Songcast: Unable to add audio frame to end of repair queue");
		}
	}
	
	public synchronized void repairInsertMiddle(int i, OHUMessageAudio msg) {
		try {
			log.debug("Songcast: Adding frame " + msg.getFrameNumber() + " to middle of repair queue");
			repairQueue.add(i, msg);
		} catch (Exception e) {
			log.error("Songcast: Unable to add audio frame to middle of repair queue");
		}
	}
	
	public synchronized Integer repairFirstFrameNumber() {
		if (repairIsEmpty())
			return null;
		return repairQueue.elementAt(0).getFrameNumber();
	}
	
	public synchronized Integer repairLastFrameNumber() {
		if (repairIsEmpty())
			return null;
		return repairQueue.lastElement().getFrameNumber();
	}
	
	public synchronized Integer repairMiddleFrameNumber(int i) {
		if (repairIsEmpty())
			return null;
		return repairQueue.elementAt(i).getFrameNumber();
	}
	
	public synchronized int repairSlotsUsed() {
		return repairQueue.size();
	}
	
	public synchronized boolean repairIsEmpty() {
		return repairQueue.isEmpty();
	}
	
	private void setAudioInformation(AudioInformation ai) {
		audioInformation = ai;
		try {
			TrackInfo info = new TrackInfo();
			info.setBitDepth(ai.getBitDepth());
			info.setCodec(ai.getCodec());
			info.setBitrate(ai.getBitRate());
			info.setSampleRate((long) ai.getSampleRate());
			info.setDuration(0);
			EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
			ev.setTrackInfo(info);
			if (ev != null) {
				PlayManager.getInstance().updateTrackInfo(ev);
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Unregistered: " + ctx.name());
		if (player != null) {
			PlayManager.getInstance().setStatus("Stopped", "SONGCAST");
			player.stop();
			player = null;
		}
		if (threadPlayer != null) {
			threadPlayer = null;
		}
		super.channelUnregistered(ctx);
	}
}