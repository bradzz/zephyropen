package zephyropen.api;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.FrameworkAPI;
import zephyropen.command.Command;
import zephyropen.util.Utils;

/**
 * <p>
 * Create an API to control and manage the Framework <br>
 * <b> Note: this API registers itself if "frameworkDebug" is enabled</b>
 * <p>
 * Created: May 31, 2005
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class FrameworkAPI implements API {

    /** framework configuration */
    private final ZephyrOpen constants = ZephyrOpen.getReference();

    private final ApiFactory apiFactory = ApiFactory.getReference();

    private static FrameworkAPI singleton = null;

    private long time = 0;

    /** @return a reference to this singleton class */
    public static FrameworkAPI getReference() {

        if (singleton == null) {
            singleton = new FrameworkAPI();
        }
        return singleton;
    }

    /** Constructs the framework API */
    private FrameworkAPI() {

        /** register this API only once per process */
        apiFactory.add(this);

        time = System.currentTimeMillis();
        
        // duhhh
       // constants.put(ZephyrOpen.infoEnable, "true");
    }

    /** execute the command */
    public void execute(Command command) {

        if (command.get(ZephyrOpen.action) == null) {
        	constants.error("no action: " + command);
            return;
        }

        constants.info("delta = " + getDelta() + " in : " + command.list(), this);

        /** Terminate the Process, All of them that are listening */
        if (command.get(ZephyrOpen.action).equals(ZephyrOpen.shutdown)) {
            constants.info("shutdown command received", this);
           
            Utils.delay(300);
            constants.shutdown();
        }

        /** Terminate the Process, All of them that are listening */
        if (command.get(ZephyrOpen.action).equals(ZephyrOpen.shutdown)) {
            constants.info("shutdown command received", this);
           
            Utils.delay(300);
            constants.shutdown();
            Utils.delay(300);
        }
        
        /** Terminate the Process, All of them that are listening */
        if (command.get(ZephyrOpen.action).equals(ZephyrOpen.kill)) {
            
        	constants.info("kill command received: " + command, this);
           
            //Utils.delay(300);
            if(command.get(ZephyrOpen.deviceName).equals(constants.get(ZephyrOpen.deviceName)))
            	constants.shutdown();
            else 
            	constants.info("kill command for not this device:" + constants.get(ZephyrOpen.deviceName));
        }

        
        /** Terminate the Process, All of them that are listening */
        if (command.get(ZephyrOpen.action).equals(ZephyrOpen.frameworkDebug)) {

            if (command.get(ZephyrOpen.value).equals(ZephyrOpen.enable)) {

                constants.info("debug enabled", this);
                constants.put(ZephyrOpen.infoEnable, "true");
                

            } else if (command.get(ZephyrOpen.value).equals(ZephyrOpen.disable)) {

                constants.info("debug disabled", this);
                constants.put(ZephyrOpen.infoEnable, "false");
                
            }
        }

        // mark last input for get delta()
        time = System.currentTimeMillis();
    }

    public String getDeviceName() {
        return ZephyrOpen.zephyropen;
    }

    public String getAddress() {
        return constants.get(ZephyrOpen.address);
    }

    public long getDelta() {
        return System.currentTimeMillis() - time;
    }
}
