package zephyropen.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import zephyropen.api.ZephyrOpen;

public class Loader extends Thread implements Runnable {

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

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

		// this.setDaemon(true);
		this.start();
	}

	public void run() {
		
		constants.info("launching: " + code, this);
		
		if (constants.get(ZephyrOpen.os).startsWith("Mac")) {

			macProc();

		} else {
			
			winProc();
			
		}
		
		constants.info("exit: " + code, this);
	}

	public void macProc() {
		
		constants.info("launching mac: ", this);

		/** need to launch new proc */
		Runtime runtime = Runtime.getRuntime();

		/** javaw for no screen */
		String[] args = new String[] {"/bin/sh", "-c", "java -classpath " + path + " " + code + " " + arg, "&"};

		try {

			for (int i = 0; i < args.length; i++)
				constants.info("Launch args [" + i + "] " + args[i]);

			/** launch and don't wait for reply */
			Process proc = runtime.exec(args);
				
			BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = null;
			while ((line = procReader.readLine()) != null)
				constants.info("proc : " + line);

		} catch (Exception e) {
			constants.error("fatal runtime.exec() error: " + e.getMessage());
			constants.shutdown(e);
		}
	}
	
	
	/**
	 * Start a windows proc for the given class 
	 */
	public void winProc() {

		/** need to launch new proc */
		Runtime runtime = Runtime.getRuntime();

		/** javaw for no screen */
		String[] args = new String[] { "javaw", "-classpath", path, code, arg };

		try {

			for (int i = 0; i < args.length; i++)
				constants.info("Launch [" + i + "] " + args[i]);

			/** launch and don't wait for reply */
			Process proc = runtime.exec(args);

			// BufferedReader procReader = new BufferedReader(
					
			new InputStreamReader(proc.getInputStream());

			// String line = null;
			// while ((line = procReader.readLine()) != null)
			//	constants.info("proc : " + line);

		} catch (Exception e) {
			constants.error("fatal runtime.exec() error: " + e.getMessage());
			constants.shutdown(e);
		}
	}
}
