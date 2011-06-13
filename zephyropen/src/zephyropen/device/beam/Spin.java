package zephyropen.device.beam;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import gnu.io.SerialPortEventListener;

public class Spin extends Port implements SerialPortEventListener {

	/** call super class */
	public Spin(String str) {
		super(str); 
	}

	@Override
	public  void execute() {
		String response = "";
		for (int i = 0; i < super.buffSize; i++)
			response += (char) buffer[i];

		if (response.startsWith("error")) {
			
			busy = false;
			constants.info("dead");
			
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") 
						+ 8, response.length());

		} else if (response.startsWith(test) || (response.startsWith(home))) {
			
			constants.info("spin execute: " + response);
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				busy = false;
			} else if (reply[1].equals("start")) {
				busy = true;
			}
		}
	}

	/** test drive */
	public static void main(String[] args) {

		constants.init("brad");
		constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();
		Spin spin = new Spin(find.search("<id:beamspin>"));
		if (spin.connect()) {
			
			Utils.delay(2000);

			// blocking call 
			if (spin.test(true))
				constants.info("main.test done");
			else
				constants.error("main.fault");
			
		} else constants.error("can't find spin");

		constants.shutdown();
	}
}
