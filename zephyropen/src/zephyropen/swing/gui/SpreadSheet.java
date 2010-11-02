package zephyropen.swing.gui;

import java.awt.BorderLayout;
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

/** */
public class SpreadSheet extends JFrame implements API {
	
	/** framework */ 
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	private static final long serialVersionUID = 1L;
	
	/** spread sheet model */
	private Object[][] data;
	private String[] columnNames;
	private JTable table;
	private DefaultTableModel model;
	
	// display config 
	private JScrollPane scroll;
	private String deviceName;
	private long lastMessage;
	private String address;
	private int records = 10;

	/**
	 * @param name  */
	public SpreadSheet(String name) {

		super();
		deviceName = name;
		
		setTitle("display: " + deviceName);
		setSize(320, 190);
		
		/** create a table view with these rows */ 
		columnNames = PrototypeFactory.create(deviceName);
		
		if( constants.getBoolean(ZephyrOpen.frameworkDebug))
			for( int i = 0 ; i < columnNames.length ; i++ ) 
				System.out.println("["+i+"] " + columnNames[i]);
		
		model = new DefaultTableModel(data,columnNames);
		table = new JTable(model);
		
		/** over write defaults if in props file */
		int rec = constants.getInteger(ZephyrOpen.displayRecords);
		if( rec >= records ) records = rec;

		scroll = new JScrollPane(table);
		scroll.setAutoscrolls(true);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		// getContentPane().add(new StatusTextArea(), BorderLayout.CENTER);
		
		/** register for messages */
		ApiFactory.getReference().add(this);
		lastMessage = System.currentTimeMillis();
	}

	/** */
	public void execute(Command command) {
		
		// System.out.println("in: " + command.list());
		
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
		
		// TODO: show newest row 
		// scroll.set
		
		
		
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
	public static void main(String[] args) {
		
		
		String name = args[0];
		
		if( name == null ) return;
		
		constants.init();
		
		// must give a device name
		SpreadSheet frame = new SpreadSheet(name);
		
		/** make sure we have nice window decorations. */
		JFrame.setDefaultLookAndFeelDecorated(true);

		/** Turn off metal's use of bold fonts */ 
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		
		/** float on top of all other windows */
		frame.setAlwaysOnTop(true);

		/** close on gui exit */ 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/** colour it */
		frame.setBackground(java.awt.Color.decode("#dddddd"));
		frame.setVisible(true);
		
	}


}
