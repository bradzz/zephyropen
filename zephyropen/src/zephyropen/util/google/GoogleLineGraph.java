package zephyropen.util.google;

import java.util.Arrays;
import java.util.Date;

import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.state.TimedEntry;

import com.googlecode.charts4j.AxisLabelsFactory;
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

            final String titleText = "[" + constants.get(ZephyrOpen.userName) + "]  " 
            + title.toUpperCase() + " = " 
            + (state.getNewest()).getValueString() + " " + units + "   " + " (" + state.size() + ") "
            + new Date().toString() ;
      
            chart.setTitle(titleText);

            // add grid 
            chart.setGrid(5, 20, 3, 2);

            // place three time stamps on y label... oldest, middle, and newest 
            chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(Arrays.asList(state.getOldest().getAge(), ((TimedEntry) state.get((getState().size() / 2)))
                    .getAge(), state.getNewest().getAge()), Arrays.asList(7, 50, 93)));

            // construct a URL that can be used to display this graph  
            return chart.toURLString();

        } catch (final Exception e) {
            // constants.error(e.getMessage(), this);
            return null;
        }
    }
    

    /** @return construct a URL that can be used to display this graph */
    @Override
    public String getURLString(){ 

        try {
        	
        	int x = constants.getInteger(ZephyrOpen.xSize);
    		int y = constants.getInteger(ZephyrOpen.ySize);
    		
    		if( x == ERROR ) x = DEFAULT_X_SIZE;
    		if( y == ERROR ) y = DEFAULT_Y_SIZE;

            final Line valuesLine = Plots.newLine(DataUtil.scale(state.getScaledData()));
            valuesLine.setColor(dataColor);

            // scaled data
            final int averageValue = (int) state.scale(state.getAverage());
            final Line avgLine = Plots.newLine(new Data(averageValue, averageValue));
            avgLine.setColor(averageColor);

            //Line meanLine = Plots.newLine(DataUtil.scale(avg.getScaledData()));
            //meanLine.setColor(Color.GOLD);

            //final Line meanH = Plots.newLine(new Data(state.getAverageValue(), state.getAverageValue()));
            //meanH.setColor(Color.BLACK);

            // put lines on the graph 
            final Line[] lines = new Line[2];
            lines[0] = valuesLine;
            lines[1] = avgLine;
            //lines[2] = meanLine;
            //lines[3] = meanH;

            // new chart 
            final LineChart chart = GCharts.newLineChart(lines);

            // set the size 
            chart.setSize(x, y);
            chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(state.getMinInt(), state.getMaxInt()));

            final String titleText = // "[" + constants.get(ZephyrOpen.userName) + "]  " 
            title.toUpperCase() + " = " 
            + (state.getNewest()).getValueString() + " " + units + "   " + " (" + state.size() + ") "
            + new Date().toString() ;
            //	" D:" + getState().getDelta() + ")";  

            //	" A:" + state.getAverageLevel() +
            //	" T:" + state.getThreshold()
            //	")";

            //[" + Utils.getTime() + "]"; 
            chart.setTitle(titleText);

            //double max = 
            //((double)super.maxIndex / (double)this.size())*100;

            //double min = 
            //((double)super.minIndex / (double)this.size())*100;

            //if( minIndex < 2 ) minIndex = 2;
            //if( maxIndex > 98 ) maxIndex = 98;

            // add inflection markers 
            //chart.addVerticalRangeMarker(max-1, max+1,
            //	Color.newColor(Color.GREEN, 25));

            //    chart.addVerticalRangeMarker(min-1, min+1,
            //     		Color.newColor(Color.RED, 25));

            // System.out.println( "max index = " + super.maxIndex +
            //	" % = " + max);
            // System.out.println( "min index = " + super.minIndex +
            //	" % = " + min);

            // add grid 
            chart.setGrid(5, 20, 3, 2);

            // place three time stamps on y label... oldest, middle, and newest 
            chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(Arrays.asList(state.getOldest().getAge(), ((TimedEntry) state.get((getState().size() / 2)))
                    .getAge(), state.getNewest().getAge()), Arrays.asList(7, 50, 93)));

            // construct a URL that can be used to display this graph  
            return chart.toURLString();

        } catch (final Exception e) {
            // constants.error(e.getMessage(), this);
            return null;
        }
    }
}
