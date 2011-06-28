package zephyropen.device.beam;

import javax.imageio.ImageIO;
import javax.swing.*;

import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Chart {

	/** framework configuration */
	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final Color yellow = new Color(245, 237, 48);
	public static final Color orange = new Color(252, 176, 64);
	public static final Color blue = new Color(245, 237, 48);
	public static final Color red = new Color(241, 83, 40);
	public static final int WIDTH = 400;
	public static final int HEIGHT = 300;

	JFrame frame = new JFrame("Beam Scan v0.1 "); 
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
		new Chart();
	}

	/** Methods to create Image corresponding to the frame. */
	public static void screenCapture(Component component) {
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
							topLeft2 = "Spin: " + scan.getSpinVersion() + " " + scan.getSpinPort();
							topLeft3 = "Read: " + scan.getReadVersion() + " " + scan.getReadPort();
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
						screenCapture(beamCompent);
					}
				}.start();
			}
		}
	};

	/** */
	public Chart() {

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
		frame.getContentPane().add(beamCompent);
		frame.setSize(WIDTH, HEIGHT + 50);
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
		
		constants.put("xoffset", xOffset);
		constants.put("yoffset", yOffset);
		
		topLeft1 = "xCenter: " + xCenter + " yCenter: " + yCenter;
		topLeft2 = "xMax:" + scan.getMaxIndexX() + " yMax:" + scan.getMaxIndexY();
		topLeft3 = "xOffset:" + xOffset + " yOffset:" + yOffset;
		
		constants.info("xCenter: " + xCenter + " yCenter: " + yCenter);
		
		int[] slice = scan.getSlice(100);
		if (slice != null) {
			constants.info("slice 100 x: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));

			constants.put("x1", slice[0]);
			constants.put("x2", slice[1]);
			constants.put("y1", slice[2]);
			constants.put("y2", slice[3]);

			topRight2 = "100: " + slice[0] + " " + slice[1] + " " + slice[2] + " " + slice[3];
			scan.lineGraph("slice 100_"); 
		}
		
		slice = scan.getSlice(400);
		if (slice != null) {
			constants.info("slice 400 x: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));

			constants.put("redX1", slice[0]);
			constants.put("redX2", slice[1]);
			constants.put("redY1", slice[2]);
			constants.put("redY2", slice[3]);

			topRight3 = "400: " + (constants.getInteger("redY2")-constants.getInteger("redY1"));
			//slice[0] + " " + slice[1] + " " + slice[2] + " " + slice[3];
			scan.lineGraph("slice 400_"); 
		}	
		
		beamCompent.repaint();
		screenCapture(beamCompent);
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

			// bottom right 
			////if(bottomRight != null)
				//g.drawString(bottomRight, (w/2 + 5), h - 10);
		
			g.setColor(yellow);
			int yellowWidth = (constants.getInteger("x2") - constants.getInteger("x1"));
			int yellowHeight = (constants.getInteger("y2") - constants.getInteger("y1"));
			g.fillOval(
					(w/2 - yellowWidth/2) + constants.getInteger("xoffset"),
					(h/2 - yellowHeight/2) + constants.getInteger("yoffset"),
							yellowWidth, yellowHeight);
			
			
			g.setColor(red);
			int redWidth = (constants.getInteger("redX2") - constants.getInteger("redX1"));
			int redHeight = (constants.getInteger("redY2") - constants.getInteger("redY1"));
			g.fillOval((w/2 - redWidth/2) + constants.getInteger("xoffset"),
					(h/2 - redHeight/2) + constants.getInteger("yoffset"),
					redWidth, redHeight);
			
			g.drawString("red w: " + redWidth + "  red h:" + redHeight, (w/2 + 5), h - 10);

			g.setColor(Color.BLACK);
			//g.setStroke(new BasicStroke(2));
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);

		}
	}
}