package zephyropen.swing.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.port.bluetooth.Discovery;

/**
 * Open a basic SWING based tool to manage connections and views
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class SearchGUI extends JPanel implements Runnable {

    /** swing needs it */
    private static final long serialVersionUID = 1L;

    /** framework configuration */
    static ZephyrOpen constants = ZephyrOpen.getReference();

    /** file name to hold search results */
    static final String FILE_NAME = "search.properties";

    /** size of GUI window */
    static final int XSIZE = 280;

    static final int YSIZE = 140;

    /** create and set up the window with start up title */
    JFrame frame = new JFrame(ZephyrOpen.zephyropen + " v" + ZephyrOpen.VERSION);

    /** choose from discovered devices */
    JComboBox deviceList = new JComboBox();

    /** object to hold found in BT Search */
    Properties props = new Properties();

    /** re-use command */
    Command command = null;

    /** get back a hash of device */
    Vector<RemoteDevice> results = null;

    /** Add items with icons each to each menu item */
    JMenuItem killItem = new JMenuItem("Kill All");

    JMenuItem serverItem = new JMenuItem("Connect to Device", new ImageIcon("images/Connect.png"));

    JMenuItem tableItem = new JMenuItem("SpreadSheet View");

    JMenuItem graphItem = new JMenuItem("Chart View");

    JMenuItem logItem = new JMenuItem("Start Log");

    JMenuItem killDeviceItem = new JMenuItem("Close device");

    JMenuItem debugOnItem = new JMenuItem("Debug ON");

    JMenuItem debugOffItem = new JMenuItem("Debug OFF");

    /** search button */
    JButton search = new JButton("Search For Devices", new ImageIcon("images/Bluetooth32.png"));

    /** Try to save searching, use last search results */
    private void initCombo() {

        try {

            props.load(new FileInputStream(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + FILE_NAME));

        } catch (Exception ex) {

            frame.setTitle("No Devices");
            constants.error("no properties found : " + FILE_NAME, this);
            return;
        }

        /** Add devices found in the search results */
        Enumeration<Object> e = props.keys();
        while (e.hasMoreElements()) {

            /*
             * deviceName=xxxxx will be in the prop file, and chosen as the default next
             * launch
             */
            String name = (String) e.nextElement();
            if (!name.equals(ZephyrOpen.deviceName))
                deviceList.addItem(name);
        }

        /** choose same as last time ? */
        String selected = props.getProperty(ZephyrOpen.deviceName);
        if (selected != null)
            deviceList.setSelectedItem(selected);

        /** let user select */
        deviceList.setEnabled(true);

        /** re-draw list */
        deviceList.repaint();
    }

    /** Look for BT devices, blocking call, hold GUI captive */
    public void search() {
        try {

            /** clean slate on each search */
            deviceList.removeAllItems();
            deviceList.setEnabled(false);
            frame.setTitle("searching...");

            results = new Discovery().getDevices();

            /** block on this call, wipe devices not found this time */
            if (results.size() >= 1) {

                Enumeration<RemoteDevice> list = results.elements();
                while (list.hasMoreElements()) {

                    RemoteDevice target = list.nextElement();
                    try {

                        deviceList.addItem(target.getFriendlyName(false));
                        props.put(target.getFriendlyName(false), target.getBluetoothAddress());

                    } catch (Exception e) {
                        constants.info(e.getMessage(), this);
                    }
                }
            }

            deviceList.setEnabled(true);
            frame.setTitle("Found " + deviceList.getItemCount());
            saveProps();

        } catch (Exception e) {
            constants.error("search(): blue tooth search error : " + e.getMessage(), this);
        }
    }

    /** write the search device props file to disk */
    private void saveProps() {

        String selected = null;
        FileOutputStream out = null;

        try {

            selected = (String) deviceList.getSelectedItem();
            out = new FileOutputStream(constants.get(ZephyrOpen.root) + ZephyrOpen.fs + FILE_NAME);

        } catch (Exception e) {
            e.printStackTrace();
        }

        /** save the selected one if found */
        if (selected == null)
            return;
        props.put(ZephyrOpen.deviceName, deviceList.getSelectedItem());

        /** save back to disk */
        try {
            props.store(out, " Search results created by : " + this.getClass().getName());
        } catch (Exception e) {
            constants.error(e.getMessage(), this);
        }
    }

    /** Listen for menu events and send XML messages */
    private final ActionListener listen = new ActionListener() {
        public void actionPerformed(ActionEvent e) {

            /** get item source */
            Object source = e.getSource();

            /** if search, save command building steps below */
            if (source.equals(search)) {
            	
                search();
                return;

            } else if (source.equals(killItem)) {

                /** send command to kill group */
                constants.shutdownFramework();
                return;

            } else if (source.equals(debugOffItem) || source.equals(debugOnItem)) {

                /** framework command */
                command = new Command();
                command.add(ZephyrOpen.action, ZephyrOpen.frameworkDebug);

                if (source.equals(debugOnItem))
                    command.add(ZephyrOpen.value, ZephyrOpen.enable);
                else
                    command.add(ZephyrOpen.value, ZephyrOpen.disable);

                command.send();
                return;

            } else {

                /** device is selected in check box */
                String device = (String) deviceList.getSelectedItem();
                if (device == null) {
                    return;
                } else {
                    command = new Command(ZephyrOpen.launch);
                    command.add(ZephyrOpen.deviceName, device);
                }

                /** send a kill to the current device only */
                if (source.equals(killDeviceItem)) {
                    constants.killDevice(device);
                    return;
                }

                /** address is in props file */
                String address = (String) props.get(device);
                if (address == null) {
                    return;
                } else {
                    command.add(ZephyrOpen.address, address);
                }

                if (source.equals(graphItem)) {

                    command.add(ZephyrOpen.kind, ZephyrOpen.graph);
                    command.add(ZephyrOpen.code, zephyropen.swing.gui.viewer.DeviceViewer.class.getName());

                } /* else if (source.equals(tableItem)) {

                    command.add(ZephyrOpen.kind, ZephyrOpen.table);
                    command.add(ZephyrOpen.code, zephyropen.swing.gui.viewer.DeviceViewer.class.getName());

                } */  else if (source.equals(serverItem)) {

                    command.add(ZephyrOpen.kind, ZephyrOpen.server);
                    command.add(ZephyrOpen.code, zephyropen.util.LoggerAPI.class.getName());

                } else if (source.equals(logItem)) {

                    command.add(ZephyrOpen.kind, ZephyrOpen.log);
                    command.add(ZephyrOpen.code, zephyropen.util.LoggerAPI.class.getName());
                }

                /** send this command */
                command.send();
            }
        }
    };

    /** add the menu items to the frame */
    public void addMenu() {

        /** Create the menu bar */
        JMenuBar menuBar = new JMenuBar();

        /** Build the menu items */
        JMenu device = new JMenu("Device");
        JMenu debug = new JMenu("Debug");

        /** Add the lit to each menu item */
        killItem.addActionListener(listen);
        killDeviceItem.addActionListener(listen);
        serverItem.addActionListener(listen);
        tableItem.addActionListener(listen);
        graphItem.addActionListener(listen);
        logItem.addActionListener(listen);
        debugOnItem.addActionListener(listen);
        debugOffItem.addActionListener(listen);

        /** add the sub menu items to their respective menus */
        device.add(serverItem);
        device.add(killDeviceItem);
        device.add(graphItem);
        device.add(tableItem);
        device.add(logItem);
        debug.add(debugOnItem);
        debug.add(debugOffItem);
        debug.add(killItem);

        /** set the Frames JMenuBar */
        menuBar.add(device);
        menuBar.add(debug);
        frame.setJMenuBar(menuBar);
    }

    /** Construct a frame for the GUI and call swing */
    public SearchGUI() {

        constants.init();
        this.setLayout(new BorderLayout());
        this.add(deviceList, BorderLayout.NORTH);

        search.setActionCommand("Bluetooth Search");
        search.setToolTipText("Begin Searching for BlueTooth Devices");
        search.addActionListener(listen);
        this.add(search, BorderLayout.CENTER);

        /** TODO: nag or put an add here, load image */
        JLabel copy = new JLabel("<html><font color=\"#0e1f5b\"> &#169; 2009 Brad Zdanivsky</font>");

        copy.setHorizontalAlignment(JLabel.CENTER);
        this.add(copy, BorderLayout.SOUTH);

        /** add menu */
        addMenu();

        /** avoid a search */
        initCombo();

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
        new SearchGUI();
    }
}
