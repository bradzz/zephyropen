package zephyropen.port.bluetooth;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.util.Utils;

/* TEST DRIVER -- show bluetooth devices to the terminal in XML format */
public class DiscoveryViewver implements API {
	
	ZephyrOpen constants = ZephyrOpen.getReference();

	public DiscoveryViewver(){
		
		constants.init();
		
		//ApiFactory.getReference().add(ZephyrOpen.ZEPHYR_OPEN, this);
		ApiFactory.getReference().add(this);
		
		// wait on input 
		Utils.delay(Integer.MAX_VALUE);
	}

	public void execute(Command command) {
		String status = command.get(ZephyrOpen.status);
		if( status != null )
			System.out.println("status = " + status);
			
		else System.out.println(command); 
	}
	
	public String getAddress() {
		return constants.get(ZephyrOpen.address);
	}

	public long getDelta() {
		return 0;
	}

	public String getDeviceName() {
		return ZephyrOpen.discovery;
	}
	 
	/** driver */
	public static void main(String[] args) {
		new DiscoveryViewver();
	}
}
