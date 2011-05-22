package zephyropen.device;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

/** Spin locked thread to ensure the connection to the device is active */
public class WatchDog extends Thread implements Runnable { 		
	
	/** framework configuration */
	private ZephyrOpen constants = ZephyrOpen.getReference();

	/** device to keep an eye on */
	private Device device = null;
	
	/** constructor */
	public WatchDog(Device dev){ 	
		this.device = dev;
		this.setDaemon(true);
	}

	/*
	public void reset(){
		device.
	}*/
	
	/** start() call back */
	public void run() {
		
		// constants.info("watchdog thread id: ", String.valueOf(this.getId()));
		constants.info("watchdog connected: " + device.getDeviceName(), this);
		
		/** keep checking for input with a spin lock */ 
		while (device.getDelta() < ZephyrOpen.TIME_OUT) 
			Utils.delay(DeviceServer.SPIN_TIME);
		
		/** log run time */  
		constants.info(device.getDeviceName() + ", TIMEOUT = " + ZephyrOpen.TIME_OUT, this);
		constants.info(device.getDeviceName() + ", TIMEOUT delta = " + device.getDelta(), this);
		
		/** let device clean up */ 
		//constants.info("watchdog thread id: ", this.getId());
		constants.info("watchdog closing device: " + device.getDeviceName(), this);
		
		device.close();
		//constants.shutdown();
	}
} 