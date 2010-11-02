package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;
import zephyropen.util.google.GoogleMeter;

/**
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class WiiViewer extends AbstractViewer implements Viewer {

    /** */
    public WiiViewer(API caller) {

        api = caller;

        charts = new GoogleChart[6];
        charts[0] = new GoogleLineGraph(PrototypeFactory.accel, "%", Color.DARKVIOLET);
        charts[1] = new GoogleLineGraph(PrototypeFactory.roll, "%", Color.OLIVE);
        charts[2] = new GoogleLineGraph(PrototypeFactory.pitch, "%", Color.ORANGERED);
        charts[3] = new GoogleLineGraph(PrototypeFactory.yaw, "%", Color.BLACK);
        charts[4] = new GoogleMeter("Pull Rate", "ppm");
        charts[5] = new GoogleLineGraph(PrototypeFactory.connection, "ms", Color.FORESTGREEN);
        
        frame = new TabbedFrame(charts);
    }

    /** new XML command incoming */
    public void update(Command command) {

        charts[0].add(command.get(PrototypeFactory.accel));
        charts[1].add(command.get(PrototypeFactory.roll));
        charts[2].add(command.get(PrototypeFactory.pitch));
        charts[3].add(command.get(PrototypeFactory.yaw));
        charts[4].add(command.get(PrototypeFactory.accel));
        charts[5].add(Utils.formatFloat(api.getDelta()));
    }
}
