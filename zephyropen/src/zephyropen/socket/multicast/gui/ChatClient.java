package zephyropen.socket.multicast.gui;

import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 *	Start the chat client, and connect to the server using the given IP and port numbers 
 *
 * Created: 2007.11.3
 * @author Brad Zdanivsky 
 */
public class ChatClient {
   
   /**
    * Constructs an chat client with a specified server name and a
    * specified port number.
    *
    * @param host is the server name.
    * @param port is the server port number.
    */
   public ChatClient(String host, int port) throws IOException {  
	   try {
		   
		// construct the client socket
		Socket s = new Socket(host, port);
				
		// create a useful title 
		String title = "Connected " + s.toString();
		   	
		// pass socket on to read and write swing components 
      		ChatFrame frame = new ChatFrame(new InputField(s), new OutputArea(s), title);
      		
      		// create and show this application's GUI. 		
      		javax.swing.SwingUtilities.invokeLater(frame);
     		
      } catch(Exception e) {
    	  e.printStackTrace();
      }
   }
  
   // driver
   public static void main(String args[]) throws IOException {

      // get the parameters from the command line
      String hostname = args[0];
      int port = Integer.parseInt(args[1]);

      // instantiate and execute the client
      new ChatClient(hostname, port);
   }
}