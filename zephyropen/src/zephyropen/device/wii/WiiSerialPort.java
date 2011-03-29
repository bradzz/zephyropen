package zephyropen.device.wii;

import java.io.IOException;
import java.util.Random;

import gnu.io.*;

import zephyropen.api.ZephyrOpen;
import zephyropen.device.wii.WiiUtils;
import zephyropen.port.AbstractPort;
import zephyropen.port.Port;
import zephyropen.util.Utils;

/**
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class WiiSerialPort extends AbstractPort implements Port, SerialPortEventListener {

    /** serial port configuration parameters */
    private static final int BAUD_RATE = 115200;

    private static final int TIMEOUT = 2000;

    private static final int DATABITS = SerialPort.DATABITS_8;

    private static final int STOPBITS = SerialPort.STOPBITS_1;

    private static final int PARITY = SerialPort.PARITY_NONE;

    private static final int FLOWCONTROL = SerialPort.FLOWCONTROL_NONE;

    private final byte[] buffer = new byte[125];

    private int bytesRead = 0;

    private int bufferPointer = 0;

    private int err, event, data = 0;

    /** reference to the underlying serial port */
    private static SerialPort serialPort = null;

    /** constructor takes a com port number as argument */
    public WiiSerialPort(String addr) {

        address = addr;

        try {

            /* construct the serial port */
            serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(address).open("WiiSerialServer", TIMEOUT);

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

            /* register this port wrapper to listen on serial port events */
            serialPort.addEventListener(this);

            /* call back on data avail event */
            serialPort.notifyOnDataAvailable(true);

        } catch (Exception e) {
            constants.error("error connecting to: " + address);
            constants.shutdown(e);
            return false;
        }

        // connected
        constants.info("connected to: " + address, this);
        return true;
    }

    /** manage incoming data, send out an xml command */
    public void serialEvent(SerialPortEvent arg) {

        event++;

        try {

            bytesRead = inputStream.read(buffer);
            bufferPointer = bytesRead;

            String text = parse();
            if (text.equals(""))
                return;

            // create an xml command to send to viewers 
            double d = Double.parseDouble(text);
            WiiUtils.create(d, d, d, d).send();

            data++;

        } catch (Exception e) {

            //System.err.println(bytesRead + " bytes were in the buffer, but can't parse to an int");
            // constants.info(e.getMessage(), this);

            bufferPointer = 0;
            err++;

        }
    }

    /**
     * @return the parsed text from the serial port
     */
    private String parse() {

        String raw = "";
        for (int i = 0; i < bufferPointer; i++) {

            if (buffer[i] == (char) 13)
                break;

            raw += (char) buffer[i];
        }

        return raw.trim();
    }

    /**
     * 
     * @param i
     *            is the number of events sent from the test driver
     * @return the in/out counters as a formated string
     */
    public String getStats(int i) {

        if (i != err)
            constants.info("sent: " + i + " got " + err);

        if ((data + err) != event)
            constants.info("dropped commands -- err: " + err + " data: " + data + " event: " + event);

        return " event: " + event + " data: " + data + " err:" + err;
    }

    /**
     * 
     * Test driver only
     * 
     */
    public static void main(String[] args) throws IOException {

        //
        // default framework 
        //
        constants = ZephyrOpen.getReference();
        constants.init();

        WiiSerialPort port = new WiiSerialPort("COM10");

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

                if (i % 10 == 0)
                    System.out.println(i + " " + port.getStats(i));
            }

        } else
            constants.info("error connecting");
    }

}