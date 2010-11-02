package zephyropen.socket;

import zephyropen.command.Command;
import zephyropen.socket.OutputChannel;

/**
 * Abstract base class for modelling output channel classes.
 * <p>
 * Created: 2005.11.08
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public abstract class AbstractOutputChannel implements OutputChannel {

   public AbstractOutputChannel(){}

   public void write(Command out){
      System.out.println("AbstractOutputChannel.write() : override this method.");
   }
}
