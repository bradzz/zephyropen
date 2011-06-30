package zephyropen.util.google;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.state.TimedEntry;

/**
 * <p> A wrapper for the google RESTful graphing service. Add new data points, and 
 * create URL's for chart images. 
 * 
 * <p> Docs here: http://code.google.com/p/charts4j/
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public abstract class Chart extends JLabel {

	/** swing needs this */
	private static final long serialVersionUID = 1L;


	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	protected static ApiFactory apiFactory = ApiFactory.getReference();
	
	/** icon to put on the JLabel */
	protected ImageIcon icon = null;
	
	/** title information */
	protected String title = null;
	protected String units = null;
	
	/** need a state holding object */ 
	protected State state = null; 

	/** get the RESTful URL for the chart  */  
	public abstract String getURLString(final int x, final int y);
	public abstract String getURLString(final int x, final int y, String title);

	/** add new entry 
	public void add(String data) {
		if (data == null)
			return;
		if (data.equals(""))
			return;
		
		// add to state keeping object 
		state.add(new TimedEntry(data)); 
	}*/
	

	/** add new entry */
	public void add(String data, long time) {
		if (data == null)
			return;
		if (data.equals(""))
			return;
		
		// add to state keeping object 
		state.add(new TimedEntry(data, time)); 
	}
	
	/** re-draw the icon in this swing app */
	public void updateIcon(final int x, final int y){
		
		try {
		
			String str = getURLString(x, y);
			if(str!=null){
				
				icon = new ImageIcon(new URL(str)); 
			 
				// now set this image onto the JLable 
				if(icon != null) setIcon(icon);
			
			} // else icon = new ImageIcon("loading...");
			
		} catch (final Exception e) {	
			constants.error(e.getMessage(), this);
		} 
	}
	
	/** get the state object (data points) for this chart */
	public State getState(){
		return state;
	}
	
	/** @return the title for this graph */
	public String getTitle(){
		return title;
	}  
	
	/** @return the name of the state object for this graph */
	public String getName(){
		return state.toString();
	}  
	
	/** @return a URL String that google can turn into an image 
	public String toString() {	
		try {
			return getURLString(DEFAULT_X_SIZE, DEFAULT_Y_SIZE);
		} catch (final Exception e) {
			return null;
		}
	}
	*/
}