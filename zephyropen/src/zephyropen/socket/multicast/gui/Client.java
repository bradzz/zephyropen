package zephyropen.socket.multicast.gui;

import java.io.*;
import java.net.*;

public class Client {
   
	public Client(String host, int port, final String usr, final String pass) throws IOException {
		try {

			// construct the client socket
			MulticastSocket s = null;

			// create a useful title
			String title = usr + s.getInetAddress().toString();

			// pass socket on to read and write swing components
			Frame frame = new Frame(new Input(s), new Output(s), title);

			// create and show this application's GUI.
			javax.swing.SwingUtilities.invokeLater(frame);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
  
   // driver
   public static void main(String args[]) throws IOException {
     
   }
}