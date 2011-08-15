package zephyropen.device.beamscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;

import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

public class BeamScan {
	
	private static ZephyrOpen constants = ZephyrOpen.getReference();
	private CommPort comm = null;

	/** */
	public BeamScan() { 
		
		// System.out.println("....connect"); 
		
		String port = constants.get("beamscanport");
		
		// need to go look?
		if (port == null){ 
			Find find = new Find();
			port = find.search("<id:beamscan>");
		}
		
		// not found 
		if (port == null) 
			constants.shutdown("can't find beamscan");
	
		comm = new CommPort(port);
		if (!comm.connect()){
			close();
			constants.error("can't connect to beamscan");
		}
				
		
		// re-fresh the file
		// found.put(beamscan, port.);
		// writeProps();

		Utils.delay(3000);	
		constants.info("beamscan port: " + comm.portName);
		constants.info("beamscan version: " + comm.getVersion());
		Utils.delay(2000);
		
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
	
	

	public String getVersion() {
		return comm.getVersion();
	}

	public String getPort() {
		return comm.getPortName();
	}
	
	public void stop() {
		comm.sendCommand(new byte[] { 's' });
	}

	public void start() {
		comm.sendCommand(new byte[] { 'e' });
	}
	

	/** test driver */
	public static void main(String[] args) {

		constants.init();
		BeamScan scan = new BeamScan();
		
		for(int i = 0 ; i < 50 ; i++){
		scan.start();
		Utils.delay(200);
		scan.stop();
		Utils.delay(2000);
		}
		System.out.println("...done");
		scan.close();
		constants.shutdown();
	}

	
}
