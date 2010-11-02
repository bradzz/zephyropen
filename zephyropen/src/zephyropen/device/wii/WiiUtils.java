package zephyropen.device.wii;

import java.net.DatagramPacket;

import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.device.zephyr.ZephyrUtils;
import zephyropen.util.Utils;

/**
 * 
 * <p/> A Utilities class for OSC messages to be turned into XML 
 * <p/> Created: Nov 11, 2009
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class WiiUtils {
	
	/** framework configuration */
	private final static ZephyrOpen constants = ZephyrOpen.getReference();

	/**
	 * Takes a byte array starting with a string padded with null characters so
	 * that the length of the entire block is a multiple of 4, and seperates it
	 * into a String and a byte array of the remaining data. These are then
	 * returned in a Vector.
	 * 
	 * @param block
	 *            block of data beginning with a string
	 * @param stringLength
	 *            number of characters in the string
	 */
	public static String getName(byte[] block, int stringLength) {

		if (stringLength % 4 != 0) {
			constants.info("WiiUtils.getName: bad boundary");
			return null;
		}

		int i = 0;
		for (; block[i] != '\0'; i++) {
			if (i >= stringLength) {
				constants.error("printNameAndArgs: Unreasonably long string");
				return null;
			}
		}
		return (new String(ZephyrUtils.copy(block, 0, i)));
	}

	public byte[] getData(byte[] block, int stringLength) {
		int i = 1;
		for (; (i % 4) != 0; i++) {
			if (i >= stringLength) {
				constants.info("printNameAndArgs: Unreasonably long string");
				return null;
			}
			if (block[i] != '\0') {
				constants.info("printNameAndArgs: Incorrectly padded string.");
				return null;
			}
		}
		return ZephyrUtils.copy(block, i);
	}

	/** error check 
	public boolean valid(String msg) {
		if (!msg.startsWith("/" + deviceName)) {
			constants.info("wrong device", this);
			return false;
		}
		return true;
	}*/

	/** Get remote number from OSC message 
	private static String getMoteNumber(String msg) {
		return msg.substring(msg.indexOf(PrototypeFactory.wii) + msg.length() + 1,
				msg.indexOf(PrototypeFactory.wii) + msg.length() + 2);
	}*/

	/**
	 * Fill an XML command from an OSC message 
	 * 
	 * @param packet is a raw byte OSC message 
	 * @param command is an XML command to be populated 
	 * @return the XML command object 
	 */
	public static Command parseOSC(DatagramPacket packet, Command command) {
		
		
		byte[] bytes = packet.getData();
		String deviceName = getName(bytes, bytes.length);
		
		// default to 1 
		command.add(PrototypeFactory.mote, "1"); //getMoteNumber(deviceName) );
		
		// magic numbers 
		byte[] data = new byte[] { bytes[24], bytes[25], bytes[26], bytes[27] };
		Float value = new Float(ZephyrUtils.toFloat(ZephyrUtils.copy(data,0, 4)));
		
		// chop off extra dec points 
		String dec = Utils.formatFloat(value, ZephyrOpen.PRECISION); 
		
		char axis = deviceName.charAt(deviceName.length()-1);
		if (axis == '0')
			command.add(PrototypeFactory.pitch,dec);
		else if (axis == '1')
			command.add(PrototypeFactory.roll,dec);
		else if (axis == '2')
			command.add(PrototypeFactory.yaw,dec);
		else if (axis == '3')
			command.add(PrototypeFactory.accel,dec);
		else {
			constants.error( "no axis infirmation: " + deviceName);
			return null;
		}
		
		return command;
	}
	
	/**
	 * Fill an XML command from an OSC message 
	 * 
	 * @return the XML command object 
	 */
	public static Command create(String mote, double acc, double p, double r, double y) {
		Command command = new Command(PrototypeFactory.wii);
		command.add(PrototypeFactory.mote, mote);
		command.add(PrototypeFactory.pitch, Utils.formatFloat(p, ZephyrOpen.PRECISION));
		command.add(PrototypeFactory.roll, Utils.formatFloat(r, ZephyrOpen.PRECISION));
		command.add(PrototypeFactory.yaw, Utils.formatFloat(y, ZephyrOpen.PRECISION));
		command.add(PrototypeFactory.accel, Utils.formatFloat(acc, ZephyrOpen.PRECISION));
		return command;
	}
	
	/**
	 * Use the default of remote number 1
	 * 
	 * @param acc
	 * @param p is the pitch 
	 * @param r is the roll 
	 * @param y is the yaw 
	 * @return the xml command 
	 */
	public static Command create(double acc, double p, double r, double y) {
		return create("1", acc, p, r, y);
	}
}
