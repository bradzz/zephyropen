package zephyropen.device.beam;

import gnu.io.SerialPortEventListener;
import zephyropen.api.ZephyrOpen;
import zephyropen.util.LogManager;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;
import zephyropen.util.google.ScreenShot;

import com.googlecode.charts4j.Color;

public class Reader extends Port implements SerialPortEventListener {

	private LogManager log = new LogManager();
	public GoogleChart chart = new GoogleLineGraph("beam", "ma", Color.BLUEVIOLET);

	/**  */
	public Reader(String str) {
		super(str);
		log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + System.currentTimeMillis() + "_beam.txt");
	}

	@Override
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];

		if (response.startsWith("error")) {
			
			busy = false;
			constants.info("dead, time out?");
			
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") 
						+ 8, response.length());

		} else if (response.startsWith(test) || (response.startsWith(home))) {

			constants.info("reader execute: " + response);
			
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				busy = false;	
			} else if (reply[1].equals("start")) {
				busy = true;
			}
			
		} else {
			log.append(response);
			chart.add(response);
		}
	}

	/***/
	public static void main(String[] args) {

		constants.init("brad");
		constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();
		String portstr = find.search("<id:beamreader>");
		if (portstr != null) {
			Reader reader = new Reader(portstr);
			Utils.delay(2000);
			if (reader.connect()) {
				Utils.delay(2000);
				if(reader.test(true)){
					new ScreenShot(reader.chart, "points = " + reader.chart.getState().size());
					Utils.delay(2000);
				}

			} else System.err.println("cant connect");
		} else System.err.println("null port");

		constants.shutdown();
	} 
}
