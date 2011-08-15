package zephyropen.util.google;

import java.util.Arrays;

import zephyropen.api.ZephyrOpen;
import zephyropen.device.beam.BeamGUI;
import zephyropen.state.State;
import zephyropen.state.TimedEntry;
import zephyropen.util.Utils;

import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.Shape;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.Plots;

/**
 * <p>
 * A wrapper for the google RESTful graphing service. Add new data points, and create
 * URL's for chart images.
 * 
 * <p>
 * Docs here: http://code.google.com/p/charts4j/
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * @author <a href="mailto:jasonthrasher@gmail.com">Jason Thrasher</a>
 * 
 */
public class GoogleLineGraph extends GoogleChart {

    /** swing requires */
    private static final long serialVersionUID = 1L;

    /** default line colors */
    protected static Color averageColor = Color.BLUE;

    protected Color dataColor = Color.RED;

    public GoogleLineGraph(final String title, final String units, final Color color) {
    	
    	this.title = title;
    	this.state = new State(title);
        this.dataColor = color;
        this.units = units;
    }
    
    /** @return construct a URL that can be used to display this graph */
    @Override
    public String getURLString(final int x, final int y) {

        try {
        
            final Line valuesLine = Plots.newLine(DataUtil.scale(state.getScaledData()));
            valuesLine.setColor(dataColor);

            // scaled data
            final int averageValue = (int) state.scale(state.getAverage());
            final Line avgLine = Plots.newLine(new Data(averageValue, averageValue));
            avgLine.setColor(averageColor);

            // put lines on the graph 
            final Line[] lines = new Line[2];
            lines[0] = valuesLine;
            lines[1] = avgLine;
         
            // new chart 
            final LineChart chart = GCharts.newLineChart(lines);

            // set the size 
            chart.setSize(x, y);
            chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(state.getMinInt(), state.getMaxInt()));

        //    final String titleText = title.toUpperCase() + " = " 
        //    + (state.getNewest()).getValueString() + " " + units + "   " + " (" + state.size() + ") "
        //    + Utils.getDate(); 
      
    
            //String t = constants.get("t");
            //if(t!=null) chart.setTitle();

            // add grid 
            chart.setGrid(5, 20, 3, 2);

            // place three time stamps on y label... oldest, middle, and newest 
      //      chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(Arrays.asList(state.getOldest().getAge(), ((TimedEntry) state.get((getState().size() / 2)))
        //            .getAge(), state.getNewest().getAge()), Arrays.asList(7, 50, 93)));

            // construct a URL that can be used to display this graph  
            return chart.toURLString();

        } catch (final Exception e) {
            return null;
        }
    }

	@Override
	public String getURLString(int x, int y, String title) {
        try {
        
            Line valuesLine = Plots.newLine(DataUtil.scale(state.getScaledData()));
            valuesLine.setColor(dataColor);
     
            /*
            final double red = (double)constants.getInteger("redSlice");
            Line redLine = Plots.newLine(new Data(red/10, red/10)); 
            redLine.setColor(Color.RED);
            
            final double orange = (double)constants.getInteger("orangeSlice");
            Line orangeLine = Plots.newLine(new Data(orange/10, orange/10)); 
            orangeLine.setColor(Color.BROWN);
          

            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.YELLOW, 2, constants.getInteger(BeamGUI.yellowX1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.YELLOW, 2, constants.getInteger(BeamGUI.yellowX2));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.YELLOW, 2, constants.getInteger(BeamGUI.yellowY1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.YELLOW, 2, constants.getInteger(BeamGUI.yellowY2));
        
            
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.RED, 2, constants.getInteger(BeamGUI.redX1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.RED, 2, constants.getInteger(BeamGUI.redX2));           
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.RED, 2, constants.getInteger(BeamGUI.redY1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.RED, 2, constants.getInteger(BeamGUI.redY2));
        
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.ORANGE, 2, constants.getInteger(BeamGUI.orangeX1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.ORANGE, 2, constants.getInteger(BeamGUI.orangeX2));           
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.ORANGE, 2, constants.getInteger(BeamGUI.orangeY1));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_PARTIAL, Color.ORANGE, 2, constants.getInteger(BeamGUI.orangeY2));
         */
            
            
            // grid 
           
            //  valuesLine.addShapeMarker(Shape.HORIZONTAL_LINE, Color.BLUE, 1, 3);
            
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 1, (state.size()/2));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 1, (state.size()/4));
            valuesLine.addShapeMarker(Shape.VERTICAL_LINE_FULL, Color.BLACK, 1, ((state.size()/2) + (state.size()/4)));
            
            LineChart chart = GCharts.newLineChart(new Line[] {valuesLine}); // , redLine, orangeLine});

            // set the size 
            chart.setSize(x, y);
            chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(state.getMinInt(), state.getMaxInt()));

            chart.setTitle(title);

            // add grid 
            chart.setGrid(5, 20, 3, 2);

            // place three time stamps on y label... oldest, middle, and newest 
      
            // chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(Arrays.asList( "12", "56", "90" )));
            		
            		//state.getOldest().getAge(), ((TimedEntry) state.get((getState().size() / 2)))
        //            .getAge(), state.getNewest().getAge()), Arrays.asList(7, 50, 93)));

            
        
            
            // construct a URL that can be used to display this graph  
            return chart.toURLString();

        } catch (final Exception e) {
            return null;
        }
	}
}
