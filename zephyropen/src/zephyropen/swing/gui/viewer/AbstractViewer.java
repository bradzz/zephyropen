/*
 *
 * Created on 2010-07-01
 * 
 * @author brad.zdanivsky@gmail.com
 * 
 */
package zephyropen.swing.gui.viewer;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.FTPManager;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;

public class AbstractViewer {

	protected static ZephyrOpen constants = ZephyrOpen.getReference();

	protected static final long DROPPED = 6000;

	protected static int DRAW_DELAY = 1500;

	protected TabbedFrame frame = null;

	protected GoogleChart[] charts = null;

	protected API api = null;

	protected String battery = null;
	
	//private String disconnected = null; 

	private FTPManager ftpManager = FTPManager.getReference();

	/** */
	public TabbedFrame getFrame() {
		return frame;
	}

	/** */
	public void poll() {

		int delay = constants.getInteger("drawdelay");
		if(delay == ZephyrOpen.ERROR) delay = DRAW_DELAY;
		
		while (true) {
			
			Utils.delay(delay);

			if (api.getDelta() < DROPPED) {

				// connected
				frame.setTitle(getText(false));

				// re-draw selected tab
				frame.updateSelected();
				
			} else {
				
			//	System.out.println(api.getDelta()); 
				
				// been dropped, data too slow
				frame.setTitle(getText(true));

				// update connection, even if disconnected
				updateConnectionTab();
			}
		}
	}

	/** what should the frame say based on connection info */
	private String getText(boolean dropped) {

		/*
		if (dropped){
			
			String str =  "[" + constants.get(ZephyrOpen.user) + ", " 
				+ constants.get(ZephyrOpen.deviceName) + "] Lost Connection "; 
			
			if((api.getDelta() / 1000) > 60){
				if(disconnected == null)
					disconnected = new Date().toString();
				
				str += "on: " + disconnected;
				
			} else {
				
				str += (api.getDelta() / 1000) + " seconds ago"; 
				
			}
			
			return str;
		}
		*/
		
		// reconnected 
		// disconnected = null;
		
		// build string based on configuration and settings
		String text = "[" + constants.get(ZephyrOpen.user) + "] "	+ api.getDeviceName();

		if (battery != null)
			text += " battery = " + battery;

		if(constants.getBoolean(ZephyrOpen.loopback))
			text += " -l";

		if (constants.getBoolean(State.pack))
			text += " -p";

		if (constants.getBoolean(ZephyrOpen.frameworkDebug))
			text += " -d";

		if(ftpManager != null )
		if (ftpManager.ftpConfigured()) {
			text += " -ftp";

			if (constants.getBoolean(ZephyrOpen.filelock) 
					&& constants.getBoolean(FTPManager.ftpEnabled))
				text += "*";
		}

		if (constants.getBoolean(ZephyrOpen.loggingEnabled)) {
			text += " -log";

			if (constants.getBoolean(ZephyrOpen.filelock))
				text += "*";
		}

		if (constants.getBoolean(ZephyrOpen.recording))
			text += " -rec";

		/* hold size in constants 
		if (constants.getBoolean(ZephyrOpen.frameworkDebug))
			text += "   [" + constants.getInteger(ZephyrOpen.xSize) + ", "
					+ constants.getInteger(ZephyrOpen.ySize) + "]";
		*/
		
		return text;
	}

	/**
	 * add new data points to connection tab. required to see in disconnected state
	 */
	public void updateConnectionTab() {
		
		GoogleChart connection = charts[charts.length-1];	
		if(connection.getState().size() < 5 ) return;
			
		if(connection.getName().equals(PrototypeFactory.connection)){	
			connection.getState().update(api.getDelta());
		} 
		else {
			constants.error("can't locate connection tab", this);
			constants.shutdown("can't locate connection tab");
		}
	}
}

