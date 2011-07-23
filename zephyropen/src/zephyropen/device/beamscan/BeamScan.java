package zephyropen.device.beamscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;

import zephyropen.util.Utils;

public class BeamScan {

	private final static String beamscan = "<id:beamscan>";
	private static ZephyrOpen constants = ZephyrOpen.getReference();
	private final String path = constants.get(ZephyrOpen.userHome) 
		+ ZephyrOpen.fs + "beam.properties";

	/** store past searches */
	// TODO: TAKE FROM LAUNCH 
	private Properties found = new Properties();
	private String port = null;
	private CommPort comm = null;

	/** */
	public BeamScan() {
		readProps();
	}
	
	/** */
	public boolean connect(){
		
		// need to go look?
		if (port == null){ 
			Find find = new Find();
			port = find.search(beamscan);
		}
		
		// not found 
		if (port == null) return false;
		
		comm = new CommPort(port);
		if (!comm.connect()){
			constants.error("can't find spin");
			close();
			return false;
		}
				
		
		// re-fresh the file
		// found.put(beamreader, reader.getPortName());
		// found.put(beamspin, spin.getPortName());
		writeProps();

		Utils.delay(2000);
		constants.info("spin version: " + comm.getVersion());
		Utils.delay(2000);
		return true;
	}
	
	/** */
	public boolean isConnected(){
		if (comm == null) return false;
		
		return true;
	}

	/** */
	public void close() {
		if(comm != null)
			comm.close();
	}

	/** add devices that require com port mapping, not searching */
	public void readProps() {
		if (new File(path).exists()) {
			try {

				File file = new File(path);
				FileInputStream fi = new FileInputStream(file);
				found.load(fi);
				fi.close();

				Enumeration<Object> keys = found.keys();
				while (keys.hasMoreElements()) {
					String dev = (String) keys.nextElement();
					if (dev.equalsIgnoreCase(beamscan))
						port = found.getProperty(dev);
				}
			} catch (Exception e) {
				constants.error(e.getMessage(), this);
			}
		}
	}

	/** */
	public void writeProps() {
		try {

			// write to search props
			FileWriter fw = new FileWriter(new File(path));
			found.store(fw, null); 
			fw.close();

		} catch (Exception e) {
			constants.error(e.getMessage(), this);
		}
	}

	/** */
	public void test() {
		
		if(!isConnected()){
			constants.error("not connected, can not run BeamScan.test()");
			return;
		}

		/* non-blocking */
		comm.test(false);
		//spin.test(false);

		/* wait for both */
		while (comm.isBusy()){ 
			// constants.info("wait...");
			Utils.delay(500);
		}
		

		constants.put("dataPoints", comm.points.size());
		constants.put("spinTime", comm.getRuntime());
		constants.put("readTime", comm.getRuntime());


		constants.info("dataPoints: " + comm.points.size());
		constants.info("spinTime: " + comm.getRuntime());
		constants.info("readTime: " + comm.getRuntime());
		
	}

	/**  */
	public Vector<Integer> getPoints(){
		return comm.points; // (Vector<Integer>) reader.points.clone();
	}

	/**  */
	public int[] getSlice(final int target) {
		int[] values = { 0, 0, 0, 0 };
		try {
		
			values[0] = getDataInc(target, 0);
			// constants.info("x1: " + values[0] + " value: " + reader.points.get(values[0]));

			values[1] = getDataDec(target, values[0]);
			// constants.info("x2: " + values[1] + " value: " + reader.points.get(values[1]));

			values[2] = getDataInc(target, comm.points.size()/2);
			// constants.info("y1: " + values[2] + " value: " + reader.points.get(values[2]));
			
			values[3] = getDataDec(target, values[2]);
			// constants.info("y2: " + values[3] + " value: " + reader.points.get(values[3]));
		
		} catch (Exception e) {
			constants.error("can't take slice of beam");
			return null;
		}
		
		return values;
	} 

	/** */
	private int getDataInc(final int target, final int start) {

		int j = start;

		//constants.info("start : " + j + " target : " + target);

		for (; j < comm.points.size(); j++) {
			if (comm.points.get(j) > target){
				// constants.info( "inc_index: " + j + " value: " + reader.points.get(j));
				break;
			}
		}

		return j;
	}	
	
