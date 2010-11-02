package zephyropen.api;

import zephyropen.command.Command;

/**
 * Interface for modeling API classes.
 *
 * Created: 2003.09.05
 * 
 * @author Peter Brandt-Erichsen
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public interface API {

    /** Executes this API */
    public abstract void execute(Command command);

    /** @return the name of this */
    public abstract String getDeviceName();
    
    /** @return the time in MilliSeconds since the last message from this API */
    public abstract long getDelta();

    /** @return the physical address, be it comm or bluetooth */
    public abstract String getAddress();

    /** Clear the state objects for this device */
	// public abstract void reset();

}
