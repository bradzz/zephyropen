package zephyropen.util.ftp;

import java.io.FileInputStream;
import java.util.Properties;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;
import zephyropen.util.google.GoogleChart;

/**
 * Manage FTP configuration and connections. Start new threads for each FTP
 * transaction
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class FTPManager {

	private final ZephyrOpen constants = ZephyrOpen.getReference();
	
	public final static String ftpEnabled = "ftpEnabled";
	
	public final static String host = "host";

	public final static String ftpPort = "ftpPort";
	
	final public static String ftpUser = "ftpUser";

	private final String FTP_PROPERTIES = "ftp.properties";

	private static FTPManager singleton = null;

	private int port = 21;

	private int xSize = 450;

	private int ySize = 130;

	private String folderName = null;

	private String ftpURL = null;

	private String userName = null;

	private String password = null;

	private boolean configured = false;

	private LogManager log = null;

	/** @return a reference to this singleton class */
	public static FTPManager getReference() {
		if (singleton == null) {
			singleton = new FTPManager();
		}
		return singleton;
	}

	/** try to configure FTP parameters */
	private FTPManager() {

		if (parseFile(constants.get(ZephyrOpen.userHome))) {

			configured = true;

		} else {

			// try root if nothing else
			configured = parseFile(constants.get(ZephyrOpen.root));

		}

		if (configured) {

			// set global flag
			constants.put(ftpEnabled, "true");

			// toggle from props file
			log = new LogManager();
			log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName) + "_ftp.log");

		} else {
			constants.put(ftpEnabled, "false");
		}
	}

	/** Create an ftp thread to send this google report to the hosted server */
	public void upload(GoogleChart report) {

		// constants.info("ftp thread called: " + report.getTitle(), this);

		if (!configured) {
			constants.error("ftp not configured", this);
			return;
		}

		if (!constants.getBoolean(ftpEnabled)) {
			constants.error("ftp not enabled", this);
			return;
		}

		if (!constants.getBoolean(ZephyrOpen.filelock)) {
			// constants.error("do not have the lock file", this);
			return;
		}

		String data = report.getURLString(xSize, ySize);
		if (data != null) {

			// example: heart.php
			new ftpThread(data, report.getTitle() + ".php").start();

			if (log != null)
				log.append(report.getTitle() + ", " + data);
		}
	}

	/** FTP given file to host web server */
	private boolean ftpFile(String data, String fileName) {

		FTP ftp = new FTP();

		try {

			ftp.connect(ftpURL, port, userName, password);

		} catch (Exception e) {
			constants.error("FTP can not connect to: " + ftpURL + " user: " + userName, this);
			return false;
		}

		try {

			if (!ftp.ascii()) {
				constants.error("FTP can not switch to ASCII mode", this);
				return false;
			}

			if (!ftp.cwd(folderName)) {
				constants.error("FTP can not CD to: " + folderName, this);
				return false;
			}

			if (ftp.storString(fileName, data))
				return true;

		} catch (Exception e) {
			constants.error("FTP upload exception : " + fileName, this);
			return false;
		}

		// error state
		return false;
	}

	/** try reading in defaults, add to constants */
	private boolean parseFile(String path) {

		path = path.trim() + ZephyrOpen.fs + FTP_PROPERTIES;

		Properties props = new Properties();

		try {

			// set up new properties object from file
			FileInputStream propFile = new FileInputStream(path);
			props.load(propFile);
			propFile.close();

			ftpURL = props.getProperty(host);
			if (ftpURL == null)
				return false;

			userName = props.getProperty(ftpUser);
			if (userName == null)
				return false;

			folderName = props.getProperty(ZephyrOpen.folder);
			if (folderName == null)
				return false;

			password = props.getProperty(ZephyrOpen.password);
			if (password == null)
				return false;

		} catch (Exception e) {
			constants.error("can't parse FTP config file: " + path, this);
			return false;
		}

		int x = getInt(props.getProperty("xSize"));
		if (x > ZephyrOpen.ERROR)
			xSize = x;

		int y = getInt(props.getProperty("ySize"));
		if (y > ZephyrOpen.ERROR)
			ySize = y;

		// override default
		int ftp = getInt(props.getProperty("ftpPort"));
		if (ftp > ZephyrOpen.ERROR)
			ySize = y;

		// all set
		return true;
	}

	private int getInt(String value) {

		int ans = ZephyrOpen.ERROR;

		try {
			ans = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return ZephyrOpen.ERROR;
		}

		return ans;
	}

	/** is the ftp configuration valid */
	public boolean ftpConfigured() {
		return configured;
	}

	/** do the work */
	private class ftpThread extends Thread {

		private String d = null;
		private String f = null;

		public ftpThread(String value, String name) {
			d = value;
			f = name;
		}

		@Override
		public void run() {
			if (!ftpFile(d, f))
				constants.error("ftp failed: " + f);
		}
	}
}