	/** */
	private int getDataDec(final int target, final int start) {

		int j = start;
		// constants.info("start : " + j + " target : " + target);

		for (; j < comm.points.size(); j++) {
			if (comm.points.get(j) < target){
				// constants.info( "dec_index: " + j + " value: " + reader.points.get(j));
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
			if(comm.points.get(j) > max){
				max = comm.points.get(j);
				index = j;
			}
		}

		return index;
	}
	
	/** 
	private int[] getRange(final int target, final int start) {

		int j = start;
		// constants.info(j + " target : " + target);

		for (; j < reader.points.size() - 1; j++) {
			// constants.info( j + " get: " + reader.points.get(j));
			if (reader.points.get(j) >= target)
				break;
		}
		
		// create an array 
		int[] ans = new int[j];
		for(int i = 0 ; i < j ; i++)
			ans[i] = reader.points.get(i);
		
		return ans;
	}*/

	/*
	public int getXCenter(){
		return reader.points.size()/4;
	}
	
	public int getYCenter(){
		return (reader.points.size()/2) + (reader.points.size()/4);
	}
	*/
	
	public int getMaxIndexX(){
		return getMaxIndex(0, comm.points.size()/2);
	}
	
	public int getMaxIndexY(){
		return getMaxIndex(comm.points.size()/2, comm.points.size());
	}
	
	/** write report to file */
	public void log() {
		LogManager log = new LogManager();
		// log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + "beam.log");
		// log.append("spin version: " + spin.getVersion());
		log.append("version: " + comm.getVersion());
		log.append("date: " + new Date().toString());
		// log.append("step: " + comm.getSteps());
		log.append("data: " + comm.points.size());
		log.append("spin: " + comm.getRuntime() + " ms");
		log.append("read: " + comm.getRuntime() + " ms");
		for (int j = 0; j < comm.points.size(); j++)
			log.append(j+ "  " +String.valueOf(comm.points.get(j)));
	}

	public String getReadVersion() {
		return comm.getVersion();
	}
	public String getcommVersion() {
		return comm.getVersion();
	}

	public String getReadPort() {
		return comm.getPortName();
	}
	
	/** test driver 
	public static void main(String[] args) {

		constants.init(args[0]);
		BeamScan scan = new BeamScan();
		scan.test();
		
		// final int maxX = scan.getMaxIndex(0, scan.reader.points.size()/2);
		// constants.info("max: " + scan.reader.points.get(maxX) + " index: " + maxX);
		
		// final int maxY = scan.getMaxIndex(scan.reader.points.size()/2, scan.reader.points.size());
		// constants.info("max2: " + scan.reader.points.get(maxY)  + " index: " + maxY);
	
		//constants.put("xBeam", maxX);
		//constants.put("yBeam", maxY);
			
		scan.log();
		
		int j = 200;
		int[] slice = null;
		
		
		//for(; j <= 700 ; j+=200){
		
			slice = scan.getSlice(j);
			if (slice != null){
				
				// constants.info("slice: " + j + " xMax: " + maxX + " yMax: " + maxY);
				// constants.info("slice: " + j + " xCenter: " + xCenter + " yCenter: " + yCenter);
				// constants.info("slice: " + j + " x: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));
				
				constants.put("x1", slice[0]);
				constants.put("x2", slice[1]);
				constants.put("y1", slice[2]);
				constants.put("y2", slice[3]);
				
				//constants.put("x1offset", slice[0]-xCenter);
				//constants.put("x2offset", slice[1]-xCenter);
				//constants.put("y1offset", slice[2]-yCenter);
				//constants.put("y2offset", slice[3]-yCenter);
				
				scan.lineGraph( "   slice: " + j );
				
				//  "   x1: " + String.valueOf(slice[0]) + "_" + String.valueOf(slice[0]-xCenter) 
				// + "  x2: " + String.valueOf(slice[1]) + "_" + String.valueOf(slice[1]-xCenter) 
				// + "  y1: " + String.valueOf(slice[2]) + "_" + String.valueOf(slice[2]-yCenter)
				// + "  y2: " + String.valueOf(slice[3]) + "_" + String.valueOf(slice[3]-yCenter));
		
			// }
		}
		
		scan.close();
		Utils.delay(3000);
		constants.shutdown();
	}*/
}
