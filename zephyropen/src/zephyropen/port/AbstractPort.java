package zephyropen.port;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;

public abstract class AbstractPort implements Port {

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();

	/** The COM port or bluetooth address */
	protected String address = null;
	
	/** serial port via COM or BT */ 
	protected Port port = null;
	
	/** feedback command */
	protected Command command = null;

	/** in and out */
	protected InputStream inputStream = null;
	protected OutputStream outputStream = null;
	
	/** track message inputs times */
	protected long start = System.currentTimeMillis();
	protected long last = System.currentTimeMillis();
	  
    public boolean isOpen(){
    	return true;
    }

	/** @return the address of this device */
	public String getAddress(){
		return address;
	}
	
	/** @return the amount of time passed since last message */
	public long getDelta() {
		return (System.currentTimeMillis() - last);
	}

	/** Return the time since the first message from the device */
	public long getElapsedTime() {
		return (System.currentTimeMillis() - start);
	}
	
	public Command getCommand(){
		return command;
	}

	/**
	 * Wrapper the InputStream method
	 * 
	 * @return the number of bytes that can be read from the device
	 * @throws IOException
	 */
	public int available() throws IOException {
		return inputStream.available();
	}

	/**
	 * Wrapper the InputStream method
	 * 
	 * @param data
	 *            to read from the device
	 * @return the data read from the device
	 * @throws IOException
	 *             is thrown if this write operation fails
	 */
	public int read(byte[] data) throws IOException {
		return inputStream.read(data);
	}

	/**
	 * Wrapper the outputStream method
	 * 
	 * @param data
	 *            to write to the device
	 * @throws IOException
	 *             is thrown if this write operation fails
	 */
	public void writeBytes(byte[] data) throws IOException {
		outputStream.write(data);
	}
	
	public boolean connect() {
		if (port.connect()){
			last = System.currentTimeMillis();
			return true;
		}
		
		return false;
	}
	
	/** Close the serial port profile's streams */
	public void close() {

		constants.info("closing port : " + address, this);
		
		try {

			if (port != null)
				port.close();

		} catch (Exception e) {
			constants.error("close() :" + e.getMessage(), this);
		}
	/*
		try {

			if (inputStream != null)
				inputStream.close();

		} catch (IOException e) {
			constants.error("close() :" + e.getMessage(), this);
		}

		try {

			if (outputStream != null)
				outputStream.close();

		} catch (IOException e) {
			constants.error("close() :" + e.getMessage(), this);
		}
		*/
	
	}
}
