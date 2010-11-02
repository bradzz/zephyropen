package zephyropen.device.wii;

import java.util.Random;

import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.socket.AbstractOutputChannel;
import zephyropen.socket.InputChannel;
import zephyropen.socket.OutputChannel;
import zephyropen.util.Utils;

/**
 * <p>
 * Listen for OSC messages on a given UDP Port, convert to XML commands for the framework
 * 
 * Package : zephyr.framework.socket.multicast
 * <p>
 * Created: 20 AUG 2009
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class WiiRtest extends AbstractOutputChannel implements OutputChannel, InputChannel {

    /** framework configuration */
    protected static ZephyrOpen constants = ZephyrOpen.getReference();

    /** Constructor */
    private WiiRtest() {
        run();
    }

    // do it
    public void run() {
        try {

            /** loop forever, waiting to receive packets */
            double j = 0;
            double i = 0;
            double y = 45;
            double peroid = 0.1;
            Command feedback = null;
            Random rand = new Random();
            while (true) {

                j += peroid;
                i = (float) ((float) (Math.sin(j) * y) + y);
                // i = (float) ((float)(Math.cos(j + rand.nextDouble()) * y) + y)+1;	

                i += rand.nextDouble() * 5;

                feedback = WiiUtils.create(i, i, i, i);
                feedback.send();

                System.out.println(feedback.toString());
                Utils.delay(500);

            }
        } catch (Exception e) {
            constants.shutdown(e);
        }
    }

    /** @param args */
    public static void main(String[] args) {

        /** basic configuration to send on multicast UDP */
        constants.init();
        new WiiRtest();

    }
}
