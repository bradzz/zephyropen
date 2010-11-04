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
public abstract class GoogleChart extends JLabel {

	/** swing needs this */
	private static final long serialVersionUID = 1L;

	/** image size defaults */
	public static int DEFAULT_X_SIZE = 600;
	public static int DEFAULT_Y_SIZE = 200;

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	protected static ApiFactory apiFactory = ApiFactory.getReference();
	
	/** icon to put on the JLabel */
	protected ImageIcon icon = new ImageIcon("loading...");
	
	/** title information */
	protected String title = null;
	protected String units = null;
	
	/** need a state holding object */ 
	protected State state = null; 

	/** get the RESTful URL for the chart  */ 
	public abstract String getURLString(); 
	public abstract String getURLString(final int x, final int y);
	
	/** add new entry */
	public void add(String data) {
		
		// constants.info(state.getTitle() + " : " + data);

		if (data == null)
			return;
		if (data.equals(""))
			return;
		
		// add to state keeping object 
		state.add(new TimedEntry(data));
		
		// constants.info(state.getTitle() + " : " + state.size()); 
	}
	
	/** @return a URL that google can turn into an image to place on SWNIG label */
	public URL getURL(){ 
		
		URL url = null;
		
		int x = constants.getInteger(ZephyrOpen.xSize);
		int y = constants.getInteger(ZephyrOpen.ySize);
		
		if( x == ERROR ) x = DEFAULT_X_SIZE;
		if( y == ERROR ) y = DEFAULT_Y_SIZE;
		
		try {
			url = new URL(getURLString(x,y));
		} catch (Exception e) {
			return null; 
		}
		
		return url;
	}
	
	/** @return a URL that google can turn into an image to place on SWNIG label */
	public URL getURL(final int x, final int y){
		
		URL url = null;
		
		try {
			url = new URL(getURLString(x,y));
		} catch (Exception e) {
			return null; 
		}
		
		return url;
	}
	
	/** re-draw the icon in this swing app */
	public void updateIcon(){
			
		int x = constants.getInteger(ZephyrOpen.xSize);
		int y = constants.getInteger(ZephyrOpen.ySize);
		
		if( x == ERROR ) x = DEFAULT_X_SIZE;
		if( y == ERROR ) y = DEFAULT_Y_SIZE;
		
		try {
		
			icon = new ImageIcon(getURL()); 
			
		} catch (final Exception e) {
		
			// TODO:  must look into 
			icon = new ImageIcon("loading...");
		} 
		
		// now set this image onto the JLable 
		setIcon(icon);
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