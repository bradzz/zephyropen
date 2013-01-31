package zephyropen.device.beamscan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;


import zephyropen.api.ZephyrOpen;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/** @author brad.zdanivsky@gmal.com */
public class CommPort implements SerialPortEventListener {

	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final byte[] GET_VERSION = { 'y' };
	public static final byte[] ENABLE_MOTOR = { 'e' };
	public static final byte[] SINGLE = { 'q' };
	public static final byte GAIN = 'a';

	private static final int MAX_ATTEMPTS = 50;

	private Vector<Integer> points = new Vector<Integer>(1000);
	private SerialPort serialPort = null;
	private InputStream in;
	private OutputStream out;
	private String version = null;
	private byte[] buffer = new byte[32];
	private int buffSize = 0;
	private static ScanResults result = null;
	private static BeamGUI app = null;
	private static boolean waiting = false;
	
	
	/** constructor */
	public CommPort(BeamGUI gui) {
		
		app = gui;

		constants.info("looking for beam scaner... ");
		
		String portName = null;
		for(int i = 0 ; i < MAX_ATTEMPTS ; i++){
			portName = new Find().search("<id:beamscan>");
			if(portName != null){
				constants.info("found scanner: " + portName);
				constants.put("beamscan", portName);
				constants.updateConfigFile();	
				break;
			} 
		}
			
		if (portName == null) {
			constants.error("can't find beamscan", this);
			app.errorMessage("can't find beam scanner on any port");
		}
	}

	/** open port, enable read and write, enable events */
	public boolean connect() {
		
		String portName = constants.get("beamscan");
		if(portName==null){
			app.errorMessage("can't find beam scanner on any port");
			return false;
		}
		
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(CommPort.class.getName(), 2000);
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open streams
			out = serialPort.getOutputStream();
			in = serialPort.getInputStream();

			// register for serial events
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			// clear all 
			while(in.available()>0) {
				constants.error("avail: " + in.available(), this);
				in.skip(in.available());
			}
		} catch (Exception e) {
			constants.error("connection fail: " + e.getMessage(), this);
			return false;
		}
		
		zephyropen.util.Utils.delay(2000);
		
		getVersion();
		constants.info("beamscan port: " + portName);
		constants.info("beamscan version: " + version);
		
		// set gain on start up
		setGain(constants.getInteger("gainLevel"));
		zephyropen.util.Utils.delay(500);
		return true;
	}

	/** close the serial streams */
	public void close() {
		if (serialPort != null)
			serialPort.close();

		if (in != null){
			try {
				in.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}
		}
		
		if (out != null){
			try {
				out.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}
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

			constants.info("sending: " + command[0], this);
			
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
			zephyropen.util.Utils.delay(300);
		}
		return version;
	}

	/** */
	@SuppressWarnings("unchecked")
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];

		constants.info(response);
		
		///if (response.startsWith("fault")) {
		//	app.errorMessage("scan fault");
	//		waiting = false;
	//	} else if (response.startsWith("limit")) {
		//	app.errorMessage("limit switch failure");
		//	waiting = false;
		//} else
			
		if (response.startsWith("start")) {
			points.clear(); 
			waiting = true;
		} else if (response.startsWith("done")) { 	
			if(points.size()>0){
				String[] data = response.split(" ");
				constants.info("scan took: " + data[1] + " and got: " + points.size());
				result = new ScanResults((Vector<Integer>) points.clone(), Integer.parseInt(data[1]));
				waiting = false;
			}
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());
		} else { 
			int value = -1;
			try {
				value = Integer.parseInt(response);
			} catch (Exception e) {
				constants.error("not a value: " + response, this);
				waiting = false;
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

	/** 
	 * @return a data set after a blocking call. do one scan only
	 */
	public ScanResults sample() {	

		sendCommand(SINGLE);
	
		// wait
		waiting = true;
		for(int i = 0 ; ; i++){
			if(!waiting) break;
			else {
				if(i>20){
					constants.error("sample(): time out " + i, this);
					break;
				}
				constants.info("... waiting: " + i, this);
				zephyropen.util.Utils.delay(100);
			}
		}
		
		return result;
	}

	public void setGain(int gainLevel) {
		byte[] command = {GAIN, (byte)gainLevel};
		sendCommand(command);
	}
}
