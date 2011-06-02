package zephyropen.device.zephyr;

import java.io.IOException;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.device.Device;
import zephyropen.device.WatchDog;
import zephyropen.port.AbstractPort;
import zephyropen.port.bluetooth.SearchSPP;
//import zephyropen.port.bluetooth.SerialPortProfile;
import zephyropen.port.bluetooth.SerialUtils;
import zephyropen.util.Utils;

/**
 * 
 * <p>
 * A Basic server for the Zephyr BlueTooth Bioharness
 * <p>
 * Package : Created: September 20, 2008
 * 
 * <p>
 * See the documents here: ??? NDA required
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class BioharnessDevice extends AbstractPort implements Device {

	/** Bioharness packet constants */
	public static final byte ACK = 0x06;
	public static final byte NAK = 0x15;

	/** End of text */
	public final static byte ETX = 0x03;

	/** Start of text */
	public final static byte STX = 0x02;

	public static final int ERROR = 0;
	public static final int DATA_PACKET = 1;
	public static final int RTOR_PACKET = 2;
	public static final int LIFE_PACKET = 3;
	public static final int HXM_PACKET = 4;

	/** allocate a byte array for receiving data from the serial port */
	private static final int BUFFER_SIZE = 60;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private byte[] packet = new byte[BUFFER_SIZE];

	/**
	 * @param ame
	 *            is the bluetooth name to search for
	 */
	public BioharnessDevice(String name) {

		port = new SearchSPP(name);

		command = new Command(PrototypeFactory.bioharness);

		command.add(ZephyrOpen.deviceName, name);

	}

	/** Loop on BT input */
	public void readDevice() {

		/** sanity test if (!connected) return; */
		ZephyrUtils.setupBioharness(port);
		ZephyrUtils.setupBioharnessRtoR(port);

    	new WatchDog(this).start();    
		
		short i = 0;
		while (getDelta() < ZephyrOpen.TIME_OUT) {

			Utils.delay(200);

			try {
				packet = SerialUtils.getAvail(port, buffer, BUFFER_SIZE);
			} catch (IOException e) {
				constants.error(e.getMessage(), this);
				return;
			}
			
			//System.out.println("read device(): buff size: " + buffer.length);

			if (packet != null) {

				/** find out what type of packet */
				int type = (getPacketType(packet));

				/** parse data, send to listening devices */
			if (type == DATA_PACKET) {
					
					constants.info("data packet");

					command = ZephyrUtils.parseBioharnessPacket(packet, command);
					command.send();
					
					//last = System.currentTimeMillis();					

				
				} else
					
					if( type == RTOR_PACKET ) {
					
					constants.info("RR packet");
					
					command = ZephyrUtils.parseBioharnessRtoR(packet, command);
					command.send();
					//last = System.currentTimeMillis();

				} 
					
					//else {
					
//					constants.info("data packet");

	//				command = ZephyrUtils.parseBioharnessPacket(packet, command);
		//			command.send();
				
					
			//	}

				if (i++ % 10 == 0) {
					ZephyrUtils.setupBioharness(port);
					ZephyrUtils.setupBioharnessRtoR(port);
				}
			
				//if(constants.getBoolean(ZephyrOpen.frameworkDebug)		
				constants.info(command.toString());
		
				// keep track of incoming data times
				last = System.currentTimeMillis();
			}
		}
	}

	/**
	 * @param packet
	 *            of bytes to evaluate
	 * @return the type of packet this is
	 */
	protected int getPacketType(byte[] packet) {

		if (packet == null){		
			constants.error("null packet", this);
			return ERROR;
		}
		
		if(packet[0] != ZephyrUtils.STX){
			System.out.println("not start of text");
			return ERROR;
		}
	
		/*
		if (packet[60] != ZephyrUtils.ETX) {
			constants.error("ETC error on Bioharness", this);
			return ERROR;
		}*/


		if (packet.length != 60) {
		
			constants.error("wrong packet size on Bioharness", this);
			return ERROR;
		}
		
		if (ZephyrUtils.checkCRC(packet)) {
			constants.error("CRC error on Bioharness", this);
			return ERROR;
		}

		// This happens when two packets come in together..
		// with ank ack from sending a keep-alive message

		/*
		 * if (packet[packet.length-1] != ACK) {
		 * 
		 * if (packet[packet.length - 1] != ZephyrUtils.ETX) {
		 * 
		 * if (packet[packet.length - 1] != NAK) { constants.error(
		 * " invalid packet, size = " + packet.length + "\n --> " +
		 * ZephyrUtils.bytesToHex(packet), this);
		 * 
		 * return ERROR; } } }
		 */

		if (packet[1] == 0x20) {
			constants.info("data packet", this);
			return DATA_PACKET;
		}
		if (packet[1] == 0x24) {
			constants.info("r to r packet", this);
			return RTOR_PACKET;
		}
		if (packet[1] == 0x23) {
			constants.info("life packet", this);
			return LIFE_PACKET;
		}
		/*
		if (packet[1] == 0x26) {
			constants.info("HXM packet", this);
			return HXM_PACKET;
		}*/
		
		// ZephyrUtils.print(packet);
		
		//constants.info(SerialUtils.toString(packet, packet.length), this);
		
		constants.error("unkown bioharness packet type", this);

		// no match found
		return ERROR;
	}

	public String getDeviceName() {
		return PrototypeFactory.bioharness;
	}
}
