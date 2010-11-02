package zephyropen.command;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.command.Command;

/**
 * Dispatches commands received from the server to the appropriate API for execution.
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public final class CommandDispatcher {

	/** framework configuration */
	private static ApiFactory apiFactory = ApiFactory.getReference();

	/**
	 * Dispatches the specified command to the appropriate API for execution.
	 * 
	 * @param command is the command to dispatch
	 */
	public static void dispatch(Command command) {
		
		/** sanity test */
		if (command == null) return;
	
		/** retrieve the specified device name */
		String deviceName = command.getType();
		
		if (deviceName == null || deviceName.equals("")){
			// System.err.println("null device name: " + deviceName);
			return;
		}
		
		/** check device name first */
		if (deviceName == null || deviceName.equals("")){
			// System.err.println("null device: " + deviceName);
			return;
		}
		
		/** look up the API */
		API api = apiFactory.create(deviceName);
		
		/** error check */ 
		if (api == null){
			//System.err.println("api look fails for device name: " + deviceName);
			//System.err.println("factory: " + apiFactory.toXML());
			return;
		}
		
		/** check that this is a valid command for this device */
		if (command.isMalformedCommand(api)) return;
			
		/** execute the command */
		api.execute(command);
	}
}
