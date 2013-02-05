package zephyropen.device.beamscan;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

/** 
 * 
 * @author brad.zdanivsky@gmail.com 
 * 
 * 
 */
public class ReplayGUI implements KeyListener {
	
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final String beamscan = "beamscan";

	private final String TITLE = "Replay Scan v3.1";
	private JFrame frame = new JFrame(TITLE);
	private JLabel imageLable = new JLabel();
	private File[] files = null;
	
	private java.util.Timer timer = new java.util.Timer();
	
	private JMenuItem replayItem = new JMenuItem("Start (r)");
	private JMenuItem stopItem = new JMenuItem("Stop (s)");

	private JMenuItem forwardItem = new JMenuItem("Forward (f)");
	private JMenuItem backItem = new JMenuItem("Back (b)");
	private JMenuItem deleteItem = new JMenuItem("Delete (d)");
	private JMenuItem archiveItem = new JMenuItem("Archive All");
	
	private JMenuItem speed1Item = new JCheckBoxMenuItem("1 FPS (1 second)", true);
	private JMenuItem speed2Item = new JCheckBoxMenuItem("2 FPS (500 ms)");
	private JMenuItem speed3Item = new JCheckBoxMenuItem("3 FPS (300 ms)");
	private JMenuItem speed2SecItem = new JCheckBoxMenuItem("Slow (2 seconds)");
	
	private JMenu frameMenue = new JMenu("Frame");
	private JMenu speedMenue = new JMenu("Speed");
	private JMenu replayMenue = new JMenu("Replay");
	
	private int scan_delay = 1000;
	private int i = 0;
	
	/** key codes */
	@Override
	public void keyTyped(KeyEvent e) {
		char chr = e.getKeyChar();
		
		if(chr=='s') stop();
		
		if(chr=='r') start();
		
		if(chr=='f') forward();

		if(chr=='b') back();
	}
	
	/** driver */
	public static void main(String[] args) {
		constants.init("beamscan", "beamscan");
		new ReplayGUI();	
	}

	/** create the swing GUI */
	public ReplayGUI() {
				
		replayItem.addActionListener(listener);
		stopItem.addActionListener(listener);
		forwardItem.addActionListener(listener);
		backItem.addActionListener(listener);
		deleteItem.addActionListener(listener);
		archiveItem.addActionListener(listener);

		replayMenue.add(replayItem);
		replayMenue.add(stopItem);
		
		frameMenue.add(forwardItem);
		frameMenue.add(backItem);
		frameMenue.add(deleteItem);
		frameMenue.add(archiveItem);
		
		speedMenue.add(speed1Item);
		speedMenue.add(speed2Item);
		speedMenue.add(speed3Item);
		speedMenue.add(speed2SecItem);
		
		speed1Item.addActionListener(speedListener);
		speed2Item.addActionListener(speedListener);
		speed3Item.addActionListener(speedListener);
		speed2SecItem.addActionListener(speedListener);

		speedMenue.add(speed1Item);
		speedMenue.add(speed2Item);
		speedMenue.add(speed3Item);
		speedMenue.add(speed2SecItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(replayMenue);
		menuBar.add(frameMenue);
		menuBar.add(speedMenue);
		
		/** Create frame */
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(imageLable);
		frame.setSize(BeamGUI.WIDTH + 6, (BeamGUI.HEIGHT * 2) + 48); // room for the menu
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true); 
		frame.addKeyListener(this);
		
		getFiles();
	}

	/** */
	private void getFiles() {
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File directory, String fileName) {
	            return fileName.endsWith(".png");
	        }
	    };
	    
	    files = new File(constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "capture").listFiles(filter);		
	}

	/** run on timer */
	private class replayTimer extends TimerTask {
		@Override
		public void run() {
			
			System.out.println(new Date().toString() + " " + i + " " + files[i].getName());
			
			frame.setTitle(TITLE + " (" + i + " of " + files.length + ")");
			imageLable.setIcon(new ImageIcon(files[i].getAbsolutePath()));
			if(++i >= files.length){
				stop();
			}
		}
	}

	/** Listen for menu */
	private ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();			
			if(source == replayItem) start();
			if(source == stopItem) stop();
			if(source == forwardItem) forward();
			if(source == backItem) back();
			if(source == deleteItem) delete();
			if(source == archiveItem) acrchive();
			
			System.out.println("act: " + source.toString());
		}
	};

	/** Listen for delay info */
	private ActionListener speedListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			
			speed1Item.setSelected(false);
			speed2Item.setSelected(false);
			speed3Item.setSelected(false);
			speed2SecItem.setSelected(false);
			
			Object source = event.getSource();
			if (source.equals(speed1Item)) {
				speed1Item.setSelected(true);
				scan_delay = 1000;
			} else if(source.equals(speed2Item)){
				speed2Item.setSelected(true);
				scan_delay = 500;
			} else if(source.equals(speed3Item)){
				speed3Item.setSelected(true);
				scan_delay = 300;
			} else if(source.equals(speed2SecItem)){
				speed2SecItem.setSelected(true);
				scan_delay = 2000;
			} 
			
			System.out.println("scan delay = " + scan_delay);
		}
	};
	
	/** */
	private void start() {
		if(timer != null) timer.cancel();
		timer = new java.util.Timer();
		timer.scheduleAtFixedRate(new replayTimer(), 0, scan_delay);	
	}
	
	/** */
	private void stop(){
		if(timer != null) timer.cancel();
		timer = null;
	}
	
	/** */
	private void back(){
		stop();
		if(i <= 0) return;
		i--;
		frame.setTitle(TITLE + " (" + i + " of " + files.length + ")");
		imageLable.setIcon(new ImageIcon(files[i].getAbsolutePath()));
	}
	
	/** */
	private void forward(){
		stop();
		if(i >= files.length-1) return;
		i++;
		frame.setTitle(TITLE + " (" + i + " of " + files.length + ")");
		imageLable.setIcon(new ImageIcon(files[i].getAbsolutePath()));
	}
	
	/** */
	private void acrchive() {
		System.out.println("archive.....");
	}

	/** */
	private void delete() {
		System.out.println("delete: " + i);
		files[i].delete();
		getFiles();
		frame.setTitle(TITLE + " (" + i + " of " + files.length + ")");
		imageLable.setIcon(new ImageIcon(files[i].getAbsolutePath()));
	}
	


	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}