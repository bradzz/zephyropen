package zephyropen.socket.multicast.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Create a SWING frame to display the input/output components.
 * 
 * Created: 2007.11.3
 * 
 * @author Brad Zdanivsky
 */
public class ChatFrame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;

	public ChatFrame(JTextField in, JTextArea out, String title) {

		// do minimal layout
		setTitle(title);
		setDefaultLookAndFeelDecorated(true);
		setLayout(new BorderLayout());
		JScrollPane chatScroller = new JScrollPane(out);
		chatScroller.setPreferredSize(new Dimension(300, 600));
		this.setResizable(false);
		getContentPane().add(chatScroller, BorderLayout.NORTH);
		getContentPane().add(in, BorderLayout.PAGE_END);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	// swing will call us
	public void run() {
		setVisible(true);
	}
}
