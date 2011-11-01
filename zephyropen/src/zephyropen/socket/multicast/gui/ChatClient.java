package zephyropen.socket.multicast.gui;

import java.io.*;
import java.net.*;

/**
 *	Start the chat client, and connect to the server using the given IP and port numbers 
 *
 * Created: 2007.11.3
 * @author Brad Zdanivsky 
 */
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
			e.printStackTrace();
		}
	}
  
   // driver
   public static void main(String args[]) throws IOException {

      int port = Integer.parseInt(args[1]);

      // instantiate and execute the client
      new ChatClient(args[0], port, args[2], args[3]);
   }
}