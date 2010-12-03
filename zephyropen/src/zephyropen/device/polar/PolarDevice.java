package zephyropen.device.polar;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.device.Device;
import zephyropen.port.AbstractPort;
import zephyropen.util.Utils;

/**
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class PolarDevice extends AbstractPort implements Device {

    /** the name of the connected device */
    protected String deviceName = PrototypeFactory.polar;

    /** allocate a byte array for receiving data from the serial port */
    private static final int BUFFER_SIZE = 16;

    private static byte[] buffer = new byte[BUFFER_SIZE];

    private int bufferPointer = 0;

    private int bytesRead = 0;

    /** */
    public PolarDevice(String addr) {
        port = new PolarSerialPort(addr);
        command = new Command(PrototypeFactory.polar);
        command.add(ZephyrOpen.address, addr);
    }

    @Override
    public boolean connect() {
        if (port.connect())
            return true;

        return false;
    }

    /** poll device with message "G1<enter>" */
    private void getHR() {

        // send command
        byte[] bytes = new byte[3];
        bytes[0] = (char) 'G';
        bytes[1] = (byte) '1';
        bytes[2] = (int) 13;

        try {
            port.writeBytes(bytes);
        } catch (Exception e) {
            constants.error(e.getMessage(), this);
            close();
            return;
        }

        // wait for whole massage
        Utils.delay(400);

        try {

            // read into buffer 
            bytesRead = port.read(buffer);
            bufferPointer = bytesRead;

            // constants.info("read bytes: " + bytesRead);

            // update command
            if (parse()) {
                command.send();
            }

        } catch (Exception e) {
            constants.info(e.getMessage(), this);
            bufferPointer = 0;
        }
    }

    /** Loop on COM input */
    public void readDevice() {
        while (true) {
            getHR();
            Utils.delay(500);
        }
    }

    /**
     * 
     * example string from Port: 255,60,60,60,60,..
     * 
     * First value is the beat counter, followed by a number of past heart rate values
     * 
     */
    private boolean parse() {

        String value = parseHR();

        // lost connection 
        if (value == null)
            return false;

        // parse from port's data string 
        String beat = value.substring(0, value.indexOf(','));
        String hr = value.substring(value.indexOf(',') + 1, value.length());

        // update the command 
        command.add(PrototypeFactory.beat, beat);
        command.add(PrototypeFactory.heart, hr);

        last = System.currentTimeMillis();
        return true;
    }

    /** @return the current heart rates in the buffer separated by commas */
    private String parseHR() {

        // only read valid data 
        if (buffer[0] != '1')
            return null;

        String raw = "";
        for (int i = 0; i < bufferPointer; i++) {

            if (buffer[i] == (char) 13)
                break;

            //System.out.println("[" + i + "]" + (char)buffer[i]);
            if (buffer[i] == (char) 32) {
                raw += ",";
            } else {
                raw += (char) buffer[i];
            }
        }

        // chop out off extra commas 
        return raw.substring(2, raw.length() - 1);
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
    public long getElapsedTime() {
        return (System.currentTimeMillis() - start);
    }

    @Override
    public void close() {
        port.close();
    }

    @Override
    public String getDeviceName() {
        return PrototypeFactory.polar;
    }
}
