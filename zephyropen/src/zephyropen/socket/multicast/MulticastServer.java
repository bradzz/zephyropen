package zephyropen.socket.multicast;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * 	Start the chat server. Start new threads for a each connection on the given port
 *
 * Created: 2007.11.3
 * @author Brad Zdanivsky 
 */
public class MulticastServer {

	// store reference to all the currently active user's output connections 
	//
	// note: this vector is shared by all users/threads 
	//
	private Vector<PrintWriter> printers = new Vector<PrintWriter>(); 
	
	/**
	 * Threaded client handler
	 */
	class ConnectionHandler extends Thread {

		// reference to the client socket and in/out streams 
		private Socket clientSocket = null;
		private BufferedReader in = null;
		private PrintWriter out = null; 
	    
	    /**
	     * Construct this inner class
	     *
	     * @param clientSocket is the socket connection received from the client
	     */
	    public ConnectionHandler(Socket clientSocket) {
	    	  
	    	super(); 
	    	this.clientSocket = clientSocket;
	    	     
	    	// construct input and output streams 
	    	try {
	    		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);
	    	} catch (IOException e) {
			e.printStackTrace();
	    	} 
	    	  
	    	// keep track of all other user sockets output streams 
		printers.add(out);	
	    }
	      
	    
	    /**
	     * 	Run this thread by calling Thread.start() from main() of this class
	     */
	    public void run() {
	    	
		// notify other users of joining the group 
	    	sendToGroup(" has joined the group!");
	    	
	    	// log to console 
	    	System.out.println("currently [" + printers.size() + "] users are connected.");
	    	
	    	// show newly logged in user how many others are online 
	    	out.println("[" + clientSocket.getLocalPort() + "] currently [" + printers.size() + "] users are connected.");
	    	
	    	try {
				
	    		// loop on input from the client
	    		while(true) {
	 
	    			// blocking read from the client stream up to a '\n'  
	    			String str = in.readLine();
				     
	    			// client is terminating? 
	    			if(str == null) break;
                    		if(str.equals("quit")) break; 
                    
	    			// show client input and corresponding server side port number as well as shared counter 
	    			System.out.println("address [" + clientSocket + "] message [" + str + "]");
				    
	    			// echo input out on all open connections 
	    			sendToGroup( str ); 
	    		}
	    		
	    		// clean up 
			shutDown();

	    	} catch(Exception e) {
	    		shutDown();
	    	}	   
	    }
	    
	    // close resources 
	    private void shutDown(){
	    	
	    	// log to console, and notify other users of leaving 
			System.out.println("server: closing socket [" + clientSocket + "]");
			sendToGroup(" has left the group!");
		    	
			try {
				// close resources 
				printers.remove(out);
				in.close();
				out.flush();
				out.close();
				clientSocket.close();
			} catch (Exception e) {				
				e.printStackTrace();
			} 
				
			// show this users is no longer in the group 
			System.out.println("currently [" + printers.size() + "] users are connected.");
	    }
	      
	    // send input back to all the clients currently connected 
	    public void sendToGroup(String str){
	    	PrintWriter pw = null; 
	    	for(int c = 0 ; c < printers.size() ; c++){  	    	 
	    		pw = printers.get(c);
	    		
	    		// still connected?
	    		if( pw.checkError() ) {
	    			pw.close(); 
	    			printers.remove(pw);
	    		}
	    		
	    		// send to user(s) with sender's port number 
	    		else pw.println( "[" + clientSocket.getPort() + "] " + str); 
	    	}
	    }
	}//end inner class
   
   
   /**
    * Constructs a server with given port number.
    *
    * @param port is the port number to listen on
    * @throws an IOException if the ServerSocket fails
    */
   public MulticastServer(int port) throws IOException {

	   // server socket, using the specified port number
	   ServerSocket s = new ServerSocket(port);
	   System.out.println("\nserver: listening with socket [" + s + "] ");

	   // serve new connections until killed 
	   while(true){
		   try {
    		  
			   // new user has connected
			   Socket socket = s.accept();
			   System.out.println("server: connection accepted [" + socket + "]");
          
			   // start new thread, pass on connection 
			   new ConnectionHandler(socket).start(); 
    		  
	      } catch(Exception e) {
	          e.printStackTrace();
	      } 
      }
   }
   
   
   // driver
   public static void main(String args[]) throws IOException {

	   int portNumber = 4444;
	   
	   // get the arguments from the command line
	   if(args.length > 0) portNumber = Integer.parseInt(args[0]);

      // instantiate and execute the server
      new MulticastServer(portNumber);   	  
   }
}
