package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

/**
 * 
 * View for the Ploar Device
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class PolarViewer extends AbstractViewer implements Viewer {

    public PolarViewer(API caller ) {
    	
    	//api = ApiFactory.getReference().create(constants.get(ZephyrOpen.deviceName));
    	
    	//constants.info("started: " + api.getDeviceName() + " : " + constants.get(ZephyrOpen.deviceName));
    	
    	api = caller;
   
    	
    	constants.info("started: " + api.getDeviceName() + " : " + constants.get(ZephyrOpen.deviceName));
    	
    	
        charts = new GoogleChart[3];
        charts[0] = new GoogleLineGraph(PrototypeFactory.heart, "BPM", Color.RED);
        charts[1] = new GoogleLineGraph(PrototypeFactory.beat, "Seq#", Color.FIREBRICK);
        charts[2] = new GoogleLineGraph(PrototypeFactory.connection, "sec", Color.FORESTGREEN);

        // now build a frame for the charts
        frame = new TabbedFrame(charts);
    }

    public void update(Command command) {
        charts[0].add(command.get(PrototypeFactory.heart));
        charts[1].add(command.get(PrototypeFactory.beat));
        charts[2].add(String.valueOf(api.getDelta() / 1000));
    }
}
