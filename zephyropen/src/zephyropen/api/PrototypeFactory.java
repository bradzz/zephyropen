package zephyropen.api;

/**
 * 
 * <p>
 * Create the required type of Port for connecting to a given device
 * <p>
 * Manage all known device names and XML prototypes
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * 
 */
public class PrototypeFactory {

	/** Device types */
	public final static int ERROR = 0;

	public final static int BIOHARNESS = 1;

	public final static int HXM = 2;

	public final static int HRM = 3;

	public final static int POLAR = 4;

	public final static int WII = 5;

	public final static int MOTION_NODE = 5;

	public static final int ARDUINO = 6;

	public static final int ELEVATION = 7;

	/** Known types, names of XML tags */
	public final static String cadence = "cadence";

	public final static String strides = "strides";

	public final static String heart = "heart";

	public final static String beat = "beat";

	public static final String rr = "rr";

	public final static String type = "type";

	public final static String battery = "battery";

	public final static String speed = "speed";

	public final static String distance = "distance";

	public final static String respiration = "respiration";

	public final static String temperature = "temperature";

	public final static String connection = "connection";

	public final static String posture = "posture";

	public final static String wii = "wii";

	public final static String pitch = "pitch";

	public final static String accel = "accel";

	public final static String roll = "roll";

	public final static String mote = "mote";

	public final static String yaw = "yaw";

	public final static String polar = "polar";

	public final static String seat = "seat";

	public final static String seconds = "seconds";

	public final static String elevation = "elevation";

	public final static String back = "back";

	public final static String bioharness = "bioharness";

	public final static String hxm = "hxm";

	public final static String hrm = "hrm";

	/** tags than any XML message must have in order to be valid */
	public static final String[] WII_PROTOTYPE = { accel, roll, pitch, yaw, mote };

	/** Polar HRM via USB COM Port */
	public static final String[] POLAR_PROTOTYPE = { beat, heart };

	/** launching new process */
	public static final String[] LAUNCH_PROTOTYPE = { ZephyrOpen.deviceName, ZephyrOpen.kind, ZephyrOpen.code };

	/** minimal command */
	public static final String[] DEFAULT_PROTOTYPE = { ZephyrOpen.action };

	public static final String[] DISCOVERY_PROTOTYPE = { ZephyrOpen.address, ZephyrOpen.deviceName };

	/** List the required tags for the HXM XML */
	public final static String[] HXM_PROTOTYPE = { beat, heart, battery, speed, strides, cadence };

	/** List the required tags for the HRM XML */
	public final static String[] HRM_PROTOTYPE = { beat, heart, battery };

	/** List the required tags for the Bioharness XML */
	public final static String[] BIOHARNESS_PROTOTYPE = { beat, heart, respiration, posture, temperature, battery };

	public static final String[] ELEVATION_PROTOTYPE = { seat, back };

	/** Determine the type of device this is via the naming convention */
	public static int getDeviceType(String deviceName) {

		// System.out.println("PrototypeFactory get device type = " + deviceName);
		
		deviceName = deviceName.toLowerCase();

		if (deviceName.startsWith(hxm))
			return HXM;

		else if (deviceName.startsWith(hrm))
			return HRM;

		else if (deviceName.startsWith("bh zbh"))
			return BIOHARNESS;

		else if (deviceName.startsWith("zbh"))
			return BIOHARNESS;

		else if (deviceName.equals(bioharness))
			return BIOHARNESS;

		else if (deviceName.toLowerCase().equals(polar))
			return POLAR;

		else if (deviceName.toLowerCase().equals(wii))
			return WII;

		else if (deviceName.toLowerCase().equals(elevation))
			return ELEVATION;

		// error state
		return ERROR;
	}

	/** Determine the type of device this is via the naming convention */
	public static String getDeviceTypeString(String deviceName) {

		int kind = getDeviceType(deviceName);

		if (kind == HXM)
			return hxm;

		else if (kind == HRM)
			return hrm;

		else if (kind == BIOHARNESS)
			return bioharness;

		else if (kind == POLAR)
			return polar;

		else if (kind == WII)
			return wii;

		else if (kind == ELEVATION)
			return elevation;

		// error state
		return ZephyrOpen.zephyropen;
	}

	/** @returns the associated prototype */
	public static String[] create(String type) {

		// manage know cases here
		if (type.equals(ZephyrOpen.launch))
			return LAUNCH_PROTOTYPE;

		if (type.equals(ZephyrOpen.discovery))
			return DISCOVERY_PROTOTYPE;

		// is a device
		int kind = getDeviceType(type);

		if (kind == HXM)
			return HXM_PROTOTYPE;

		else if (kind == HRM)
			return HRM_PROTOTYPE;

		else if (kind == BIOHARNESS)
			return BIOHARNESS_PROTOTYPE;

		else if (kind == POLAR)
			return POLAR_PROTOTYPE;

		else if (kind == WII)
			return WII_PROTOTYPE;

		else if (kind == ELEVATION)
			return ELEVATION_PROTOTYPE;

		return DEFAULT_PROTOTYPE;
	}
}
