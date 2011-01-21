package zephyropen.swing.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

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
public class BluetoothGUI extends JPanel implements Runnable {

	/** swing needs it */
	private static final long serialVersionUID = 1L;

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();

	/** file name to hold search results */
	static final String LAUNCH_FILE_NAME = "launch.properties";

	/** size of GUI window */
	static final int XSIZE = 230;
	static final int YSIZE = 130;

	/** mutex to ensure one search thread at a time */
	static Boolean searching = false;

	/** create and set up the window with start up title */
	JFrame frame = new JFrame("v" + ZephyrOpen.VERSION);

	/** choose from discovered devices */
	JComboBox deviceList = new JComboBox();

	/** choose from discovered devices */
	JComboBox userList = new JComboBox();

	/** Add items with icons each to each menu item */
	JMenuItem killItem = new JMenuItem("kill all");
	JMenuItem closeSeverItem = new JMenuItem("close servers");
	JMenuItem serverItem = new JMenuItem("connect");
	JMenuItem newUserItem = new JMenuItem("new user");
	JMenuItem killDeviceItem = new JMenuItem("close device");
	JMenuItem debugOnItem = new JMenuItem("debug ON");
	JMenuItem debugOffItem = new JMenuItem("debug OFF");
	JMenuItem searchItem = new JMenuItem("search");
	JMenuItem viewerItem = new JMenuItem("viewer");
	JMenuItem testerItem = new JMenuItem("test pattern");
	
	/** Main menu */
	JMenu userMenue = new JMenu("User");
	JMenu device = new JMenu("Device");

	/** get list of users for the directory structure */
	private void initUsers() {

		// start clean
		userList.removeAllItems();

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

	/** Look for BT devices, blocking call, hold GUI captive */
	public void search() {
		new Thread() {
			public void run() {
				synchronized (searching) {
					if (searching) {
						constants.info("search(), radio is busy", this);
						return;
					}
				}

				constants.info("searching...", this);

				searching = true;
				Vector<RemoteDevice> bluetoothDevices = new Discovery().getDevices();

				// if new device, add to the list
				if (!bluetoothDevices.isEmpty()) {
					Enumeration<RemoteDevice> list = bluetoothDevices.elements();
					while (list.hasMoreElements()) {
						RemoteDevice target = list.nextElement();
						try {
							addDevice((String) target.getFriendlyName(false));
						} catch (Exception e) {
							constants.error(e.getMessage(), this);
						}
					}
				}
				searching = false;
			}
		}.start();
	}

	/** Listen for menu events and send XML messages or Launch new proc */
	private final ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {

			Object source = event.getSource();
			if (source.equals(searchItem)) {
				
				/** update on timer */
				java.util.Timer timer = new java.util.Timer();
				timer.scheduleAtFixedRate(new RefreshTask(), 0, ZephyrOpen.FIVE_MINUTES);

			} else if (source.equals(newUserItem)) {

				userList.setEditable(true);

			} else if(source.equals(closeSeverItem)){
		
				constants.closeServers();
				
			} else if (source.equals(killItem)) {
		
				constants.shutdownFramework();

			} else if(source.equals(killDeviceItem)){
				
				constants.killDevice((String) deviceList.getSelectedItem(), (String) userList.getSelectedItem());
				
			} else if (source.equals(debugOffItem) || source.equals(debugOnItem)) {

				/** build framework command */
				Command command = new Command();
				command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);
				command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);
				if (source.equals(debugOnItem))
					command.add(ZephyrOpen.value, ZephyrOpen.enable);
				else
					command.add(ZephyrOpen.value, ZephyrOpen.disable);

				command.send();

			} else if (source.equals(viewerItem)) {
				if (createLaunch())
					new Loader("zephyropen.swing.gui.viewer.DeviceViewer",
							(String) userList.getSelectedItem());

			} else if (source.equals(serverItem)) {
				if (createLaunch())
					new Loader("zephyropen.device.DeviceServer",
							(String) userList.getSelectedItem());

			} else if (source.equals(testerItem)) {
				if (createLaunch())
					new Loader("zephyropen.device.DeviceTester",
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
		String dev = null;
		String usr = null;

		// get gui values
		dev = (String) deviceList.getSelectedItem();
		usr = (String) userList.getSelectedItem();

		if (dev == null)
			return false;
		if (usr == null)
			return false;

		// user/deviceName.prperties
		File propFile = new File(constants.get(ZephyrOpen.root) + ZephyrOpen.fs
				+ userList.getSelectedItem() + ZephyrOpen.fs + LAUNCH_FILE_NAME);

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

		// constants.info("creating launch file: " + propFile.getPath(), this);

		// overwrite if existed
		userProps.put(ZephyrOpen.userName, usr);
		userProps.put(ZephyrOpen.deviceName, dev);

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

	/** add the menu items to the frame */
	public void addMenu() {

		/** Add the lit to each menu item */
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
		
		if(bluetoothEnabled()){
			searchItem.addActionListener(listener);
			device.add(searchItem);
		} 
		
		device.add(testerItem);
		device.add(debugOnItem);
		device.add(debugOffItem);
		device.add(closeSeverItem);
		device.add(killDeviceItem);
		device.add(killItem);

		userMenue.add(viewerItem);
		userMenue.add(serverItem);
		userMenue.add(newUserItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(device);
		frame.setJMenuBar(menuBar);
	}

	public boolean bluetoothEnabled() {

		LocalDevice local = null;

		try {

			/** get local services */
			local = LocalDevice.getLocalDevice();

		} catch (BluetoothStateException e) {
			constants.error(e.getMessage(), this);
			return false;
		}

		/** radio better be on */
		if (local == null) {
			constants.error("Blue Tooth Radio is not ready, terminate!", this);
			return false;

		} else if (!LocalDevice.isPowerOn()) {
			constants.error("Blue Tooth Radio is not ready, terminate!", this);
			return false;
		}

		// constants.info("Blue Tooth Radio is configured", this);
		return true;
	}

	/** Construct a frame for the GUI and call swing */
	public BluetoothGUI() {

		/** configuration to ignore kill commands */
		constants.init();
		
		/** ignore framework */
		// constants.put(ZephyrOpen.frameworkDebug, "false");
		// ApiFactory.getReference().remove(ZephyrOpen.zephyropen);
		constants.lock();
		
		userList.addItemListener(new UserListener());
		initUsers();
		initDevices();
		
		/** add to grid */
		setLayout(new SpringLayout());
		
		add(new JLabel("device: ", JLabel.TRAILING));
		add(deviceList);
		
		add(new JLabel("name: ", JLabel.TRAILING));
		add(userList);
		
		//add(new JLabel("ports: ", JLabel.TRAILING));
		//add(portList);
		
		SpringUtilities.makeCompactGrid(this, 2, 2, 2, 2, 5, 5);
		/** add menu */
		addMenu();
		
		/** show window */
		javax.swing.SwingUtilities.invokeLater(this);		
	}

	/** add devices that require com port mapping, not searching */
	private void initDevices() {
		addDevice(PrototypeFactory.polar);
		addDevice(PrototypeFactory.elevation);
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

	private class RefreshTask extends TimerTask {
		@Override
		public void run() {
			search();
		}
	}

	/** Launch the search GUI -- no args needed */
	public static void main(String[] args) {
		new BluetoothGUI();
	}
}
