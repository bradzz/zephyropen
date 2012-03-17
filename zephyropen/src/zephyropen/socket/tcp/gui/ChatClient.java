package zephyropen.socket.tcp.gui;

import java.io.*;
import java.net.*;

/**
 * Start the chat client, and connect to the server using the given IP and port
 * numbers
 * 
 * Created: 2007.11.3
 * 
 * @author Brad Zdanivsky
 */
public class ChatClient {

	public ChatClient(String host, int port, final String usr) throws IOException {
		try {

			// construct the client socket
			Socket s = new Socket(host, port);

			// create a useful title
			String title = usr + " " + s.getInetAddress().toString();

			// pass socket on to read and write swing components
			ChatFrame frame = new ChatFrame(new InputField(s, usr), new OutputArea(s), title);

			// create and show this application's GUI.
			javax.swing.SwingUtilities.invokeLater(frame);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// driver
	public static void main(String args[]) throws IOException {

		int portNumber = 4444;
		String user = System.getProperty("user.name");
		String ip = "localhost";
		
		// TODO: get the arguments from the command line

		// instantiate and execute the client
		new ChatClient(ip, portNumber, user);
	}
}