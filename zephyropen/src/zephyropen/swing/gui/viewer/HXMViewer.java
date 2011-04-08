package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

public class HXMViewer extends AbstractViewer implements Viewer {

    public HXMViewer(API caller) {

        api = caller;

        charts = new GoogleChart[5];
        charts[0] = new GoogleLineGraph(PrototypeFactory.heart, "bpm", Color.RED);
        charts[1] = new GoogleLineGraph(PrototypeFactory.speed, "m/s", Color.ORANGE);
        charts[2] = new GoogleLineGraph(PrototypeFactory.distance, "meters", Color.BROWN);
        charts[3] = new GoogleLineGraph(PrototypeFactory.beat, "seq", Color.ORANGERED);
        charts[4] = new GoogleLineGraph(PrototypeFactory.connection, "sec", Color.FORESTGREEN);

        frame = new TabbedFrame(charts);
    }

    /** */
    public void update(Command command) {
        battery = command.get(PrototypeFactory.battery);
        charts[0].add(command.get(PrototypeFactory.heart));
        charts[1].add(command.get(PrototypeFactory.speed));
        charts[2].add(command.get(PrototypeFactory.distance));
        charts[3].add(command.get(PrototypeFactory.beat));
        charts[4].add(String.valueOf(api.getDelta()));
    }
}
