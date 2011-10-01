package zephyropen.socket.multicast.gui;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

/**
 *	A minimal SWING input field to read from a given socket 
 *
 * @author Brad Zdanivsky  
 * Created: 2007.11.3
 */
public class OutputArea extends JTextArea implements Runnable {
	
	private Socket socket = null; 
	private BufferedReader in = null;
	
	// Create a text area for user input 
	public OutputArea(Socket socket) {
		this.socket = socket;
		
      	// don't alow editing the textArea
      	setEditable(false);
     	
		try{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
	        System.out.println("OuputArea() : " + e.getMessage());
	    }
	    
	    // read from sock as a new thread 	    
	    Thread thread = new Thread(this);
	    thread.start();
	}
	
   	// Manage input coming from server 
   	public void run() {
   	   
       	   // loop on input from socket
           String input = null;
   	   while(true){		   
            try {
            
		// block on input and then update text area 
            	input = in.readLine();

		append("\n <!--   " + new Date() + "    -->");
	    	append("\n" + input + "\n");

	    	// move focus to it new line we just added 
	    	setCaretPosition(getDocument().getLength());
            	
             } catch (Exception e) {
	          System.out.println("OuputArea() : " + e.getMessage());
	          System.exit(0);
	     }     
   	   }  
	}
}