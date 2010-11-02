package zephyropen.swing;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;

/**
 * <p> Create a JTable from a device name, and update as commands are available 
 *
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class AbstractTable implements API {
	
	private static final long serialVersionUID = 1L;
	
	/** framework configuration */ 
	protected ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** table configuration */ 
	protected static Object[][] data = null;
	protected static String[] columnNames = null; 
	protected static DefaultTableModel model = null; 
	
	/** table to hold parsed values */
	protected JTable table = null;
	
	/** how many records to hold in the model at one time */
	protected int records = 15;
	private String address = "not found";
	private long last;
	
	/** BlueTooth device name and packet values to be parsed out */
	protected String deviceName = null; 
	
	/**
	 * Create a table using the device name 
	 * @param name is the HxM device name 
	 */
	public AbstractTable(String name) {
	
		deviceName = name; 
		
		/** create a table view with these rows */ 
		columnNames = PrototypeFactory.create(deviceName);
		model = new DefaultTableModel(data,columnNames);
		table = new JTable(model);
		
		/** register for commands */
		ApiFactory.getReference().add(/*deviceName,*/ this);
		
		/** over write defaults if in props file */
		int rec = constants.getInteger(ZephyrOpen.displayRecords);
		if( rec >= 5 ) records = rec;
		
		last = System.currentTimeMillis();
	}

	/** @return the table that is now accepting xml commands for display */
	public Component getComponent(){
		return table;
	}
	
	/** Add new data to the bottom, of the text are */
	public void execute(Command command) {

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
		
		last = System.currentTimeMillis();
	}

	/** */	
	public String getDeviceName() {
		return deviceName;
	}
	
	/** */
	public String getAddress() {
		return address;
	}

	/** */
	public long getDelta() {
		return System.currentTimeMillis() - last;
	}
}
