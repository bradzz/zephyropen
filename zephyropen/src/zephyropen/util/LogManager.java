package zephyropen.util;

import java.io.RandomAccessFile;
import java.util.Date;

/**
 * Manage a log file on local storage
 * 
 * Created: 2002.05.10
 * 
 * @author Peter Brandt-Erichsen
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class LogManager {
	
	/** framework configuration */
	// public static ZephyrOpen constants = ZephyrOpen.getReference();

    public static final String CRLF = "\r\n";
    
    private RandomAccessFile logfile = null;

    public LogManager() {
    }

    /**
     * Opens the specified logfile with read/write access.
     * 
     * @param filename
     *            is the name of the log file.
     */
    public void open(String filename) {

        // sanity check
        if (isOpen()){
        	System.err.println("log file is allready open: " + filename);
            return ;
        }
              
        // filename = constants.get(ZephyrOpen.userLog) + System.getProperty("file.separator") + filename; 

        try {

            logfile = new RandomAccessFile(filename, "rw");

        } catch (Exception e) {
        	System.err.println("can't open: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * Closes the logfile.
     */
    public void close() {
    	
        try {
        	if(isOpen())
        		logfile.close();
        } catch (Exception e) {
        	System.err.println("error on close?");
            e.printStackTrace();
        }
    }

    /**
     * Appends data to the log file.
     * <p/>
     * If the logfile has not been previously opened, or if there is a file reading error,
     * this method will do nothing.
     * 
     * @param data
     *            is the text to append to the logfile.
     */
    public synchronized void append(String data) {
    	
    	// sanity check
        if (!isOpen()) {
            return;
        }
        
        try {

            // position file pointer at the end of the logfile
            logfile.seek(logfile.length());
            
            // add date 
            data = new Date().toString() + ", " + data;

            // log zephyr.framework.state
            logfile.writeBytes(data + CRLF);

        } catch (Exception e) {
        	System.err.println("error on append?");
            e.printStackTrace();
        }
    }

    // Returns true if the logfile is open, otherwise false.
    public boolean isOpen() {
        return logfile != null;
    }
}