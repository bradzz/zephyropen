package zephyropen.swing.gui;

import java.awt.BorderLayout;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.util.Utils;

public class DiscoverySpreadSheet extends JFrame implements API {
	
	/** framework */ 
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	private static final long serialVersionUID = 1L;
	
	/** create and set up the window with start up title */
	private Object[][] data;
	private String[] columnNames;
	private JTable table;
	private DefaultTableModel model;
	
	private long lastMessage;
	private int records = 10;
	
	/** remove old ones */
	protected Timer killer = new Timer();
	private String deviceName;
	private String address;
	
	/**
	 * 
	 * 
	 * @param name  
	 * 
	 * 
	 */
	public DiscoverySpreadSheet() {

		super();
		/** create a table view with these rows */ 
		columnNames = PrototypeFactory.create(ZephyrOpen.discovery);
		
		// for( int i = 0 ; i < columnNames.length ; i++ ) System.out.println("["+i+"] " + columnNames[i]);
		
		setSize(500, 250);
		model = new DefaultTableModel(data,columnNames);
		table = new JTable(model);
		
		/** over write defaults if in props file */
		int rec = constants.getInteger(ZephyrOpen.displayRecords);
		if( rec >= records ) records = rec;

		JScrollPane scroll = new JScrollPane(table);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		/** register for messages */
		ApiFactory.getReference().add(this);
		lastMessage = System.currentTimeMillis();
			
		/*
		killer.schedule(new TimerTask(){
			public void run() {
				
				System.out.println("searching..");
				
				new Discovery().sendXML();
				
			}
		}, ZephyrOpen.ONE_MINUTE, ZephyrOpen.ONE_MINUTE);
	
	
	*/
		
		Utils.delay(Integer.MAX_VALUE);
	}

	/** */
	public void execute(Command command) {
		
		System.out.println("in: " + command.list());
		
		address = command.get(ZephyrOpen.address);
		deviceName = command.get(ZephyrOpen.deviceName);
		
		// parse command into table cells 
		Object[] newRow = new Object[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) 
			newRow[i] = command.get(columnNames[i]);
	
		// add to current model
		model.addRow(newRow);

		// manage size, push out oldest
		if (model.getRowCount() > records)
			model.removeRow(0);

		// simply re-draw the table
		table.repaint();
		
		// track input times
		lastMessage = System.currentTimeMillis();
	}

	public String getAddress() {
		return address;
	}

	public long getDelta() {
		return System.currentTimeMillis() - lastMessage;
	}

	public String getDeviceName() {
		return deviceName;
	}
	
	/** driver */
	public static void main() {
		
		constants.init();
		
		// must give a device name
		DiscoverySpreadSheet frame = new DiscoverySpreadSheet();
		
		/** make sure we have nice window decorations. */
		JFrame.setDefaultLookAndFeelDecorated(true);

		/** Turn off metal's use of bold fonts */ 
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		
		/** float on top of all other windows */
		// frame.setAlwaysOnTop(true);

		/** close on gui exit */ 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/** colour it */
		frame.setBackground(java.awt.Color.decode("#dddddd"));
		frame.setVisible(true);
		
	}

}
