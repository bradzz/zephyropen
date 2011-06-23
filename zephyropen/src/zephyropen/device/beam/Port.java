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

public abstract class Port implements SerialPortEventListener {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();

	// ready to be sent
	public static final byte TEST = 't';
	public static final byte[] HOME = { 'h' };
	public static final byte[] GET_VERSION = { 'y' };

	public static final String test = "test";
	public static final String home = "home";

	// comm channel
	private SerialPort serialPort = null;
	private InputStream in;
	private OutputStream out;

	// will be discovered from the device
	protected String version = null;

	// input buffer
	protected byte[] buffer = new byte[32];
	protected int buffSize = 0;

	// track write times
	protected long lastSent = System.currentTimeMillis();
	protected long lastRead = System.currentTimeMillis();

	protected String portName = null;
	protected boolean busy = true;
	protected long runTime = 0;
	
	
	/**  */
	public Port(String str){
		portName = str;
	}
	
	/**  */
	public void close(){
		if(serialPort != null) serialPort.close();
		
		if(in != null)
			try {
				in.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}
		
		if(out != null)
			try {
				out.close();
			} catch (IOException e) {
				constants.shutdown(e);
			}
	}
	
	/**  */
	public long getRuntime() {
		return runTime;
	}
	
	/**  */
	public String getPortName() {
		return portName;
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

	/** */ 
	public boolean test(boolean blocking) {
		
		if (busy) {
			constants.info("busy device: " + this.getClass().getName());
			return false;
		}
		
		sendCommand(new byte[] { TEST });
	    
		if(blocking){
			busy = true;
			while (busy) {
				constants.info("waiting device: " + this.getClass().getName());
				Utils.delay(1000);	
			}
		} else {
			Utils.delay(300);
		}
		
		return true;
	}
	
	/** 
	public boolean test(boolean blocking, int arg) {
		
		if (busy) {
			constants.info("busy device: " + this.getClass().getName());
			return false;
		}
		
		sendCommand(new byte[] { TEST, (byte) arg });
	
		if(blocking){	
			busy = true;
			while (busy) {
				constants.info("waiting device: " + this.getClass().getName());
				Utils.delay(1000);	
			}
		} else {
			Utils.delay(300);
		}
		
		return true;
	}*/ 
	
	// act on feedback from arduino
	public abstract void execute();

}
