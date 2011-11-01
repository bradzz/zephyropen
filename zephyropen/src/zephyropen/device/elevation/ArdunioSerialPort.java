package zephyropen.device.elevation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.*;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.util.LogManager;
import zephyropen.util.Utils;

/**
 * 
 * Read the Polar Heart Rate Monitor Board over COM Port
 * 
 * @see http://danjuliodesigns.com/sparkfun/sparkfun.html
 * @see http://www.sparkfun.com/commerce/product_info.php?products_id=8661
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class ArdunioSerialPort implements SerialPortEventListener /* implements Port */{

    /* framework configuration */
    public static ZephyrOpen constants = ZephyrOpen.getReference();

    /* serial port configuration parameters */
    private static final int BAUD_RATE = 115200;
    private static final int TIMEOUT = 2000;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;
    private static final int FLOWCONTROL = SerialPort.FLOWCONTROL_NONE;

    /* build from address manually */
    protected String address = null;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;

    /* reference to the underlying serial port */
    private static SerialPort serialPort = null;
    
	protected byte[] buffer = new byte[32];
	protected int buffSize = 0;
	LogManager log = new LogManager();

    /* constructor takes a com port number as argument */
    public ArdunioSerialPort() {
    	
    	address = constants.get(ZephyrOpen.serialPort);
    	
        if(connect()){        
                
        	log.open(constants.get(ZephyrOpen.userHome) + "\\elevation.log");
        	
        	Utils.delay(Long.MAX_VALUE);
        
        } else {
        	System.out.println("die");
        }
    }

    /** connects on start up, return true is currently connected */
    public boolean connect() {

        try {

            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(address).open("ArduinoSerialPort", TIMEOUT);
        	
            /* configure the serial port */
            serialPort.setSerialPortParams(BAUD_RATE, DATABITS, STOPBITS, PARITY);
            serialPort.setFlowControlMode(FLOWCONTROL);

            /* extract the input and output streams from the serial port */
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            
            // register for serial events
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

        } catch (Exception e) {
            constants.error("error connecting to: " + address);
            return false;
        }

        // connected
        constants.info("connected to: " + address, this);
        return true;
    } 
    
    public boolean isOpen(){
    	return true;
    }

	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
		//	System.out.println("da");
			manageInput();
		}
	}
		
	/** */
	public void manageInput(){
		try {
			byte[] input = new byte[32];
			int read = inputStream.read(input);
			for (int j = 0; j < read; j++) {
				
				// print() or println() from arduino code
				if ((input[j] == 13) || (input[j] == 10)) {
				
					// do what ever is in buffer
					if (buffSize > 0){

						String data = new String();
						for(int c = 0 ; c < buffSize ; c++){
							data+= (char)buffer[c];
						}
												
						String[] val = data.split(" : ");
						Command cmd = new Command(PrototypeFactory.elevation);
						cmd.add("back", val[0].trim());
						cmd.add("seat", val[1].trim());
						cmd.add(ZephyrOpen.user, "brad");
						 //System.out.println(cmd.toXML());
						
						// listeners 
						cmd.send();
						
						// log
						String fin = System.currentTimeMillis() + " " + val[0].trim() + " " + val[1].trim();
						// System.out.println(fin);
						log.append(fin);
						
					}
					
					// reset
					buffSize = 0;
					// track input from arduino
					// lastRead = System.currentTimeMillis();
				} else if (input[j] == '<') {
					// start of message
					buffSize = 0;
				} else {
					// buffer until ready to parse
					buffer[buffSize++] = input[j];
				}
			}
		} catch (IOException e) {
			System.out.println("event : " + e.getMessage());
		}
	}

    /** Close the serial port profile's streams */
    public void close() {

        constants.info("closing " + address, this);

        try {

            if (inputStream != null)
                inputStream.close();

        } catch (IOException e) {
            constants.error("close() :" + e.getMessage(), this);
        }

        try {

            if (outputStream != null)
                outputStream.close();

        } catch (IOException e) {
            constants.error("close() :" + e.getMessage(), this);
        }
    }
    
    
	public static void main(String args[]) {
		
		constants = ZephyrOpen.getReference();
		constants.init();
		constants.put(ZephyrOpen.serialPort, "COM4");
		constants.put(ZephyrOpen.user, "brad");

		new ArdunioSerialPort();
	}
    
}