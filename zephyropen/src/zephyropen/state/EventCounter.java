package zephyropen.state;

import zephyropen.state.TimedEntry;

/**
 * <p> A state holder for the incoming data
 * <p> Creates a list of data points that are time stamped as they are added. This class
 * will also maintain a calculated max, min and average value as entries are added and
 * removed over time.  
 * <p/> Package : Created: September 30, 2008
 * <p>
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class EventCounter extends State {
	
	/** averaging */
	protected int bufferCount = 0;
	protected int bufferWeigth = 0;
	protected double buffer = 0;

	/** Current trend in this data flow */
	private short direction = TimedEntry.ZERO;

	/** Relative change level, default level */
	private double changeThreashold = 0;
	private int inflectionPoints = 0;
	
	/**
	 * <p/>Creates a list of data points that are time stamped as they are added. This class
	 * will also maintain a calculated max, min and average value as entries are added and
	 * removed over time. 
	 * 
	 * @param name of the data set 
	 * @param size of the data set (older records removed as newer are added)  
	 * @param avg is the number of data points to collect before computing an average and inserting this as a single datapoint. 
	 */
	public EventCounter( String name, int size, int avg, double threshold ) {
		super( name );
		this.bufferWeigth = avg;
		this.changeThreashold = threshold;
	}
	
	public void setAverageLevel(int avg){	
		if(avg < 2) return;
		bufferWeigth = avg;
		
		// reset 
		bufferCount = 0;
		buffer = 0;
	}
	
	public int getAverageLevel(){
		return bufferWeigth;
	}
	
	public void setThreshold(double threshold){
		if(threshold < 0.1) return;
		changeThreashold = threshold;
	}
	
	public double getThreshold(){
		return changeThreashold;
	}
	
	/**
	 * Add new entry, push out older entries when full 
	 * 
	 * @param timedEntry to be added 
	 */
	public void add(TimedEntry timedEntry) {

		// sanity test 
		if (timedEntry == null) return;
		
		// add to buffer 
		buffer += timedEntry.getValueDouble();
		bufferCount++;
				
		// full so clear buffer 
		if( bufferCount == bufferWeigth ){	
			
			// track input speed
			last = System.currentTimeMillis();	
			
			// add this to the data points 
			insert(new TimedEntry(buffer / bufferWeigth));
			
			// reset 
			bufferCount = 0;
			buffer = 0;
		}	
	}
	
	/** add a record to the list */ 
	private void insert(TimedEntry timedEntry){
			
		// push out oldest record
		if (list.size() == list.capacity()){
			
			// keep track of inflection points
			if(getOldest().isInflection()) 
				inflectionPoints--;
			
			// take it out 
			list.removeElementAt(0);
		}
		
		// added ok, re calculate average
		if (list.add(timedEntry)){
			calculateAverage();
			trend();
		}
	}
	
	/** track changes in this data stream */
	private void trend() {
		
		// sanity test
		if( list.size() < 3 ) return;
		
		// System.out.println("i: " + inflectionPoints );
			
		final double value = getNewest().getValueDouble();
		final TimedEntry lastEntry = getIndex( list.size() - 2 );
		final double last = lastEntry.getValueDouble();
		final double delta = (value - last); 
	
		// changing enough to care? 
		if( Math.abs(delta) < changeThreashold ) {
			//System.out.print("low: " + delta);
			return; 
		}
		
		// increasing 
		if( value > last ){
				
			// changing direction? 
			if( direction == TimedEntry.DOWN ){
				getNewest().setInflection(TimedEntry.UP);
				System.out.println(" UP " + delta + " i: " + inflectionPoints);
				inflectionPoints++;
			}
				
			direction = TimedEntry.UP;
			
		// decreasing 
		} else {
			
			// changing direction? 
			if( direction == TimedEntry.UP ){
				getNewest().setInflection(TimedEntry.DOWN);
				System.out.println(" DOWN " + delta + " i: " + inflectionPoints);
				inflectionPoints++;
			}
				
			direction = TimedEntry.DOWN;
		}
	}
}
