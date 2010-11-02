package zephyropen.util;

/**
 * <p> Create logs for the given device. 
 * 
 * <p> If the device is "HXM110075" the logs will be: 
 * <p> -- HXM00110075.log (best for exel import)
 * <p> -- HXM001175.xml (best for reporting software) 
 * <p><b>Note that the XML file will get large quickly</b>
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.state.FilterFactory;

public class LoggerAPI implements API {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	
	public static final String CRLF = "\r\n";
	
	/** device listening too */
	private String deviceName = null;

	private RandomAccessFile logfile = null;
	private FileChannel fileChannel = null;
	private FileLock lock = null;

	private long last = System.currentTimeMillis();

	/**
	 * Listen for XML with the given deviceName
	 */
	public LoggerAPI() {

		deviceName = PrototypeFactory.getDeviceTypeString(constants.get(ZephyrOpen.deviceName));
		
		// wait to acquire lock file 
		openFile();
		
		constants.info("started logging on [" + deviceName + "]", this);
		
		/** listen for this device */
		ApiFactory.getReference().add(this);

		/** g'night, wait onx input */
		Utils.delay(Long.MAX_VALUE);
		
	}
	
	/**  blocking call */
	private void openFile() {
		
		try {
			
			String fileName = constants.get(ZephyrOpen.userLog) 
				+ System.getProperty("file.separator") + constants.get(ZephyrOpen.deviceName) + ".xml";
			
			System.out.println("writting to [" + fileName + "]");

			try {
				logfile = new RandomAccessFile(fileName, "rw");
			} catch (FileNotFoundException e) {
				constants.error("can't open file: " + fileName);
				constants.shutdown();
			}

			fileChannel = logfile.getChannel();
			if (fileChannel == null) {
				constants.error("can't get a file channel", this);
				constants.shutdown();
			}
		
			// blocking call
			System.out.println(".. waiting on lockfile");
			lock = fileChannel.lock();
			
		} catch (Exception e) {
			constants.error("can't lock file: " + e.getMessage(), this);
			constants.shutdown();
		}
	}


	/** write it all to disk as comes in, and time stamp it */
	public void execute(Command command) {

		System.out.println(getDelta() + " : " + command.get(ZephyrOpen.userName) + " " + deviceName );

		// send to log file
		if( FilterFactory.filter(command))
			append(command.toString());
		else 
			System.err.println("filter: " + command);
		
		last = System.currentTimeMillis();
	}

	@Override
	public long getDelta() {
		return System.currentTimeMillis() - last;
	}

	@Override
	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public String getAddress() {
		return constants.get(ZephyrOpen.address);
	}

	/** Closes the logfile and clears the lock */
	public void close() {

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

		logfile = null;
		fileChannel = null;
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
			constants.error("trying to write to un-open file", this);
			return;
		}

		if (!isLocked()) {
			constants.error("trying to write to un-locked file", this);
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

	/** @return true if the logfile is open, otherwise false. */
	public boolean isOpen() {

		if (fileChannel == null)
			return false;

		return fileChannel.isOpen();
	}

	/** @return true if the logfile is locked for us only, otherwise false. */
	public boolean isLocked() {

		if (lock == null)
			return false;

		return lock.isValid();
	}
	
	/**
	 * Use command line arguments to configure the framework and the start the server
	 */
	public static void main(String[] args) {

		if (args.length == 1) {

			// configure the framework with properties file
			constants.init(args[0]);

			// properties file must supply the device Name */
			new LoggerAPI();
		}
	}
}
