package zephyropen.port.bluetooth;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
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
public class SearchSPP implements DiscoveryListener, Port {

    /** framework configuration */
    public static final ZephyrOpen constants = ZephyrOpen.getReference();

    /** time between BT connection spin locking */
    public final long DELAY = 200;

    private boolean deviceSearch = true;

    private boolean serviceSearch = true;

    private DiscoveryAgent agent = null;

    private String serviceURL = null;

    private RemoteDevice targetDevice = null;

    private String targetDeviceName = null;

    private LocalDevice local = null;

    private StreamConnection connection = null;

    private DataInputStream inputStream = null;

    private OutputStream outputStream = null;

    private String address;

    /**
     * Create and try to discover a RF-COMM SPP device via the BlueTooth Stack
     * 
     * @param target
     *            is the friendly name of the blue tooth device to be found
     */
    public SearchSPP(String target) {

        targetDeviceName = target;

        try {

            /** get local services */
            local = LocalDevice.getLocalDevice();
            agent = local.getDiscoveryAgent();

        } catch (BluetoothStateException e) {
            constants.error(e.getMessage(), this);
            constants.shutdown();
        }

        /** radio better be on */
        if (!LocalDevice.isPowerOn()) {
            constants.error("Blue Tooth Radio is not ready, terminate!", this);
            constants.shutdown();
        }
    }

    /**
     * Find and attach to serial port profile for this device
     * 
     * @return true if connection was established, false if not
     */
    public boolean connect() {

        /** look for the BT device in our BT control module */
        if (!findDevice())
            return false;

        /** now look for the service url */
        if (!findService()) {
            constants.error("can't find service url", this);
            return false;
        }

        try {

            constants.info("url-" + serviceURL, this);

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

    /**
     * Find the given device name on the BT network
     * 
     * @return true if target device was found
     */
    private boolean findDevice() {

        constants.info("Searching for [" + targetDeviceName + "]", this);

        /** Be sure to re-start the process of searching each time this is called */
        targetDevice = null;
        deviceSearch = true;

        try {

            agent.startInquiry(DiscoveryAgent.GIAC, this);

            /** wait to find the device */
            while (deviceSearch)
                Thread.sleep(DELAY);

        } catch (Exception e) {
            agent.cancelInquiry(this);
            return false;
        }

        if (targetDevice == null) {
            constants.error("can not find device [" + targetDeviceName + "]", this);
            return false;
        }

        /** found target */
        return true;
    }

    /** @return true if target device was found */
    private boolean findService() {

        constants.info("Searching for SPP on [" + targetDeviceName + "]", this);
        serviceSearch = true;

        /** Serial Port Profile UUID */
        int[] attributes = { 0x100 };
        UUID[] uuids = new UUID[1];
        uuids[0] = com.intel.bluetooth.BluetoothConsts.RFCOMM_PROTOCOL_UUID;

        /** look for serial port service */
        int serviceSearchID = 0;
        try {

            serviceSearchID = local.getDiscoveryAgent().searchServices(attributes, uuids, targetDevice, this);

            /** spin lock */
            while (serviceSearch)
                Thread.sleep(DELAY);

        } catch (Exception e) {
            agent.cancelServiceSearch(serviceSearchID);
            return false;
        }

        /** found target */
        return true;
    }

    /** Called by the blue tooth control as devices are found */
    public void deviceDiscovered(RemoteDevice device, DeviceClass deviceClass) {

        try {

            // discovered address 
            address = device.getBluetoothAddress();

            /** debug output only, need the address sometimes */
            constants.info("discovered [" + device.getFriendlyName(false) + "] address = " + address, this);

            if (device.getFriendlyName(false).equals(targetDeviceName)) {

                /** device has been found */
                targetDevice = device;

                /** we don't care about other devices, we can stop searching now */
                deviceSearch = false;
                agent.cancelInquiry(this);

            }
        } catch (Exception e) {
            constants.error(e.getMessage(), this);
        }
    }

    /** This is called by the blue tooth control when the inquiry has completed */
    public void inquiryCompleted(int respCode) {
        constants.info("inquiryCompleted()", this);
        deviceSearch = false;
    }

    /** not used by this class */
    public void servicesDiscovered() {
    }

    /** This is called by the bluetooth control when search has completed */
    public void serviceSearchCompleted(int arg0, int arg1) {
        constants.info("serviceSearchCompleted()", this);
        serviceSearch = false;
    }

    /**
     * Find a Bluetooth Serial Port service for this device
     * 
     * <p>
     * <b>note: this is a NOAUTHENTICATE_NOENCRYPT device only </b>
     * 
     * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int,
     *      javax.bluetooth.ServiceRecord[])
     */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {

            /** check for SPP */
            String name = (String) (servRecord[i].getAttributeValue(0x100)).getValue();

            if (name.equals("Bluetooth Serial Port")) {
                serviceURL = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                serviceSearch = false;
            }
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

    /** Override, give better output */
    @Override
    public String toString() {
        return targetDeviceName;
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

    /** @return the address as a hex encoded string for this device */
    public String getAddress() {
        return address;
    }

    /**
     * Start a SPP with given name
     * 
     * java SearchSPP "Brad'd iMac"
     * 
     * @param args
     *            the bluetooth friendly name of the device
     

    public static void main(String[] args) {

        constants.init();

        SearchSPP spp = new SearchSPP("BH ZBH001354");
        // args[0]);

        // continue to search and attempt to connect to the device, keep stats
        int connected = 0;
        int failed = 0;
        int error = 0;
        for (int i = 1;; i++) {

            if (spp.connect()) {

                connected++;
                System.out.println("connected to device and closing ...");
                spp.close();

            } else {
                failed++;
                System.out.println("connect() failed...");
            }

            System.out.println("attempts: " + i + " connected: " + connected + " failed: " + failed + " errors: " + error);
            Utils.delay(ZephyrOpen.ONE_MINUTE);
        }
    }*/

}