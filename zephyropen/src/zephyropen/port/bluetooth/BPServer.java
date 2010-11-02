package zephyropen.port.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import zephyropen.api.ZephyrOpen;

/**
 * Bluetooth Service Advertising an SPP server. Clients can search for this service and connect. 
 * The server blocks on requests and sends back a simple text string. 
 * 
 * @see JSR82-specification for more info on server advertising.
 */
public class BPServer {
	
	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	
    /** Protocol name entry from connection URL */
    private String connProtocol;
     
    /** Service class id entry from connection URL */
    private String connServiceClassId;
     
    /** Target entry from connection URL */
    private String connTarget;
    
    /** Name entry from connection URL */
    private String connServiceName;
    
    /** String containing server response */
    private static String serverResp;
    
    /** Stores a reference to incoming connection notifier */
    private StreamConnectionNotifier conNotifier;
    
    /** count connections */
    private  static int i = 0; 
    
    /** Constructs the object and initializes server */
    public BPServer() {
    	
    	constants.init();
        
        /** RFCOMM protocol */
        this.connProtocol = "btspp";
        
        /** localhost as we are a server */
        this.connTarget = "localhost";
        
        /** random 128-bit id */
        this.connServiceClassId = "11111111111111111111111111111111";
        
        /** name of service when searched */ 
        this.connServiceName = "PWAccessP";
        
        /** simple text reply */ 
        serverResp = "hello world \n\r testing 1 2 3 ";
    }
   
    /** Method creates a connection notifier and initializes a connection */
    public void startSerialPortServer() {
        
        String url = connProtocol +
                "://" +
                connTarget +
                ":" +
                connServiceClassId +
                ";" +
                "name=" + connServiceName;
        
        constants.info("listening on url = " + url, this);
        //constants.info("thread count = " + Thread.activeCount() , this);
        
        try {
          
            /** Open the server and block on connection */ 
        	conNotifier = (StreamConnectionNotifier) Connector.open(url, Connector.READ, true); 
        	constants.info( "Blocking, waiting... "); 
        	final StreamConnection connection = (StreamConnection) conNotifier.acceptAndOpen();
            constants.info( "Incomming connection..."); 
            
            doConnReply( connection );
            conNotifier.close();
            
        } catch ( Exception e ) {
        	System.out.println();
        	constants.error("serial start error : " + e.getMessage() , this);
        }
    }
    

    public static String readString( DataInputStream inputStream ) throws IOException {
		String reply = new String();
		byte[] buffer = new byte[256];

		int count = 0;
		while (count > -1) {

			count = inputStream.read(buffer);
			if (count != -1) {
				reply += to(buffer, count);
			}
		}
		return reply.trim();
	}
	public static String to(byte[] bytes, int count) {
		String s = new String();
		for (int i = 0; i < count; i++) {

			Character ch = new Character((char) bytes[i]);
			if (ch != null) {
				if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch))
					s += ch;
			}
		}
		return s;
	}

	
	
    /**
     * Opens a DataOutputStream via connection member variable.
     * Writes a string from strServerResp to the stream and closes a stream.
     * @see DataOutputStream
     * @see StreamConnection
     */
    protected static void doConnReply( StreamConnection connection ) {
    	
        DataOutputStream out = null;
        constants.info( i++ + " Sending server response [" + i++ + "]..." + serverResp); 
        

        DataInputStream inputStream;
        

		try {
			
			inputStream = connection.openDataInputStream();

			constants.info( "reply = " + readString(inputStream));
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        try {
            out = connection.openDataOutputStream();
            out.writeChars(serverResp);
            out.flush();
            out.close();
        } catch ( Exception e) {
        	constants.error( "Error writing to the bt output stream!");
        	constants.error(e.getMessage());
            return;
        } finally {
            try {
				out.close();
			} catch (IOException e) {
				constants.error("Error closing the bt output stream!");
				constants.info(e.getMessage());
			}
        }
    }
    
    /** test for this class -- no args needed */
	public static void main(String[] args) {
	
		BPServer spp = new BPServer();
		
		while(true)
			spp.startSerialPortServer();

	}    
}