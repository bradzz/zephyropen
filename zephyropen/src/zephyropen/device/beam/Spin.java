package zephyropen.device.beam;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

public class Spin implements SerialPortEventListener {

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

	private String portName = null;
	private boolean busy = true;

	/**  */
	public Spin(String str) {
		portName = str;
	}

	/** open port, enable read and write, enable events */
	public boolean connect() {
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(Spin.class.getName(), 2000);
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

		if (response.startsWith("error")) {
			constants.shutdown("dead");
			
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());

		} else if (response.startsWith(test) || (response.startsWith(home))) {
			
			constants.info("spin execute: " + response);
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				busy = false;
			} else if (reply[1].equals("start")) {
				busy = true;
			}
		}
	}

	/**
	 * Send a multi byte command to send the arduino
	 * 
	 * @param command
	 *            is a byte array of messages to send
	 */
	private void sendCommand(final byte[] command) {

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
	public String getVersion() {
		if (version == null) {
			sendCommand(GET_VERSION);
			Utils.delay(300);
		}
		return version;
	}

	/** @return the time since last read operation */
	public long getReadDelta() {
		return System.currentTimeMillis() - lastRead;
	}

	/** @return true if the device is busy */
	public boolean isBusy(){
		return busy;
	}
	
	/**
	 * 
	 * @param mod
	 * @param filter
	 * @return
	 */
	public boolean test(int mod, int filter) {

		if (busy)
			return false;

	//	chart.getState().reset();

		sendCommand(new byte[] { TEST, (byte) mod, (byte) filter });

		Utils.delay(300);

		while (busy) {
			Utils.delay(1000);
		}
		return true;
	}

	/* */
	public boolean test() {

		if (busy)
			return false;

		//chart.getState().reset();

		sendCommand(new byte[] { TEST });
		busy = true;
		
		//Utils.delay(300);
 
		//while (busy) {
		//	Utils.delay(1000);	
		//	constants.info("wait: test()");
		//}
		
		constants.info("done: test()");
		
		return true;
	}

	/* */
	public boolean startTest() {
		
		//if (busy) return false;
		
		//new Thread(){
			//public void run(){
				
				sendCommand(new byte[] { TEST });
				busy = true;
				
				// wait to become busy 
				// Utils.delay(300);

				//while (busy) {
				//	Utils.delay(1000);	
				//	constants.info("wait: startTest()");
				//}
				
				constants.info("done: startTest()");
				
			//}}.start();
		
		return true;
	}

	
	
	public static void main(String[] args) {

		constants.init("brad");
		constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();

		Port spin = new Port(find.search("<id:beamscan>"));
		if (spin.connect()) {
			Utils.delay(2000);

			if (spin.test())
				System.out.println("test done");
			else
				System.out.println("fault");
			
		} else
			System.out.println("can't find spin");

		constants.shutdown();
	}
/**
	public static void poll(Spin port, int mod, int filter) {

		System.out.println("poll.starting test with version: " + port.getVersion());
		if (port.test(10, 10)) { // mod, filter)) {

			/// System.out.println("poll.state max: " + port.chart.getState().getMaxValueString());
			// new ScreenShot(port.chart, "mod: " + mod);
			Utils.delay(2000);

		} else
			System.out.println("poll.fault");
	} */
}
