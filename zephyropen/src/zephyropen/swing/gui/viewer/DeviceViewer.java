package zephyropen.swing.gui.viewer;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.util.DataLogger;

/**
 * A minimal SWING based graphing display for the given device.
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class DeviceViewer implements API {

	public static ZephyrOpen constants = ZephyrOpen.getReference();

	/** Input Metrics */
	protected long lastMessage = System.currentTimeMillis();

	/** device info */
	private String deviceName = null;

	/** device specific viewer */
	private Viewer viewer = null;

	/** XML commands might contain the physical address */
	private String address = null;

	private DataLogger logger = null;

	/** Constructor only takes a device name */
	public DeviceViewer() {

		deviceName = PrototypeFactory.getDeviceTypeString(constants.get(ZephyrOpen.deviceName));

		/** create the associated display for this device */
		viewer = ViewerFactory.create(this);
		if (viewer == null) {
			constants.error("can't create viewer for: " + deviceName, this);
			constants.shutdown();
		}

		/** show window */
		javax.swing.SwingUtilities.invokeLater(viewer.getFrame());
		
		/** register for messages */
		ApiFactory.getReference().add(this);

		/** loop forever, refreshing the display */
		viewer.poll();
	}

	/** Update graphs with incoming XML packets */
	public void execute(Command command) {

		System.out.println("viewer exe: " + command.toString());

		// not for us
		if (!constants.get(ZephyrOpen.user).equals(command.get(ZephyrOpen.user))) {
			constants.error("wrong userName: " + command.get(ZephyrOpen.user), this);
			return;
		}

		// manage logging
		if (constants.getBoolean(ZephyrOpen.loggingEnabled)) {
			if (logger == null) {
				logger = new DataLogger();
			} else {
				logger.append(command.toXML());
			}
		}

		// update
		viewer.update(command);
		lastMessage = System.currentTimeMillis();
	}

	/** @return the time in milliseconds since last XML message */
	public long getDelta() {
		return System.currentTimeMillis() - lastMessage;
	}

	/** @return the blue tooth friendly name the device */
	public String getDeviceName() {
		return deviceName;
	}

	public String getAddress() {
		return address;
	}

	/**
	 * 
	 * configure via properties file only
	 * 
	 */
	public static void main(String[] args) {

	//	if (args.length == 2) {

			/** configure the framework, use properties file given */
			// constants.init(args[0]);
		
			constants.init();
			constants.put(ZephyrOpen.user, "brad");
			constants.put("drawdelay", "3000");
			constants.put(ZephyrOpen.deviceName, PrototypeFactory.elevation);
			
		//}
		
			/** launch new report */
			new DeviceViewer();
		
	//	}
	}
}
