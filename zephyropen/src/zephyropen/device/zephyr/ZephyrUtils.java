package zephyropen.device.zephyr;

import java.math.BigInteger;

import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.port.Port;

/**
 * <p>
 * Operations to read BT packets from Zephyr Devices
 * 
 * <p>
 * See the documents here:
 * <p>
 * http://www.zephyrtech.co.nz/support/softwaredevelopmentkit
 * <p>
 * http://www.zephyrtech.co.nz/assets/pdfs/bluetooth_hxm_api_guide.pdf
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class ZephyrUtils {

	/** framework configuration */
	private final static ZephyrOpen constants = ZephyrOpen.getReference();

	/** Zephyr packet constants */
	public final static int checksumPolynomial = 0x8C;

	/** HXM message id */
	public final static byte HXM_ID = 0x26;

	/** End of text */
	public final static byte ETX = 0x03;

	/** Start of text */
	public final static byte STX = 0x02;

	/** HXM packet size */
	public final static byte HXM_DLC = 0x37;

	/**
	 * Do a CBC check on this packet
	 * 
	 * @param packet
	 *            to test
	 * @return true if error free
	 */
	public static boolean checkCRC(byte[] packet) {

		int crc = 0;
		for (int i = 2; i < 57; i++)
			crc = ZephyrUtils.ChecksumPushByte(crc, ZephyrUtils
					.readUnsignedByte(packet[i]));

		/** Then compare to the packet CRC */
		if (crc == ZephyrUtils.readUnsignedByte(packet[57]))
			return true;

		return false;
	}

	/** CRC check taken from Zephyr PDF's */
	public static int ChecksumPushByte(int currentChecksum, int newByte) {

		currentChecksum = (currentChecksum ^ newByte);

		for (int bit = 0; bit < 8; bit++) {

			if ((currentChecksum & 1) == 1)
				currentChecksum = ((currentChecksum >> 1) ^ checksumPolynomial);

			else
				currentChecksum = (currentChecksum >> 1);
		}

		return currentChecksum;
	}

	/**
	 * @param b
	 *            is the byte to convert
	 * @return a integer from the given byte
	 */
	public static int readUnsignedByte(byte b) {
		return (b & 0xff);
	}

	/**
	 * Basic byte array copy
	 * 
	 * @param dest
	 * @param input
	 * @param startIndex
	 * @param size
	 */
	public static void copy(final byte[] dest, final byte[] input,
			final int startIndex, final int size) {
		int c = 0;
		for (int i = startIndex; i < size; i++)
			dest[c++] = input[i];
	}

	/**
	 * 
	 * @param packet
	 *            of bytes
	 * @param index
	 *            of the byte to parse in the byte array
	 * @return a String of the indexed byte
	 */
	public static String parseString(byte[] packet, int index) {
		String hex = byteToHex(packet[index]);
		short value = Short.parseShort(hex, 16);
		return String.valueOf(value);
	}

	/**
	 * 
	 * @param packet
	 *            of bytes
	 * @param index
	 *            of the byte to parse in the byte array
	 * @return a String of the indexed byte
	 */
	public static short parseShort(byte[] packet, int index) {
		String hex = byteToHex(packet[index]);
		return Short.parseShort(hex, 16);
	}

	/**
	 * Merge two bytes into a signed 2's complement integer
	 * 
	 * @param low
	 *            byte is LSB
	 * @param high
	 *            byte is the MSB
	 * @return a signed intt value
	 */
	public static int merge(byte low, byte high) {
		int b = 0;
		b += (high << 8) + low;
		if ((high & 0x80) != 0) {
			b = -(0xffffffff - b);
		}
		return b;
	}

	/**
	 * Merge two bytes into a unsigned integer
	 * 
	 * @param low
	 *            byte is LSB
	 * @param high
	 *            byte is the MSB
	 * @return an unsigned int value
	 */
	public static int mergeUnsigned(byte low, byte high) {
		int lint = low & 0xff;
		int hint = high & 0xff;
		return (int)( hint << 8 | lint );

	}

	/**
	 * Convert a byte to a hex string.
	 * 
	 * @param data
	 *            the byte to convert
	 * @return String the converted byte
	 */
	public static String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}

	/**
	 * Convert a byte array to a hex string.
	 * 
	 * @param data
	 *            the byte[] to convert
	 * @return String the converted byte[]
	 */
	public static String bytesToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			buf.append(byteToHex(data[i]));
		}
		return buf.toString();
	}

	/**
	 * Convert an int to a hex char.
	 * 
	 * @param i
	 *            is the int to convert
	 * @return char the converted char
	 */
	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9))
			return (char) ('0' + i);
		else
			return (char) ('a' + (i - 10));
	}

	/**
	 * Display this BT packet to the console
	 * 
	 * @param packet
	 *            of raw bytes to display in hex and byte formats
	 */ 
	   
	public static void print(byte[] packet) { 
		
		if (packet == null) return;
		
		int size = packet.length; 
		for (int c = 0; c < size;c++) 
			System.err.println("[" + c + "] \t hex : " +
	             byteToHex(packet[c]) + "\t byte : " + packet[c]); }
	 

	/**
	 * Convert a string into an array of hex bytes
	 * 
	 * @param data
	 *            string in hex format (two chars per byte)
	 * @return the pairs as an array of bytes
	 */
	public byte[] getBytes(String data) {
		if (data == null)
			return null;
		return new BigInteger(data, 16).toByteArray();
	}

	/**
	 * Write a string to a SPP
	 * 
	 * @param bytes
	 *            to be sent to the bioharness
	 * @return true if the packet was sent
	 */
	public static boolean writeSPP(byte[] bytes, Port spp) {

		try {

			spp.writeBytes(bytes);

		} catch (Exception e) {
			spp.close();
			return false;
		}

		return true;
	}

	/** Send the packet to enable data packets, 1 per second */
	public static void setupBioharness(Port spp) {

		// setup hr packets
		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = 0x14; // msg ID
		data[2] = 0x01; // dlc
		data[3] = 0x01; // turn on data stream every second
		data[4] = 0x5e; // crc
		data[5] = ETX;

		writeSPP(data, spp);
	}

	/** Send the packet to enable data packets, 1 per second */
	public static void setupBioharnessRtoR(Port spp) {

		// setup hr packets
		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = 0x19; // msg ID
		data[2] = 0x01; // dlc
		data[3] = 0x01; // turn on data stream every second
		data[4] = 0x5e; // crc
		data[5] = ETX;

		writeSPP(data, spp);
	}

	/** Send reset command to bioharness */
	public static void resetBioharness(Port spp) {

		// setup hr packets at one per second
		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = 0x1F; // msg ID
		data[2] = 0x07; // dlc
		data[4] = 0x5e; // crc
		data[5] = ETX;

		writeSPP(data, spp);
	}

	/**
	 * Convert a raw bluetooth packet to XML command object
	 * 
	 * @param packet
	 *            packet is the raw bytes from the SPP
	 * @return Command is the same command but with the Base Bioharness elements
	 *         added
	 */
	public static Command parseBioharnessPacket(byte[] packet, Command command) {

		try {

			/** add packet type to avoid confusion with RR packets */
			// command.add(constants.KIND, DATA);
			command.add(PrototypeFactory.beat, ZephyrUtils.parseString(packet, 3));
			
			String hrBytes = byteToHex(packet[12]);
			short hrValue = Short.parseShort(hrBytes, 16);
			command.add(PrototypeFactory.heart, Short.toString(hrValue));

			int v = merge(packet[24], packet[25]);
			command.add(PrototypeFactory.battery, String.valueOf(((double) v / (double) 1000)));

			int p = merge(packet[18], packet[19]);
			command.add(PrototypeFactory.posture, String.valueOf(((double) p / (double) 10)));

			int r = merge(packet[14], packet[15]);
			command.add(PrototypeFactory.respiration, String.valueOf(
					Math.abs(((double) r / (double) 10))));

			int t = merge(packet[16], packet[17]);
			command.add(PrototypeFactory.temperature, String.valueOf(((double) t / (double) 10)));

		} catch (Exception e) {
			constants.error("parseBioharnessPacket() : " + e.getMessage());
		}

		/** add other tags before sending ? */
		return command;
	}

	/**
	 * Convert a raw bluetooth packet an XML command object
	 * 
	 * @param packet
	 *            is the raw bytes from the SPP
	 * @return Command is the same command passed in, but with the Accelerometer
	 *         elements added
	 */
	public static Command parseHxmPacket(byte[] packet, Command command) {

		try {

			/** add packet type to avoid confusion with RR packets */
			// command.add(PrototypeFactory.type, "HXM");
			command.add(PrototypeFactory.strides, ZephyrUtils.parseString(packet, 54));

			// turn into a string, after scale factor applied
			int d = ZephyrUtils.mergeUnsigned(packet[50], packet[51]);
			command.add(PrototypeFactory.distance, String.valueOf(Math
					.abs(((double) d / (double) 16))));

			int s = ZephyrUtils.mergeUnsigned(packet[52], packet[53]);
			command.add(PrototypeFactory.speed, String.valueOf(Math
					.abs(((double) s / (double) 256))));

			int c = ZephyrUtils.mergeUnsigned(packet[54], packet[55]);
			command.add(PrototypeFactory.cadence, String.valueOf(Math
					.abs(((double) c / (double) 16))));

		} catch (Exception e) {
			constants.error("parseHxmPacket() : " + e.getMessage());
		}

		/** add other tags before sending ? */
		return command;
	}

	/**
	 * Convert a raw bluetooth packet an XML command object
	 * 
	 * @param packet
	 *            is the raw bytes from the SPP
	 * @return Command is the same command but with the HRM elements added
	 */
	public static Command parseHrmPacket(byte[] packet, Command command) {

		//command.add("type", "HRM");
		command.add(PrototypeFactory.battery, ZephyrUtils.parseString(packet, 11));
		command.add(PrototypeFactory.heart, ZephyrUtils.parseString(packet, 12));
		command.add(PrototypeFactory.beat, ZephyrUtils.parseString(packet, 13));

		return command;
	}

	/**
	 * Convert a raw bluetooth packet an XML command object
	 * 
	 * @param packet
	 *            is the raw bytes from the SPP
	 * @return Command is the same command but with the RR elements added
	 */
	public static Command parseHxmRtoR(byte[] packet, Command command) {

		// command.add(constants.KIND, RtotR);

		/** first rr time stamp index, see pdf */
		int index = 14;
		for (int i = 0; i < 15; i++) {

			/** two bytes per time stamp */
			command.add(PrototypeFactory.rr + String.valueOf(i), String.valueOf(Math
					.abs(ZephyrUtils.mergeUnsigned(packet[index],
							packet[index + 1]))));

			/** each time stamp is two bytes wide */
			index += 2;
		}
		return command;
	}

	/**
	 * Convert a raw bluetooth packet an XML command object
	 * 
	 * @param packet
	 *            is the raw bytes from the SPP
	 * @return Command is the same command but with the RR elements added
	 */
	public static Command parseBioharnessRtoR(byte[] packet, Command command) {

		command.add(PrototypeFactory.beat, ZephyrUtils.parseString(packet, 3));
		
		// command.add("type", "RtoR");
		
		int j = 0;
		for (int i = 12; i < 48; i += 2) {

			command.add(PrototypeFactory.rr + String.valueOf(j++), String.valueOf(Math
					.abs(merge(packet[i], packet[i + 1]))));

		}

		/** add other tags before sending ? */
		return command;
	}

	/**
	 * Check if this is valid HXM packet
	 * 
	 * @param packet
	 *            of raw bytes to test before parsing
	 * @return true if this is an error free transmission
	 */
	public static boolean vaildHxmPacket(byte[] packet) {

		if (packet == null)
			return false;

		if (packet.length != 60) {
			/** most common, happens when not in sync with HXM */
			constants.error("wrong packet size on HXM");
			return false;
		}

		if (packet[0] != STX) {
			constants.error("STX error on HXM");
			return false;
		}

		if (packet[1] != HXM_ID) {
			constants.error("MSG_ID error on HXM");
			return false;
		}

		if (packet[2] != HXM_DLC) {
			constants.error("DLC error on HXM");
			return false;
		}

		if (packet[59] != ETX) {
			constants.error("ETC error on HXM");
			return false;
		}

		if (ZephyrUtils.checkCRC(packet)) {
			constants.error("CRC error on HXM");
			return false;
		}

		/** all is well, parse this one */
		return true;
	}

	/**
	 * Gets the end of the byte array given.
	 * 
	 * @param b
	 *            byte array
	 * @param pos
	 *            the position from which to start
	 * @return a byte array consisting of the portion of b between pos and the
	 *         end of b.
	 */
	public static byte[] copy(byte[] b, int pos) {
		return copy(b, pos, b.length - pos);
	}

	/**
	 * Gets a sub-set of the byte array given.
	 * 
	 * @param b
	 *            byte array
	 * @param pos
	 *            the position from which to start
	 * @param length
	 *            the number of bytes to copy from the original byte array to
	 *            the new one.
	 * @return a byte array consisting of the portion of b starting at pos and
	 *         continuing for length bytes, or until the end of b is reached,
	 *         which ever occurs first.
	 */
	public static byte[] copy(byte[] b, int pos, int length) {
		byte[] z = new byte[length];
		System.arraycopy(b, pos, z, 0, length);
		return z;
	}

	/**
	 * Build a float from the first 4 bytes of the array.
	 * 
	 * @param b
	 *            the byte array to convert.
	 */
	public static float toFloat(byte[] b) {
		int i = toInt(b);
		return Float.intBitsToFloat(i);
	}

	/**
	 * Build an int from first 4 bytes of the array.
	 * 
	 * @param b
	 *            the byte array to convert.
	 */
	public static int toInt(byte[] b) {
		return (((int) b[3]) & 0xFF) + ((((int) b[2]) & 0xFF) << 8)
				+ ((((int) b[1]) & 0xFF) << 16) + ((((int) b[0]) & 0xFF) << 24);
	}


	/**
	 * Basic byte array add to end of byte array. 
	 * 
	 * @param dest
	 * @param input
	 * @param startIndex
	 * @param bytes
	 *
 	 * @return true if successful
	 */
	public static boolean add(final byte[] dest, final byte[] input,
			final int startIndex, final int bytes ) {
		
		if ( startIndex+bytes > dest.length  ) {
			System.out.println("ZehyrUtils.add(d,i,s):  Cannot add more bytes than there are in destination array");
			// Cannot add more bytes than there are in destination array
			return false;
		}

		for (int i = 0; i < bytes; i++) {
			dest[i + startIndex] = input[i];
		}
		
		return true;
	}
}
