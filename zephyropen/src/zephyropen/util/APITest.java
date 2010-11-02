/**
 * 
 */
package zephyropen.util;


import org.junit.Test;

import zephyropen.api.PrototypeFactory;
import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class APITest extends TestCase {
	
	@Test
    public void testEmptyCollection() {
        
        assertTrue("is true", PrototypeFactory.BIOHARNESS_PROTOTYPE.length == 6);
    	
    }


}
