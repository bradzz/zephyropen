package developer.swing;

import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;

import java.awt.event.*;

/**
 * A minimal SWING input field to write to a given socket
 * 
 * @author Brad Zdanivsky Created: 2007.11.3
 */
public class InputField extends JTextField implements KeyListener {

	private static final long serialVersionUID = 1L;
	private Socket socket = null;
	private PrintWriter out = null;
	private String userInput = null;
	private Vector<String> history = new Vector<String>();
	int ptr = 0;

	public InputField(Socket s, final String usr, final String pass) {
		super("users");
		socket = s;

		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			out.println(usr + ":" + pass);
		} catch (Exception e) {
			System.out.println("InputField() : " + e.getMessage());
		}

		addKeyListener(this);
	}
	
	// Manager user input
	public void send() {
		try {

			// get keyboard input
			userInput = getText().trim();
			
			history.add(userInput);
			
			if (history.size() > 10) history.remove(0);

			// log to console
			System.out.println("user typed :" + userInput);
			
			// send the user input to the server if is valid
			if (userInput.length() > 0) out.println(userInput);
			
			if (out.checkError()) System.exit(-1);

		} catch (Exception e) {
			System.exit(0);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		final char c = e.getKeyChar();
		if (c == '\n' || c == '\r') {
			final String input = getText().trim();
			if (input.length() > 2) {

				send();
				
				// clear input screen 
				setText("");
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP){
			if (history.isEmpty()) {
				setText("");
				return;
			}

			setText(history.get(ptr));
			if (history.size() > 0) ptr--;
			if (ptr < 0) ptr = history.size() - 1;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
}
