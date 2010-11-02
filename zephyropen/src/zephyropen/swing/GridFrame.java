package zephyropen.swing;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import zephyropen.util.google.GoogleChart;

/**
 * <p> Create a table that displays HXM data as line Graphs  
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class GridFrame extends AbstractFrame { 

	private static final long serialVersionUID = 1L;
	
	protected GoogleChart[] components = null;
	
	public GridFrame( GoogleChart[] parts ) { 
		   
		this.setLayout(new GridLayout(parts.length, 1));
		
		components = parts;
	
		// add components to the frame 
		for( int i = 0 ; i < components.length ; i++ ) {
			
			// center the image 
			((JLabel) components[i]).setHorizontalAlignment(SwingConstants.CENTER);
			
			// set the size needed for the image 
			components[i].setPreferredSize(	
					new Dimension(GoogleChart.DEFAULT_X_SIZE, GoogleChart.DEFAULT_Y_SIZE));
			
			// add to this frame 
			add( components[i] );
		}
	}
		
	@Override
	public void updateSelected() {
		for (int i = 0; i < components.length; i++)
			components[i].updateIcon(); // frame.getWidth(), frame.getHeight());
		
		// make sure of the size 
		frame.pack();
	}
}
