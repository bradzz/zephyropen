package zephyropen.api;

import java.util.Enumeration;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.FrameworkAPI;
import zephyropen.command.Command;

/**
 * <p>
 * Create an API to control and manage the Framework <br>
 * <b> Note: this API registers itself if "frameworkDebug" is enabled</b>
 * <p>
 * Created: May 31, 2005
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class FrameworkAPI implements API {

	/** framework configuration */
	private final ZephyrOpen constants = ZephyrOpen.getReference();

	private final ApiFactory apiFactory = ApiFactory.getReference();

	private static FrameworkAPI singleton = null;

	private long time = 0;

	/** @return a reference to this singleton class */
	public static FrameworkAPI getReference() {

		if (singleton == null) {
			singleton = new FrameworkAPI();
		}
		return singleton;
	}

	/** Constructs the framework API */
	private FrameworkAPI() {

		/** register this API only once per process */
		apiFactory.add(this);

		time = System.currentTimeMillis();
	}

	/** execute the command */
	public void execute(Command command) {

		constants.info("delta = " + getDelta() + " in : " + command.list(), this);

		/** Terminate the Process, All of them that are listening */
		if (command.get(ZephyrOpen.action).equals(ZephyrOpen.shutdown))
			constants.shutdown("shutdown command received");

		/** Terminate the Process if matching userName and deviceName */
		else if (command.get(ZephyrOpen.action).equals(ZephyrOpen.kill)) {
			for (Enumeration<String> e = apiFactory.getApiList(); e.hasMoreElements();) {
				String tag = (String) e.nextElement();
				if (tag.equals(command.get(ZephyrOpen.deviceName)))
					if (constants.get(ZephyrOpen.user).equalsIgnoreCase(command.get(ZephyrOpen.user)))
						constants.shutdown("kill command receieved");
			}
		}

		/** Terminate the Process that are servers, or tester servers too */
		else if (command.get(ZephyrOpen.action).equals(ZephyrOpen.close)) {
			if (apiFactory.containsClass(zephyropen.device.DeviceServer.class.getName()) ||
					apiFactory.containsClass(zephyropen.device.DeviceTester.class.getName()))
				constants.shutdown("close command given");
		}

		/** Terminate the Process that are viewers */
		// else if (command.get(ZephyrOpen.action).equals(ZephyrOpen.close)) {
		// if(apiFactory.containsClass(zephyropen.swing.gui.viewer.DeviceViewer.class.getName()))
		// constants.shutdown("close command given");

		/** Toggle debugging */
		else if (command.get(ZephyrOpen.action).equals(ZephyrOpen.frameworkDebug)) {
			if (command.get(ZephyrOpen.value).equals(ZephyrOpen.enable))
				constants.put(ZephyrOpen.frameworkDebug, "true");
			else if (command.get(ZephyrOpen.value).equals(ZephyrOpen.disable))
				constants.put(ZephyrOpen.frameworkDebug, "false");
		}

		/** mark last input for getDelta() */
		time = System.currentTimeMillis();
	}

	public String getDeviceName() {
		return ZephyrOpen.zephyropen;
	}

	public String getAddress() {
		return constants.get(ZephyrOpen.address);
	}

	public long getDelta() {
		return System.currentTimeMillis() - time;
	}
}
