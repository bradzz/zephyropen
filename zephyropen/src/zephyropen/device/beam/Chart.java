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

	JFrame frame = new JFrame("Beam Scan v0.1 (z:" + ZephyrOpen.VERSION + ")");
	BeamScan scan = null;
	
	BeamComponent beamCompent = new BeamComponent();
	
	String message = "NOT Connected";
	String scanInfo = "NO SCAN FOUND";
	String screenInfo = "NO SCAN FOUND";
	
	JMenuItem connectItem = new JMenuItem("connect");
	JMenuItem closeItem = new JMenuItem("close");
	JMenuItem scanItem = new JMenuItem("sngle scan");	
	JMenuItem screenshotItem = new JMenuItem("screen capture");
	JMenu userMenue = new JMenu("Scan");
	JMenu deviceMenue = new JMenu("Device");
	
	public static final Color yellow = new Color(245, 237, 48);
	public static final  Color orange = new Color(252, 176, 64);
	public static final  Color blue = new Color(245, 237, 48);
	public static final  Color red = new Color(241, 83, 40);
	
	static String path = null; 
	
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
			if(image != null)
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
			if (source.equals(scanItem)){
				new Thread(){ 
					public void run(){ 
						test();
						beamCompent.repaint();
						screenCapture(frame); // beamCompent);
					}	
				}.start();
			} else if (source.equals(connectItem)){
				new Thread(){ 
					public void run(){ 
						if(scan!=null){
							scan.close();
							scan = null;
						}
						scan = new BeamScan();
						if(scan!=null){
							message = "device is now connected";
							beamCompent.repaint(); 
						}
					}	
				}.start();
			}  else if (source.equals(closeItem)){
				if(scan!=null) scan.close();
				constants.shutdown();
			} else if (source.equals(screenshotItem)){
				new Thread(){ 
					public void run(){ 
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
		if((new File(path)).mkdirs())
			constants.info("created: " + path);

		/** Resister listener */ 
		screenshotItem.addActionListener(listener);
		scanItem.addActionListener(listener);
		closeItem.addActionListener(listener);
		connectItem.addActionListener(listener);
	
		/** Add to menu */
		deviceMenue.add(connectItem);
		deviceMenue.add(closeItem);
		userMenue.add(screenshotItem);
		userMenue.add(scanItem);
	
		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(deviceMenue);
		
		/** Create frame */ 
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setLayout(new GridLayout(2, 1));
		frame.getContentPane().add(beamCompent); 
		frame.setSize(500, 400);
		frame.setResizable(false);
		frame.setVisible(true);
		
		/** register shutdown hook */
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
	}

	/** shutdown hook, close any left over files and loggers */
	public class ShutdownThread extends Thread {
		public void run() {
			// System.out.println("closing scan usb ports");
			if(scan!=null)
				scan.close();
		}
	}
	
	/** */
	public void test() {
		
		if(scan==null) {
			message = "ERROR: not connected to device";
			beamCompent.repaint();
			return;
		}

		scan.test();

		final int xCenter = scan.getXCenter();
		final int yCenter = scan.getYCenter();
		
		//((scan.reader.points.size() / 2) + (scan.reader.points.size() / 4));

		//final int maxX = scan.getMaxIndex(0, scan.reader.points.size() / 2);
		//constants.info("max: " + scan.reader.points.get(maxX) + " index: " + maxX);

		//final int maxY = scan.getMaxIndex(scan.reader.points.size() / 2, scan.reader.points.size());
		//constants.info("max2: " + scan.reader.points.get(maxY) + " index: " + maxY);

		//constants.put("xBeam", maxX);
		//constants.put("yBeam", maxY);

		int j = 500;
		int[] slice = null;
		
			slice = scan.getSlice(j);
			if (slice != null){
				
				// constants.info("slice: " + j + " xMax: " + maxX + " yMax: " + maxY);
				constants.info("slice: " + j + " xCenter: " + xCenter + " yCenter: " + yCenter);
				constants.info("slice: " + j + " x: " + (slice[1] - slice[0]) + " y: " + (slice[3] - slice[2]));
				
				constants.put("x1", slice[0]);
				constants.put("x2", slice[1]);
				constants.put("y1", slice[2]);
				constants.put("y2", slice[3]);
				
				constants.put("x1offset", slice[0]-xCenter);
				constants.put("x2offset", slice[1]-xCenter);
				constants.put("y1offset", slice[2]-yCenter);
				constants.put("y2offset", slice[3]-yCenter);
				
				message = "   slice: " + j + 
				 "   x1: " + String.valueOf(slice[0]) + "_" + String.valueOf(slice[0]-xCenter) 
					+ "  x2: " + String.valueOf(slice[1]) + "_" + String.valueOf(slice[1]-xCenter) 
					+ "  y1: " + String.valueOf(slice[2]) + "_" + String.valueOf(slice[2]-yCenter)
					+ "  y2: " + String.valueOf(slice[3]) + "_" + String.valueOf(slice[3]-yCenter);
				
				// frame.paint(beamCompent.getGraphics());
				scan.lineGraph(message);
				
		}
		scan.log();
	}

	public class BeamComponent extends JComponent {
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {

			// grid lines
			final int w = getWidth();
			final int h = getHeight();

			if(scanInfo == null) scanInfo = "";
			g.drawString(scanInfo, w-100, 15);

			if(message == null) message = "";
			g.drawString(message + "    h: " + h + " w: " + w, 15, 15);
			
			g.setColor(yellow);
			g.fillOval(w / 4, h / 4, w / 2, h / 2);

			g.setColor(orange);
			g.fillOval((w / 2 - w / 6), ((h / 2 - h / 6)), w / 3, h / 3);

			g.setColor(red);
			g.fillOval((w / 2 - w / 12), ((h / 2 - h / 12)), w / 6, h / 6);

			g.setColor(Color.BLACK);
			// g2.setStroke(new BasicStroke(3));
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);

		}
	}
}