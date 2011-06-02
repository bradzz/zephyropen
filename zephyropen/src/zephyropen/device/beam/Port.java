package zephyropen.device.beam;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.Date;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;
import zephyropen.util.Utils;

public class Port implements SerialPortEventListener {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();

	// ready to be sent
	public static final byte TEST = 't';
	public static final byte[] HOME = { 'h' };
	public static final byte[] GET_VERSION = { 'y' };

	// private static final byte[] ECHO_ON = { 'e', '1' };
	// private static final byte[] ECHO_OFF = { 'e', '0' };

	private static final String test = "test";
	private static final String home = "home";
	private static final String PATH = "home";

	// comm channel
	private SerialPort serialPort = null;
	private InputStream in;
	private OutputStream out;

	// will be discovered from the device
	protected String version = null;

	// input buffer
	private byte[] buffer = new byte[32];
	private int buffSize = 0;

	// track write times
	private long lastSent = System.currentTimeMillis();
	private long lastRead = System.currentTimeMillis();

	private LogManager log = new LogManager();

	private String portName = null;
	private boolean busy = true;

	int max = 0;

	/**  */
	public Port(String str) {
		portName = str;

		log.open(System.currentTimeMillis() + "_beam.txt");
	}

	/** open port, enable read and write, enable events */
	public boolean connect() {
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(Port.class.getName(), 2000);
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open streams
			out = serialPort.getOutputStream();
			in = serialPort.getInputStream();

			// register for serial events
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

		} catch (Exception e) {
			constants.error(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Buffer input on event and trigger parse on '>' charter
	 * 
	 * Note, all feedback must be in single xml tags like: <feedback 123>
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte[] input = new byte[32];
				int read = in.read(input);
				for (int j = 0; j < read; j++) {
					// print() or println() from arduino code
					if ((input[j] == '>') || (input[j] == 13) || (input[j] == 10)) {
						// do what ever is in buffer
						if (buffSize > 0)
							execute();
						// reset
						buffSize = 0;
						// track input from arduino
						lastRead = System.currentTimeMillis();
					} else if (input[j] == '<') {
						// start of message
						buffSize = 0;
					} else {
						// buffer until ready to parse
						buffer[buffSize++] = input[j];
					}
				}
			} catch (IOException e) {
				constants.error("event : " + e.getMessage(), this);
			}
		}
	}

	// act on feedback from arduino
	private void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];

		// System.out.println("in: " + response);
		// log.append(response);

		if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());

		} else if (response.startsWith(test)) {

			System.out.println("test: " + response);
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				busy = false;
				if (max > 0)
					System.out.println("max = " + max);
				max = 0;
			} else if (reply[1].equals("start")) {
				busy = true;
				max = 0;
			}

		} else if (response.startsWith(home)) {

			System.out.println("home: " + response);
			String[] reply = response.split(" ");
			if (reply[1].equals("done"))
				busy = false;
			else if (reply[1].equals("start"))
				busy = true;

		} else {

			String[] reply = response.split(" ");
			try {
				// int step = Integer.parseInt(reply[0]);
				int value = Integer.parseInt(reply[1]);

				if (value > 2)
					log.append(response);

				if (value > max)
					max = value;

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send a multi byte command to send the arduino
	 * 
	 * @param command
	 *            is a byte array of messages to send
	 */
	private/* synchronized */void sendCommand(final byte[] command) {

		try {

			// send
			out.write(command);

			// end of command
			out.write(13);

		} catch (Exception e) {
			constants.error(e.getMessage());
		}

		// track last write
		lastSent = System.currentTimeMillis();
	}

	/** @return the time since last write() operation */
	public long getWriteDelta() {
		return System.currentTimeMillis() - lastSent;
	}

	/** @return this device's firmware version */
	public/* synchronized */String getVersion() {
		if (version == null) {
			sendCommand(GET_VERSION);
			Utils.delay(1000);
		}
		return version;
	}

	/** @return the time since last read operation */
	public long getReadDelta() {
		return System.currentTimeMillis() - lastRead;
	}

	public/* synchronized */boolean test(int delay) {

		if (busy)
			return false;

		// System.out.println("sending test");
		sendCommand(new byte[] { TEST, (byte) delay });

		Utils.delay(300);

		busy = true;
		while (busy) {
			System.out.println("wait");
			Utils.delay(1000);
		}
		return true;
	}

	/** */
	public static void main(String[] args) {

		// if (args.length == 1) {

		// configure the framework with properties file
		constants.init(); // args[0]);

		// properties file must supply the device Name
		Port port = new Port("COM26");
		if (port.connect()) {

			Utils.delay(3000);

			System.out.println("starting test with version: " + port.getVersion());

			for (int i = 0; i < 250; i += 20) {

				System.out.println("starting test with: " + i);
				if (port.test(i)) {
					System.out.println("test done with: " + i + "\n");
				} else
					System.out.println("fault");
			}
		}
		constants.shutdown();
	}
}
