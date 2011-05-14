package zephyropen.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import zephyropen.api.ZephyrOpen;

public class Loader {
	//extends Thread implements Runnable {

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** need to launch new proc */
	static Runtime runtime = Runtime.getRuntime();
	
	private String path = constants.get(ZephyrOpen.path);
	private String code = null;
	private String arg = null;

	public Loader(String c, String a) {

		code = c;
		arg = a;

		if (path == null || code == null || arg == null) {
			constants.error("null params on exec()", this);
			return;
		}
		
		new Thread(
			new Runnable() {		
				@Override
				public void run() {
					
					if (constants.get(ZephyrOpen.os).startsWith("Mac")) {
						macProc();
					} else {
						winProc();
					}
				}	
			}).start();
	}

	public void macProc() {
		
		/** java started as a shell script, note 32 bit mode used on mac */
		String[] args = new String[] {"/bin/sh", "-c", "java -d32 -classpath " + path + " " + code + " " + arg, "&"};

		try {

			// for (int i = 0; i < args.length; i++)
				//constants.info("Launch args [" + i + "] " + args[i]);

			/** launch and don't wait for reply */
			// Process proc = 
			
			runtime.exec(args);
				
			// BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			// String line = null;
			// while ((line = procReader.readLine()) != null)
				// constants.info("proc : " + line);

		} catch (Exception e) {
			constants.error("fatal runtime.exec() error: " + e.getMessage());
			constants.shutdown(e);
		}
	}
	
	/**
	 * Start a windows proc for the given class 
	 */
	public void winProc() {

		/** javaw for no screen */
		String[] args = new String[] { "javaw", "-classpath", path, code, arg };

		try {

			for (int i = 0; i < args.length; i++)
				constants.info("Launch [" + i + "] " + args[i]);

			/** launch and don't wait for reply */
			Process proc = runtime.exec(args);

			BufferedReader procReader = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));

			String line = null;
			while ((line = procReader.readLine()) != null)
				constants.info("proc : " + line);
			
			constants.info("exit winProc()", this);

		} catch (Exception e) {
			constants.error("fatal runtime.exec() error: " + e.getMessage());
			constants.shutdown(e);
		}
	}
}
