package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

/**
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class BioharnesViewer extends AbstractViewer implements Viewer {

    public BioharnesViewer(API caller) {

        this.api = caller;

        charts = new GoogleChart[6];
        charts[0] = new GoogleLineGraph(PrototypeFactory.heart, "BPM", Color.RED);
        charts[1] = new GoogleLineGraph(PrototypeFactory.respiration, "BPM", Color.ORANGE);
        charts[2] = new GoogleLineGraph(PrototypeFactory.temperature, "C", Color.BROWN);
        charts[3] = new GoogleLineGraph(PrototypeFactory.posture, "degrees", Color.PURPLE);
        charts[4] = new GoogleLineGraph(PrototypeFactory.beat, "seq#", Color.ORANGERED);
        charts[5] = new GoogleLineGraph(PrototypeFactory.connection, "sec", Color.FORESTGREEN);

        // now build a tabbed frame for the charts
        frame = new TabbedFrame(charts);
    }

    /** new XML command incoming */
    public void update(Command command) {

        battery = command.get(PrototypeFactory.battery);
        
        charts[0].add(command.get(PrototypeFactory.heart));
        charts[1].add(command.get(PrototypeFactory.respiration));
        charts[2].add(command.get(PrototypeFactory.temperature));
        charts[3].add(command.get(PrototypeFactory.posture));
        charts[4].add(command.get(PrototypeFactory.beat));
        charts[5].add(String.valueOf(api.getDelta()));

        /*
         * 
         * heart.add(command.get(PrototypeFactory.heart));
         * respiration.add(command.get(PrototypeFactory.respiration));
         * temperature.add(command.get(PrototypeFactory.temperature));
         * posture.add(command.get(PrototypeFactory.posture));
         * //System.out.println("GraphReport.execute(): Timecur: "+current+ " Timeprev:"+
         * previous); // Check for roll over if ( current - previous < 0) { previous =
         * 0xffff - previous;
         * //System.out.println("GraphReport.execute(): add cur + prev: " + (current +
         * previous) ); rr.add( Integer.toString( current + previous ) );
         * 
         * } else { //System.out.println("GraphReport.execute(): add cur - prev: " +
         * (current - previous) ); rr.add( Integer.toString( current - previous ) ); }
         */

    }
}
