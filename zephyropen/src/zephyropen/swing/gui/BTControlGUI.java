package zephyropen.swing.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;
import javax.comm.CommPortIdentifier;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.port.bluetooth.Discovery;

/**
 * Open a basic SWING based tool to manage connections and views
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class BTControlGUI extends JPanel implements Runnable {

	/** swing needs it */
	private static final long serialVersionUID = 1L;

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

	/** file name to hold search results */
	static final String SEARCH_FILE_NAME = "search.properties";

	/** file name to hold search results */
	static final String LAUNCH_FILE_NAME = "launch.properties";

	/** size of GUI window */
	static final int XSIZE = 270;

	static final int YSIZE = 150;

	/** create and set up the window with start up title */
	JFrame frame = new JFrame(ZephyrOpen.zephyropen + " v" + ZephyrOpen.VERSION);

	/** choose from discovered devices */
	JComboBox deviceList = new JComboBox();

	/** choose from discovered devices */
	JComboBox portList = new JComboBox();

	/** object to hold found in BT Search */
	Properties deviceProps = new Properties();

	/** choose from discovered devices */
	JComboBox userList = new JComboBox();

	/** re-use command */
	Command command = null;

	/** get back a hash of device */
	Vector<RemoteDevice> results = null;

	/** Add items with icons each to each menu item */
	JMenuItem killItem = new JMenuItem("Kill All");

	JMenuItem serverItem = new JMenuItem("Connect to Device", null);

	JMenuItem editUserItem = new JMenuItem("Edit User");

	JMenuItem killDeviceItem = new JMenuItem("Close device");

	JMenuItem debugOnItem = new JMenuItem("Debug ON");

	JMenuItem debugOffItem = new JMenuItem("Debug OFF");

	JMenuItem searchItem = new JMenuItem("Search");

	JMenuItem viewerItem = new JMenuItem("Start Viewer");

	JMenu userMenue = new JMenu("User");

	/** Try to save searching, use last search results */
	private void initDevices() {

		deviceProps.clear();

		try {

			FileInputStream file = new FileInputStream(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + SEARCH_FILE_NAME);
			deviceProps.load(file);
			file.close();

		} catch (Exception ex) {
			constants.error("no properties found : " + SEARCH_FILE_NAME, this);
		}

		// hard wired
		addDevice(PrototypeFactory.polar);

		/** Add devices found in the search results */
		Enumeration<Object> e = deviceProps.keys();
		while (e.hasMoreElements())
			addDevice((String) e.nextElement());

		/** re-draw list */
		deviceList.repaint();
	}

	/** get list of ports available on this particular computer */
	private void initPorts() {
		
		// clean start
		portList.removeAllItems();
		
		// add all avail 
		@SuppressWarnings("rawtypes")
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();
		while (pList.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				//if(!cpi.isCurrentlyOwned())
					portList.addItem(cpi.getName());
			}
		}	
		
		// let user choose 
		portList.setEnabled(true);
		portList.repaint();
	}
	
	/**  */
	private void updatePorts(){
		
		String dev = (String) deviceList.getSelectedItem();
		
		if(dev == null)	return;	
	
		if(dev.equals(PrototypeFactory.polar)){
			initPorts();
			return;
		}
		
		// try to match port with device  
		String addr = (String) deviceProps.getProperty((String) deviceList.getSelectedItem());
		if( addr != null ){
			portList.addItem(addr);
			portList.setSelectedItem(addr);
			portList.setEnabled(false);
		}
	
		/** re-draw list */
		portList.repaint();
	}

	/** get list of users for the directory structure */
	private void initUsers() {

		// start clean 
		userList.removeAllItems();

		String[] users = new File(constants.get(ZephyrOpen.root)).list();
		for (int i = 0; i < users.length; i++)
			if (new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + users[i]).isDirectory())
				if (!users[i].equals(ZephyrOpen.zephyropen))
					addUser(users[i]);
		
		if(userList.getItemCount() == 0){
			addUser("default");
		}

		/** re-draw list */
		userList.repaint();
	}

	/** Look for BT devices, blocking call, hold GUI captive */
	public void search() {

		frame.setTitle("searching...");

		// clean slate if found new
		deviceList.removeAllItems();
		addDevice(PrototypeFactory.polar);
		
		// blocking call, wipe devices not found this time
		results = new Discovery().getDevices();
		if (!results.isEmpty()) {

			Enumeration<RemoteDevice> list = results.elements();
			while (list.hasMoreElements()) {
				RemoteDevice target = list.nextElement();
				try {

					if(addDevice(target.getFriendlyName(false)))
						deviceProps.put(target.getFriendlyName(false),
							target.getBluetoothAddress());
					

				} catch (Exception e) {
					frame.setTitle("search error");
					constants.info(e.getMessage(), this);
				}
			}
		}

		// show result
		saveSearchProps();
		frame.setTitle("found [" + deviceList.getItemCount() + "] devices");
	}

	private void addUser(String usr) {
		
		// create directory 
		new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + usr).mkdirs();
		
		if (!userExists(usr)){
		
			userList.addItem(usr);
		
		}
	}

	private boolean addDevice(String dev) {
		if (PrototypeFactory.getDeviceTypeString(dev) == ZephyrOpen.zephyropen)
			return false;

		if (deviceExists(dev))
			return false;

		deviceList.addItem(dev);
		return true;
	}

	private boolean deviceExists(String device) {
		for (int i = 0; i < deviceList.getItemCount(); i++)
			if (deviceList.getItemAt(i).equals(device))
				return true;

		return false;
	}

	private boolean userExists(String user) {
		for (int i = 0; i < userList.getItemCount(); i++)
			if (userList.getItemAt(i).equals(user))
				return true;

		return false;
	}

	/** write the search device props file to disk */
	private void saveSearchProps() {

		FileOutputStream out = null;

		/** save back to disk */
		try {

			out = new FileOutputStream(constants.get(ZephyrOpen.root)
					+ ZephyrOpen.fs + SEARCH_FILE_NAME);

			deviceProps.store(out, this.getClass().getName());
		} catch (Exception e) {
			constants.error(e.getMessage(), this);
		}
	}

	/** Listen for menu events and send XML messages */
	private final ActionListener listen = new ActionListener() {
		public void actionPerformed(ActionEvent event) {

			/** get item source */
			Object source = event.getSource();

			if (source.equals(searchItem)) {

				search();

			} else if (source.equals(editUserItem)) {

				userList.setEditable(true);

			} else if (source.equals(killItem)) {

				// send command to kill group
				constants.shutdownFramework();

			} else if (source.equals(debugOffItem)
					|| source.equals(debugOnItem)) {

				/** framework command */
				command = new Command();
				command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);

				if (source.equals(debugOnItem))
					command.add(ZephyrOpen.value, ZephyrOpen.enable);
				else
					command.add(ZephyrOpen.value, ZephyrOpen.disable);

				command.send();
				return;

			} else if (source.equals(viewerItem)) {

				createLaunch();
	
				new Loader(
						"\"./bin;./comm.jar;./lib/bluecove.jar;./lib/charts4j.jar;./lib/BrowserLauncher.jar\"",
						"zephyropen.swing.gui.viewer.DeviceViewer",
						(String) userList.getSelectedItem());
						
					

			} else if (source.equals(killDeviceItem)) {
				
				String device = (String) deviceList.getSelectedItem();
				if( device != null )
					constants.killDevice(device);

			} else if (source.equals(serverItem)) {

				createLaunch();

				/**/
				new Loader("\"./bin;./comm.jar;./lib/bluecove.jar\"",
						"zephyropen.device.DeviceServer",
						(String) userList.getSelectedItem());
						
			}
		}
	};

	/**
	 * 
	 * 
	 */
	private boolean createLaunch() {

		Properties userProps = new Properties();

		// get gui values
		String dev = (String) deviceList.getSelectedItem();
		String usr = (String) userList.getSelectedItem();
		String port = (String) portList.getSelectedItem();

		if (dev == null) return false;
		if (usr == null) return false;
		if (port == null) return false;
		
		// user/deviceName.prperties
		File propFile = new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs
				+ userList.getSelectedItem() + ZephyrOpen.fs + LAUNCH_FILE_NAME);
		
		if(propFile.exists()){
			try{ 
				
				FileInputStream fi = new FileInputStream(propFile);
				userProps.load(fi);
				fi.close();
				
			} catch (Exception e) {
				constants.error(e.getMessage(), this);
				return false;
			}
		}
		
		// create directory if not there 
		//(new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs
			//	+ userList.getSelectedItem())).mkdirs();
		
		constants.info("creating launch file: " + propFile.getPath(), this);

		// overwrite if existed 
		userProps.put(ZephyrOpen.userName, usr);
		userProps.put(ZephyrOpen.deviceName, dev);
		userProps.put(ZephyrOpen.com, port);
		
		try {
			
			// write to file 
			FileWriter fw = new FileWriter(propFile);
			userProps.store(fw, null);
			fw.close();
			
		} catch (Exception e) {
			constants.error(e.getMessage(), this);
			return false;
		}
		
		return true;
	}

	// device drop down box changed
	class DeviceListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			updatePorts();
		}
	}

	// user drop box changed
	class UserListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			
			// if( userList.getSelectedItem() == null ) return;

			constants.info(" user :: " + userList.getSelectedItem(), this);

			if (userList.getSelectedItem() == null){
				initUsers();
				userList.setSelectedIndex(0);
				return;
			}
			
			if (userList.isEditable()) {
				userList.setEditable(false);
				addUser((String) userList.getSelectedItem());
			}
		}
	}

	// port drop box changed
	/*
	class PortListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {

			constants.info(" port :: " + portList.getSelectedItem(), this);

			if (portList.getSelectedItem() == null)
				return;

			// saveUser();
			// createLaunch();
		}
	}
	 */
	
	/** add the menu items to the frame */
	public void addMenu() {

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();

		/** Build the menu items */
		// JMenu user = new JMenu("User");
		JMenu device = new JMenu("Device");
		JMenu debug = new JMenu("Debug");

		deviceList.addItemListener(new DeviceListener());
		userList.addItemListener(new UserListener());
		
		// portList.addItemListener(new PortListener());

		/** Add the lit to each menu item */
		viewerItem.addActionListener(listen);
		editUserItem.addActionListener(listen);
		// newUserItem.addActionListener(listen);
		searchItem.addActionListener(listen);
		killItem.addActionListener(listen);
		killDeviceItem.addActionListener(listen);
		serverItem.addActionListener(listen);
		debugOnItem.addActionListener(listen);
		debugOffItem.addActionListener(listen);

		/** add the sub menu items to their respective menus */
		userMenue.add(viewerItem);
		userMenue.add(editUserItem);
		// user.add(newUserItem);

		device.add(serverItem);
		device.add(killDeviceItem);
		device.add(searchItem);

		debug.add(debugOnItem);
		debug.add(debugOffItem);
		debug.add(killItem);

		/** set the Frames JMenuBar */
		menuBar.add(userMenue);
		menuBar.add(device);
		menuBar.add(debug);
		frame.setJMenuBar(menuBar);
	}

	/** Construct a frame for the GUI and call swing */
	public BTControlGUI() {

		constants.init(); 

		constants.lock();

		/** find devices for prop files */
		initDevices();
		initPorts();
		initUsers();

		/** add to grid */
		this.setLayout(new GridLayout(4, 1, 5, 5));
		this.add(deviceList);
		this.add(userList);
		this.add(portList);

		/** TODO: nag or put an add here, load image */
		JLabel copy = new JLabel(
				"<html><font color=\"#0e1f5b\"> &#169; 2009 Brad Zdanivsky</font>");
		copy.setHorizontalAlignment(JLabel.CENTER);
		this.add(copy);

		/** add menu */
		addMenu();

		/** show window */
		javax.swing.SwingUtilities.invokeLater(this);
	}

	/** Create the GUI and show it. */
	public void run() {

		/** make sure we have nice window decorations. */
		JFrame.setDefaultLookAndFeelDecorated(true);

		/** Turn off metal's use of bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		/** float on top of all other windows */
		frame.setAlwaysOnTop(true);

		/** close on gui exit */
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/** create and set up the content pane, content panes must be opaque */
		this.setOpaque(true);
		frame.setContentPane(this);

		/** set min size */
		frame.setPreferredSize(new Dimension(XSIZE, YSIZE));

		/** display the window */
		frame.pack();
		frame.setVisible(true);
	}

	/** Launch the search GUI -- no args needed */
	public static void main(String[] args) {
		new ControlGUI();
	}
}
