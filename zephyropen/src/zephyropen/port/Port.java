package zephyropen.port;

import java.io.IOException;

public interface Port {
	
	/** connect to the device */
	public boolean connect();
		
	public String getAddress();
	
	/** Close the serial port profile's streams */
	public void close();
	
	/**
	 * Wrapper the InputStream method
	 * 
	 * @return the number of bytes that can be read from the device
	 * @throws IOException
	 */
	public int available() throws IOException;

	/**
	 * Wrapper the InputStream method
	 * 
	 * @param data to read from the device
	 * @return the data read from the device
	 * @throws IOException is thrown if this write operation fails
	 */
	public int read(byte[] data) throws IOException;

	/**
	 * Wrapper the outputStream method
	 * 
	 * @param data to write to the device
	 * @throws IOException is thrown if this write operation fails
	 */
	public void writeBytes(byte[] data)throws IOException;

	public boolean isOpen();
	
}
