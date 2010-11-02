package zephyropen.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Process;

/**
 * Start the Launcher and SWING GUI
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class WindowsStartUp {

	public WindowsStartUp(String c, String j) {
		startProcessWindows(c, j);
	}

	/**
	 * Launch a new process on MAC, Linux
	 * 
	 * @param classpath
	 *            to use in cmd line
	 * @param javaClass
	 *            to run within this package
	 */
	public void startProcessWindows(String classpath, String javaClass) {

		final String[] args = new String[] { "java", "-classpath",
				classpath + " " + javaClass };

		/** new search thread */
		Thread runner = new Thread() {
			public void run() {

				try {

					System.out.println("Launch : " + args[2]);

					/** launch and don't wait for reply */
					Process proc = Runtime.getRuntime().exec(args);

					BufferedReader procReader = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));

					String line;
					while ((line = procReader.readLine()) != null)
						System.out.println("proc : " + line);

					if (proc.waitFor() != 0)
						System.err.println("proc exit value = "
								+ proc.exitValue());

				} catch (Exception e) {
					System.out.println("fatal runtime.exec() error: " + e.getMessage());
					System.exit(-1);
				}
			}
		};
		runner.start();

		/**
		 * blocking on search results try {
		 * 
		 * while(run) runner.join();
		 * 
		 * } catch (InterruptedException e) {
		 * System.out.println("fatal runtime.exec() error: " + e.getMessage());
		 * System.exit(-1); }
		 */

	}
	
	/** Requires no configuration, just start these two proc's */
	public static void main(String[] args) {

		new WindowsStartUp("./bin;/lib/bluecove.jar\"",
			"zephyropen.device.DeviceServer brad.properties");
		
		// new WindowsStartUp("./bin;/lib/bluecove.jar\"",
			// "zephyropen.viewer.DeviceViewer brad.properties");


	}
}
