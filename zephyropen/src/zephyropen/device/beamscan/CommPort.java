package zephyropen.device.beamscan;

import java.io.IOException;
import java.util.Vector;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;
import zephyropen.util.google.ScreenShot;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/** */ 
public class CommPort extends Port implements SerialPortEventListener {
	
	// hold data points 
	public Vector<Integer> points = new Vector<Integer>(1000);
	public long start = 0;
	public long stop = 0;
	
	/** constructor */
	public CommPort(String str) {
		super(str);
	}

	@Override
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];
		
		//response = response.trim();
		//log.append(response);
		// System.out.println(response);
		
		if(response.startsWith("start")){
			
			points = new Vector<Integer>(1000);//points.clear();
			start = System.currentTimeMillis();
			
		} else if(response.startsWith("done")){
						
			if(points.size() > 0){
				stop = System.currentTimeMillis();		
				System.out.println("size : " + points.size());
				System.out.println("took : " + (stop - start) + " ms");	
						
				@SuppressWarnings("unchecked")
				final Vector<Integer> snapshot = ((Vector<Integer>) points.clone());

				new Thread(
						new Runnable() {
							public void run() {
						
								LogManager log = new LogManager();
								log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + System.currentTimeMillis() + ".log");
								log.append(new java.util.Date().toString());
								log.append("size : " + snapshot.size());
								log.append("took : " + (stop - start) + " ms");
					
								//@SuppressWarnings("unchecked")
								//GoogleChart chart = new GoogleLineGraph("beam", "ma", com.googlecode.charts4j.Color.BLUEVIOLET);
								for (int j = 0; j < snapshot.size(); j++){
									//if(j%5==0) chart.add(String.valueOf(snapshot.get(j)));
									log.append(j + " " + String.valueOf(snapshot.get(j)));
								}
								log.close();
								//System.out.println("url: " + chart.getURLString(600, 300, " state points = " + chart.getState().size()));
								//new ScreenShot(chart, " points = " + chart.getState().size());
					
					}
				}).start();
			}
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());
		} else {	
			int value = -1;
			try {
				value = Integer.parseInt(response);
			} catch (Exception e) {
				constants.error(e.getMessage());
			}
			if( value != -1 ) {
				points.add(value);
				System.out.println(points.size() + " " + value);
			}
		}
	}

	
	/**
	 * Buffer input on event and trigger parse on '>' charter
	 * 
	 * Note, all feedback must be in single xml tags like: <feedback 123>
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte[] input = new byte[32];
				int read = in.read(input);
				for (int j = 0; j < read; j++) {
					// print() or println() from arduino code
					if ((input[j] == '>') || (input[j] == 13) || (input[j] == 10)) {
						// do what ever is in buffer
						if (buffSize > 0)
							execute();
						// reset
						buffSize = 0;
						// track input from arduino
						lastRead = System.currentTimeMillis();
					} else if (input[j] == '<') {
						// start of message
						buffSize = 0;
					} else {
						// buffer until ready to parse
						buffer[buffSize++] = input[j];
					}
				}
			} catch (IOException e) {
				constants.error("event : " + e.getMessage(), this);
			}
		}
	}
}
