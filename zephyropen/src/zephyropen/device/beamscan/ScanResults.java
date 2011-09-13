package zephyropen.device.beamscan;

import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;

public class ScanResults {

	private ZephyrOpen constants = ZephyrOpen.getReference();
	public static final int DEFUALT_LIMIT = 10;
	private LogManager log = new LogManager();
	public Vector<Integer> points = null;
	private int delta = 0;
	private int filtered = 0;
	private int lowLevel = 0;

	/** */
	public ScanResults(final Vector<Integer> data, int ms) {

		points = data;
		delta = ms;

		lowLevel = constants.getInteger("lowLevel");
		if (lowLevel == ZephyrOpen.ERROR) {
			lowLevel = DEFUALT_LIMIT;
			constants.put("lowLevel", DEFUALT_LIMIT);
			constants.updateConfifFile();
		}
		
		for(int i = 0 ; i < points.size() ; i++){
			if(points.get(i) < lowLevel){
				points.set(i, 0);
				filtered++;
			}
		}	
		
		if(constants.getBoolean(ZephyrOpen.loggingEnabled)) writeLog();
	}
	
	/** */
	public void writeLog() {
		log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + System.currentTimeMillis() + ".log");
		log.append(new java.util.Date().toString());
		log.append("filtered : " + filtered);
		log.append("filter : " + constants.get("lowLevel"));
		log.append("time : " + delta + " ms");
		log.append("size : " + points.size());
		for (int j = 0; j < points.size(); j++)
			log.append(j + " " + String.valueOf(points.get(j)));
		log.close();
	}

	public int scanTime() {
		return delta;
	}

	public int getFilered() {
		return filtered;
	}

	/**  */
	public int[] getSlice(final int target) {
		int[] values = { 0, 0, 0, 0 };
		try {

			values[0] = getDataInc(target, 0);
			// constants.info("x1: " + values[0] + " value: " +
			// reader.points.get(values[0]));

			values[1] = getDataDec(target, values[0]);
			// constants.info("x2: " + values[1] + " value: " +
			// reader.points.get(values[1]));

			values[2] = getDataInc(target, points.size() / 2);
			// constants.info("y1: " + values[2] + " value: " +
			// reader.points.get(values[2]));

			values[3] = getDataDec(target, values[2]);
			// constants.info("y2: " + values[3] + " value: " +
			// reader.points.get(values[3]));

		} catch (Exception e) {
			constants.error("can't take slice of beam");
			return null;
		}

		return values;
	}

	/** */
	public int getDataInc(final int target, final int start) {
		
		int j = start;

		// constants.info("start : " + j + " target : " + target);

		for (; j < points.size(); j++) {
			if (points.get(j) > target) {
				// constants.info( "inc_index: " + j + " value: " +
				// reader.points.get(j));
				break;
			}
		}

		return j;
	}

	/** */
	public int getDataDec(final int target, final int start) { 
		
		int j = start;
		// constants.info("start : " + j + " target : " + target);

		for (; j < points.size(); j++) {
			if (points.get(j) < target) {
				// constants.info( "dec_index: " + j + " value: " +
				// reader.points.get(j));
				break;
			}
		}

		return j;
	}

	/** */
	public int getMaxIndex(final int start, final int stop) {

		int j = start;
		int max = 0;
		int index = 0;

		// constants.info("getMaxIndex start: " + start);
		// constants.info("getMaxIndex stop: " + stop);

		for (; j < stop; j++) {
			if (points.get(j) > max) {
				max = points.get(j);
				index = j;
			}
		}

		return index;
	}

	public int getMaxIndexX() { 
		return getMaxIndex(0, points.size() / 2);
	}

	public int getMaxIndexY() {
		return getMaxIndex(points.size() / 2, points.size());
	}

	public void setFiltered(int filtered) {
		this.filtered = filtered;
	}
}
