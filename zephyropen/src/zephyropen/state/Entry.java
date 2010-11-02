package zephyropen.state;

/**
 * Hold data and time stamp 
 * Created: 2005.11.08
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public interface Entry {

	public abstract long getTimestamp();
	
	public abstract String toString();

}
