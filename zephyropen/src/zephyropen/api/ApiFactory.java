package zephyropen.api;

import java.util.Enumeration;
import java.util.Hashtable;

//import zephyropen.framework.factory.Factory;

// 
// TODO: add a 'promiscuous' ability to insert net API's automatically 
// TODO: add vectors to deal with multiple API mappings 
//

/**
 * <p/>
 * Create API's based on factory Input
 * <p/>
 * Created: May 31, 2005 : 5:38:32 PM
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * @author Peter Brandt-Erichsen
 */
public class ApiFactory {
	
	/** framework configuration */
	private final ZephyrOpen constants = ZephyrOpen.getReference();

	// holds the API's recognized by the application
	private static Hashtable<String, API> apiTable = null;
	private static ApiFactory singleton = null;

	public static ApiFactory getReference() {
		if (singleton == null) 
			singleton = new ApiFactory();

		return singleton;
	}

	/** Constructs an API factory with zero parameters. */
	private ApiFactory() {
		apiTable = new Hashtable<String, API>();
	}

	/**
	 * Returns an instantiated API as specified by the command parameter. Null
	 * if not found
	 * 
	 * @param deviceName
	 *            specifies which API to return
	 */
	public API create(String deviceName) {
		
		if(apiTable.isEmpty()) return null; 

		String tag = PrototypeFactory.getDeviceTypeString(deviceName);

		return apiTable.get(tag);
	}

	/**
	 * Loads the API table with API classes.
	 */
	public void add(API api) {

		if (api == null)
			return;

		String tag = PrototypeFactory.getDeviceTypeString(api.getDeviceName());

		apiTable.put(tag, api);
	}
	
	/**
	 * Loads the API table with API classes.
	 */
	public void add(String tag, API api) {

		if (api == null || tag == null || tag.equals(""))
			return;

		apiTable.put(tag, api);
	}

	/**
	 * Remove the API from the table
	 */
	public void remove(String apiName) {
		apiTable.remove(apiName);
	}

	/**
	 * get all the API from the table
	 */
	public Enumeration<String> getApiList() {
		return apiTable.keys();
	}

	/**
	 * @return true if the given class is in the api list 
	 */
	public boolean containsClass(String clazzName) {
		
		API api = null;
		for (Enumeration<String> e = apiTable.keys(); e.hasMoreElements();) {
			
			api = apiTable.get(e.nextElement());
				
			constants.info(constants.get(ZephyrOpen.user) + " :: " + api.getClass().getName(), this);
			
			if(clazzName.equals(api.getClass().getName()))
				return true;
			
		}
		
		// not found
		return false;
	}
	
	/**
	 * @return a string format of the API's in the list
	 */
	public String toXML() {

		StringBuffer buff = new StringBuffer();
		buff.append("<api>");

		String tag = null;
		for (Enumeration<String> e = apiTable.keys(); e.hasMoreElements();) {

			tag = e.nextElement();
			buff.append("<" + tag + ">");
			buff.append(getXML(PrototypeFactory.create(tag)));
			buff.append("</" + tag + ">");
		}
		buff.append("</api>");

		return buff.toString();
	}
	
	/**
	 * @return a string format of the API's in the list
	 */
	public String toString() {
		
		if(apiTable.isEmpty()) return null;

		StringBuffer buff = new StringBuffer();
		for (Enumeration<String> e = apiTable.keys(); e.hasMoreElements();) 
			buff.append(e.nextElement() + ", ");

		return buff.toString();
	}

	/** turn into xml */
	private String getXML(String[] tag) {
		
		if(apiTable.isEmpty()) return null;
		
		String str = "";
		for (int i = 0; i < tag.length; i++)
			str += "<" + tag[i] + "/>";

		return str;
	}
}
