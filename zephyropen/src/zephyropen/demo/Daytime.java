package zephyropen.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class Daytime {

	   // reference to the client socket
	   private Socket socket = null;

	   // stores the computer name of the server
	   private String serverName = null;

	   // stores the port number that the server is listening on
	   private int serverPort = 13;


	   /**
	    * Constructs a Daytime Protocol Client with a specified server address 
	    *
	    * @param host is the server name.
	    */
	   public Daytime(String host) {
	      serverName = host;
	   }

	   /** Connects the client to the server. */
	   public void connect() throws IOException {
		   
		   System.out.println("connect: " + serverName + " port: " + serverPort);

	      try {

	         // open a socket to the server
	         socket = new Socket(serverName, serverPort);

	      }
	      catch(Exception e) {
	         e.printStackTrace();
	      }
	      finally {
	         socket.close();
	      }
	   }

	   /** Disconnects the client from the server. */
	   public void disconnect() throws IOException {
	      socket.close();
	   }

	   // driver
	   public static void main(String args[]) {

	      // get the parameters from the command line
	      String hostname = args[0];
	      
	      // instantiate and execute the client
	      Daytime client = new Daytime(hostname);

	      try {
	         client.connect();
	         client.disconnect();
	      }
	      catch(Exception e) {
	         System.out.println("client: execution error");
	         e.printStackTrace();
	      }
	      
	      
	      System.out.println("..done");
	   }
	}