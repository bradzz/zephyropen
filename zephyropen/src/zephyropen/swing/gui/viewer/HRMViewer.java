package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

public class HRMViewer extends AbstractViewer implements Viewer {
    
    public HRMViewer(API caller) {

    	// TODO: super(api);??
        api = caller;
        
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
        charts[2].add(String.valueOf(api.getDelta()));
    }
}
