package zephyropen.device.polar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.*;

import zephyropen.api.ZephyrOpen;
import zephyropen.port.Port;

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
public class PolarSerialPort implements Port {

    /* framework configuration */
    public static ZephyrOpen constants = ZephyrOpen.getReference();

    /* serial port configuration parameters */
    private static final int BAUD_RATE = 9600;

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

    /* constructor takes a com port number as argument */
    public PolarSerialPort(String addr) {

        address = addr;

        try {

            /* construct the serial port */
            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(address).open(this.getClass().getName(), TIMEOUT);

        } catch (Exception e) {
            constants.error("error ininitalizing: " + address);
            constants.shutdown(e);
        }
    }

    /** connects on start up, return true is currently connected */
    public boolean connect() {

        try {

            /* configure the serial port */
            serialPort.setSerialPortParams(BAUD_RATE, DATABITS, STOPBITS, PARITY);
            serialPort.setFlowControlMode(FLOWCONTROL);

            /* extract the input and output streams from the serial port */
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

        } catch (Exception e) {
            constants.error("error connecting to: " + address);
            constants.shutdown(e);
            return false;
        }

        // connected
        constants.info("connected to: " + address, this);
        return true;
    }

    /**/
    @Override 
    public String getAddress() {
        return address;
    }
   
    /**
     * Wrapper the InputStream method
     * 
     * @return the number of bytes that can be read from the device
     * @throws IOException
     */
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * Wrapper the InputStream method
     * 
     * @param data
     *            to read from the device
     * @return the data read from the device
     * @throws IOException
     *             is thrown if this write operation fails
     */
    public int read(byte[] data) throws IOException {
        return inputStream.read(data);
    }

    /**
     * Wrapper the outputStream method
     * 
     * @param data
     *            to write to the device
     * @throws IOException
     *             is thrown if this write operation fails
     */
    public void writeBytes(byte[] data) throws IOException {
        outputStream.write(data);
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
}