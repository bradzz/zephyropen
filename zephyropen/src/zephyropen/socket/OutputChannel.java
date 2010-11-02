package zephyropen.socket;

import zephyropen.command.Command;

/**
 * Abstract base class for modelling output channel classes.
 * <p>
 * Created: 2005.11.08
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public interface OutputChannel {
   /**
    * write a command object to socket
    */
   public void write(Command command);
}
