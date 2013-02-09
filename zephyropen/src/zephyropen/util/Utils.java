package zephyropen.util;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import zephyropen.api.ZephyrOpen;

/**
 * Contains commonly used static utility methods.
 * 
 * Note: these methods must not write to the console unless if catching an exception
 * 
 * <p>
 * Created: 2003.09.05
 * 
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public class Utils {

	public static void addTree(File file, Collection<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
                for (File child : children) {
                        all.add(child);
                        addTree(child, all);
                }
        }
	}
	
	public static int countFiles(String path){
       Collection<File> all = new ArrayList<File>();
       addTree(new File(path), all);
       return all.size();
	}
	
		/*public static long countFileSizes(String path){

		long total = 0;
		Collection<File> all = new ArrayList<File>();
		addTree(new File(path), all);
		for (Object tmp : all) {
           File file = (File) tmp;
           total+= file.getTotalSpace();
		}
		
		System.out.println("cont: " + total + " lkup: " + new File(path).getTotalSpace());
	
		
		return (new File(path).getTotalSpace() / 1024) / 1000;
	}*/

	
    /**
     * Delays program execution for the specified delay.
     * 
     * @param delay
     *            is the specified time to delay program execution (milliseconds).
     */
    public static void delay(long delay) {

        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Delays program execution for the specified delay.
     * 
     * @param delay
     *            is the specified time to delay program execution (milliseconds).
     */
    public static void delay(int delay) {

        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
	 * 
	 */
    public static String getTime(long ms) {

        //  Sat May 03 15:33:11 PDT 2008
        String date = new Date(ms).toString();

        int index1 = date.indexOf(' ', 0);
        int index2 = date.indexOf(' ', index1 + 1);
        int index3 = date.indexOf(' ', index2 + 1);
        int index4 = date.indexOf(' ', index3 + 1);

        //System.out.println("1: " + index1 + " 2: " + index2 + " 3: " + index3 + " 4: " + index4);

        String time = date.substring(index3 + 1, index4);

        return time;
    }

    /*
	 * 
	 */
    public static String getTime() {
        return getTime(System.currentTimeMillis());
    }

    /**
     * Returns the specified double, formatted as a string, to n decimal places, as
     * specified by precision.
     * <p/>
     * ie: formatFloat(1.1666, 1) -> 1.2 ie: formatFloat(3.1666, 2) -> 3.17 ie:
     * formatFloat(3.1666, 3) -> 3.167
     */
    public static String formatFloat(double number, int precision) {

        String text = Double.toString(number);
        if (precision >= text.length()) {
            return text;
        }

        int start = text.indexOf(".") + 1;
        if (start == 0)
            return text;

        //  cut off all digits and the '.'
        //
        if (precision == 0) {
            return text.substring(0, start - 1);
        }

        if (start <= 0) {
            return text;
        } else if ((start + precision) <= text.length()) {
            return text.substring(0, (start + precision));
        } else {
            return text;
        }
    }

    /**
     * Returns the specified double, formatted as a string, to n decimal places, as
     * specified by precision.
     * <p/>
     * ie: formatFloat(1.1666, 1) -> 1.2 ie: formatFloat(3.1666, 2) -> 3.17 ie:
     * formatFloat(3.1666, 3) -> 3.167
     */
    public static String formatFloat(double number) {

        String text = Double.toString(number);
        if (ZephyrOpen.PRECISION >= text.length()) {
            return text;
        }

        int start = text.indexOf(".") + 1;
        if (start == 0)
            return text;

        if (start <= 0) {
            return text;
        } else if ((start + ZephyrOpen.PRECISION) <= text.length()) {
            return text.substring(0, (start + ZephyrOpen.PRECISION));
        } else {
            return text;
        }
    }
    
    /**
     * Returns the specified double, formatted as a string, to n decimal places, as
     * specified by precision.
     * <p/>
     * ie: formatFloat(1.1666, 1) -> 1.2 ie: formatFloat(3.1666, 2) -> 3.17 ie:
     * formatFloat(3.1666, 3) -> 3.167
     */
    public static String formatString(String number, int precision) {

        String text = number;
        if (precision >= text.length()) {
            return text;
        }

        int start = text.indexOf(".") + 1;

        if (start == 0)
            return text;

        // System.out.println("format string - found dec point at index = " + start );

        //  cut off all digits and the '.'
        //
        if (precision == 0) {
            return text.substring(0, start - 1);
        }

        if (start <= 0) {
            return text;
        } else if ((start + precision) <= text.length()) {
            return text.substring(0, (start + precision));
        }

        return text;
    }

    /**
     * Convert a <code>long</code> timestamp to a string
     * 
     * @param timestamp
     *            the timestamp.
     * @return String the formatted timestamp as a string.
     */
    public static final String toTimestampString(long timestamp) {

        StringBuffer ts = new StringBuffer();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST"));
        calendar.setTime(new Date(timestamp));

        //year
        int temp = calendar.get(Calendar.YEAR);
        ts.append(temp);

        //month
        temp = calendar.get(Calendar.MONTH);
        if (temp < 10) {
            ts.append("-0");
        } else {
            ts.append("-");
        }
        ts.append(temp);

        //day
        temp = calendar.get(Calendar.DAY_OF_MONTH);
        if (temp < 10) {
            ts.append("-0");
        } else {
            ts.append("-");
        }
        ts.append(temp);

        //hour
        temp = calendar.get(Calendar.HOUR_OF_DAY);
        if (temp < 10) {
            ts.append(" 0");
        } else {
            ts.append(" ");
        }
        ts.append(temp);

        //minute
        temp = calendar.get(Calendar.MINUTE);
        if (temp < 10) {
            ts.append("-0");
        } else {
            ts.append("-");
        }
        ts.append(temp);

        //second
        temp = calendar.get(Calendar.SECOND);
        if (temp < 10) {
            ts.append("-0");
        } else {
            ts.append("-");
        }
        ts.append(temp);

        /*
         * millisecond temp = calendar.get(Calendar.MILLISECOND); ts.append(".");
         * ts.append(temp);
         */

        return ts.toString();
    }

    /**
     * @param b
     *            is the byte to convert
     * @return a integer from the given byte
     * 
     *         public static int readUnsignedByte(byte b) { return (b & 0xff); }
     */

    /**
     * Basic byte array copy
     * 
     * @param dest
     * @param input
     * @param startIndex
     * @param size
     */
    public static void copy(final byte[] dest, final byte[] input, final int startIndex, final int size) {
        int c = 0;
        for (int i = startIndex; i < size; i++)
            dest[c++] = input[i];
    }

    /**
     * 
     * @param packet
     *            of bytes
     * @param index
     *            of the byte to parse in the byte array
     * @return a String of the indexed byte
     * 
     *         public static String parseString(byte[] packet, int index) { String hex =
     *         byteToHex(packet[index]); short value = Short.parseShort(hex, 16); return
     *         String.valueOf(value); }
     */

    /**
     * 
     * @param packet
     *            of bytes
     * @param index
     *            of the byte to parse in the byte array
     * @return a String of the indexed byte
     */
    public static short parseShort(byte[] packet, int index) {
        String hex = byteToHex(packet[index]);
        return Short.parseShort(hex, 16);
    }

    /**
     * Merge two bytes into a signed 2's complement integer
     * 
     * @param low
     *            byte is LSB
     * @param high
     *            byte is the MSB
     * @return a signed intt value
     */
    public static int merge(byte low, byte high) {
        int b = 0;
        b += (high << 8) + low;
        if ((high & 0x80) != 0) {
            b = -(0xffffffff - b);
        }
        return b;
    }

    /**
     * Merge two bytes into a unsigned integer
     * 
     * @param low
     *            byte is LSB
     * @param high
     *            byte is the MSB
     * @return an unsigned int value
     */
    public static int mergeUnsigned(byte low, byte high) {
        int lint = low & 0xff;
        int hint = high & 0xff;
        return (hint << 8 | lint);

    }

    /**
     * Convert a byte to a hex string.
     * 
     * @param data
     *            the byte to convert
     * @return String the converted byte
     */
    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convert a byte array to a hex string.
     * 
     * @param data
     *            the byte[] to convert
     * @return String the converted byte[]
     */
    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
        }
        return buf.toString();
    }

    /**
     * Convert an int to a hex char.
     * 
     * @param i
     *            is the int to convert
     * @return char the converted char
     */
    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9))
            return (char) ('0' + i);
        else
            return (char) ('a' + (i - 10));
    }

    /**
     * Convert a string into an array of hex bytes
     * 
     * @param data
     *            string in hex format (two chars per byte)
     * @return the pairs as an array of bytes
     */
    public byte[] getBytes(String data) {
        if (data == null)
            return null;
        return new BigInteger(data, 16).toByteArray();
    }

    /**
     * Gets the end of the byte array given.
     * 
     * @param b
     *            byte array
     * @param pos
     *            the position from which to start
     * @return a byte array consisting of the portion of b between pos and the end of b.
     */
    public static byte[] copy(byte[] b, int pos) {
        return copy(b, pos, b.length - pos);
    }

    /**
     * Gets a sub-set of the byte array given.
     * 
     * @param b
     *            byte array
     * @param pos
     *            the position from which to start
     * @param length
     *            the number of bytes to copy from the original byte array to the new one.
     * @return a byte array consisting of the portion of b starting at pos and continuing
     *         for length bytes, or until the end of b is reached, which ever occurs
     *         first.
     */
    public static byte[] copy(byte[] b, int pos, int length) {
        byte[] z = new byte[length];
        System.arraycopy(b, pos, z, 0, length);
        return z;
    }

    /**
     * Build a float from the first 4 bytes of the array.
     * 
     * @param b
     *            the byte array to convert.
     */
    public static float toFloat(byte[] b) {
        int i = toInt(b);
        return Float.intBitsToFloat(i);
    }

    /**
     * Build an int from first 4 bytes of the array.
     * 
     * @param b
     *            the byte array to convert.
     */
    public static int toInt(byte[] b) {
        return ((b[3]) & 0xFF) + (((b[2]) & 0xFF) << 8) + (((b[1]) & 0xFF) << 16) + (((b[0]) & 0xFF) << 24);
    }

    /** @return the message delta as a string for display */
    public static String getDeltaString(long last) {

        //if( last == 0 ) return "0.0";

        return Utils.formatString(Double.toString((System.currentTimeMillis() - last) / 1000L), 2) + " seconds";
    }

    /**
     * Basic byte array add to end of byte array.
     * 
     * @param dest
     * @param input
     * @param startIndex
     * @param bytes
     * 
     * @return true if successful
     */
    public static boolean add(final byte[] dest, final byte[] input, final int startIndex, final int bytes) {

        if (startIndex + bytes > dest.length) {
            // System.out.println("ZehyrUtils.add(d,i,s):  Cannot add more bytes than there are in destination array");
            // Cannot add more bytes than there are in destination array
            return false;
        }

        for (int i = 0; i < bytes; i++) {
            dest[i + startIndex] = input[i];
        }

        return true;
    }

	public static String getDate() {

        //  Sat May 03 15:33:11 PDT 2008
        String date = new Date().toString();

        int index1 = date.indexOf(' ', 0);
        int index2 = date.indexOf(' ', index1 + 1);
        int index3 = date.indexOf(' ', index2 + 1);
        int index4 = date.indexOf(' ', index3 + 1);

        //System.out.println("1: " + index1 + " 2: " + index2 + " 3: " + index3 + " 4: " + index4);

        String time = date.substring(index1 + 1, index4);

        return time;
	}

}
