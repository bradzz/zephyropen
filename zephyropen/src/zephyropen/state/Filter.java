/*
 * Created on 2010-04-19
 * @author brad
 * @version $Id: Filter.java 46 2010-04-24 23:16:43Z brad.zdanivsky $
 */
package zephyropen.state;

public interface Filter {

    // boolean isValid(double input, Filter filter);

    int getMax();

    int getMin();

    double getThreshold();

}
