package zephyropen.socket.multicast.gui;

import javax.swing.*;
import java.awt.*;

/**
 *	Create a SWING frame to display the input/output components. 
 *
 * Created: 2007.11.3
 * @author Brad Zdanivsky 
 */
public class ChatFrame extends JFrame implements Runnable {
	
   	public ChatFrame(JTextField in, JTextArea out, String title) {
    
      	// do minimal layout 
      	setTitle(title); 	
      	setDefaultLookAndFeelDecorated(true);     	
       	setLayout(new BorderLayout());
        JScrollPane chatScroller = new JScrollPane(out);
   	chatScroller.setPreferredSize(new Dimension(600, 300));
  
   	// add to the frame 
    	getContentPane().add(chatScroller, BorderLayout.NORTH);
    	getContentPane().add(in, BorderLayout.PAGE_END);
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	pack();
   	}  
  
   	// swing will call us 
  	public void run(){
   		setVisible(true); 
  	}
}
