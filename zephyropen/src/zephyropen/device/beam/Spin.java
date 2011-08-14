package zephyropen.device.beam;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import gnu.io.SerialPortEventListener;

public class Spin extends Port implements SerialPortEventListener {

	private int steps = 0;

	/** call super class */
	public Spin(String str) {
		super(str);
	}

	public int getSteps() {
		return steps;
	}

	@Override
	public void execute() {
		String response = "";
		for (int i = 0; i < super.buffSize; i++)
			response += (char) buffer[i];

		if (response.startsWith("error")) {
			busy = false;
			constants.info("dead");
		} else if (response.startsWith("version:")) {
			if (version == null)
				version = response.substring(response.indexOf("version:") + 8, response.length());
		} else if (response.startsWith(test) || (response.startsWith(home))) {
			String[] reply = response.split(" ");
			if (reply[1].equals("done")) {
				if (reply.length >= 4) {
					runTime = Integer.parseInt(reply[2]);
					steps = Integer.parseInt(reply[3]);
				}
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
		Spin spin = new Spin(find.search(BeamScan.beamspin));
		if (spin.connect()) {

			Utils.delay(2000);

			// blocking call
			//if (spin.test(true)) {
			//	constants.info("test took: " + spin.runTime + "ms, took: "
			//			+ spin.getSteps() + " steps");
			//} else
				constants.error("fault");
		} else
			constants.error("can't find spin");

		constants.shutdown();
	}
}
