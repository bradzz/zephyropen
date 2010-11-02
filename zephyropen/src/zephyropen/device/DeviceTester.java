package zephyropen.device;

import java.util.Random;

import zephyropen.command.Command;
import zephyropen.state.Filter;
import zephyropen.state.FilterFactory;
import zephyropen.util.Utils;
import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;

/**
 * <p>
 * Process the log file of the given device, send out the commands as if they
 * were coming from the device. Used in debugging if you don't wan to send
 * actual bluetooth, zigbee, usb data.
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class DeviceTester implements API {

	/** framework configuration */
	private final static ZephyrOpen constants = ZephyrOpen.getReference();

	/** Time between sending messages */
	private long delay = 500;

	private String deviceName = null;

	private long last = 0;

	private Random random = new Random();


	/**
	 * Attach to the XML log file for the device of this name
	 * 
	 * @param delay
	 */
	public DeviceTester() {

		deviceName = constants.get(ZephyrOpen.deviceName);

		// try to get an interval time in props
		delay = constants.getInteger("delay");
		if (delay < 1000)
			delay = 1000;

		ApiFactory.getReference().add(this);

		last = System.currentTimeMillis();
		
		Command out = new Command(
				PrototypeFactory.getDeviceTypeString(deviceName));
		
		
		/**
		*
	
		while (true) {
			out = generate(out);
			if (FilterFactory.filter(out)) {
				
				out.send();
				Utils.delay(delay);

				// System.out.println("out: " + out);
			} else System.err.println(out);
		}	*/
		
		
		run(out);
	}

	// do it
    public void run(Command feedback) {
        try {

            /** loop forever, waiting to receive packets */
            double j = 0;
            double i = 0;
            double y = 60;
            double peroid = 0.1;
           
            //Random rand = new Random();
            while (true) {

                j += peroid;
                i = (float) ((float) (Math.sin(j) * y) + y);
               // i = (float) ((float)(Math.cos(j + rand.nextDouble()) * y) + y)+1;	

                //i += rand.nextDouble() * 5;

             // get this API's command prototype to test against this command
        		String[] commandPrototype = PrototypeFactory.create(feedback.getType());

        		String tag = null;

        		// index into the prototype
        		for (int index = 0; index < commandPrototype.length; index++) {

        			tag = commandPrototype[index];

        			feedback.add(tag, Utils.formatFloat(i, 2));

        		}

                
                feedback.send();

                // System.out.println(feedback.toString());
                Utils.delay(delay); // + Math.abs((random.nextInt()%400)));

            }
        } catch (Exception e) {
            constants.shutdown(e);
        }
    }
	
	/**
	 * Test a full command
	 * 
	 * @param command
	 *            to perform rage checks on
	 * @return true if this command have vauld rang4s of data
	 */
	public Command generate(Command command) {

		// get this API's command prototype to test against this command
		String[] commandPrototype = PrototypeFactory.create(command.getType());

		String tag = null;

		// index into the prototype
		for (int index = 0; index < commandPrototype.length; index++) {

			tag = commandPrototype[index];

			command.add(tag, Utils.formatFloat(getRandom(tag), 2));

		}

		return command;
	}

	private double getRandom(String tag) {

		double value = 0;

		Filter filter = FilterFactory.create(tag);

		//System.out.println("tag = " + tag);

		if (filter == null)
			return 0;

		while (! FilterFactory.inRange(value, filter))
			value = (Math.abs(random.nextDouble() * 1000) % filter.getMax()) + filter.getMin();

		return value;
	}
	
	public Command generateSin(Command command) {

		// get this API's command prototype to test against this command
		String[] commandPrototype = PrototypeFactory.create(command.getType());

		String tag = null;

		// index into the prototype
		for (int index = 0; index < commandPrototype.length; index++) {

			tag = commandPrototype[index];

			command.add(tag, Utils.formatFloat(getRandom(tag), 2));

		}

		return command;
	}
		

	@Override
	public void execute(Command command) {
		System.out.println(getDelta() + " exec: " + command.toXML());
		last = System.currentTimeMillis();
	}

	@Override
	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public String getAddress() {
		return constants.get(ZephyrOpen.address);
	}

	@Override
	public long getDelta() {
		return System.currentTimeMillis() - last;
	}

	/** Find log file by naming convention given in args[] */
	public static void main(String[] args) throws Exception {

		constants.init(args[0]);

		System.out.println(constants);

		new DeviceTester();
	}
}
