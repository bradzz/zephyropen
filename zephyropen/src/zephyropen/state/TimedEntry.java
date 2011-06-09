package zephyropen.state;

import zephyropen.state.Entry;
import zephyropen.util.Utils;

/**
 * <p> Hold an Entry, and add a time stamping feature.  
 * <p>
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class TimedEntry implements Entry {

	/** default number of decimal points */
	public static final int PRECISION = 2;
	
	/** inflection values */
	public static final short UP = 0;
	public static final short DOWN = 1;
	public static final short ZERO = 3;
	
	/** creation time stamp */ 
	private long timestamp = 0x0;
	
	/** most often called for as a string */ 
	private String value = null;
	
	/** is this an interesting data point */ 
	private boolean inflection = false; 
	
	/** if is an inflection point */
	private short direction = ZERO;

	/**
	 * 
	 * @param value
	 */
	public TimedEntry(String value) {
		
		this.value = value;
		this.timestamp = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @param value
	 */
	public TimedEntry(String value, short direction) {
		
		this.value = value;
		this.timestamp = System.currentTimeMillis();
		
		// we are interesting
		this.inflection = true;
		this.direction = direction;
	}
	
	/**
	 * 
	 * @param value
	 */
	public TimedEntry(double value) {
		try {

			this.value = String.valueOf(value);
			this.timestamp = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param value
	 */
	public TimedEntry(double value, short direction) {
		try {

			this.value = String.valueOf(value);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.timestamp = System.currentTimeMillis();
		
		// we are interesting
		this.inflection = true;
		this.direction = direction;
	}
	
	public TimedEntry(String data, long time) {

		try {

			this.value = String.valueOf(value);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.timestamp = time;
		
		// we are interesting
		//this.inflection = true;
		//this.direction = direction;
	}

	public short getInflection(){
		return direction;
	}

	public boolean isInflection(){
		return inflection;
	}
	
	public boolean isIncreasing(){
		return (inflection && (direction == UP));
	}
	
	public boolean isDecreasing(){
		return (inflection && (direction == DOWN));
	}
	
	public void setInflection(short direction){
		this.direction = direction;
		this.inflection = true;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public String getValueString() {
		return Utils.formatString(value, 2);
	}

	public double getValueDouble() {
		return Double.parseDouble(value);
	}

	public String toString() {
		return "age: " + getAge() + "\tvalue: " + value;
	}

	public static String getTime(long time) {
		// Mon Jul 04 17:04:30 PDT 2005
		return getTime(time);
	}

	/**
	 * 
	 * Returns the entry's age in a formatted string, to n
	 * 
	 * decimal places, as specified by precision.
	 * 
	 * <p/>
	 * 
	 * eg. "45 ms", "3.5 mins", or "7.12 hrs"
	 * 
	 * <p/>
	 * 
	 * <p/>
	 * 
	 * ie: getAge(1) -> "1.2 sec | hrs | days"
	 * 
	 * ie: getAge(2) -> "1.21 sec | hrs | days"
	 * 
	 * ie: getAge(3) -> "1.216 sec | hrs | days"
	 */

	public String getAge() {

		// get age in seconds
		long ms = (System.currentTimeMillis() - timestamp);
		double seconds = getSeconds(ms);
		double minutes = getMinutes(ms);
		// double hours = getHours(ms);

		// System.out.println( ms + " = " + getSecondsString(ms) + " \t " +
		// getMinutesString(ms) + " \t " + getHoursString(ms) + " \t" );

		if (seconds <= 60)
			return getSecondsString(ms);

		else if (minutes <= 60)
			return getMinutesString(ms);

		// else if( hours <= 24 )

		return getHoursString(ms);
	}

	
	public static String age(long now) {

		// get age in seconds
		long ms = Math.abs( System.currentTimeMillis() - now );
		double seconds = getSeconds(ms);
		double minutes = getMinutes(ms);
		// double hours = getHours(ms);

		// System.out.println( ms + " = " + getSecondsString(ms) + " \t " +
		// getMinutesString(ms) + " \t " + getHoursString(ms) + " \t" );

		if (seconds <= 60)
			return getSecondsString(ms);

		else if (minutes <= 60)
			return getMinutesString(ms);

		// else if( hours <= 24 )

		return getHoursString(ms);
	 }

	public static double getSeconds(long ms) {
		return (double) (ms) / (double) (1000);
	}

	public static double getMinutes(long ms) {
		double seconds = getSeconds(ms);
		return seconds / (double) (60);
	}

	public static double getHours(long ms) {
		double minutes = getMinutes(ms);
		return minutes / (double) (60);
	}

	public static String getSecondsString(long ms) {
		return Utils.formatFloat(getSeconds(ms), PRECISION) + "seconds";
	}

	public static String getMinutesString(long ms) {
		return Utils.formatFloat(getMinutes(ms), PRECISION) + "minutes";
	}

	public static String getHoursString(long ms) {
		return Utils.formatFloat(getHours(ms), PRECISION) + "hours";
	}
}
