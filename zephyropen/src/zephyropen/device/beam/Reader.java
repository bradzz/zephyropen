package zephyropen.device.beam;

import java.util.Vector;
import gnu.io.SerialPortEventListener;

/** */ 
public class Reader extends Port implements SerialPortEventListener {
	
	// hold data points 
	public Vector<Integer> points = new Vector<Integer>();

	/** constructor */
	public Reader(String str) {
		super(str);
	}

	@Override
	public void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];
		if (response.startsWith("error")) {
			busy = false;
			constants.error("dead, time out?");
		} else if (response.startsWith("reset")) {
			busy = false;
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:")
						+ 8, response.length());
		} else if (response.startsWith(test)){ 
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				runTime = Integer.parseInt(reply[2]);
				busy = false;
			} else if(reply[1].equals("wait")) {
				busy = true;
				runTime = 0;
				points.clear();
			}
		} else {
			points.add(Integer.parseInt(response));
		}
	}

	/**
	public void log() {
		LogManager log = new LogManager();
		log.open(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + "beam.log");
		log.append("date: " + new Date().toString());
		log.append("data: " + points.size());
		log.append("read: " + getRuntime());
		for (int j = 0; j < points.size(); j++)
			log.append(String.valueOf(points.get(j)));
	} */

	/** test driver only 
	public static void main(String[] args) {

		//constants.init("brad");
		//constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();
		String portstr = find.search("<id:beamreader>");
		if (portstr != null) {
			Reader reader = new Reader(portstr);
			Utils.delay(2000);
			if (reader.connect()) {
				Utils.delay(2000);
				if (reader.test(true)) {
					reader.log();
					Utils.delay(2000);
				}
			} else System.err.println("cant connect");
		} else System.err.println("null port");
		constants.shutdown();
	}*/
}
