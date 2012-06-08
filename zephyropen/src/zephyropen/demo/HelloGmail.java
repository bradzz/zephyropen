package zephyropen.demo;

import java.util.Enumeration;
import java.util.Properties;

import zephyropen.util.google.SendGmail;

public class HelloGmail {
	public static void main(String[] args) throws Exception {
		
		System.out.println("args: " + args.length);
		System.out.println(getProps());
		
		if(args.length!=2) return;
		
		SendGmail gmail = new SendGmail(args[0], args[1]);
		
		if (gmail.sendMessage("Hello Gmail", getProps(), ".classpath")){
			System.out.println("email sent");
		} else {
			System.out.println("email failed, check your settings");
		}
	}
	
	
	public static String getProps() {
		String out = new String();
		Properties sys = System.getProperties();
		Enumeration<Object>keys = sys.keys();
		while(keys.hasMoreElements()){
			String key = (String) keys.nextElement();
			String value = (String) sys.getProperty(key);
			out += key + " = " + value + "\n";
		}
		
		return out;
	}
}
