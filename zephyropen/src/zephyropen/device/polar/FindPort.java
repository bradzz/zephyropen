package zephyropen.device.polar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

import gnu.io.*;

public class FindPort {

	/* serial port configuration parameters */
	public static final int BAUD_RATE = 9600;
	public static final int TIMEOUT = 2000;
	public static final int DATABITS = SerialPort.DATABITS_8;
	public static final int STOPBITS = SerialPort.STOPBITS_1;
	public static final int PARITY = SerialPort.PARITY_NONE;
	public static final int FLOWCONTROL = SerialPort.FLOWCONTROL_NONE;

	/* reference to the underlying serial port */
	private SerialPort serialPort = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;

	/* list of all free ports */
	private Vector<String> ports = new Vector<String>();

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

	/* constructor makes a list of available ports */
	public FindPort() {
		constants.init();
		getAvailableSerialPorts();
	}

	/** */
	public void getAvailableSerialPorts() {

		@SuppressWarnings("rawtypes")
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
			if (com.getPortType() == CommPortIdentifier.PORT_SERIAL)
				ports.add(com.getName());
		}
	}

	/**
	 * connects on start up, return true is currently connected
	 * 
	 * @throws IOException
	 */
	private boolean connect(String address) throws IOException {

		/// constants.info("connecting to: " + address);

		/* construct the serial port */
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(address).open("FindPort", TIMEOUT);

		} catch (PortInUseException e) {
			try {
				constants.info("port in use : " + e.getMessage() + " " + CommPortIdentifier.getPortIdentifier(address).getCurrentOwner());
			} catch (NoSuchPortException e1) {
				e1.printStackTrace();
			}
			close();
			return false;
		} catch (NoSuchPortException e) {
			constants.info("port error : " + e.getMessage());
			close();
			return false;
		}

		/* configure the serial port */
		try {
			serialPort.setSerialPortParams(BAUD_RATE, DATABITS, STOPBITS, PARITY);
		} catch (UnsupportedCommOperationException e) {
			constants.info("port error : " + e.getMessage());
			close();
			return false;
		}
		try {
			serialPort.setFlowControlMode(FLOWCONTROL);
		} catch (UnsupportedCommOperationException e) {
			constants.info("port error : " + e.getMessage());
			close();
			return false;
		}

		/* extract the input and output streams from the serial port */
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			constants.info("port error : " + e.getMessage());
			close();
			return false;
		}

		outputStream = serialPort.getOutputStream();

		/*
		 * } catch (PortInUseException e) { constants.info("port busy : " +
		 * address); close(); return false; }
		 */

		if (inputStream == null)
			return false;

		if (outputStream == null)
			return false;

		// constants.info("connected to: " + address);
		return true;
	}

	/** Close the serial port streams */
	public void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			constants.error("close():" + e.getMessage());
		}

		try {
			if (outputStream != null)
				outputStream.close();
		} catch (Exception e) {
			constants.error("close():" + e.getMessage());
		}

		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * Loop through all available serial ports and ask for product id's
	 * 
	 * @param target
	 *            is the device we are looking for on this host's serial ports
	 *            (ie: oculus|lights)
	 * @return the COMXX value of the given device
	 * @throws Exception
	 */
	public String search() throws Exception {
		for (int i = ports.size() - 1; i >= 0; i--) {
			if (connect(ports.get(i))) {
				Thread.sleep(TIMEOUT);
				if (isPolar()) {
					close();
					return ports.get(i);
				}
			}
			close();
		}

		// error state
		return null;
	}

	// send a message the Polar chip will respond to 
	boolean isPolar() {

		byte[] buffer = new byte[64];
		int bytesRead = 0;
		
		// send command
		byte[] bytes = new byte[3];
		bytes[0] = (char) 'G';
		bytes[1] = (byte) '2';
		bytes[2] = (byte) 13;

		try {
			outputStream.write(bytes);
		} catch (Exception e) {
			constants.error(e.getMessage(), this);
			close();
			return false;
		}

		// wait for whole massage
		Utils.delay(500);

		try {

			// read into buffer
			bytesRead = inputStream.read(buffer);
			
			///String data = "";
			///for(int i = 0 ; i < bytesRead ; i++)
				//data += buffer[i] + ", ";
			// constants.info("read bytes: " + bytesRead);
			// constants.info("data: " + data);
			
			if( bytesRead >= 10 )
				if( buffer[bytesRead-1] == 13 ){
					close();
					return true;
				}
			
		} catch (Exception e) {
			constants.info(e.getMessage(), this);
		}

		close();
		return false;
	}

	/**
	 * test driver
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		FindPort port = new FindPort();

		String com = port.search();
		if (com != null) 
			constants.info("found polar on: " + com);
		else
			constants.info("Polar NOT found");

		// port.close();

		constants.info("scan took: " + (System.currentTimeMillis() - start) + " ms");

	}
}
