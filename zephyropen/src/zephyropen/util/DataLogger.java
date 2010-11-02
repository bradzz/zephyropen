package zephyropen.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import zephyropen.api.ZephyrOpen;

/**
 * Manage a log file on local storage and lock it for this thread's use only 
 * 
 * Created: 2002.05.10
 *
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class DataLogger extends Thread implements Runnable {

	public static ZephyrOpen constants = ZephyrOpen.getReference();

	//private final String  fs = System.getProperty("file.separator");
	
	public static final String CRLF = "\r\n";

	//private String folder = null;
	private String fileName = null;
	private RandomAccessFile logfile = null;
	private FileChannel fileChannel = null;
	private FileLock lock = null;

	public DataLogger() {

		/** start logging this data */
		if (!constants.getBoolean(ZephyrOpen.loggingEnabled)) {
			constants.info("logging is not enabled", this);
			return;
		}
		
		// this.setDaemon(true);
		this.start();
	}

	/** start() call back */
	public void run() {

		try {
			
			fileName = constants.get(ZephyrOpen.userLog) + System.getProperty("file.separator")
			+ constants.get(ZephyrOpen.deviceName) + ".xml";

			try {
				logfile = new RandomAccessFile(fileName, "rw");
			} catch (FileNotFoundException e) {
				constants.error("can't open file: " + fileName);
				return;
			}

			fileChannel = logfile.getChannel();
			if (fileChannel == null) {
				constants.error("can't get a file channel for: " + fileName, this);
				return;
			}
			
			constants.put(ZephyrOpen.filelock, "false");
			constants.info("waiting on lock file", this);

			// blocking call
			lock = fileChannel.lock();

			// use in other classes 
			constants.info("have the lock file", this);
			constants.put(ZephyrOpen.filelock, "true");
			
			// register with framework for shut down -- clear locked files on shutdown
			constants.addLogger(this);

		} catch (Exception e) {
			constants.error(e.getMessage());
			constants.error("can't lock file: " + fileName, this);
			close();
			logfile = null;
			lock = null;
			return;
		}
	}

	/** Closes the logfile and clears the lock */
	public void close() {
		
		constants.info("unlocked file: " + fileName, this);

		try {
			lock.release();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		try {
			fileChannel.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			logfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Appends data to the log file.
	 * <p/>
	 * If the logfile has not been previously opened, or if there is a file
	 * reading error, this method will do nothing.
	 * 
	 * @param data
	 *            is the text to append to the logfile.
	 */
	public void append(String data) {

		// sanity check
		if (!isOpen()) {
			// constants.error("trying to write to un-open file", this);
			return;
		}

		if (!isLocked()) {
			// constants.error("trying to write to un-locked file", this);
			return;
		}

		try {

			// position file pointer at the end of the logfile
			logfile.seek(logfile.length());

			// log zephyr.framework.state
			logfile.writeBytes(data + CRLF);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Returns true if the logfile is open and locked for us only, otherwise false.
	public boolean isOpen() {

		if (fileChannel == null)
			return false;

		return fileChannel.isOpen();
	}

	// Returns true if the logfile is open, otherwise false.
	public boolean isLocked() {

		if (lock == null)
			return false;

		return lock.isValid();
	}
}
