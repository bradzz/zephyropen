package zephyropen.socket.multicast.gui;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *	A minimal SWING input field to write to a given socket 
 *
 * @author Brad Zdanivsky  
 * Created: 2007.11.3
 */
public class InputField extends JTextField implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private Socket socket = null;
	private PrintWriter out = null;
	private String userInput = null;
	  
	// build the input field 
	public InputField(Socket s) {
	    super("cam");	      
	    socket = s; 
	      
	    try{
	    	out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
	  	} catch (Exception e) {
	    	System.out.println("InputField() : " + e.getMessage());
	   	}
	     
	   	// listen for keyboard events
      	addActionListener(this);
	 } 
			   
	// Manager user input 
	public void actionPerformed(ActionEvent evt) {	
		try {

	    	// get keyboard input
	        userInput = getText().trim();

	        // clear for next input 
	        setText("");

	        // log to console 
	        System.out.println("user typed :" + userInput);
			  
		if( socket == null ) System.exit(-1); 

	        // send the user input to the server if is valid 
		if( userInput.length() > 0) out.println(userInput);
			 
	  	} catch (Exception e) {
	        	System.out.println("InputField.actionPerformed() : " + e.getMessage());
	        	System.exit(0);
	    }   
	} 
}
