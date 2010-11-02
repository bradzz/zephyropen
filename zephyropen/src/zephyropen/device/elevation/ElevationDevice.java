package zephyropen.device.elevation;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.device.Device;
import zephyropen.port.AbstractPort;
import zephyropen.util.Utils;

/**
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class ElevationDevice extends AbstractPort implements Device {

	/** the name of the connected device */
	protected String deviceName = PrototypeFactory.polar;

	/** allocate a byte array for receiving data from the serial port */
	private static final int BUFFER_SIZE = 16;

	/** */
	public ElevationDevice(String addr) {
		port = new SparkfunSerialPort(addr);
		command = new Command(PrototypeFactory.elevation);
	}

	/*
	@Override
	public boolean connect() {
		if (port.connect())
			return true;

		return false;
	} */
	
	 /** poll device with message "G1<enter>" */
    private String getHR() {
    	
    	byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = 0;

        // send command
        byte[] bytes = new byte[3];
        bytes[0] = (char) 'G';
        bytes[1] = (byte) '1';
        bytes[2] = (int) 13;

        try {
            port.writeBytes(bytes);
        } catch (Exception e) {
            constants.error(e.getMessage(), this);
            return "0";
        }

        // wait for whole massage
        Utils.delay(200);

        try {

            // read into buffer 
            bytesRead = port.read(buffer);
            
        } catch (Exception e) {
            constants.info(e.getMessage(), this);
            return "0";
        }
    	
    	// only read valid data 
        if (buffer[0] != '1')
            return null;

        String raw = "";
        for (int i = 0; i < bytesRead; i++) {

            if (buffer[i] == (char) 13)
                break;

            //System.out.println("[" + i + "]" + (char)buffer[i]);
            if (buffer[i] == (char) 32) {
                raw += ",";
            } else {
                raw += (char) buffer[i];
            }
        }

        String value = raw.substring(2, raw.length() - 1);
        
        // parse from port's data string 
        String hr = value.substring(value.indexOf(',') + 1, value.length());

        command.add(PrototypeFactory.heart, hr);

        last = System.currentTimeMillis();
        return hr;
    }

	/** poll device with message "A1<enter>" */
	private String getAnalog(int line) {

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = 0;

		// send command
		byte[] bytes = new byte[3];
		bytes[0] = (char) 'A';
		bytes[2] = (int) 13;
		
		//bytes[1] = (byte) 1;
		
		// i/o lines 
		if( line == 0 ) bytes[1] = (byte) '0';
		if( line == 1 ) bytes[1] = (byte) '1';
		if( line == 2 ) bytes[1] = (byte) '2';
		if( line == 3 ) bytes[1] = (byte) '3';
		
		try {
			port.writeBytes(bytes);
		} catch (Exception e) {
			constants.error("can't write to device, fatal error", this);
			port.close();
			constants.shutdown(e);
		}

		// wait for whole massage
		Utils.delay(200);

		try {

			// read into buffer
			bytesRead = port.read(buffer);

			//constants.info("getAnalog() read bytes: " + bytesRead);

			String raw = "";
			for (int i = 0; i < bytesRead; i++) {

				if (buffer[i] == (char) 13)
					break;

				raw += (char) buffer[i];
			}

			// constants.info("read: " + raw);

			return raw.trim();
			
		} catch (Exception e) {
			constants.info(e.getMessage(), this);
			return null;
		}
	}

	/** Loop on COM input */
	public void readDevice() {
		
		String back;
		String seat;
		String heart;
		
		while (true) {
		
			back = getAnalog(1);
			if(back != null)
				command.add(PrototypeFactory.back, back);
			
			seat = getAnalog(2);
			if(seat != null)
				command.add(PrototypeFactory.seat, seat);
			
			heart = getHR();
			if(heart != null)
				command.add(PrototypeFactory.heart, heart);
			else 
				command.add(PrototypeFactory.heart, "70");
			
			if ( ! command.isMalformedCommand(PrototypeFactory.ELEVATION_PROTOTYPE)){
				
				command.send();
				
				last = System.currentTimeMillis();
		
			} else {
			
				constants.error("invalid xml: " + command.toString());
				
			}
		}
	}

	/**
	 * Used by watch dog thread
	 * 
	 * @return the amount of time passed since last message
	 */
	@Override
	public long getDelta() {
		return (System.currentTimeMillis() - last);
	}

	/** Return the time since the first message from the device */
	@Override
	public long getElapsedTime() {
		return (System.currentTimeMillis() - start);
	}

	@Override
	public void close() {
		port.close();
	}

	@Override
	public String getDeviceName() {
		return PrototypeFactory.elevation;
	}
}
