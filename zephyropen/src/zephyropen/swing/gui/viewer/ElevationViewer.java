package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

public class ElevationViewer extends AbstractViewer implements Viewer {

    public ElevationViewer(API caller) {

        api = caller;

        charts = new GoogleChart[3];
        charts[0] = new GoogleLineGraph(PrototypeFactory.back, " units? ", Color.BLACK);
        charts[1] = new GoogleLineGraph(PrototypeFactory.seat, " angle, units? ", Color.BROWN);
    //    charts[2] = new GoogleLineGraph(PrototypeFactory.heart, "BPM", Color.RED);
        charts[2] = new GoogleLineGraph(PrototypeFactory.connection, "sec", Color.FORESTGREEN);
        
        frame = new TabbedFrame(charts);
    }

    /** */
    public void update(Command command) {
    	    	
        charts[0].add(command.get(PrototypeFactory.back));
        charts[1].add(command.get(PrototypeFactory.seat));
     //   charts[2].add(command.get(PrototypeFactory.heart));
        charts[2].add(String.valueOf(api.getDelta()));
    }
}
