package zephyropen.swing.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;

import gnu.io.*;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.port.bluetooth.Discovery;
import zephyropen.util.Loader;

/**
 * Open a basic SWING based tool to manage connections and views
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class ControlGUI extends JPanel implements Runnable {

	/** swing needs it */
	private static final long serialVersionUID = 1L;

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

	/** file name to hold search results */
	static final String LAUNCH_FILE_NAME = "launch.properties";

	/** size of GUI window */
	static final int XSIZE = 380;
	static final int YSIZE = 200;

	private static final Object selected = "selected";

	/** mutex to ensure one search thread at a time */
	static Boolean searching = false;
	
	/** store past searches */
	Properties found = new Properties();

	final String path = constants.get(ZephyrOpen.root) + ZephyrOpen.fs + "search.properties";
	
	/** limit event rate */
	long last = System.currentTimeMillis();
	
	/** create and set up the window with start up title */
	JFrame frame = new JFrame(ZephyrOpen.zephyropen + " v" + ZephyrOpen.VERSION);

	/** write status to label */
	JLabel status = new JLabel("ready", JLabel.LEFT);

	/** choose from discovered devices */
	JComboBox deviceList = new JComboBox();

	/** choose from discovered devices */
	JComboBox portList = new JComboBox();

	/** choose from discovered devices */
	JComboBox userList = new JComboBox();

	/** search event timer */
	java.util.Timer timer = new java.util.Timer();
	
	/** Add items with icons each to each menu item */
	JMenuItem killItem = new JMenuItem("kill all");
	JMenuItem closeSeverItem = new JMenuItem("close servers");
	JMenuItem serverItem = new JMenuItem("connect");
	JMenuItem newUserItem = new JMenuItem("new user");
	JMenuItem killDeviceItem = new JMenuItem("close device");
	JMenuItem debugOnItem = new JMenuItem("debug ON");
	JMenuItem debugOffItem = new JMenuItem("debug OFF");
	JMenuItem searchItem = new JMenuItem("bluetooth search");
	JMenuItem stopSearchItem = new JMenuItem("stop search");
	JMenuItem viewerItem = new JMenuItem("viewer");
	JMenuItem testerItem = new JMenuItem("test pattern");

	JMenu userMenue = new JMenu("User");
	JMenu device = new JMenu("Device");

	/** add devices that require com port mapping, not searching */
	private void initDevices() {
		addDevice(PrototypeFactory.polar);
		addDevice(PrototypeFactory.elevation);
		
		if(new File(path).exists()){
			try {
				
				File file = new File(path);
				FileInputStream fi = new FileInputStream(file);
				found.load(fi);
				fi.close();
				
				Enumeration<Object> keys = found.keys();
				while(keys.hasMoreElements()){
					String dev = (String) keys.nextElement();
					addDevice(dev);
				}
				
				System.out.println("found " + found.getProperty((String) selected));
				deviceList.setSelectedItem(found.get(selected));
				
			} catch (Exception e) {
				constants.error(e.getMessage(), this);
			}
		}
	}
	
	public void writeFound(){
		try {
			
			// write to search props 
			FileWriter fw = new FileWriter(new File(path));
			found.put(selected, deviceList.getSelectedItem());
			found.store(fw, "comments");
			fw.close();
			
		} catch (Exception e) {
			constants.error(e.getMessage(), this);
		}
	}

	/** get list of ports available on this particular computer */
	private void initPorts() {
		new Thread() {
			@Override
			public void run() {
				@SuppressWarnings("rawtypes")
				Enumeration pList = CommPortIdentifier.getPortIdentifiers();
				while (pList.hasMoreElements()) {
					CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
					if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL)
						if (!portExists(cpi.getName()))
							portList.addItem(cpi.getName());
				}
			}
		}.start();
	}

	/** get list of users for the directory structure */
	private void initUsers() {

		String[] users = new File(constants.get(ZephyrOpen.root)).list();
		for (int i = 0; i < users.length; i++)
			if (new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + users[i]).isDirectory())
				if (!users[i].equals(ZephyrOpen.zephyropen))
					addUser(users[i]);

		if (userList.getItemCount() == 0) {
			addUser("brad");
		}

		/** re-draw list */
		userList.repaint();
	}

	private void addUser(String usr) {

		usr = usr.trim();
		if (!userExists(usr)) {

			// constants.info("adding user: " + usr, this);

			userList.addItem(usr);

			// create directory
			new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + usr).mkdirs();
		}
	}

	private boolean addDevice(String dev) {
		if (PrototypeFactory.getDeviceTypeString(dev) == ZephyrOpen.zephyropen)
			return false;

		if (deviceExists(dev))
			return false;

		deviceList.addItem(dev.trim());
		return true;
	}

	private boolean deviceExists(String device) {
		device = device.trim();
		for (int i = 0; i < deviceList.getItemCount(); i++)
			if (deviceList.getItemAt(i).equals(device))
				return true;

		return false;
	}

	private boolean userExists(String user) {
		user = user.trim();
		for (int i = 0; i < userList.getItemCount(); i++)
			if (userList.getItemAt(i).equals(user))
				return true;

		return false;
	}

	private boolean portExists(String port) {
		port = port.trim();
		for (int i = 0; i < portList.getItemCount(); i++)
			if (portList.getItemAt(i).equals(port))
				return true;

		return false;
	}

	/** Look for BT devices, blocking call, hold GUI captive */
	public void search() {
		if (bluetoothEnabled()) {
			new Thread() {
				@Override
				public void run() {
					synchronized (searching) {
						if (searching) {
							status.setText("radio is busy");
							return;
						}

						searching = true;
						status.setText("bluetooth searching...");
						Vector<RemoteDevice> bluetoothDevices = new Discovery().getDevices();

						// if new device, add to the list
						if (!bluetoothDevices.isEmpty()) {
							Enumeration<RemoteDevice> list = bluetoothDevices.elements();
							while (list.hasMoreElements()) {
								RemoteDevice target = list.nextElement();
								try {
									
									addDevice(target.getFriendlyName(false));
									found.put(target.getFriendlyName(false), target.getBluetoothAddress());
									
								} catch (Exception e) {
									constants.error(e.getMessage(), this);
								}
							}
						}
						
						writeFound();
						
						/*
						try {
							
							// write to search props 
							FileWriter fw = new FileWriter(file);
							found.put(selected, deviceList.getSelectedItem());
							found.store(fw, "comments");
							fw.close();
							
						} catch (Exception e) {
							constants.error(e.getMessage(), this);
						}
						*/
						
						searching = false;
						status.setText("search complete");
					}
				}
			}.start();
		}
	}

	/** Listen for menu events and send XML messages or Launch new proc */
	private final ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {

			long delta = System.currentTimeMillis() - last;
			if( delta < 1000 ){
				// System.out.println("too fast:" + delta);
				return;
			}
			
			Object source = event.getSource();
			if (source.equals(searchItem)) {

				/** update on timer */
				timer.scheduleAtFixedRate(new RefreshTask(), 0, ZephyrOpen.TWO_MINUTES);
				device.add(stopSearchItem);
				device.remove(searchItem);
				
			} else if(source.equals(stopSearchItem)){

				/** turn off timer */
				timer.cancel();
				device.add(searchItem);
				device.remove(stopSearchItem);
				
			} else if (source.equals(closeSeverItem)) {
		
				constants.closeServers();

			} else if (source.equals(newUserItem)) {

				userList.setEditable(true);

			} else if (source.equals(killItem)) {

				constants.shutdownFramework();

			} else if (source.equals(killDeviceItem)) {

				constants.killDevice((String) deviceList.getSelectedItem(), (String) userList.getSelectedItem());

			} else if (source.equals(debugOffItem) || source.equals(debugOnItem)) {

				/** build framework command */
				Command command = new Command();
				command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);
				command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);
				if (source.equals(debugOnItem))
					command.add("value", true);
				else
					command.add("value", false);

				command.send();

			} else if (source.equals(viewerItem)) {
				if (createLaunch())
					new Loader("zephyropen.swing.gui.viewer.DeviceViewer",
							(String) userList.getSelectedItem()
							+ " " + (String) deviceList.getSelectedItem());

			} else if (source.equals(serverItem)) {
				if (createLaunch())
					new Loader("zephyropen.device.DeviceServer",
							(String) userList.getSelectedItem()
							+ " " + (String) deviceList.getSelectedItem());

			} else if (source.equals(testerItem)) {
				if (createLaunch())
					new Loader("zephyropen.device.DeviceTester",
							(String) userList.getSelectedItem() 
							+ " " + (String) deviceList.getSelectedItem());
			}
			
			// don't let event be handled too often 
			last = System.currentTimeMillis();
		}
	};

	/** create a luanch file for this user and device */
	private boolean createLaunch() {

		Properties userProps = new Properties();
		String dev = null;
		String usr = null;
		String port = null;

		// get gui values
		dev = (String) deviceList.getSelectedItem();
		usr = (String) userList.getSelectedItem();
		port = (String) portList.getSelectedItem();

		if (dev == null)
			return false;
		if (usr == null)
			return false;

		// user/deviceName.prperties
		File propFile = new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs
				+ userList.getSelectedItem() + ZephyrOpen.fs + dev + ".properties");

		if (propFile.exists()) {
			try {

				FileInputStream fi = new FileInputStream(propFile);
				userProps.load(fi);
				fi.close();

			} catch (Exception e) {
				constants.error(e.getMessage(), this);
				return false;
			}
		}


			
			/*
		FileWriter f = new FileWriter("");
		found.setProperty(selected, deviceList.getSelectedItem().toString());
		found.store(f, new Date().toString());
		f.close();
		}catch(Exception e){
			constants.error(e.getMessage(), this);
		}
		*/
			
		
		writeFound();
		
		//.get(selected)
		// constants.info("creating launch file: " + propFile.getPath(), this);

		// overwrite if existed
		userProps.put(ZephyrOpen.user, usr);
		userProps.put(ZephyrOpen.deviceName, dev);
		if (port != null)
			userProps.put(ZephyrOpen.com, port);

		try {

			// write to file
			FileWriter fw = new FileWriter(propFile);
			userProps.store(fw, new Date().toString());
			fw.close();

		} catch (Exception e) {
			constants.error(e.getMessage(), this);
			return false;
		}

		return true;
	}

	// user drop box changed
	class UserListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {

			if (userList.getSelectedItem() == null)
				return;

			// constants.info(" user :: " + userList.getSelectedItem(), this);

			if (userList.getSelectedItem() == null) {
				initUsers();
				return;
			}

			if (userList.isEditable()) {
				userList.setEditable(false);

				String newUsr = (String) userList.getSelectedItem();
				addUser(newUsr.trim());
			}
		}
	}

	// device drop box changed
	/*
	class DeviceListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {

			if (deviceList.getSelectedItem() == null) return;

			constants.info(" device :: " + deviceList.getSelectedItem(), this);

			if (((String) deviceList.getSelectedItem()).equalsIgnoreCase(PrototypeFactory.polar)) {

				constants.info(" device is polar, search", this);
				
				synchronized (searching) {
					if (searching) {
						searching = true;
						status.setText("device is busy");
						return;
					}
				}
				
				new Thread() {
					@Override
					public void run() {
						String port = null;
						try {
							port = new Find().search();
						} catch (Exception e) {
							constants.error("DeviceListener :: " + e.getMessage(), this);
						}
						if (port != null) {
							portList.removeAllItems();
			 				portList.addItem(port);
							status.setText("found polar usb device");
						}
					}
				}.start();
				
				searching = false;
			
			//}
		}
	}*/

	/** add the menu items to the frame */
	public void addMenu() {

		/* listen for users */
		userList.addItemListener(new UserListener());
		
		// deviceList.addItemListener(new DeviceListener());

		/* Add the lit to each menu item */
		viewerItem.addActionListener(listener);
		newUserItem.addActionListener(listener);
		killItem.addActionListener(listener);
		killDeviceItem.addActionListener(listener);
		serverItem.addActionListener(listener);
		debugOnItem.addActionListener(listener);
		debugOffItem.addActionListener(listener);
		testerItem.addActionListener(listener);
		closeSeverItem.addActionListener(listener);
		serverItem.addActionListener(listener);
		searchItem.addActionListener(listener);
		stopSearchItem.addActionListener(listener);

		device.add(testerItem);
		device.add(debugOnItem);
		device.add(debugOffItem);
		device.add(closeSeverItem);
		device.add(killDeviceItem);
		device.add(killItem);

		if (bluetoothEnabled())
			device.add(searchItem);

		userMenue.add(viewerItem);
		userMenue.add(serverItem);
		userMenue.add(newUserItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(device);
		frame.setJMenuBar(menuBar);
	}

	/* */
	public boolean bluetoothEnabled() {

		LocalDevice local = null;

		try {

			local = LocalDevice.getLocalDevice();

		} catch (BluetoothStateException e) {
			constants.error(e.getMessage(), this);
			status.setText(e.getMessage());
			return false;
		}

		// radio better be on
		if (local == null) {

			status.setText("Blue Tooth Radio is not available");
			return false;

		} else if (!LocalDevice.isPowerOn()) {

			status.setText("Blue Tooth Radio is not powered");
			return false;
		}

		return true;
	}

	/** Construct a frame for the GUI and call swing */
	public ControlGUI() {

		/** configuration to ignore kill commands */
		constants.put(ZephyrOpen.loggingEnabled, true);
		constants.put(ZephyrOpen.frameworkDebug, true);
		// zephyropen.api.ApiFactory.getReference().remove(ZephyrOpen.zephyropen);
		constants.init();
		constants.lock();

		/** find devices for prop files */
		initDevices();
		initPorts();
		initUsers();
		userList.addItemListener(new UserListener());

		/** add to panel */
		setLayout(new SpringLayout());

		add(new JLabel("device type"));
		add(deviceList);

		add(new JLabel("user name"));
		add(userList);

		add(new JLabel("comm port"));
		add(portList);

		add(new JLabel("status"));
		add(status);

		SpringUtilities.makeCompactGrid(this, 4, 2, 4, 2, 5, 5);

		/** add menu */
		addMenu();

		/** show window */
		javax.swing.SwingUtilities.invokeLater(this);
	}

	/** Create the GUI and show it. */
	@Override
	public void run() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		// UIManager.put("swing.boldMetal", Boolean.FALSE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(XSIZE, YSIZE));
		frame.setContentPane(this);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		frame.pack();
		setOpaque(true);
	}

	private class RefreshTask extends TimerTask {
		@Override
		public void run() {

			// initPorts();

			search();
		}
	}

	/** Launch the search GUI -- no args needed */
	public static void main(String[] args) {
		new ControlGUI();
	}
}