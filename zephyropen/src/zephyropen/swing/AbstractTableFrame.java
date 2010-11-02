package zephyropen.swing;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import zephyropen.api.ZephyrOpen;

/**
 * <p> Create a table that displays HXM data 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class AbstractTableFrame extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	
	/**  Framework configuration */
	private ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** 10% grey background */
	private static final java.awt.Color BACK_GROUND_COLOR = java.awt.Color.decode("#dddddd");
	
	private JFrame frame = null;
	private String name = null;

   /**
    * <p> Put the HXM, HRM or Bioharness Table on a frame 
    * 
    * @param sheet is the spread sheet, or table to add to the panel 
    * @param status is the stats pane to add to the panel 
    * @param name 
    */
	public AbstractTableFrame(JScrollPane sheet, JScrollPane status, String name){ 

      super(new BorderLayout());

      // add Components to this panel.
      add(sheet, BorderLayout.NORTH ); 
      add(status, BorderLayout.PAGE_END);
      
      this.name = name;
   }

   /**
    * Create the GUI and show it.  For thread safety,
    * this method should be invoked from the
    * event-dispatching thread.
    */
   public void run() {

      //Make sure we have nice window decorations.
      JFrame.setDefaultLookAndFeelDecorated(true);

      //Create and set up the window with params title
      frame = new JFrame( "ZephyrOpen Spread Sheet SpreadSheet [" + name 
    		  + "] version [" + constants.get(ZephyrOpen.frameworkVersion)+"]");
      
      // close on gui exit
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //Create and set up the content pane.
      JComponent newContentPane = this;

      //content panes must be opaque
      newContentPane.setOpaque(true);
      frame.setContentPane(newContentPane);
      
      /** set color */ 
      frame.setBackground( BACK_GROUND_COLOR ); 
		
      //Display the window.
      frame.pack();
      frame.setVisible(true);
      
   }
}
