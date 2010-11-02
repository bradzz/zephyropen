package zephyropen.socket.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.device.wii.WiiUtils;
import zephyropen.socket.AbstractOutputChannel;
import zephyropen.socket.InputChannel;
import zephyropen.socket.OutputChannel;
import zephyropen.util.Utils;

/**
 * <p>
 * Listen for OSC messages on a given UDP Port, convert to XML commands for the
 * framework
 * 
 * Package : zephyr.framework.socket.multicast
 * <p>
 * Created: 20 AUG 2009
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class OSCChannel extends AbstractOutputChannel implements OutputChannel, InputChannel, API {

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();

	// size of the OSC Message
	final private static int BUFFER_SIZE = 16;

	// xml tag for this device
	private static final String deviceName = PrototypeFactory.wii;

	// TODO: take from properties file
	private static final int port = 9000;

	private DatagramSocket serverSocket = null;
	private Command feedback = null;
	
	private long lastMessage = System.currentTimeMillis();
	private int oscMessages, xmlMessages = 0;

	/** Constructor */
	private OSCChannel() {

		/** reuse command object */
		feedback = new Command(deviceName);

		try {

			/** construct the server socket */
			serverSocket = new DatagramSocket(port);

		} catch (Exception e) {
			constants.shutdown(e);
		}

		constants.info("listeing on port: " + port, this);

		/** register for messages */
		ApiFactory.getReference().add(this);
		
		run();
	}

	/** loop forever */
	public void run() {
		try {

			/** re-use the data buffering objects */
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);

			/** loop forever, waiting to receive packets */
			while (true) {

				/** Wait to receive a UDP packet */
				serverSocket.receive(packet);

				/** count incoming */
				oscMessages++;

				/** Parse out the data, put into command object */
				feedback = WiiUtils.parseOSC(packet, feedback);

				/** wait for a full XML data data */
				if (feedback.get(PrototypeFactory.accel) != null
						&& feedback.get(PrototypeFactory.pitch) != null
						&& feedback.get(PrototypeFactory.roll) != null
						&& feedback.get(PrototypeFactory.yaw) != null) {

					// send it, then clear it
					feedback.send();
					feedback.flush();
					xmlMessages++;
				}
			}
		} catch (Exception e) {
			constants.shutdown(e);
		}
	}

	/**
	 * @param out
	 *            is a string to write to the socket
	 */
	public void write(String out) {

		try {

			/** create new packet */
			DatagramPacket packet = new DatagramPacket(out.getBytes(), out
					.length(), InetAddress.getLocalHost(), port);

			/** dump it into the socket */
			serverSocket.send(packet);

		} catch (Exception e) {
			constants.error("unable to write to socket", this);
		}
	}

	/** send to terminal to verify we are sending */
	public void execute(Command command) {
		
		if (xmlMessages % 100 == 0)
			System.out.println(Utils.getTime() 
					+ " xml [" + xmlMessages +"] osc [" + oscMessages +  "] delta : " + getDelta()); 
			
		lastMessage = System.currentTimeMillis();
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getAddress() {
		return constants.get(ZephyrOpen.address);
	}

	public long getDelta() {
		return System.currentTimeMillis() - lastMessage;
	}

	/** @param args */
	public static void main(String[] args) {

		/** basic configuration to send on multicast UDP */
		constants.init();
		new OSCChannel();

	}
}
