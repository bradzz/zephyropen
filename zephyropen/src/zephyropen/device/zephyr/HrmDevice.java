package zephyropen.device.zephyr;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.device.Device;
import zephyropen.port.AbstractPort;
import zephyropen.port.bluetooth.SearchSPP;
import zephyropen.port.bluetooth.SerialUtils;


/**
 * 
 * <p> A Basic server for the Zephyr BlueTooth devices 
 * <p> Package : Created: September 20, 2008
 * 
 * 
 * <p>See the documents here: 
 * <p> http://www.zephyrtech.co.nz/support/softwaredevelopmentkit
 * <p> http://www.zephyrtech.co.nz/assets/pdfs/bluetooth_hxm_api_guide.pdf
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class HrmDevice extends AbstractPort implements Device {
	
	/** allocate a byte array for receiving data from the serial port */
	private static final int BUFFER_SIZE = 60;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private byte[] packet = new byte[BUFFER_SIZE];
	
	/**
	 * <p> Constructor for the HxM Server API
	 * 
	 * @param name is the blue tooth friendly name of the HXM 
	 */
	public HrmDevice(String name) {
		
		port = new SearchSPP(name);

		command = new Command(PrototypeFactory.hrm);
		
		command.add(ZephyrOpen.deviceName, getDeviceName());
		
	} 
	
	/** loop on BT input */
	public void readDevice() {
			
		command.add(ZephyrOpen.address, port.getAddress());
		
		while (getDelta() < ZephyrOpen.TIME_OUT) {

			packet = SerialUtils.getAvail(port, buffer, BUFFER_SIZE);

			if (packet != null) {

				// track arrival of data packets
				last = System.currentTimeMillis();

				if( ZephyrUtils.vaildHxmPacket(packet)) {
					
					// add speed, distance etc 
					command = ZephyrUtils.parseHrmPacket(packet, command);
			
					// add rr into same packet 
					command = ZephyrUtils.parseHxmRtoR(packet, command);
					
					/*
					if( command.get(PrototypeFactory.battery).equals("0") ) {
						
						constants.error("HRM battery is dead?", this);
						port.close();
						
					} 
					*/
					
						
					// send it 
					command.send();
					
				}
			}
		}
	}

	@Override
	public String getDeviceName() {
		return PrototypeFactory.hrm;
	}
}
