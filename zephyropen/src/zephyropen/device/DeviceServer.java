package zephyropen.device;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

/**
 * <p>
 * A base class server for the Zephyr BlueTooth devices. Based on the naming conventions
 * from Zephyr, this server will create and connect to the bluetooth device.
 * 
 * If a connection can be established, the server will start a watch dog timer thread and
 * start reading the serial data. The connection will time out and be closed if the data
 * stream is lost, it is up to the daemon services to start new server processes to
 * maintain the connection.
 * <p>
 * http://www.zephyrtech.co.nz/
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class DeviceServer {

    /** framework configuration */
    public static ZephyrOpen constants = ZephyrOpen.getReference();

    /** time to sleep while in watch dog thread */
    public final static int SPIN_TIME = 1000;

    /** the device to read from */
    private Device device = null;
    
    /** track attempts */
    private int i = 0; 

    /**
     * <p>
     * Constructor for the DeviceServer. Use a factory to create a server for the specific
     * device based only on the naming convention from the manufacturing company.
     * 
     */
    public DeviceServer() {
    
        // do until stopped with signal ^C
        while(true){
	       
            device = DeviceFactory.create();
            if (device == null) {
                constants.error("Can't create device, terminate.", this);
                return;
            }
        	
        	if (device.connect()) {
	        	
	            // blocking call
	            device.readDevice();
	            
	        } else {
	            constants.info("can't connect [" + i++ + "]: " + device.getDeviceName(), this);
	            Utils.delay(30000);
	        }
        }   
    }
    
    /*
     * Use command line arguments to configure the framework with given properties file
     * 
     * {@code java DeviceServer polar.propetries} {@code java Server zephyr.properties}
    */
    public static void main(String[] args) {

        if (args.length == 1) {

            // configure the framework with properties file 
            constants.init(args[0]);

            // properties file must supply the device Name 
            new DeviceServer();
    
        }
    } 
}
