package zephyropen.port.bluetooth;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import zephyropen.api.ZephyrOpen;
import zephyropen.port.Port;
import zephyropen.util.Utils;

/**
 * <p>
 * Wrapper for the Blue Tooth JNI layer for searching and connecting to a given BT device.
 * <p>
 * SPP must be discovered as a service on the given Device Name
 * <p>
 * This is a blocking discovery, it will not return until the device is found, or times
 * out.
 * <p>
 * Package : Created: May 11, 2008
 * 
 * <p>
 * The BT device must be of the following type: <br>
 * RFCOMM_PROTOCOL_UUID <br>
 * NOAUTHENTICATE_NOENCRYPT
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class SerialPortProfile extends RemoteDevice implements DiscoveryListener, Port {

    /** framework configuration */
    private static ZephyrOpen constants = ZephyrOpen.getReference();
    
    private boolean serviceSearch = true;

    private DiscoveryAgent agent = null;

    private String serviceURL = null;

    private LocalDevice local = null;

    private StreamConnection connection = null;

    private DataInputStream inputStream = null;

    private OutputStream outputStream = null;

    private boolean found = false;
    
    public boolean isOpen(){
    	return true;
    }

    /**
     * Create and try to discover a RF-COMM SPP device via the BlueTooth Stack
     * 
     * @param target
     *            is the friendly name of the blue tooth device to be found
     */
    public SerialPortProfile(String target) {

        super(target);

        /** radio better be on */
        if (!LocalDevice.isPowerOn()) {
            constants.error("Blue Tooth Radio is not ready, terminate!", this);
            constants.shutdown();
        }

        try {

            /** get local services */
            local = LocalDevice.getLocalDevice();
            agent = local.getDiscoveryAgent();

        } catch (BluetoothStateException e) {
            constants.error(e.getMessage(), this);
            constants.shutdown();
        }
    }

    /**
     * Find and attach to serial port profile for this device
     * 
     * @return true if connection was established, false if not
     */
    public boolean connect() {

        Thread runner = new Thread() {
            @Override
            public void run() {
                found = findService();
            }
        };
        runner.start();

        constants.info("blocking....", this);

        // block on search
        try {
            while (serviceSearch)
                runner.join();
        } catch (InterruptedException e) {
            constants.error("getDevices() :" + e.getMessage(), this);
        }

        if (!found) {
            constants.error("can't find serial port service on " + getBluetoothAddress(), this);
            return false;
        }

        try {
            /** found it */
            constants.info("url = " + serviceURL, this);

            /** open the serial port streams */
            connection = (StreamConnection) Connector.open(serviceURL);
            inputStream = connection.openDataInputStream();
            outputStream = connection.openDataOutputStream();

        } catch (Exception e) {
            constants.error("connect(): " + e.getMessage(), this);
            return false;
        }

        /** all is well, streams open */
        return true;
    }

    /** @return true if target device was found */
    private boolean findService() {

        constants.info("Searching for SPP " + getBluetoothAddress(), this);

        /** Serial Port Profile UUID */
        int[] attributes = { 0x100 };
        UUID[] uuids = { com.intel.bluetooth.BluetoothConsts.RFCOMM_PROTOCOL_UUID };

        /** look for serial port service */
        int serviceSearchID = 0;
        try {

            serviceSearchID = agent.searchServices(attributes, uuids, this, this);

        } catch (Exception e) {
            agent.cancelServiceSearch(serviceSearchID);
            return false;
        }

        /** found target serial port profile ? */
        if (serviceURL == null)
            return false;
        else
            return true;
    }

    public String readUTFString() {
        try {
            return inputStream.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String readString() throws IOException {
        String reply = new String();
        byte[] buffer = new byte[256];

        int count = 0;
        while (count > -1) {

            count = inputStream.read(buffer);
            if (count != -1) {
                reply += to(buffer, count);
            }
        }
        return reply.trim();
    }

    public String to(byte[] bytes, int count) {
        String s = new String();
        for (int i = 0; i < count; i++) {

            Character ch = new Character((char) bytes[i]);
            if (ch != null) {
                if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch))
                    s += ch;
            }
        }
        return s;
    }

    /** Close the serial port profile's streams */
    public void close() {
        try {

            if (inputStream != null)
                inputStream.close();

        } catch (IOException e) {
            constants.error(e.getMessage(), this);
        }

        try {

            if (outputStream != null)
                outputStream.close();

        } catch (IOException e) {
            constants.error(e.getMessage(), this);
        }
    }

    // not used, we have an address already 
    public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
    }

    public void inquiryCompleted(int arg0) {
    }

    /** done looking */
    public void serviceSearchCompleted(int arg0, int arg1) {
        serviceSearch = false;
    }

    /** found a service on this device */
    public void servicesDiscovered(int arg0, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {

            DataElement nameElement = servRecord[i].getAttributeValue(0x100);
            if ((nameElement != null) && (nameElement.getDataType() == DataElement.STRING)) {

                /** retrieve the name of the service */
                String name = (String) nameElement.getValue();
                name = name.trim();

                constants.info("found ------- [" + name + "]", this);
                if (name.equals("Bluetooth Serial Port")) {
                    constants.info(" ..is a port " + name, this);
                    serviceURL = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    serviceSearch = false;
                } else
                    constants.info(" .. not a port", this);
            }
        }
    }

    /** Look for and try to connect to all devices with BT SPP services */
    public static void discoveryTest() {

        Discovery bt = new Discovery();

        /** search for devices, block on this call */
        System.out.println("blocking on search....");
        Vector<RemoteDevice> results = bt.getSPPDevices();

        if (results == null)
            return;
        else
            Discovery.printResults(results);

        /** now connect to them all */
        Enumeration<RemoteDevice> list = results.elements();
        while (list.hasMoreElements()) {

            try {
                SerialPortProfile spp = new SerialPortProfile(list.nextElement().getBluetoothAddress());
                if (spp.connect()) {

                    System.out.println("connected to : " + spp);
                    System.out.println("reply = " + spp.readString());
                    spp.close();

                } else {
                    System.out.println("connect() failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Start a SPP with given address like:
     * 
     * java SerialPortProfile 000A3A863719
     * 
     * @param args
     *            the bluetooth address of the device
     */
    public static void main(String[] args) {

        constants.init();
        SerialPortProfile spp = new SerialPortProfile("00078088F38E"); // args[0]);

        /// continue to search and attempt to connect to the device, keep stats
        int connected = 0;
        int failed = 0;
        int error = 0;
        for (int i = 1;; i++) {

            if (spp.connect()) {

                connected++;
                System.out.println("connected to device...");

                String s = null;
                try {

                    s = spp.readString();

                } catch (IOException e) {
                    e.printStackTrace();
                    error++;
                }

                System.out.println("reply : " + s);
                spp.close();

            } else {
                failed++;
                System.out.println("connect() failed...");
            }

            System.out.println("attempts: " + i + " connected: " + connected + " failed: " + failed + " errors: " + error);
            Utils.delay(ZephyrOpen.ONE_MINUTE / 2);
        }
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

    @Override
    public String getAddress() {
        return getAddress();
    }

}