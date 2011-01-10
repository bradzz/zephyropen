/*
 *
 * Created on 2010-07-01
 * @author brad
 */
package zephyropen.swing.gui.viewer;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.Utils;
import zephyropen.util.ftp.FTPManager;
import zephyropen.util.google.GoogleChart;

public class AbstractViewer {

	protected static ZephyrOpen constants = ZephyrOpen.getReference();

	protected static final long DROPPED = 5000;

	// private static final int MIN_SIZE = 5;

	protected static long DRAW_DELAY = 1300;

	protected TabbedFrame frame = null;

	protected GoogleChart[] charts = null;

	protected API api = null;

	protected String battery = null;

	private int counter = 0;

	private FTPManager ftpManager = FTPManager.getReference();

	/** */
	public TabbedFrame getFrame() {
		return frame;
	}

	/** */
	public void poll() {

		while (true) {

			Utils.delay(DRAW_DELAY);

			if (api.getDelta() < DROPPED) {

				// connected
				frame.setTitle(getText(false));

			} else {

				// been dropped, data too slow
				frame.setTitle(getText(true));

				// update connection, even if disconnected
				updateConnectionTab();
			}

			// re-draw selected tab
			frame.updateSelected();
		}
	}

	/** what should the frame say based on connection info */
	private String getText(boolean dropped) {

		if (dropped)
			return "[" + constants.get(ZephyrOpen.userName) + ", "
					+ constants.get(ZephyrOpen.deviceName)
					+ "] Lost Connection " + (api.getDelta() / 1000)
					+ " seconds ago";

		// build string based on configuration and settings

		String text = "[" /* + api.getAddress() + ", " */
				+ constants.get(ZephyrOpen.userName) + "] "
				+ api.getDeviceName();

		if (battery != null)
			text += " battery = " + battery;

		// if(constants.getBoolean(ZephyrOpen.loopback))
		// text += " -l";

		if (constants.getBoolean(State.pack))
			text += " -p";

		if (constants.getBoolean(ZephyrOpen.frameworkDebug))
			text += " -d";

		if( ftpManager != null )
		if (ftpManager.ftpConfigured()) {
			text += " -ftp";

			if (constants.getBoolean(ZephyrOpen.filelock) && constants.getBoolean(FTPManager.ftpEnabled))
				text += "*";
		}

		if (constants.getBoolean(ZephyrOpen.loggingEnabled)) {
			text += " -log";

			if (constants.getBoolean(ZephyrOpen.filelock))
				text += "*";
		}

		if (constants.getBoolean(ZephyrOpen.recording))
			text += " -rec";

		/* hold size in constants */
		if (constants.getBoolean(ZephyrOpen.frameworkDebug))
			text += "   [" + constants.getInteger(ZephyrOpen.xSize) + ", "
					+ constants.getInteger(ZephyrOpen.ySize) + "]";

		return text;
	}

	/**
	 * add new data points to connection tab. required to see in disconnected state
	 */
	public void updateConnectionTab() {
		
		counter++;

		// String chartName = null;
		for (int i = 0; i < charts.length; i++) {

			// chart = charts[i].getName();
			if (charts[i].getName().equals(PrototypeFactory.connection)) {

				if(api.getDelta() > ZephyrOpen.TIME_OUT){
					
					// don't fill chart with empty space
					charts[i].getState().update((double) api.getDelta());
					
					// done 
					return;

				} else {
					
					charts[i].add(String.valueOf(api.getDelta()));
					
					// done
					return;
					
				}
				
				
				// look no further
				// return;
			}
		}
	}
}