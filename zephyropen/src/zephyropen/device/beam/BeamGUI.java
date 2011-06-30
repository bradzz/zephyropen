package zephyropen.device.beam;

import javax.imageio.ImageIO;
import javax.swing.*;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

public class BeamGUI {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final Color yellow = new Color(245, 237, 48);
	public static final Color orange = new Color(252, 176, 64);
	public static final Color blue = new Color(245, 237, 48);
	public static final Color red = new Color(241, 83, 40);
	public static final int WIDTH = 900;
	public static final int HEIGHT = 300;
	
	public static final String yellowX1 = "yellowX1";
	public static final String yellowX2 = "yellowX2";
	public static final String yellowY1 = "yellowY1";
	public static final String yellowY2 = "yellowY2";
	
	public static final String orangeX1 = "orangeX1";
	public static final String orangeX2 = "orangeX2";
	public static final String orangeY1 = "orangeY1";
	public static final String orangeY2 = "orangeY2";
	
	public static final String redX1 = "redX1";
	public static final String redX2 = "redX2";
	public static final String redY1 = "redY1";
	public static final String redY2 = "redY2";

	JFrame frame = new JFrame("Beam Scan v0.1 "); 
	JLabel curve = new JLabel();
	BeamScan scan = new BeamScan();
	BeamComponent beamCompent = new BeamComponent();
	
	// TODO: get from the config file 
	private static String path = null;

	String topLeft1 = "NOT Connected";
	String topLeft2 = "try, device -> connect";
	String topLeft3 = "test";
	String topRight1 = "test";
	String topRight2 = "test";
	String bottomRight = "test";
	String topRight3 = "test";
	
	JMenuItem connectItem = new JMenuItem("connect");
	JMenuItem closeItem = new JMenuItem("close");
	JMenuItem scanItem = new JMenuItem("single scan");
	JMenuItem screenshotItem = new JMenuItem("screen capture");
	JMenu userMenue = new JMenu("Scan");
	JMenu deviceMenue = new JMenu("Device");

	

	/** */
	public static void main(String[] args) {
		constants.init(args[0]);
		new BeamGUI();
	}

