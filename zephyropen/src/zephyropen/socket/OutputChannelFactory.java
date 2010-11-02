package zephyropen.socket;

// import zephyr.framework.socket.tcp.tcpOutputChannel;
// import zephyr.framework.socket.udp.udpOutput;
import zephyropen.api.ZephyrOpen;
import zephyropen.socket.multicast.MulticastChannel;


/**
 * A factory to create output channel classes.
 * <p>
 * Created: 2005.11.08
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public class OutputChannelFactory /*implements Factory*/ {

   private static ZephyrOpen constants = ZephyrOpen.getReference();

   private OutputChannelFactory() {
   }

   /**
    * use props file to choose channel type
    *
    * @return the specified outputChannel
    */
   public static OutputChannel create() {

      ///System.out.println("OutputChannelFactory.create");

      String seviceType = constants.get("networkService");
     
      // String serverAddress = constants.get("serverAddress");
      // int serverPort = Integer.parseInt(constants.get("serverPort"));

      if (seviceType.equals("multicast")) {

          // return new MulticastOutput(serverAddress, serverPort);

         //
         // this class will look in properties file itself. as this does
         // both input and output, it must be singleton
         //
         return MulticastChannel.getReference();

      } 
      
      
      /* only multicast at this point 
       
       else if (seviceType.equals("udp")) {

         return new udpOutput(serverAddress, serverPort);

      } else if (seviceType.equals("tcp")) {

         return new tcpOutputChannel(serverAddress, serverPort);
      }

      */
      // never get here
      return null;
   }


   /**
    *
    * @param type of output channel to create 
    * @return the OutputChannel
   */
    public OutputChannel create(String type) {
    
    	constants.error("OutputChannel(): type needs to be implemented! ", this);
    	return  null;
    }
}
