package zephyropen.socket;

import zephyropen.api.ZephyrOpen;
import zephyropen.socket.InputChannel;
import zephyropen.socket.multicast.MulticastChannel;

/**

 */
public class InputChannelFactory { 
   private static ZephyrOpen constants = ZephyrOpen.getReference();
   private static InputChannel input = null; 

   /**
    * use props file to choose channel type
    *
    * @return the specified outputChannel
    */
   public static Object create() {

      String seviceType = constants.get(ZephyrOpen.networkService);
      
      //
      // Create a socket for the given Service Type
      //
      if (seviceType.equals(ZephyrOpen.multicast)) {

    	  input = MulticastChannel.getReference();
    	
    	 // is loop back enabled 
    	 ((MulticastChannel) input).setLoopback(constants.getBoolean(ZephyrOpen.loopback));
       
      } // else if ....
      
      
      return input;
   }
}
