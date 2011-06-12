package zephyropen.device.beam;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import zephyropen.util.google.ScreenShot;

public class BeamScan {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		constants.init("brad");
		constants.put(ZephyrOpen.deviceName, "beamscan");

		Find find = new Find();
		
		Spin spin = new Spin(find.search("<id:beamspin>"));
		Reader reader = new Reader(find.search("<id:beamreader>"));

		if( !spin.connect())
			constants.shutdown("can't find spin");
		
		if( !reader.connect())
			constants.shutdown("can't find reader");
		
		Utils.delay(2000);

		constants.info("+call");
		spin.startTest();
		reader.test(false);
		constants.info("-call");
		
		/* wait for both */
		while(spin.isBusy() || reader.isBusy()){
			constants.info("wait in main");
			Utils.delay(1000);
		}
		
		new ScreenShot(reader.chart, "points = " + reader.chart.getState().size());
		Utils.delay(2000);
		constants.shutdown();
	}
}
