package zephyropen.swing.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import zephyropen.api.ZephyrOpen;

public class Loader extends Thread implements Runnable {

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

	private String path = null;
	private String code = null;
	private String arg = null;

	Loader(String p, String c, String a) {

		path = p;
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
		
		constants.info("<!-- launching: " + code, this);
		
		startProc();
		
		constants.info("<!-- launched: " + code, this);
	}

	public void startProc() {

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
