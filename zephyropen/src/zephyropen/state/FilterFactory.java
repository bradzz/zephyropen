/*
 * Created on 2010-04-19
 * @author brad
 * @version $Id: FilterFactory.java 46 2010-04-24 23:16:43Z brad.zdanivsky $
 */
package zephyropen.state;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;

public class FilterFactory {

	private static final int MIN_POINTS = 3;

	private static ZephyrOpen constants = ZephyrOpen.getReference();
	
	private static final String heartMax = "heartMax";
	private static final String heartMin = "heartMim";

	
	public static Filter create(String type) {

		if (type.equals(PrototypeFactory.beat))
			return new HeartBeatFilter();

		if (type.equals(PrototypeFactory.heart))
			return new HeartRateFilter();

		if (type.equals(PrototypeFactory.accel))
			return new DefaultFilter();

		if (type.equals(PrototypeFactory.roll))
			return new DefaultFilter();

		if (type.equals(PrototypeFactory.pitch))
			return new DefaultFilter();

		if (type.equals(PrototypeFactory.yaw))
			return new DefaultFilter();

		if (type.equals(PrototypeFactory.posture))
			return new NoFilter();

		if (type.equals(PrototypeFactory.temperature))
			return new TemperatureFilter();

		if (type.equals(PrototypeFactory.respiration))
			return new RespirationFilter();

		if (type.equals(PrototypeFactory.connection))
			return new NoFilter();

		return new NoFilter();
	}

	/**
	 * Test a full command
	 * 
	 * @param command
	 *            to perform rage checks on
	 * @return true if this command have vauld rang4s of data
	 */
	public static boolean filter(Command command) {

		// get this API's command prototype to test against this command
		String[] commandPrototype = PrototypeFactory.create(command.getType());

		Filter filter = null;
		double value = 0;
		String tag = null;
		
		// index into the prototype
		for (int index = 0; index < commandPrototype.length; index++) {

			tag = command.get(commandPrototype[index]);
			if (tag != null) {

				filter = create(commandPrototype[index]);
				if (filter != null) {

					try {

						value = Double.parseDouble(tag);

					} catch (NumberFormatException e) {
						constants.error(e.getMessage());
						constants.error("for: " + command.get(commandPrototype[index]));
					}

					/* check for old test patterns */
					String[] parsed = new String[2];
					if (tag.indexOf('.') > 0) {
						parsed = tag.split("\\.");
						if (parsed[1].length() > 0) {
							// constants.error("too many dec: " + parsed[1] + " str: " + value);
							return false;
						}
					}

					if (!inRange(value, filter)) {

						constants.error(commandPrototype[index] + " "
								+ filter.getClass().getName()
								+ " not in range: " + value);

						return false;

					}
				}
			}
		}

		return true;
	}

	/**
	 * Check for bad data points before adding
	 * 
	 * @param input
	 *            is the new value to try to add to the state
	 * @param filter
	 *            is the state's filtering rules.
	 * @return true if the value is valid or to be filtered
	 */
	public static boolean inRange(double input, Filter filter) {

		if (filter == null)
			return false;

		if (input > filter.getMax()) {
			return false;
		}

		if (input < filter.getMin()) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param input
	 * @param filter
	 * @param state
	 * @return
	 */
	public static boolean tooFast(double input, Filter filter, State state) {

		if (filter == null)
			return false;

		// no need to continue
		if (filter.getThreshold() == 0)
			return false;

		// only is is data to be had
		if (state.size() < MIN_POINTS)
			return false;

		// get the change -- should be continuous signal
		double delta = Math.abs(state.getNewestValue() - input);

		if (delta == 0)
			return false;

		if (Math.abs(delta) > filter.getThreshold()) {
			constants.error(state.getTitle() + " delta: " + delta
					+ " threshold: " + filter.getThreshold() + " current: "
					+ state.getNewestValue() + " new : " + input);
			return true;
		}

		return false;
	}

	/** let anything through in 0-100 range */
	private static class DefaultFilter implements Filter {

		@Override
		public int getMax() {
			return 100;
		}

		@Override
		public int getMin() {
			return 0;
		}

		@Override
		public double getThreshold() {
			return 10;
		}
	}

	/** let anything through in 0-50 range */
	private static class TemperatureFilter implements Filter {

		@Override
		public int getMax() {
			return 50;
		}

		@Override
		public int getMin() {
			return 0;
		}

		@Override
		public double getThreshold() {
			return 5;
		}
	}

	private static class HeartRateFilter implements Filter {

		@Override
		public int getMax() {
			
			int tmp = constants.getInteger(heartMax);
			if( tmp != ZephyrOpen.ERROR) return tmp;
			
			return 130;
		}

		@Override
		public int getMin() {
			
			int tmp = constants.getInteger(heartMin);
			if( tmp != ZephyrOpen.ERROR) return tmp;
			
			return 30;
		}

		@Override
		public double getThreshold() {
			return 10;
		}
	}

	private static class HeartBeatFilter implements Filter {

		@Override
		public int getMax() {
			return 255;
		}

		@Override
		public int getMin() {
			return 0;
		}

		@Override
		public double getThreshold() {
			return 0;
		}
	}

	private static class RespirationFilter implements Filter {

		@Override
		public int getMax() {
			return 60;
		}

		@Override
		public int getMin() {
			return 0;
		}

		@Override
		public double getThreshold() {
			return 3;
		}
	}

	/** let anything through in 0-100 range */
	private static class NoFilter implements Filter {

		@Override
		public int getMax() {
			return Integer.MAX_VALUE;
		}

		@Override
		public int getMin() {
			return Integer.MIN_VALUE;
		}

		@Override
		public double getThreshold() {
			return 0.0;
		}
	}
}
