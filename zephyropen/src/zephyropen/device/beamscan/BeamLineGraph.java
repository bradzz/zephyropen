package zephyropen.device.beamscan;

import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.state.TimedEntry;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

/**
 * <p>
 * A wrapper for the google RESTful graphing service. Add new data points, and create
 * URL's for chart images.
 * 
 * <p>
 * Docs here: http://code.google.com/p/charts4j/
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class BeamLineGraph {

	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** need a state holding object */ 
	protected State state = null; 
	
	/** add new entry */
	public void add(String data) {
		if (data == null) return;
		if (data.equals("")) return;
		state.add(new TimedEntry(data)); 
	}
	
    public BeamLineGraph(){
    	state = new State(this.getClass().getName());
    }

	public String getURLString(int x, int y, String title) {
        try {
        
            Line valuesLine = Plots.newLine(DataUtil.scale(state.getScaledData()));
            valuesLine.setColor(Color.BLUE);
            valuesLine.setFillAreaColor(Color.LIGHTGREY);
            
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 2, (state.size()/2));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 2, (state.size()/4));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 2, ((state.size()/2) + (state.size()/4)));
           
            LineChart chart = GCharts.newLineChart(new Line[] {valuesLine});

            // set the size 
            chart.setSize(x, y);
            
            // add grid 
            chart.setGrid(5, 20, 3, 2);
  
            // construct a URL that can be used to display this graph  
            return chart.toURLString();

        } catch (final Exception e) {
            return null;
        }
	}
}
