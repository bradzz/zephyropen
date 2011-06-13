package zephyropen.device.beam;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import zephyropen.util.google.ScreenShot;

public class BeamScan {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();

	/** test driver */
	public static void main(String[] args) {

		constants.init("brad");
		constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();
		Spin spin = new Spin(find.search("<id:beamspin>"));
		Reader reader = new Reader(find.search("<id:beamreader>"));

		if (!spin.connect())
			constants.shutdown("can't find spin");

		if (!reader.connect())
			constants.shutdown("can't find reader");
		
		Utils.delay(2000);
		constants.info("spin version: " + spin.getVersion());
		constants.info("read version: " + reader.getVersion());
		Utils.delay(2000);

		/* non-blocking */
		reader.test(false);
		spin.test(false);
		
		/* wait for both */
		while (spin.isBusy() || reader.isBusy()) {
			// constants.info("wait in main");
			Utils.delay(500);
		}
		
		new ScreenShot(reader.chart, "points = " + reader.chart.getState().size());
		Utils.delay(2000);
		constants.shutdown();
	}
}
