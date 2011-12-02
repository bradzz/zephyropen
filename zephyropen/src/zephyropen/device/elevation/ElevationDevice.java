/*
 *
 * Created on 2010-07-05
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * @version $Id: code-templates.xml 4670 2010-01-10 07:33:42Z vlads $
 */
package zephyropen.device.elevation;

import java.io.IOException;
import java.util.Random;

import zephyropen.api.ZephyrOpen;
import zephyropen.device.Device;
import zephyropen.device.arduino.ArduinoSerialPort;
import zephyropen.port.AbstractPort;
import zephyropen.util.Utils;

public class ElevationDevice extends AbstractPort implements Device {

    /** the name of the connected device */
    protected String deviceName = "Arduino";

    /** allocate a byte array for receiving data from the serial port */
    //private static final int BUFFER_SIZE = 16;

    //private static byte[] buffer = new byte[BUFFER_SIZE];

    //private final int bufferPointer = 0;

    //private final int bytesRead = 0;

    public ElevationDevice(String addr) {
        address = addr;
        port = new ArduinoSerialPort("COM4");
    }

    @Override
    public boolean connect() {
        return port.connect();
    }
    
    @Override
    public long getDelta() {

        return 0;
    }  
    
    @Override
    public long getElapsedTime() {

        return 0;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }
 
    @Override
    public void readDevice() {

    }

    /**
     * @param args
     * @throws IOException
     */
    /*
    public static void main(String[] args) throws IOException {

        //
        // default framework 
        //
        constants = ZephyrOpen.getReference();
        constants.init();

        ArduinoSerialPort port = new ArduinoSerialPort("COM10");

        Random rand = new Random();

        if (port.connect()) {

            // just wait for incoming data 
            // 
            // Utils.delay(Long.MAX_VALUE);

            // now try sending dummy commands 
            for (int i = 0;; i++) {

                port.writeBytes(new String("a b c \n").getBytes());

                // wait 1 - 5 seconds 
                Utils.delay((Math.abs(rand.nextInt()) % 4000) + 100);

                // if (i % 10 == 0)
                // System.out.println(i + " " + port.getStats(i));
            }

        } else
            constants.info("error connecting");
    }
	*/
    
}
