package zephyropen.util.google;

//import zephyropen.state.EventCounter;
import java.net.URL;

import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.util.Utils;

import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.GoogleOMeter;

/**
 * <p>
 * A wrapper for the google RESTful graphing service. Add new data points, and create
 * URL's for chart images.
 * 
 * <p>
 * Docs here: http://code.google.com/p/charts4j/
 * 
 * @see State
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class GoogleMeter extends GoogleChart {

    /** swing requires */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for this class
     * 
     * @param title
     *            that will appear on the graph
     * @param units
     *            is the string to add to the title (eg ms, meters etc)
     */
    public GoogleMeter(String title, String units) {

        this.state = new State(title, 10); //, 3, 5);
        this.title = title;
        this.units = units;
    }

    /** @return construct a URL that can be used to display this graph */
    @Override
    public String getURLString(final int x, final int y) {

        String value = Utils.formatFloat(state.getAverage(), ZephyrOpen.PRECISION);

        try {

            GoogleOMeter chart = GCharts.newGoogleOMeter(state.getAverage(), "Fast", "", Color.newColor("1148D4"), Color.newColor("5766DE"), Color
                    .newColor("DB3270"));

            //	 , Color.newColor("D41111")); );
            chart.setTitle(title + " = " + value, Color.BLACK, 14);
            chart.setSize(x, y);

            //     LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.BLUE, 100);
            //     fill.addColorAndOffset(Color.RED, 0);
            //     chart.setBackgroundFill(fill);
            //     chart.setAreaFill(Fills.newSolidFill(Color.newColor(Color.GRAY, 70)));

            return chart.toURLString();

        } catch (Exception e) {
            constants.info(e.getMessage(), this);
            return null;
        }
    }

	@Override
	public String getURLString(int x, int y, String title) {
		// TODO Auto-generated method stub
		return null;
	}
    
    /** @return construct a URL that can be used to display this graph
    @Override
    public String getURLString(){ // final int x, final int y) {

    	int x = constants.getInteger(ZephyrOpen.xSize);
		int y = constants.getInteger(ZephyrOpen.ySize);
		
		if( x == ERROR ) x = DEFAULT_X_SIZE;
		if( y == ERROR ) y = DEFAULT_Y_SIZE;
    	
        String value = Utils.formatFloat(state.getAverage(), ZephyrOpen.PRECISION);

        try {

            GoogleOMeter chart = GCharts.newGoogleOMeter(state.getAverage(), "Fast", "", Color.newColor("1148D4"), Color.newColor("5766DE"), Color
                    .newColor("DB3270"));

            //	 , Color.newColor("D41111")); );
            chart.setTitle(title + " = " + value, Color.BLACK, 14);
            chart.setSize(x, y);

            //     LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.BLUE, 100);
            //     fill.addColorAndOffset(Color.RED, 0);
            //     chart.setBackgroundFill(fill);
            //     chart.setAreaFill(Fills.newSolidFill(Color.newColor(Color.GRAY, 70)));

            return chart.toURLString();

        } catch (Exception e) {
            constants.info(e.getMessage(), this);
            return null;
        }
    }*/
    
}
