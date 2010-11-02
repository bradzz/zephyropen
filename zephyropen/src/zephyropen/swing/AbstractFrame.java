package zephyropen.swing;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import zephyropen.api.ZephyrOpen;

/**
 * <p> Open a SWING based graphing display for the HxM device. 
 * <p> Package : Created: Dec 30, 2008
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
@SuppressWarnings("serial")
public abstract class AbstractFrame extends JPanel implements Runnable {
	
	/** 10% grey background */
	public static final java.awt.Color BACK_GROUND_COLOR = java.awt.Color.decode("#dddddd");
	
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** create and set up the window with start up title */
	protected JFrame frame = new JFrame(ZephyrOpen.zephyropen);
	
	protected String title = null;
	
	/** Create the GUI and show it. */
	public void run() {
		
		/** make sure we have nice window decorations. */
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		/** set color */ 
		frame.setBackground( BACK_GROUND_COLOR ); 

		/** optionally float on top of all other windows */
		if (constants.getBoolean("onTop")) frame.setAlwaysOnTop(true);

		/** close on gui exit */ 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/** Turn off metal's use of bold fonts */ 
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		
		/** create and set up the content pane, content panes must be opaque */ 
		this.setOpaque(true);
		frame.setContentPane(this);
		
		/** display the window */
		frame.pack();
		frame.setVisible(true);
	}

	public void setTitle(String title) {
		if( frame != null ) frame.setTitle(title); 
	}
	
	public abstract void updateSelected();
	

}
	
