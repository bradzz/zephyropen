package zephyropen.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AddressDiscovery {

	private static Runtime runtime = Runtime.getRuntime();
	
 	public static void main(String[] args) {	
		String ip = zephyropen.util.ExternalNetwork.getExternalIPAddress();
		System.out.println(System.getProperty("java.class.path"));
		System.out.println(new java.util.Date().toString() + " ip = " + ip);
		
		osxARP();
		osxPing("localhost");
		
		// DOTO: create arp and ping methods for windows 
		// DOTO: then loop through all known ip address and ping them. 
		
	}
 	
 	public static void osxPing(final String ip) {
		
		String[] args = new String[] {"/bin/sh"};

		try {
			
			/** launch and wait for reply */
			Process proc = runtime.exec(args);
			BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = null;
			while ((line = procReader.readLine()) != null)
				System.out.println("proc : " + line);

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
	
	public static String[][] osxARP() {
		
		String[] args = new String[] {"/bin/sh", "-c", "arp -a"};

		try {

			for (int i = 0; i < args.length; i++)
				System.out.println("Launch args [" + i + "] " + args[i]);

			/** launch and wait for reply */
			Process proc = runtime.exec(args);
			BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = null;
			while ((line = procReader.readLine()) != null)
				System.out.println("proc : " + line);

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}

		// TODO: parse into 2d array of ip and mac address 
		return null;
	}
}
