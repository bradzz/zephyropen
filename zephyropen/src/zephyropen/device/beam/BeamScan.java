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
		
		Spin spin = new Spin(find.search("<id:beamscan>"));
		Reader reader = new Reader(find.search("<id:beamreader>"));

		if( !spin.connect())
			constants.shutdown("can't find spin");
		
		//if( !reader.connect())
			//constants.shutdown("can't find spin");
		
		Utils.delay(2000);

		reader.test();Utils.delay(2000);
		spin.test();
		
		new ScreenShot(reader.chart, "points = " + reader.chart.getState().size());
		
	//)
		//		System.out.println("test done");
		//	else
			//	System.out.println("fault");
			
		Utils.delay(2000);
		constants.shutdown();
	}

}
