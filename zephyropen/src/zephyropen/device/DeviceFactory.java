package zephyropen.device;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.device.arduino.ArduinoDevice;
import zephyropen.device.elevation.ElevationDevice;
import zephyropen.device.polar.PolarDevice;
import zephyropen.device.zephyr.BioharnessDevice;
import zephyropen.device.zephyr.HrmDevice;
import zephyropen.device.zephyr.HxmDevice;

/**
 * 
 * Create a device by the given nane -- use conventions for each name/device
 * mappings.
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class DeviceFactory {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();

	/** the device details must be in the framework */
	public static Device create() {
	
		String deviceName = constants.get(ZephyrOpen.deviceName);
		int type = PrototypeFactory.getDeviceType(deviceName);
		
		constants.info("DeviceFactory.create(" + deviceName + ")");
		
		if (type == PrototypeFactory.HXM) {
			Device device = new HxmDevice(deviceName);
        	if( constants.getBoolean(ZephyrOpen.enableWatchDog))
        		new WatchDog(device).start();
			return device;
		}

		if (type == PrototypeFactory.HRM) {
			Device device = new HrmDevice(deviceName);
        	if( constants.getBoolean(ZephyrOpen.enableWatchDog))
        		new WatchDog(device).start();
        	return device;
		}

		if (type == PrototypeFactory.BIOHARNESS) {
			Device device = new BioharnessDevice(deviceName);
        	if( constants.getBoolean(ZephyrOpen.enableWatchDog))
        		new WatchDog(device).start();
			return device;
		}

		// ensure is a com port in properties
		String com = constants.get(ZephyrOpen.com);
		
		if( com == null ){
			constants.error("DeviceFactory(): no com port in properties");
			return null;
		}
	
		if (type == PrototypeFactory.POLAR)
			return new PolarDevice(com);

		if (type == PrototypeFactory.ELEVATION)
			return new ElevationDevice(com);

		if (type == PrototypeFactory.ARDUINO)
			return new ArduinoDevice(com);

	
		return null;
	}

}
