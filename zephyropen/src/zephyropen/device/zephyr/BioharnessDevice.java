package zephyropen.device.zephyr;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.device.Device;
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
			
			//new SerialPortProfile("00078088F38E");

		command = new Command(PrototypeFactory.bioharness);

		command.add(ZephyrOpen.deviceName, name);

	}

	/*
	 * public String trimName(String name) { int space = name.indexOf(' ');
	 * return name.substring(space + 1); }
	 */

	/** Loop on BT input */
	public void readDevice() {

		command.add(ZephyrOpen.address, port.getAddress());

		/** sanity test if (!connected) return; */
		ZephyrUtils.setupBioharness(port);
		// ZephyrUtils.setupBioharnessRtoR(port);

		short i = 0;
		while (getDelta() < ZephyrOpen.TIME_OUT) {

			Utils.delay(300);

			packet = SerialUtils.getAvail(port, buffer, BUFFER_SIZE);

			if (packet != null) {

				/** find out what type of packet */
				int type = (getPacketType(packet));

				/** parse data, send to listening devices */
				if (type == DATA_PACKET) {

					command = ZephyrUtils
							.parseBioharnessPacket(packet, command);

					command.send();

					/** parse R to R to same command */

				}

				/*
				 * else if( type == RTOR_PACKET ) {
				 * 
				 * command = ZephyrUtils.parseBioharnessRtoR(packet, command);
				 * 
				 * // all good, send it // if( ! command.isEmpty() ){
				 * 
				 * 
				 * if(command.isMalformedCommand(ZephyrUtils.BioharnessPrototype
				 * )){ constants.info( "malformed", this); return; }
				 * 
				 * // constants.info("xml: " + command.toXML(), this); //
				 * constants.info("out: " +
				 * command.list(ZephyrUtils.BioharnessPrototype), this); //
				 * constants.info("out: " + command.toString(), this); //
				 * 
				 * // clear it // command.flush(); //} }
				 */

				if (i++ % 10 == 0) {
					ZephyrUtils.setupBioharness(port);
					// ZephyrUtils.setupBioharnessRtoR(port);
				}

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
		
		/*
		 * if(packet[0] != ZephyrUtils.STX){ constants.info("packet[0] != STX",
		 * this); return ERROR; }
		 * 
		 * if(packet[packet.length-1] != ZephyrUtils.ETX ){
		 * constants.info("packet[" + packet.length + "] != ETX", this); return
		 * ERROR; }
		 * 
		 * 
		 * if( ZephyrUtils.checkCRC(packet) ) {
		 * constants.error("crc error on device [" + deviceName + "]", this);
		 * return ERROR; }
		 */

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
			// constants.info("data packet", this);
			return DATA_PACKET;
		}
		if (packet[1] == 0x24) {
			// constants.info("r to r packet", this);
			return RTOR_PACKET;
		}
		if (packet[1] == 0x23) {
			// constants.info("life packet", this);
			return LIFE_PACKET;
		}
		if (packet[1] == 0x26) {
			// constants.info("HXM packet", this);
			return HXM_PACKET;
		}
		
		// System.out.println("unkown bioharness packet type");

		// no match found
		return ERROR;
	}

	public String getDeviceName() {
		return PrototypeFactory.bioharness;
	}
}
