package org.rpi.mplayer;

import java.io.PrintStream;

import org.apache.log4j.Logger;

public class InputWriter  {
	
	private Logger log = Logger.getLogger(InputWriter.class);
	
	private Process process = null;
	
	/** Stream used to send commands to mplayer */
	private volatile PrintStream streamToProcess;
	private boolean stopSendingCommands = false;
	

	
	public InputWriter(Process process)
	{
		this.process = process;//MPlayerEngine.getInstance().getProcess();
	}
	
	/***
	 * Send Command to MPlayer
	 * @param command
	 */
	public synchronized void sendCommand(final String command) {
		if (!this.isStopSendingCommands()) {
			if (this.process != null) {
				if (this.streamToProcess == null) {
					this.streamToProcess = new PrintStream(
							this.process.getOutputStream());
				}
				if (this.streamToProcess != null) {
					this.streamToProcess.print(command);
				}
				if (this.streamToProcess != null) {
					this.streamToProcess.print('\n');
				}
				if (this.streamToProcess != null) {
					this.streamToProcess.flush();
					//log.debug("Sent Command: " + command);
				}
			}
		}
	}

	/**
	 * @return the stopSendingCommands
	 */
	private boolean isStopSendingCommands() {
		return stopSendingCommands;
	}

	/**
	 * @param stopSendingCommands the stopSendingCommands to set
	 */
	public void setStopSendingCommands(boolean stopSendingCommands) {
		this.stopSendingCommands = stopSendingCommands;
	}
	
	/***
	 * 
	 */
	public void dispose()
	{
		try
		{
		this.streamToProcess = null;
		}
		catch(Exception e)
		{
			log.error("Error Closing BufferedWriter");
		}
		finally
		{
			log.debug("Closing BufferedWriter");
			CloseMe.close(streamToProcess);
		}
		}
	
	


}
