package developer.terminal.control.gui;

import java.io.*;
import java.net.*;

public class ChatClient {
   
	public ChatClient(String host, int port, final String usr, final String pass) throws IOException {
		try {

			// construct the client socket
			Socket s = new Socket(host, port);

			// create a useful title
			String title = usr + s.getInetAddress().toString();

			// pass socket on to read and write swing components
			ChatFrame frame = new ChatFrame(new InputField(s, usr, pass), new OutputArea(s), title);

			// create and show this application's GUI.
			javax.swing.SwingUtilities.invokeLater(frame);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
  
   // driver
   public static void main(String args[]) throws IOException {
      new ChatClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
   }
}