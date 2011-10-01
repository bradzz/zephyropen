package zephyropen.socket.multicast;

import java.io.*;
import java.net.*;

/**
 *	Start the chat client on the given IP, and port numbers 
 *
 * Created: 2007.11.3
 * @author Brad Zdanivsky 
 */
public class EchoClient implements Runnable {
   
   private Thread listener = null; 
   private Socket socket = null; 
   private BufferedReader in = null;
   private PrintWriter out = null;
   private boolean running = true;

   /**
    * Constructs an chat client with a specified server name and a
    * specified port number.
    *
    * @param host is the server name.
    * @param port is the server port number.
    */
   public EchoClient(String host, int port) throws IOException {  
	   try {
      
		   // construct the client socket
		   socket = new Socket(host, port);
		   in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
				   
      } catch(Exception e) {
    	  e.printStackTrace();
      } 
      
      // start listening for server input 
      listener = new Thread( this );
      listener.setDaemon(true);
      listener.start();
   }
  
   
   /**
    * 	Manage input coming from server 
    */
   public void run() {
	  
   	   String str = null; 
   	   
       // loop on input from socket
   	   while(running){
   		   
            try {
            	
				// block on input
            	str = in.readLine();
			
            	// did the sever shutdown? 
            	if( str == null ) break;
                if( str.equals("") ) break;
                
            	// dump to console 
            	System.out.println( "\n" + str );
            
            	// print new prompt because we just wrote to the console 
            	prompt(); 
            	
            } catch (IOException e) {
				e.printStackTrace();
			}     
   	   	}  
   	   
   	   // close listen thread 
   	   running = false; 
	}
   
   
   /** 
    *    Manager user input 
    */
   private void execute(){
	    
	   try{ 
		   
		   // loop on user keyboard input
		   byte[] input = new byte[256];
       
		   while(running) {	  
			   
			   // prompt for input 
			   prompt();
			   
			   // retrieve the user input from the command line -- blocking read 
			   int count = System.in.read(input);

			   // convert the user input to a String
			   String userInput = toString(input, count);	
			   
			   // termination instruction from the user?
			   if(userInput.equals("quit")) running = false; 
			   
			   // send the user input to the server if is valid 
			   if( userInput.length() > 0) out.println(userInput);		     		   
		   }

		   // clean up
		   shutDown();
		   
	   } catch (Exception e) {
			e.printStackTrace(); 
			shutDown(); 
	   }
   }


   /** 
    * We don't want to see our own output coming back from the server 
    * 
    * @param string is the input from the server in the format  
    * message format : [xxxxxx] : message text
    */
  // private boolean isLoopback(String str){
	//   return false;
   //}

   
   /**
    *   Safely close down the client 
    */
   private void shutDown(){
	   
	    // close resources
	    try {   
			in.close();
			out.close();
			socket.close(); 
		} catch (IOException e) {
			System.out.println("\nclient.shutdown() - did NOT exit safely");
			e.printStackTrace();
		} 	
		System.out.println("\nclient.shutdown() - exited safely");
   }
   
   
   // Converts the console-based input byte array to a string
   private String toString(byte input[], int length) {
      if(length <= 0) length = 1;
      return new String(input, 0, length).trim();
   }

   
   // Displays a command prompt to the screen
   private void prompt() {
      System.out.print( socket.getLocalPort() + " >");
   }

   // driver
   public static void main(String args[]) throws IOException {

      // get the parameters from the command line
      String hostname = args[0];
      int port = Integer.parseInt(args[1]);

      // instantiate and execute the client
      new EchoClient(hostname, port).execute();
   }
}
