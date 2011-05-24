package zephyropen.port.bluetooth;

import java.io.IOException;
import java.io.InputStream;

import zephyropen.api.ZephyrOpen;
import zephyropen.port.Port;
import zephyropen.util.Utils;

public class SerialUtils {
	
	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	
	/**
	 * Wrapper the InputStream method, read until connection closes 
	 * 
	 * @throws IOException
	 *             is thrown if this write operation fails
	 */
	public static String readString(InputStream inputStream) throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		int result = 0;
		while (bytesRead < buffer.length) {

			result = inputStream.read(buffer, bytesRead, buffer.length - bytesRead);

			if (bytesRead == -1) break;
			else bytesRead = result;
		}
		return new String(buffer, 0, bytesRead, "US-ASCII");
	}

	/**
	 * Wrapper the InputStream method, read until connection closes 
	 * 
	 * @throws IOException
	 *             is thrown if this write operation fails
	 */
	public static String readBuffer(InputStream inputStream) throws IOException {

		String reply = new String();
		byte[] buffer = new byte[256];

		int count = 0;
		while (count > -1) {

			count = inputStream.read(buffer);
			if (count != -1) {
				reply += toString(buffer, count);
			}
		}
		return reply;
	}

	/**
	 * convert bytes to string 
	 * 
	 * @param bytes
	 * @param count
	 * @return
	 */
	public static String toString(byte[] bytes, int count){
		
		String s = new String();
		for(int i = 0 ; i < count ; i++ ){
			
			Character ch = new Character((char) bytes[i]);
			if( ch != null ){
				if( Character.isLetterOrDigit(ch) || Character.isWhitespace(ch))
					s+=ch;
			}	
		}
		return s;
	}

	/**
	 * read only ASCII charaters 
	 * 
	 * @param inputStream
	 * @return
	 */
	public static String readASCII(InputStream inputStream){
		try {
			return readString(inputStream);
		} catch (IOException e) {
			return "error";
		}
	}
	
	

	public static byte[] getAvail(Port spp, byte[] buffer, int bufferSize) throws IOException {
		
		//
		// read bytes differently based on OS 
		//
		if(constants.get(ZephyrOpen.os).startsWith("Windows")){
			return getAvailWindows(spp, buffer, bufferSize);
		}else{
			return getAvailUnix(spp, buffer, bufferSize);
		}
	}
		
	/**
	 * Get the bytes waiting in the serial port stream
	 * @param buffer, int BUFFER_SIZE  
	 * 
	 * @return the array of bytes read from the SPP
	 * @throws IOException 
	 */
	public static byte[] getAvailUnix(Port spp, byte[] buffer, int BUFFER_SIZE ) throws IOException {
	
		constants.info("getAvailUnix");
		
		//if(!spp.isOpen()){
		//	spp.close();
		//	return null;
		//}
		
		int offset = 0;
		
		// try {
			
			//if (spp.available() < 1) {
				// No bytes available
				//return null;
			//}

			// Read port from buffer until max packet size is reached or no data is incoming
			while ( offset < BUFFER_SIZE ) {

				byte[] sppInput = new byte [ BUFFER_SIZE ];
				
				// Read available bytes from buffer
				int bytes = spp.read(sppInput);
				
				// add available bytes to end of buffer
				Utils.add(buffer, sppInput, offset, bytes);	
				
				// Lets move the buffer end byte
				offset = offset + bytes;
				
			}
		
	//} catch (Exception e) {
		//	constants.error("SerialUtils.getAvail() : " + e);
			
			// spp.close();
			//return null;
		//}
		
		// System.out.println("offset: "+offset);	
		if ( offset < BUFFER_SIZE) {
			// This is not a valid Zephyr packet
			constants.error("AbstractDeviceServer.getAvail(): Packet size too small: " + offset+".");
			return null;
		}

		return buffer;
	} 
	
	/**
	 * Get the bytes waiting in the serial port stream
	 * 
	 * @return the array of bytes read from the SPP
	*/
	public static byte[] getAvailWindows(Port spp, byte[] buffer, int bufferSize) {
		
		if(!spp.isOpen()){
			spp.close();
			return null;
		}
		
		try {

			/** don't try to parse until we get whole packet */
			if (spp.available() <= 5)
				return null;

		} catch (Exception e) {
			constants.error("getAvailWindows(), spp.avail() : " + e);
			spp.close();
			return null;
		}

		byte[] avail = null;
		int bytes = 0;

		try {

			bytes = spp.read(buffer);

		} catch (Exception e) {
			constants.error("getAvailWindows(), spp.read() : " + e);
			spp.close();
			return null;
		}

		/** read error */
		if (bytes < 1) {
			constants.error("getAvailWindows() : bytes < 1");
			return null;
		}

		/** create new byte array of proper size */
		avail = new byte[bytes];
		Utils.copy(avail, buffer, 0, bytes);
		return avail;
	} 
}
