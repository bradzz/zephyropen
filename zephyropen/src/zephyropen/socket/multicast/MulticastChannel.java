package zephyropen.socket.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.command.CommandDispatcher;
import zephyropen.socket.AbstractOutputChannel;
import zephyropen.socket.InputChannel;
import zephyropen.socket.OutputChannel;
import zephyropen.xml.Parser;
import zephyropen.xml.XMLParser;

/**
 * <p> Package : zephyr.framework.socket.multicast 
 * <p> Created: May 26, 2005 : 5:26:00 PM
 * 
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public class MulticastChannel extends AbstractOutputChannel implements OutputChannel, InputChannel, Runnable {

	/** global constants */ 
	final private static ZephyrOpen constants = ZephyrOpen.getReference();
	final private static int BUFFER_SIZE = 1024;
	
	private static MulticastChannel singleton = null;
	private static Parser xmlParser = new XMLParser();
	
	private MulticastSocket serverSocket = null;
	private InetAddress groupAddress = null;
	private int groupPort = 0;
	private String local = null;
	private Thread server = null;
	private boolean loopback = true;
	

	/** @return a reference to this singleton class. */
	public static MulticastChannel getReference() {

		if (singleton == null) {
			singleton = new MulticastChannel();
		}
		return singleton;
	}

	/** Constructor */
	private MulticastChannel() {

		/** configuration via properties file, terminate if not found! */ 
		String address = constants.get(ZephyrOpen.address);
		String port = constants.get(ZephyrOpen.port);

		if (address == null) {
			constants.error("serverAddress not found in properties file, terminate!", this);
			constants.shutdown();
		}

		if (port == null) {
			constants.error("serverPort not found in properties file, terminate!", this);
			constants.shutdown();
		}

		try {

			groupPort = Integer.parseInt(port);

			/** get group ip */
			groupAddress = InetAddress.getByName(address);

			/** construct the server socket */
			serverSocket = new MulticastSocket(groupPort);

			/** join this group */
			serverSocket.joinGroup(groupAddress);

			/** find our ip */
			local = constants.get(ZephyrOpen.localAddress);

		} catch (Exception e) {
			constants.shutdown(e);
		}

		/** start thread, block wait on input from socket */
		server = new Thread(this);
		server.setDaemon(true);
		server.start();	
	}

	/** @return the address of this node */
	public String getLocalAddress() {
		return local;
	}

	/** @param enable will enable or disable loop back if we want to talk to ourselves */
	public void setLoopback(boolean enable) {
		loopback = enable;
	}

	/** Executes this threaded class. */
	public void run() {
   
      try {

         /** loop until system termination */
         while (true) {

            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            /** block on command input */
            serverSocket.receive(packet);       
            	
            String input = new String(packet.getData()).trim();

            /** get sending node's addr */
            String sendersIp = ((InetAddress) packet.getAddress()).getHostAddress();
            
            //if( constants.getBoolean(ZephyrOpen.frameworkDebug))
            	//constants.info("sender: " + sendersIp + " : " + input);
            
            /** test the input */
            if( valid(input, sendersIp) ) {  
            	
           		/** build a command and dispatch it to the API */
            	Command command = xmlParser.parse(input); 
            	
            	// TODO: don't over write existing one? 
            	/** stamp on creation */ 
            	command.add(ZephyrOpen.TIME_MS, String.valueOf(System.currentTimeMillis()));
		    
            	/** dispatch the command */
            	CommandDispatcher.dispatch(command); 	
                	
            }
         }
      } catch (Exception e) {
    	  e.printStackTrace(System.err);
    	  constants.shutdown(e);
      }
   }
	
   /**
    * Is this a valid xml command to be dispatch? and ARE WE IN LOOPBACK MODE? 
    * 
    * @param data is the packet to check 
    * @param ip is the sender's IP
    * @return true if this packet should be parsed and dispatched 
    */
   private boolean valid(String data, String ip){
	   
       /** ignore messages coming from us? */
       if( ! loopback) {
    	   if( local.equals(ip) ) {   		  
    		   return false;		    
    	   }
       }
  
       /** sanity test */
       if( data == null) return false;
       if( data.equals("")) return false;
    
       return true; 
   }
   
	/** @param out is a string to write to the socket */
	private void write(String out) {

		try {

			/** dump it into the socket */
			serverSocket.send(new DatagramPacket(out.getBytes(), out.length(), groupAddress, groupPort));

		} catch (Exception e) {
			constants.error("unable to write to socket", this); 
		}
	}

	/** @param command to send to the channel  */
	public void write(Command command) {
	
		if( constants.getBoolean(ZephyrOpen.showLAN))
			command.add(ZephyrOpen.localAddress, constants.get(ZephyrOpen.localAddress) ); 
		
		if( constants.getBoolean(ZephyrOpen.externalLookup))
			command.add(ZephyrOpen.externalAddress, constants.get(ZephyrOpen.externalAddress));
		
		// TODO: review this
		// required since 2.0.1
		command.add(ZephyrOpen.userName, constants.get(ZephyrOpen.userName));
		
		write(command.toString());
	}
}
