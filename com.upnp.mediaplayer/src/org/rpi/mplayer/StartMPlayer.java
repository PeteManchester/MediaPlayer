package org.rpi.mplayer;



public class StartMPlayer {

	private static boolean pause = false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Add a shutdown hook to perform some actions before killing the JVM
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Closing");
				//Logger.debug("Final check for Zombie player engines");
				//PlayerHandler.this.playerEngine.destroyPlayer();
				//Logger.debug("Closing player ...");
				//engine.destroy();
			}

		}));
		
		// TODO Auto-generated method stub
		//MPlayerEngine engine = MPlayerEngine.getInstance();
		//engine.PlayFile();
		while(true)
		{
			try {
				//System.out.println("Waiting");
				Thread.sleep(5000);
				pause = !pause;
				if(pause)
				{
					//engine.Pause(true);
					//engine.Stop();
				}
				else
				{
					//engine.PlayFile();
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

}
