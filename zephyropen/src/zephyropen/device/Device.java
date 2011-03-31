package zephyropen.device;

/**
 * An interface all zephyropen devices
 * 
 * <p>
 * Created: September 27, 2008
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public interface Device {
	
	// connect to the underlying serial/bluetooth port 
    public boolean connect();

    // close the underlying ports 
    public void close();

    public long getDelta();

    public long getElapsedTime();

    public String getDeviceName();

    public void readDevice();

//    public Command getCommand();
    
}
