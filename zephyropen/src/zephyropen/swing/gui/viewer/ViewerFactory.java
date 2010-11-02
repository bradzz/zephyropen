package zephyropen.swing.gui.viewer;

import zephyropen.api.API;
import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;

/**
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class ViewerFactory {

	public static ZephyrOpen constants = ZephyrOpen.getReference();
	
	public static Viewer create(API api) {
		
		int viewType = PrototypeFactory.getDeviceType(api.getDeviceName()); 	
		if( viewType == PrototypeFactory.ERROR ){
			constants.error("can't lookup type for viewer type for: " + api.getDeviceName());
			constants.shutdown();
		}
		
		if (viewType == PrototypeFactory.POLAR) {
			
			return new PolarViewer(api);

		} else if (viewType == PrototypeFactory.BIOHARNESS) {
			
			return new BioharnesViewer(api);

		} else if (viewType == PrototypeFactory.HRM) {
			
			return new HRMViewer(api);

		} else if (viewType == PrototypeFactory.HXM) {
			
			return new HXMViewer(api);

		} else if (viewType == PrototypeFactory.WII ){
			
			return new WiiViewer(api); 

		} else if (viewType == PrototypeFactory.ELEVATION) {
			
			return new ElevationViewer(api);
		}
		
		
		// error state 
		return null;
	}
}
