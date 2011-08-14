package zephyropen.device.beamscan;

import java.awt.Component;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Icon;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/** */ 
public class CommPort extends Port implements SerialPortEventListener {
	
	// hold data points 
	public Vector<Integer> points = new Vector<Integer>();
	public long start = 0;
	public long stop = 0;
	LogManager log = new LogManager();
	
	/** constructor */
	public CommPort(String str) {
		super(str);
		log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + "beam.log");
	}

	@Override
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];
		
		response = response.trim();
		log.append(response);
		
		if(response.startsWith("start")){
			
			points.clear();
			start = System.currentTimeMillis();
			
		} else if(response.startsWith("done")){
			
			System.out.println("......" + response);
			
			if(points.size() > 0){
				
				stop = System.currentTimeMillis();		
				System.out.println("size : " + points.size());
				System.out.println("took : " + (stop - start) + " ms");	
			
				new Thread(
				new Runnable() {
					public void run() {
						
						Vector<Integer> snapshot = (Vector<Integer>) points.clone();
						log.append("size : " + snapshot.size());
						log.append("took : " + (stop - start) + " ms");
						
						Icon ico = BeamGUI.lineGraph(snapshot);
						BeamGUI.screenCapture((Component) ico);
						
					}
				}).start();
			
						
			
			}
		}
		else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());
		} else {
			points.add(Integer.parseInt(response));
			log.append(response);
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
