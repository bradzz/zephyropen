package zephyropen.socket.multicast.gui;

import java.net.*;

import javax.swing.*;

import java.awt.event.*;

public class Input extends JTextField implements KeyListener {

	private static final long serialVersionUID = 1L;
	private String userInput;

	public Input(MulticastSocket s) { // , final String usr, final String pass) {
		super();
		
		// listen for key input 
		addKeyListener(this);
	}
	
	// Manager user input
	public void send() {
		try {

			// get keyboard input
			userInput = getText().trim();
			
			// log to console
			System.out.println("user typed :" + userInput);
			
			if (userInput.equalsIgnoreCase("quit")) System.exit(-1);

		} catch (Exception e) {
			System.exit(-1);
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
	}

	@Override
	public void keyReleased(KeyEvent e) {}
}
