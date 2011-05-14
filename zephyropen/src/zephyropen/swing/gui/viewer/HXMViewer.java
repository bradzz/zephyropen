package zephyropen.swing.gui.viewer;

import com.googlecode.charts4j.Color;

import zephyropen.api.API;
import zephyropen.api.PrototypeFactory;
import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

public class HXMViewer extends AbstractViewer implements Viewer {

	private int seq, last = 0;

	public HXMViewer(API caller) {

		api = caller;

		charts = new GoogleChart[6];
		charts[0] = new GoogleLineGraph(PrototypeFactory.heart, "bpm", Color.RED);
		charts[1] = new GoogleLineGraph(PrototypeFactory.rr, "ms", Color.DARKBLUE);
		charts[2] = new GoogleLineGraph(PrototypeFactory.speed, "m/s", Color.ORANGE);
		charts[3] = new GoogleLineGraph(PrototypeFactory.distance, "meters", Color.BROWN);
		charts[4] = new GoogleLineGraph(PrototypeFactory.beat, "seq", Color.ORANGERED);
		charts[5] = new GoogleLineGraph(PrototypeFactory.connection, "sec", Color.FORESTGREEN);
		frame = new TabbedFrame(charts);
		
	}

	/** */
	public void update(Command command) {

	//	String beat = command.get(PrototypeFactory.heart);
		//seq = Integer.parseInt(beat);

		/*
		if (!(seq == (last - 1))) {

			System.out.println("seq: " + seq + " last: " + last);
		
		} else if( seq == last ){
			
			System.out.println("same beat?");
			return;
			
		}

		*/
		
		//
/*
		for (int i = 0; i < 4; i++) {
			String value = command.get(PrototypeFactory.rr + i);
			System.out.println("[" + i + "]: " + value);
		}
*/
		/*
		int v1 = Integer.parseInt(command.get("rr1"));
		int v2 = Integer.parseInt(command.get("rr2"));
        int result =  Math.abs(v1 - v2);
        
		if(result < 300  ) result = 300;
		if(result > 1600 ) result = 1600;
		*/
		// System.out.println("res: " + result);
		
		charts[0].add(command.get(PrototypeFactory.heart));
		charts[1].add("100"); // Integer.toString(result));
		charts[2].add(command.get(PrototypeFactory.speed));
		charts[3].add(command.get(PrototypeFactory.distance));
		charts[4].add(command.get(PrototypeFactory.beat));
		charts[5].add(String.valueOf(api.getDelta()));
		battery = command.get(PrototypeFactory.battery);
		last = seq;
	}
}
