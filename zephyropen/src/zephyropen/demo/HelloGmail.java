package zephyropen.demo;

import zephyropen.util.google.SendGmail;

public class HelloGmail {
	public static void main(String[] args) throws Exception {
		
		System.out.println("args: " + args.length);
		
		if(args.length!=2) return;
		
		SendGmail gmail = new SendGmail(args[0], args[1]);
		
		if (gmail.sendMessage("Important Event", "testing attachment", ".classpath")){
			System.out.println("email sent");
		} else {
			System.out.println("email failed, check your settings");
		}
	}
}
