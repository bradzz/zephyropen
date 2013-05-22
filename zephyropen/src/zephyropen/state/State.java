package zephyropen.state;

import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.state.Entry;
import zephyropen.state.TimedEntry;
import zephyropen.util.Utils;

/**
 * <p/>
 * A state holder for the incoming data
 * <p/>
 * Creates a list of data points that are time stamped as they are added. This
 * class will also maintain a calculated max, min and average value as entries
 * are added and removed over time.
 * <p/>
 * Package : Created: September 30, 2008
 * <p/>
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class State {

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();

	/** default settings */
	public static final int PRECISION = 2;

	public static final int DEFAULT_RECORDS = 800;

	private static final int PACK_AFTER = 5;

	private static final int SKIPPED_TOO_MANY = 4;

	/** hold data points */
	protected Vector<TimedEntry> list = null;

	/** keep basic stats */
	protected String name = "no name";

	// set to true in constants if want to turn this on
	public static final String pack = "pack";
	
	protected double max = Double.MIN_VALUE;

	protected double min = Double.MAX_VALUE;

	protected double average = 0.0;

	protected int maxIndex = 0;

	protected int minIndex = 0;

	/** time when add() was last called */
	protected long last = 0L;

	private Filter filter = null;

	private int filtered = 0;


	/**
	 * 
	 * Creates a list of data points that are time stamped as they are added.
	 * This class will also maintain a calculated max, min and average value as
	 * entries are added and removed over time.
	 * 
	 * 
	 */
	public State(String text, int size) {
		list = new Vector<TimedEntry>(size);
		name = text;
		filter = FilterFactory.create(name);
	}

	/** */
	public State(String text) {
		list = new Vector<TimedEntry>(DEFAULT_RECORDS);
		name = text;
		filter = FilterFactory.create(name);
	}

	/**
	 * 
	 * refresh the most current value of this state
	 * 
	 * @param value
	 *            is the new current value of this state object
	 */
	public void update(double value) {
		list.setElementAt(new TimedEntry(String.valueOf(value)), list.size() - 1);

		// track input speed
		last = System.currentTimeMillis();
	}

	/**
	 * Use the current time for this state objects latest update
	 */
	public void touch() {
		
		list.setElementAt(new TimedEntry(getNewestValueString()), list.size() - 1);

		// track input speed
		last = System.currentTimeMillis();
	}

	/** Add an entry */
	public void add(TimedEntry timedEntry) {

		//double input = Double.valueOf(timedEntry.getValueString());
		insert(timedEntry);
		/*
		// ignore any extreme input
		if (FilterFactory.inRange(input, filter)) {

			// only update the timestamp because value is the same
			if (constants.getBoolean(pack) && (list.size() > PACK_AFTER)) {
				if (Double.compare(input, getNewestValue()) == 0) {
					touch();
					return;
				}
			}
			
			// check how fast is changing, but ignore if been too long or too
			// many skipped
			if (!FilterFactory.tooFast(input, filter, this)
					|| (filtered > SKIPPED_TOO_MANY)
					|| (System.currentTimeMillis() - getNewest().getTimestamp() > ZephyrOpen.ONE_MINUTE)) {

				insert(timedEntry);

				// clear counter
				filtered = 0;

			} else {
				filtered++;
			}
		}*/
		
	}

	//
	// add new entry and update timer 
	// 
	private void insert(TimedEntry timedEntry) {
		
		// constants.info("insert: " + timedEntry.toString() + " size: " + size());

		// push out oldest record
		if (list.size() == list.capacity())
			list.removeElementAt(0);

		// add it to the list and update stats
		if (list.add(timedEntry))
			calculateAverage();
		
		// track input speed
		last = System.currentTimeMillis();
	}

	/**  */
	public String getTitle() {
		return name;
	}

	/** */
	public double[] getScaledData() {

		double[] data = new double[list.size()];
		TimedEntry entry = null;
		for (int i = 0; i < list.size(); i++) {

			entry = list.get(i);
			data[i] = scale(entry.getValueDouble());
		}
		return data;
	}

	/**  */
	public double[] getScaledData(double min, double max) {

		double[] data = new double[list.size()];
		TimedEntry entry = null;
		for (int i = 0; i < list.size(); i++) {
			entry = list.get(i);
			data[i] = scale(min, (float) entry.getValueDouble(), max);
		}
		return data;
	}

	/**	 */
	public double scale(double value) {
		double scale = (((value - min) / ((max - min) + 0.1)) * 100);

		// restrict max and min values
		if (scale < 0.0001)
			return 0.0001;
		if (scale > 99.99999)
			return 99.99999;

		return scale;
	}

	/**  */
	public double scale(double min, double value, double max) {
		double scale = (((value - min) / ((max - min) + 0.1)) * 100);

		// restrict max and min values
		if (scale < 0.0001)
			return 0.0001;
		if (scale > 99.99999)
			return 99.99999;

		return scale;
	}

	/** */
	protected void calculateAverage() {

		TimedEntry entry = null;
		double value = 0;
		double sum = 0;

		// clear stats
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;

		for (int i = 0; i < list.size(); i++) {

			entry = list.get(i);
			value = entry.getValueDouble();
			sum += value;

			if (value > max) {
				max = value;
				maxIndex = i;
			}
			if (value < min) {
				min = value;
				minIndex = i;
			}
		}

		average = sum / (list.size());
	}

	/** */
	public TimedEntry getNewest() {

		if (list.size() <= 1)
			return null;

		return list.get(size() - 1);
	}

	/** */
	public Double getNewestValue() {

		TimedEntry entry = getNewest();

		if (entry == null)
			return 0.0;

		return entry.getValueDouble();
	}

	/** */
	public String getNewestValueString() {
		return Utils.formatFloat(getNewestValue(), PRECISION);
	}

	/** */
	public TimedEntry getOldest() {
		if (list.size() <= 1)
			return null;
		return list.get(0);
	}

	/***/
	public TimedEntry getIndex(int i) {
		return list.get(i);
	}

	/**  */
	public Entry get(int i) {
		return list.get(i);
	}

	/** */
	public int size() {
		if (list != null)
			return list.size();

		return -1;
	}

	/**   */
	public int capacity() {
		return list.capacity();
	}

	/**  */
	public double getMinValue() {
		return min;
	}

	/**  */
	public double getMaxValue() {
		return max;
	}

	/**  */
	public double getAverage() {
		return average;
	}

	/**  */
	public String getMinValueString() {
		return Utils.formatFloat(min, PRECISION);
	}

	/**  */
	public String getMaxValueString() {
		return Utils.formatFloat(max, PRECISION);
	}

	/** */
	public TimedEntry getMax() {
		return list.get(maxIndex);
	}

	/**	 */
	public TimedEntry getMin() {
		return list.get(minIndex);
	}

	/**  */
	public int getMaxInt() {

		if (list.isEmpty())
			return 0;

		TimedEntry entry = list.get(maxIndex);
		return (int) entry.getValueDouble();
	}

	/**  */
	public int getMinInt() {

		if (list.isEmpty())
			return 0;

		TimedEntry entry = list.get(minIndex);
		return (int) entry.getValueDouble();
	}

	/**  */
	public String getAverageValueString() {
		return Utils.formatFloat(average, PRECISION);
	}

	/** get time since last message */
	public long getDelta() {
		return (System.currentTimeMillis() - last);
	}

	@Override
	public String toString() {
		return name;
	}

	/**  */
	public String getStats() {

		if (list.isEmpty())
			return "loading...";

		TimedEntry oldest = getOldest();
		TimedEntry newest = getNewest();

		if (oldest == null || newest == null)
			return null;

		return name + " \t [" + list.size() + "]\t" + newest.getAge() + " - "
				+ oldest.getAge() + "\t\tmin = " + getMinValueString()
				+ "\tavg = " + getAverageValueString() + "\tmax = "
				+ getMaxValueString();
	}

	/** clear the state, but insert the current and average */
	public void reset() {
		TimedEntry avg = new TimedEntry(getAverageValueString());
		TimedEntry now = new TimedEntry(getNewestValueString());
		list.clear();
		insert(avg);
		insert(now);
	}
}
