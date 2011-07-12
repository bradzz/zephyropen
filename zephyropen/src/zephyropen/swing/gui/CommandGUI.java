package zephyropen.swing.gui;

import javax.swing.*;

import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/** */
public class CommandGUI {
	
	private static final String oculus = "oculus";
	private static final String function = "function";
	private static final String argument = "argument";

	/** framework configuration */
	static ZephyrOpen constants = ZephyrOpen.getReference();
	static final String title = "command line utility v0.2";

	/** framework configuration */
	public static final int WIDTH = 300;
	public static final int HEIGHT = 80;

	private Vector<String> history = new Vector<String>();
	private int ptr = 0;

	private JFrame frame = new JFrame(title);
	private JTextField user = new JTextField();
	private JMenuItem closeItem = new JMenuItem("close");
	private JMenuItem dockItem = new JMenuItem("autodock");
	private JMenuItem undockItem = new JMenuItem("un-dock");
	private JMenuItem scriptItem = new JMenuItem("run script file");

	// JMenuItem screenshotItem = new JMenuItem("screen capture");
	// JMenu userMenue = new JMenu("Scan");
	private JMenu deviceMenue = new JMenu("Commands");
	private Command command = new Command(oculus);

	
	/** lock out default settings */
	public static void main(String[] args) {
		constants.init();
		ApiFactory.getReference().remove(ZephyrOpen.zephyropen);
		constants.put(ZephyrOpen.frameworkDebug, false);
		constants.lock();
		new CommandGUI();
	}

	/** parse input from text area */
	public class UserInput implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			String input = user.getText().trim();
			if (c == '\n' || c == '\r') {
				String s = input;
				if (s.length() > 1) {
					if (!history.contains(s)) {
						history.add(s);
						ptr = history.size() - 1;
					} else {
						ptr = history.indexOf(s);
					}	
				}
				
				// parse input string 
				String fn = null;
				String ar = null;
				int space = input.indexOf(' ');
				if(space==-1){
					fn = input;
				} else {
					fn = input.substring(0, space);
					ar = input.substring(input.indexOf(' ')+1, input.length());
				}
					
				// create command 
				if(fn!=null) command.add(function, fn);
				if(ar!=null) command.add(argument, ar);
				command.send();
				
				// clear it
				user.setText("");
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_UP){
				if (history.isEmpty()) {
					user.setText("");
					return;
				}

				user.setText(history.get(ptr));
				frame.setTitle(title + " (" + ptr + " of " + history.size() + ")");

				if (history.size() > 0) ptr--;
				if (ptr < 0) ptr = history.size() - 1;
				// if (history.size() > 10) history.remove(0);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

	/** */
	private ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source.equals(dockItem)) {
				new Thread() {
					public void run() {
						command.add(function, "autodock");
						command.add(argument, "go");
						command.send();
					}
				}.start();
			}
			
			if (source.equals(undockItem)) {
				new Thread() {
					public void run() {
						command.add(function, "dock");
						command.add(argument, "undock");
						command.send();
					}
				}.start();
			}
			
		}
	};

	/** */
	public CommandGUI() {

		/** Resister listener */
		scriptItem.addActionListener(listener);
		undockItem.addActionListener(listener);
		dockItem.addActionListener(listener);
		closeItem.addActionListener(listener);
		user.addKeyListener(new UserInput());

		/** Add to menu */
		deviceMenue.add(closeItem);
		deviceMenue.add(undockItem);
		deviceMenue.add(dockItem);
		deviceMenue.add(dockItem);
		deviceMenue.add(scriptItem);
		
		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(deviceMenue);

		/** Create frame */
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(user);
		frame.setSize(WIDTH, HEIGHT);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		/** register shutdown hook
		Runtime.getRuntime().addShutdownHook(
				new Thread() {
					public void run() {
						System.out.println("shutdown");
					}
				}
				);
				*/
	}
}