	/** Methods to create Image corresponding to the frame. */
	public static void screenCapture(final Component component) {
		new Thread() {
			public void run() {
				String fileName = path + ZephyrOpen.fs + "beam_" + System.currentTimeMillis() + ".png";
				Point p = new Point(0, 0);
				SwingUtilities.convertPointToScreen(p, component);
				Rectangle region = component.getBounds();
				region.x = p.x;
				region.y = p.y;
				BufferedImage image = null;
				try {
					image = new Robot().createScreenCapture(region);
				} catch (AWTException e) {
					e.printStackTrace();
				}
				try {
					if (image != null)
						ImageIO.write(image, "png", new File(fileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/** */
	private final ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source.equals(scanItem)) {
				new Thread() {
					public void run() {
						singleScan();
					}
				}.start();
			} else if (source.equals(connectItem)) {
				
				if(scan.isConnected()) return;
				
				//new Thread() {
					//public void run() {
						scan.connect();
						if(scan.isConnected()){
							
							topLeft1 = "CONNECTED";
							topLeft2 = "Spin V: " + scan.getSpinVersion() + "   " + scan.getSpinPort();
							topLeft3 = "Read V: " + scan.getReadVersion() + "   " + scan.getReadPort();
							topRight1 = null; // "Spin: " + scan.getSpinPort() + " v: " + scan.getSpinVersion();
							topRight2 = null; // "Read: " + scan.getReadPort() + " v: " + scan.getReadVersion();
							topRight3 = null;
							
						} else {
							
							topLeft1 = "FAULT";
							topLeft2 = "connect failed";
							topLeft3 = null;
							topRight1 = null;
							topRight2 = null;
							topRight3 = null;
							
						}
						beamCompent.repaint();
					//}
				//}.start();
			
			} else if (source.equals(closeItem)) {
				scan.close();
				constants.shutdown();
			} else if (source.equals(screenshotItem)) {
				new Thread() {
					public void run() {
						Utils.delay(300);
						screenCapture(frame);
					}
				}.start();
			}
		}
	};

	/** */
	public BeamGUI() {

		path = constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "screenshots"
				+ ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName);

		// create log dir if not there
		if ((new File(path)).mkdirs())
			constants.info("created: " + path);

		/** Resister listener */
		screenshotItem.addActionListener(listener);
		scanItem.addActionListener(listener);
		closeItem.addActionListener(listener);
		connectItem.addActionListener(listener);

		/** Add to menu */
		deviceMenue.add(connectItem);
		deviceMenue.add(closeItem);
		
		userMenue.add(scanItem);
		userMenue.add(screenshotItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(deviceMenue);

		/** Create frame */
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1));
	
		frame.add(beamCompent);
		frame.add(curve);
		frame.setSize(WIDTH, HEIGHT + 300);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		/** register shutdown hook */
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					scan.close();
				}
			}
		);
	}

	/** */
	public void singleScan() {
		
		if(scan.isConnected()){
			scan.test();
			scan.log();
		} else {
			topLeft1 = "FAULT";
			topLeft2 = "not connected";
			topLeft3 = "try connecting first";
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			beamCompent.repaint();
			return;
		}

		final int xCenter = scan.getXCenter();
		final int yCenter = scan.getYCenter();
		final int xOffset = scan.getMaxIndexX() - xCenter;
		final int yOffset = scan.getMaxIndexY() - yCenter;
		
		constants.put("xCenter", xCenter);
		constants.put("xoffset", xOffset);
		constants.put("yoffset", yOffset);
		topLeft1 = "xCenter: " + xCenter + " yCenter: " + yCenter;
		topLeft2 = "xMax:" + scan.getMaxIndexX() + " yMax:" + scan.getMaxIndexY();
		topLeft3 = "xOffset:" + xOffset + " yOffset:" + yOffset;
		
		constants.info("xCenter: " + xCenter + " yCenter: " + yCenter);
		
		int[] slice = scan.getSlice(10);
		if (slice != null) {
			constants.put(yellowX1, slice[0]);
			constants.put(yellowX2, slice[1]);
			constants.put(yellowY1, slice[2]);
			constants.put(yellowY2, slice[3]);
			constants.info("10, yellow: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));
			topRight1 = "yellow " + (constants.getInteger(yellowX2) + "_" + constants.getInteger(yellowX1) 
					+ "   " + (constants.getInteger(yellowY1) + "_" + constants.getInteger(yellowY2)));
		}
		
		slice = scan.getSlice(300);
		if (slice != null) {
			constants.put("orangeX1", slice[0]);
			constants.put("orangeX2", slice[1]);
			constants.put("orangeY1", slice[2]);
			constants.put("orangeY2", slice[3]);
			constants.info("300, orange: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));
			topRight2 = "orange " + (constants.getInteger(orangeX2) + "_" + constants.getInteger(orangeX1) 
					+ "   " + (constants.getInteger(orangeY2) + "_" + constants.getInteger(orangeY1)));
		}	
		
		slice = scan.getSlice(800);
		if (slice != null) {
			constants.put("redX1", slice[0]);
			constants.put("redX2", slice[1]);
			constants.put("redY1", slice[2]);
			constants.put("redY2", slice[3]);
			constants.info("800, red:" + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));
			topRight3 = "red    " + (constants.getInteger(redX2) + "_" + constants.getInteger(redX1) 
					+ "   " + (constants.getInteger(redY2) + "_" + constants.getInteger(redY1)));
		}	
		
		beamCompent.repaint();
		lineGraph();
		Utils.delay(300);
		screenCapture(beamCompent);
		screenCapture(curve);
		screenCapture(frame);
	}
	
	/** create graph */
	public void lineGraph() {

		GoogleChart chart = new GoogleLineGraph("beam", "ma", com.googlecode.charts4j.Color.BLUEVIOLET);
		Vector<Integer>points = scan.getPoints();
		for (int j = 0; j < points.size(); j++)
			chart.add(String.valueOf(points.get(j)));

		try {
			
			String str = chart.getURLString( WIDTH, 280, 
					"Scan centers: " + scan.getXCenter() + " " + scan.getMaxIndexY());
			constants.info(str);
			if(str!=null){
				
				Icon icon = new ImageIcon(new URL(str)); 
			 
				// now set this image onto the JLable 
				if(icon != null) curve.setIcon(icon);
			
			} 
		} catch (final Exception e) {	
			constants.error(e.getMessage(), this);
		} 

	//	new ScreenShot(chart, 1000, 250, "data: " + reader.points.size() + " " + txt);
	}
	
	public class BeamComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g) {

			final int w = getWidth();
			final int h = getHeight();

			if (topRight1 != null)
				g.drawString(topRight1, (w/2 + 5), 15);
			if (topRight2 != null)
				g.drawString(topRight2, (w/2 + 5), 30);
			if (topRight3 != null)
				g.drawString(topRight3, (w/2 + 5), 45);
			
			if (topLeft1 != null)
				g.drawString(topLeft1, 15, 15);
			if (topLeft2 != null)
				g.drawString(topLeft2, 15, 30);
			if (topLeft3 != null)
				g.drawString(topLeft3, 15, 45);

		/*	if(scan.hasResult()){
			
			g.setColor(yellow);
			
			final int yCenter = scan.getYCenter(); 
			
			//g.fillOval(xCenter - constants.getInteger(yellowX1), 
				//	(h/2 - yellowHeight/2) + constants.getInteger("yoffset"),
					//		yellowWidth, yellowHeight);
			
			}
			*/
			
		/*		*/
			//final int xCenter = scan.getXCenter();
			//if
			g.setColor(yellow);
			int yellowWidth = (constants.getInteger(yellowX2) - constants.getInteger(yellowX1));
			int yellowHeight = (constants.getInteger(yellowY2) - constants.getInteger(yellowY1));
		
			g.fillOval(
					(w/2 - yellowWidth) - constants.getInteger("xCenter"), 
					(h/2 - yellowHeight/2) + constants.getInteger("yoffset"),
							yellowWidth, yellowHeight);
		
			g.setColor(orange);
			int orangeWidth = (constants.getInteger(orangeX2) - constants.getInteger(orangeX1));
			int orangeHeight = (constants.getInteger(orangeY2) - constants.getInteger(orangeY1));
			g.fillOval((w/2 - orangeWidth/2) + constants.getInteger("xoffset"),
					(h/2 - orangeHeight/2) + constants.getInteger("yoffset"),
					orangeWidth, orangeHeight);
			
			g.setColor(red);
			int redWidth = (constants.getInteger(redX2) - constants.getInteger(redX1));
			int redHeight = (constants.getInteger(redY2) - constants.getInteger(redY1));
			g.fillOval((w/2 - redWidth/2) + constants.getInteger("xoffset"),
					(h/2 - redHeight/2) + constants.getInteger("yoffset"),
					redWidth, redHeight);
			
			
			g.setColor(Color.BLACK);
			// g.drawString("red w: " + redWidth + "  red h:" + redHeight, (w/2 + 5), h - 10);

			//g.setStroke(new BasicStroke(2));
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);

		}
	}
}