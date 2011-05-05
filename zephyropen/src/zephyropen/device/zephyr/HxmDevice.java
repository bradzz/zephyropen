package zephyropen.device.zephyr;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.device.Device;
import zephyropen.device.WatchDog;
import zephyropen.port.AbstractPort;
import zephyropen.port.bluetooth.SearchSPP;
import zephyropen.port.bluetooth.SerialUtils;
import zephyropen.util.Utils;

/**
 * 
 * <p> A Basic server for the Zephyr BlueTooth HXM 
 * <p> Package : Created: September 20, 2008
 * 
 * <p>See the documents here: 
 * <p> http://www.zephyrtech.co.nz/support/softwaredevelopmentkit
 * <p> http://www.zephyrtech.co.nz/assets/pdfs/bluetooth_hxm_api_guide.pdf
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class HxmDevice extends AbstractPort implements Device {

	/** allocate a byte array for receiving data from the serial port */
	private static final int BUFFER_SIZE = 60;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private byte[] packet = new byte[BUFFER_SIZE];

	/**
	 * <p> Constructor for the HxM Server API
	 * 
	 * @param name is the blue tooth friendly name of the HXM 
	 */
	public HxmDevice(String deviceName) {
		
		port = new SearchSPP(deviceName);
		
		command = new Command(PrototypeFactory.hxm);
		
		command.add(ZephyrOpen.deviceName, getDeviceName());
	}
	
	/** Loop on BT input */
	public void readDevice() {
				
		// command.add(ZephyrOpen.address, port.getAddress());
		
    	new WatchDog(this).start();    
		
		while(getDelta() < ZephyrOpen.TIME_OUT) {
			
			Utils.delay(300);
			packet = SerialUtils.getAvail(port, buffer, BUFFER_SIZE);

			if (packet != null) {

				/** track arrival of data packets */
				last = System.currentTimeMillis();

				if( ZephyrUtils.vaildHxmPacket(packet)) {
					
					/** add heart rate, beat count */
					command = ZephyrUtils.parseHrmPacket(packet, command);
					
					/** add speed, distance etc */
					command = ZephyrUtils.parseHxmPacket(packet, command);
					
					/** add RR info */
					command = ZephyrUtils.parseHxmRtoR(packet, command);
					
					/** dead battery -> dead connection ? */
					if( command.get(PrototypeFactory.battery).equals("0")){
						constants.error("HXM battery is dead", this);
						port.close();
					}

					System.out.println(command);
					command.send();
					Utils.delay(600);
				} 
			}
		}
	}

	@Override
	public String getDeviceName() {
		return PrototypeFactory.hxm;
	}
}
