package zephyropen.port.bluetooth;

import java.util.Enumeration;
import java.util.Hashtable;
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

import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.util.Utils;

/**
 * <p> Wrapper for the BT discovery process
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class Discovery implements DiscoveryListener {

	/** framework configuration, used fir logging only in this example */
	private static ZephyrOpen constants = ZephyrOpen.getReference();

	/** lock the search flags */
	private boolean deviceSearch = true;
	private boolean serviceSearch = true;

	/** local blue tooth device */
	private DiscoveryAgent agent = null;
	private LocalDevice local = null;

	/** list of devices found, and the service search requests */
	private Vector<RemoteDevice> devices;
	
	/** list of devices found, and the service search requests */
	private Vector<RemoteDevice> spp;

	/** needed to match request id's with remote devices */
	private Hashtable<Integer, RemoteDevice> requests = new Hashtable<Integer, RemoteDevice>();

	/** Constructor takes no arguments */
	public Discovery() {

		/** radio is on? */
		if (!LocalDevice.isPowerOn()) {
			constants.error("Blue Tooth Radio is powered down, terminate!", this);
			constants.shutdown();
		}

		/** access our radio and agent */
		try {

			local = LocalDevice.getLocalDevice();
			agent = local.getDiscoveryAgent();

		} catch (BluetoothStateException b) {
			constants.shutdown(b);
		}
	}

	/** */
	public String getLocalName() {
		return local.getFriendlyName();
	}

	/** */
	public String getLocalAddress() {
		return local.getBluetoothAddress();
	}

	/** Start looking for ALL Devices */
	private void startSearch() {

		try {

			/** get rid of past searches if any */
			agent.cancelInquiry(this);

			/** try to start new search */
			if (!agent.startInquiry(DiscoveryAgent.GIAC, this)) {
				constants.error("couldn't start search", this);
				constants.shutdown();
			}
		} catch (Exception e) {
			constants.shutdown(e);
		}
	}

	/** Look for a SPP on all known devices */
	private void startServiceService(RemoteDevice target) {

		/** Serial Port Profile UUID */
		int[] attributes = { 0x100 };
		
		UUID[] uuids = { com.intel.bluetooth.BluetoothConsts.RFCOMM_PROTOCOL_UUID };
		// UUID[] uuids = { new UUID("0000000300001000800000805F9B34FB", false)};

		/** start a requests for each device found */
		int serviceSearchID = 0;
		try {

			/** look for serial port service, get a request id */
			serviceSearchID = local.getDiscoveryAgent().searchServices(attributes, uuids, target, this);

			/** track this request via the ID */
			requests.put(new Integer(serviceSearchID), target);

		} catch (Exception e) {
			constants.error("startServiceService() : " + e.getMessage(), this);
		}
		constants.info("request started " + requests.toString(), this);
	}

	/** Called by the blue tooth control as devices are found */
	public void deviceDiscovered(RemoteDevice device, DeviceClass deviceClass) {
		// passkey(device);
		devices.add(device);
	}

	/** 
	public void passkey(RemoteDevice device) {

		try {

			String name = device.getFriendlyName(false);
			constants.info(device + " address : " + device, this);

			if (device.isAuthenticated()) {

				constants.info(name + " is isAuthenticated ", this);

			} else {

				constants.info(name + " is NOT isAuthenticated ", this);

				// try to log in
				if (RemoteDeviceHelper.authenticate(device, "1234"))
					constants.info(name
							+ "is now isAuthenticated with default passkey ",
							this);
				else
					constants.info(name + "default passkey rejected", this);

			}
		} catch (IOException e) {
			constants.info(e.getMessage(), this);
		}
	}*/

	/** This is called by the blue tooth control when the inquiry has completed */
	public void inquiryCompleted(int respCode) {

		switch (respCode) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			constants.info("inquiryCompleted(): INQUIRY_COMPLETED", this);
			break;

		case DiscoveryListener.INQUIRY_TERMINATED:
			constants.info("inquiryCompleted(): INQUIRY_TERMINATED", this);
			break;

		case DiscoveryListener.INQUIRY_ERROR:
			constants.info("inquiryCompleted(): INQUIRY_ERROR", this);
			break;

		default:
			constants.info("inquiryCompleted(): Unknown Response Code", this);
			break;
		}

		deviceSearch = false;
	}

	/**
	 * This method is called by the blue tooth control when service search has
	 * completed
	 */
	public void serviceSearchCompleted(int transID, int arg1) {

		/** done, so remove your request id */
		synchronized(requests){
			
			requests.remove(new Integer(transID));
		
			if( requests.isEmpty() ) {
			
				serviceSearch = false;
				constants.info( "no more requests", this);
			
			} else {
				constants.info( "requests now = " + requests, this);
			}
		}	 
	}

	/** This method is called by the blue tooth control when search has completed */
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

		RemoteDevice rd = null;
		String serviceName = "no service";

		try {

			/** look up this request id and get a remote device */
			rd = requests.get(new Integer(transID));
			if (rd == null) return;

			for (int i = 0; i < servRecord.length; i++) {
				
				 DataElement nameElement = (DataElement)servRecord[i].getAttributeValue(0x100);
				 if ((nameElement != null) && (nameElement.getDataType() == DataElement.STRING)) {

		                /** retrieve the name of the service */
		            	serviceName = (String)nameElement.getValue();
		            	
		            	constants.info(rd + " service name was = " + serviceName, this);  
		            	
		            	/** add serial devices if found */
		            	if (serviceName.equals("Bluetooth Serial Port"))
		            		spp.add(rd);
				 }  
			}
		} catch (Exception e) {
			constants.error("servicesDiscovered(): " + e.getMessage(), this);
		}
	}

	/** not used */
	public void servicesDiscovered() {}

	/** get results */
	public Vector<RemoteDevice> getDevices() {
		
		/** reset flag and clear the list */
		deviceSearch = true;
		devices = new Vector<RemoteDevice>();
		
		/** new search thread */
		Thread runner = new Thread() {
			public void run() {		
				startSearch();	
			}
		};
		runner.start();
		
		/** blocking on search results */
		try {
			
			while(deviceSearch)
				runner.join();
			
		} catch (InterruptedException e) {
			constants.error( "getDevices() :" + e.getMessage(), this);
		}
		
		/** send back results */
		return devices;
	}

	/** get results */
	public Vector<RemoteDevice> getSPPDevices() {
		
		/** blocking call */ 
		final Vector<RemoteDevice> discovered = getDevices(); 
		spp = new Vector<RemoteDevice>();
		
		/** sanity test, don't bother starting threads */
		if( discovered.isEmpty()) return spp;
		
		/** new search thread, see if these are SPP devices*/
		serviceSearch = true;
		for(int i = 0 ; i < discovered.size() ; i++ )
			new SearchThread(discovered.get(i));
			
		/** blocking on search results */		
		while(serviceSearch){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				constants.error( "getDevices() :" + e.getMessage(), this);
			}
		}

		/** send back the results */
		return spp;
	}
	
	/** inner class for threaded search */ 
	public class SearchThread implements Runnable {
		private Thread runner = new Thread(this);
		private RemoteDevice device = null;
		
		SearchThread(RemoteDevice dev){
			device = dev;
			runner.setDaemon(true);
			runner.start();	
		}
		
		public void run() {
			startServiceService( device );
		}
	}

	/** print results */
	public static void printResults(Vector<RemoteDevice> results) {

		if (results.isEmpty()) {
			System.out.println("no devices found");
			return;
		}

		RemoteDevice target = null;
		Enumeration<RemoteDevice> list = results.elements();
		while (list.hasMoreElements()) {

			target = list.nextElement();
			try {

				System.out.println(target.getBluetoothAddress() + " : "
						+ target.getFriendlyName(false));

			} catch (Exception e) {
				constants.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/** print results */
	public void sendXML() {

		Vector<RemoteDevice> results = getDevices();
		if (results.isEmpty()) {
			constants.info("no devices found");
			return;
		}

		Command devs = new Command(ZephyrOpen.discovery);
		devs.add(ZephyrOpen.address, local.getBluetoothAddress());
		devs.add(ZephyrOpen.deviceName, local.getFriendlyName());
		
		RemoteDevice target = null;
		Enumeration<RemoteDevice> list = results.elements();
		while (list.hasMoreElements()) {

			target = list.nextElement();
			try {

				devs.add(target.getFriendlyName(false), target.getBluetoothAddress());

			} catch (Exception e) {
				constants.error(e.getMessage());
			}
		}
		
		// share to group
		devs.send();
		constants.info( devs.toXML(), this);
	}
	
	/* **/
	public static void sendStatus(String string) {
		Command status = new Command();
		status.add(ZephyrOpen.status, string);
		status.add(ZephyrOpen.action, ZephyrOpen.status);
		status.send();
	} 
	
	/** test for this class -- no args needed */
	public static void main(String[] args) {
		
		constants.init();
			
		Discovery discovery = new Discovery();
		for( int i = 1 ;; i++ ){
		
			constants.info(" +++ " + discovery.getLocalName() + " is searching for devices.. ");
			sendStatus(discovery.getLocalName() + " is searching for devices.. ");
			Long start = System.currentTimeMillis();
			
			discovery.sendXML();
			
			constants.info(" --- search took " + (System.currentTimeMillis() - start)/1000 + " seconds");
			constants.info( "sleeping " + i);
			
			sendStatus("search took " + (System.currentTimeMillis() - start)/1000 + " seconds");
			Utils.delay(ZephyrOpen.ONE_MINUTE);
			
		}
	}
}