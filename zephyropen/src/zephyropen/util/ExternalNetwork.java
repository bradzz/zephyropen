package zephyropen.util;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import zephyropen.api.ZephyrOpen;

/**
 * <p> A Simple look up of our external address. Only way to find it is to reach out of our network. 
 * 
 * 
 * {@code example HTML reply:
 * 
 * http://checkip.dyndns.org/
 * returned body is as follows:
 * <html><head><title>Current IP Check</title></head><body>Current IP
 * Address: 204.174.35.247</body></html>
 * }
 * 
 * <p>
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 *
 */
public class ExternalNetwork {
	
	final private static ZephyrOpen constants = ZephyrOpen.getReference();

	/**
	 * @return this device's external IP address is via http lookup, or null if fails 
	 */ 
	public static String getExternalIPAddress(){

		String address = null;
		URL url = null;

		try {
			
			url = new URL("http://checkip.dyndns.org/");

			// read in file from the encoded url
			URLConnection connection = (URLConnection) url.openConnection();
			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

			int i;
			while ((i = in.read()) != -1) {
				address = address + (char) i;
			}
			in.close();

			// parse html file
			address = address.substring(address.indexOf(": ") + 2);
			address = address.substring(0, address.indexOf("</body>"));
			
		} catch (Exception e) {
			return null;
		}
		
		// all good 
		return address;
	}
	
	
	
	
	//TODO: USE THIS IN "DEVELOPER MODE" 
	// create a new thread to keep polling for any changes 
	//
	class update extends Thread {
		
		String old = null;
		String now = null;
		
		update(){
			this.setDaemon(true);
			this.start();
		}

		public void run(){
			
			Utils.delay(ZephyrOpen.FIVE_MINUTES);
						
			old = (String) constants.get(ZephyrOpen.externalAddress);
			now = getExternalIPAddress();
			
			if( old != null )
			if( ! old.equals(now) ){
				
				System.out.println(this.getClass().getName() + "updated Extenal IP" ); 
			
				// do look up, and keep checking it 
				// props.put("externalAddress", now);
			}
		}		
	}
}
