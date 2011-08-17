package zephyropen.device.beamscan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;
import zephyropen.util.google.ScreenShot;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * @author brad.zdanivsky@gmal.com
 */
public class CommPort implements SerialPortEventListener {

	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final byte[] GET_VERSION = { 'y' };

	private Vector<Integer> points = new Vector<Integer>(1000);
	private String portName = null;
	private SerialPort serialPort = null;
	private InputStream in;
	private OutputStream out;
	private String version = null;
	private byte[] buffer = new byte[32];
	private int buffSize = 0;

	/** constructor */
	public CommPort() {

		System.out.println(constants.toString());
		portName = constants.get("beam");

		// need to go look?
		if (portName == null) {
			Find find = new Find();
			portName = find.search("<id:beamscan>");
			System.out.println("found: " + portName);
		}

		// not found
		if (portName == null)
			constants.shutdown("can't find beamscan");

		// re-fresh the file
		// found.put(beamscan, port.);
		// writeProps();

	}

	/**@return the name of the port the device is on */
	public String getPortName() {
		return portName;
	}

	@SuppressWarnings("unchecked")
	public Vector<Integer> getPoints() {
		return (Vector<Integer>) points.clone();
	}

	/** open port, enable read and write, enable events */
	public boolean connect() {
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(CommPort.class.getName(), 2000);
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

		Utils.delay(2000);
		getVersion();
		constants.info("beamscan port: " + portName);
		constants.info("beamscan version: " + version);
		Utils.delay(2000);

		return true;
	}

	/** close the seriql streams */
	public void close() {
		if (serialPort != null)
			serialPort.close();

		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}

		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}
	}

	/**
	 * Send a multi byte command to send the arduino
	 * 
	 * @param command
	 *            is a byte array of messages to send
	 */
	protected void sendCommand(final byte[] command) {
		try {

			// send
			out.write(command);

			// end of command
			out.write(13);

		} catch (Exception e) {
			constants.error(e.getMessage());
		}
	}

	/** @return this device's firmware version */
	public String getVersion() {
		if (version == null) {
			sendCommand(GET_VERSION);
			Utils.delay(300);
		}
		return version;
	}

	/**  */
	public static int[] getSlice(final int target, final Vector<Integer> points) {
		int[] values = { 0, 0, 0, 0 };
		try {

			values[0] = getDataInc(target, 0, points);
			// constants.info("x1: " + values[0] + " value: " +
			// reader.points.get(values[0]));

			values[1] = getDataDec(target, values[0], points);
			// constants.info("x2: " + values[1] + " value: " +
			// reader.points.get(values[1]));

			values[2] = getDataInc(target, points.size() / 2, points);
			// constants.info("y1: " + values[2] + " value: " +
			// reader.points.get(values[2]));

			values[3] = getDataDec(target, values[2], points);
			// constants.info("y2: " + values[3] + " value: " +
			// reader.points.get(values[3]));

		} catch (Exception e) {
			constants.error("can't take slice of beam");
			return null;
		}

		return values;
	}

	/** */
	private static int getDataInc(final int target, final int start, final Vector<Integer> points) {

		int j = start;

		// constants.info("start : " + j + " target : " + target);

		for (; j < points.size(); j++) {
			if (points.get(j) > target) {
				// constants.info( "inc_index: " + j + " value: " +
				// reader.points.get(j));
				break;
			}
		}

		return j;
	}

	/** */
	private static int getDataDec(final int target, final int start, final Vector<Integer> points) {

		int j = start;
		// constants.info("start : " + j + " target : " + target);

		for (; j < points.size(); j++) {
			if (points.get(j) < target) {
				// constants.info( "dec_index: " + j + " value: " +
				// reader.points.get(j));
				break;
			}
		}

		return j;
	}

	/** */
	public static int getMaxIndex(final int start, final int stop, final Vector<Integer> points) {

		int j = start;
		int max = 0;
		int index = 0;

		// constants.info("getMaxIndex start: " + start);
		// constants.info("getMaxIndex stop: " + stop);

		for (; j < stop; j++) {
			if (points.get(j) > max) {
				max = points.get(j);
				index = j;
			}
		}

		return index;
	}

	public static int getMaxIndexX(final Vector<Integer> points) {
		return getMaxIndex(0, points.size() / 2, points);
	}

	public static int getMaxIndexY(final Vector<Integer> points) {
		return getMaxIndex(points.size() / 2, points.size(), points);
	}

	/** */
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];

		// constants.info("_" + response);

		if (response.startsWith("start")) {

			points = new Vector<Integer>(1000);

		} else if (response.startsWith("done")) {

			System.out.println("scan took: " + response);
			
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());
		} else {
			int value = -1;
			try {
				value = Integer.parseInt(response);
			} catch (Exception e) {
				constants.error(e.getMessage());
			}
			if (value != -1) {
				points.add(value);
			}
		}
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
					if ((input[j] == '>') || (input[j] == 13) || (input[j] == 10)) {

						// do what ever is in buffer
						if (buffSize > 0)
							execute();

						// reset
						buffSize = 0;

					} else if (input[j] == '<') {
						buffSize = 0;
					} else {
						buffer[buffSize++] = input[j];
					}
				}
			} catch (IOException e) {
				constants.error("event : " + e.getMessage(), this);
			}
		}
	}

	public void sample() {
		sendCommand(new byte[] { 'e' });
		sendCommand(new byte[] { 's' });
		Utils.delay(2000);
	}

	/** test driver */
	public static void main(String[] args) {

		constants.init("brad");
		CommPort scan = new CommPort();
		if (scan.connect()) {

			scan.sample();
			Utils.delay(3000);
			final Vector<Integer> snapshot = scan.getPoints();
			
			new Thread( new Runnable() {
				public void run() {

					// final long started = start;

					LogManager log = new LogManager();
					log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + System.currentTimeMillis() + ".log");
					log.append(new java.util.Date().toString());
					log.append("size : " + snapshot.size());

					GoogleChart chart = new GoogleLineGraph("_beam", "ma", com.googlecode.charts4j.Color.BLUEVIOLET);
					for (int j = 0; j < snapshot.size(); j++) {
						if (j % 5 == 0)
							chart.add(String.valueOf(snapshot.get(j)));
						log.append(j + " " + String.valueOf(snapshot.get(j)));
					}
					log.close();
					new ScreenShot(chart, " points = " + chart.getState().size());

				}}).start();

			System.out.println("...done");
			scan.close();

		} else {
			System.out.println("can't connect");
		}

		Utils.delay(5000);
		constants.shutdown();
	}

}
