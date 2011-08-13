package zephyropen.device.beam;

import javax.imageio.ImageIO;
import javax.swing.*;

import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.util.Utils;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.GoogleLineGraph;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.TimerTask;
import java.util.Vector;

public class BeamGUI implements MouseMotionListener {

	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final Color yellow = new Color(245, 237, 48);
	public static final Color orange = new Color(252, 176, 64);
	public static final Color blue = new Color(245, 237, 48);
	public static final Color red = new Color(241, 83, 40);
	
	/** these will be found in constants object */
	public static final String dataPoints = "dataPoints";
	public static final String readTime = "readTime";
	public static final String spinTime = "spinTime";
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
	
	// TODO: get from config 
    public final int WIDTH = 800;
	public final int HEIGHT = 300;
	
	private final String title = "Beam Scan v0.2 ";
	private JFrame frame = new JFrame(title); 
	private JLabel curve = new JLabel();
	private BeamScan scan = new BeamScan();
	private BeamComponent beamCompent = new BeamComponent();
	private java.util.Timer timer = null;
	private static String path = null;

	private String topLeft1 = "NOT Connected";
	private String topLeft2 = "try, device -> connect";
	private String topLeft3 = "test";
	private String topRight1 = "test";
	private String topRight2 = "test";
	private String topRight3 = "test";
	private String bottomRight1 = "";
	private String bottomRight2 = "";
	private String bottomRight3 = "";
	
	private JMenuItem connectItem = new JMenuItem("connect");	
	//private JMenuItem closeItem = new JMenuItem("close");
	private JMenuItem closeItem = new JMenuItem("close");
	private JMenuItem startItem = new JMenuItem("start");
	private JMenuItem stopItem = new JMenuItem("stop");
	private JMenuItem scanItem = new JMenuItem("single scan");
	private JMenuItem screenshotItem = new JMenuItem("screen capture");
	private JMenu userMenue = new JMenu("Scan");
	private JMenu deviceMenue = new JMenu("Device");

	/// private int dataPoints = 0;
	private double scale = 0.0;
	private double xCenterpx = 0.0;
	private double yCenterpx = 0.0;
	private double redX1px = 0.0;	
	private double redX2px = 0.0;
	private double redY1px = 0.0;	
	private double redY2px = 0.0;
	private double yellowX1px = 0.0;	
	private double yellowX2px = 0.0;
	private double yellowY1px = 0.0;	
	private double yellowY2px = 0.0;
	private double orangeX1px = 0.0;	
	private double orangeX2px = 0.0;
	private double orangeY1px = 0.0;	
	private double orangeY2px = 0.0;
	
	/** */
	public static void main(String[] args) {
		constants.init(args[0]);
		//constants.init();
		ApiFactory.getReference().remove(ZephyrOpen.zephyropen);
		constants.put(ZephyrOpen.frameworkDebug, false);
		//constants.lock();
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
	
	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		frame.setTitle(title + "    (" + e.getX() + ", " + e.getY()+")");
	}
	
	/** run on timer */ 
	private class ScanTask extends TimerTask {
		@Override
		public void run() {
			singleScan();
		}
	}
	
	/** establish connection to usb ports */
	private void connect(){
		
		System.out.println("connecting...");
		
		scan.connect();
		if(scan.isConnected()){
			topLeft1 = "CONNECTED";
			topLeft2 = "Spin V: " + scan.getSpinVersion() + "   " + scan.getSpinPort();
			topLeft3 = "Read V: " + scan.getReadVersion() + "   " + scan.getReadPort();
			topRight1 = null; 
			topRight2 = null; 
			topRight3 = null;
			
			userMenue.add(startItem);
			userMenue.remove(stopItem);
			userMenue.add(scanItem);
		} else {
			topLeft1 = "FAULT";
			topLeft2 = "connection failed";
			topLeft3 = new Date().toString();
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			
			userMenue.remove(startItem);
			userMenue.remove(stopItem);
			deviceMenue.add(connectItem);
		}
		beamCompent.repaint();	
	}
	
	/** Listen for menu */
	private ActionListener listener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source.equals(scanItem)) {
				new Thread() {
					public void run() {
						if(timer == null)
							singleScan();
					}
				}.start();
			} else if (source.equals(connectItem)) {
				if( ! scan.isConnected()){
				
					//topRight1 = "ready...";
					// beamCompent.repaint(); //0, 100, 0, 100);
				// } else {
					
					connect();
					/*
					timer.cancel();
					timer = null;
					*/
					
					userMenue.add(startItem);
					userMenue.remove(stopItem);
					userMenue.add(scanItem);
				}
			} else if (source.equals(closeItem)) {
				
				
				scan.close();
				topLeft1 = "DISCONNECTED";
				topLeft2 = "try device -> connect";
				topLeft3 = "";
				topRight1 = null; 
				topRight2 = null; 
				topRight3 = null;
				beamCompent.repaint();	
				deviceMenue.add(connectItem);
				userMenue.add(startItem);
				userMenue.remove(stopItem);
				userMenue.remove(scanItem);
				
			} else if (source.equals(screenshotItem)) {
				new Thread() {
					public void run() {
						Utils.delay(300);
						screenCapture(frame);
					}
				}.start();
			} else if (source.equals(startItem)) {
				timer = new java.util.Timer();
				timer.scheduleAtFixedRate(new ScanTask(), 0, 2500);
				userMenue.remove(startItem);
				userMenue.add(stopItem);
				userMenue.remove(scanItem);
				userMenue.remove(screenshotItem);
				deviceMenue.remove(connectItem);
			} else if(source.equals(stopItem)){
				if(timer!=null){	
					timer.cancel();
					timer = null;
					userMenue.add(startItem);
					userMenue.remove(stopItem);
					userMenue.add(scanItem);			
					userMenue.add(screenshotItem);

				}
			}
		}
	};

	/** create the swing GUI */
	public BeamGUI() {

		path = constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "screenshots"
				+ ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName);

		// create log dir if not there
		if ((new File(path)).mkdirs())
			constants.info("created: " + path);
		
		/** Resister listener */
		startItem.addActionListener(listener);
		stopItem.addActionListener(listener);
		screenshotItem.addActionListener(listener);
		scanItem.addActionListener(listener);
		closeItem.addActionListener(listener);
		connectItem.addActionListener(listener);

		/** Add to menu */
		// deviceMenue.add(connectItem);
		// deviceMenue.add(closeItem);
		// userMenue.add(startItem);
		// userMenue.add(stopItem);
		// userMenue.add(scanItem);
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
		frame.setSize(WIDTH+6, (HEIGHT*2)+48); // room for the menu 
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
  
		/** Register for mouse events */
		beamCompent.addMouseMotionListener(this);
		curve.addMouseMotionListener(this);

		/** register shutdown hook */
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					scan.close();
					//constants.shutdown();
					System.out.println("....close port");
				}
			}
		);
		
		// beamCompent = new BeamComponent();
		connect();
	}
	
	/** */
	private int[] takeSlice(int target){
		int[] slice = scan.getSlice(target);
		if (slice == null) {
			topLeft1 = "FAULT";
			topLeft2 = "re-connecting...";
			topLeft3 = "";
			beamCompent.repaint();
			scan.close();
			connect();
			return null; 
		}
		return slice;
	}
	
	/** take one slice if currently connected */
	public void singleScan() {
		if(!scan.isConnected()){
			topLeft1 = "FAULT";
			topLeft2 = "not connected";
			topLeft3 = "try connecting first";
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			beamCompent.repaint();
			return;
		}
		
		scan.test();
		scan.log();
		
		// dataPoints = scan.getPoints().size();
		scale = (double)WIDTH/(double)constants.getInteger(dataPoints); 
		xCenterpx = (((double)WIDTH) * 0.25);
		yCenterpx = (((double)WIDTH) * 0.75);
	
		int[] slice = takeSlice(100);
		if(slice==null) return;
		
		// constants.put("yellowSlice", 100);
		constants.put(yellowX1, slice[0]);
		constants.put(yellowX2, slice[1]);
		constants.put(yellowY1, slice[2]);
		constants.put(yellowY2, slice[3]);
	
		// compute pixels 
		yellowX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
		yellowX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
		yellowY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
		yellowY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
		
		topRight1 = "yellow (" + Utils.formatFloat(yellowX1px, 0) + ", " + Utils.formatFloat(yellowX2px,0) 
			+ ")(" + Utils.formatFloat(yellowY1px,0) + ", " + Utils.formatFloat(yellowY2px,0) + ")";
	
		slice = takeSlice(300);
		if(slice==null) return;
		// constants.put("orangeSlice", 300);
		constants.put(orangeX1, slice[0]);
		constants.put(orangeX2, slice[1]);
		constants.put(orangeY1, slice[2]);
		constants.put(orangeY2, slice[3]);
		orangeX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
		orangeX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
		orangeY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
		orangeY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
		
		topRight2 = "orange (" + Utils.formatFloat(orangeX1px, 0) + ", " + Utils.formatFloat(orangeX2px,0) 
		+ ")(" + Utils.formatFloat(orangeY1px,0) + ", " + Utils.formatFloat(orangeY2px,0) + ")";

		slice = takeSlice(800);
		if(slice==null) return;
		// constants.put("redSlice", 800);
		constants.put("redX1", slice[0]);
		constants.put("redX2", slice[1]);
		constants.put("redY1", slice[2]);
		constants.put("redY2", slice[3]);
		redX1px = (WIDTH/2) - (xCenterpx - ((double)slice[0] * scale));
		redX2px = (WIDTH/2) - (xCenterpx - ((double)slice[1] * scale));
		redY1px = (HEIGHT/2) - (yCenterpx - ((double)slice[2] * scale));
		redY2px = (HEIGHT/2) - (yCenterpx - ((double)slice[3] * scale));
		
		topRight3 = "red (" + Utils.formatFloat(redX1px, 0) + ", " + Utils.formatFloat(redX2px,0) 
		+ ")(" + Utils.formatFloat(redY1px,0) + ", " + Utils.formatFloat(redY2px,0) + ")";
	
		beamCompent.repaint();
		lineGraph();
		screenCapture(frame);
	}
	
	/** create graph */
	public void lineGraph() {
		GoogleChart chart = new GoogleLineGraph("beam", "ma", com.googlecode.charts4j.Color.BLUEVIOLET);
		Vector<Integer>points = scan.getPoints();
		for (int j = 0; j < points.size(); j++)
			chart.add(String.valueOf(points.get(j)));

		try {
			String str = chart.getURLString( WIDTH, HEIGHT, "data: " + dataPoints ); 
			if(str!=null){
				Icon icon = new ImageIcon(new URL(str));
				if(icon != null) curve.setIcon(icon);
			} 
		} catch (final Exception e) {	
			constants.error(e.getMessage(), this);
		} 
	}
	
	/** draw cross section chart */
	public class BeamComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		
		// TODO: TAKE FROM PROPS 
		private boolean drawLines = false;
		
		
		public void paint(Graphics g) {
			final int w = getWidth();
			final int h = getHeight();
			
			g.setColor(Color.YELLOW);
			g.fillOval((int)yellowX1px, (int)yellowY1px, (int)yellowX2px-(int)yellowX1px, (int)yellowY2px-(int)yellowY1px);
			if(drawLines){
				g.drawLine((int)yellowX1px, 0,(int)yellowX1px, h);
				g.drawLine((int)yellowX2px, 0,(int)yellowX2px, h);
				g.drawLine(0, (int)yellowY1px, w,(int)yellowY1px);
				g.drawLine(0, (int)yellowY2px, w,(int)yellowY2px);
			}
			
			g.setColor(Color.ORANGE);
			g.fillOval((int)orangeX1px, (int)orangeY1px, (int)orangeX2px-(int)orangeX1px, (int)orangeY2px-(int)orangeY1px);
			if(drawLines){
				g.drawLine((int)orangeX1px, 0,(int)orangeX1px, h);
				g.drawLine((int)orangeX2px, 0,(int)orangeX2px, h);
				g.drawLine(0, (int)orangeY1px, w,(int)orangeY1px);
				g.drawLine(0, (int)orangeY2px, w,(int)orangeY2px);
			}
			
			g.setColor(Color.RED);
			g.fillOval((int)redX1px, (int)redY1px, (int)redX2px-(int)redX1px, (int)redY2px-(int)redY1px);
			if(drawLines){
				g.drawLine((int)redX1px, 0,(int)redX1px, h);
				g.drawLine((int)redX2px, 0,(int)redX2px, h);
				g.drawLine(0, (int)redY1px, w,(int)redY1px);
				g.drawLine(0, (int)redY2px, w,(int)redY2px);
			}
			
			// draw grid 
			g.setColor(Color.BLACK);
			g.drawLine(0, h-1, w, h-1);
			Graphics2D g2d = (Graphics2D) g;
			Stroke stroke2 = new BasicStroke(
		    		 1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL, 1.0f, new float[]
				 { 6.0f, 2.0f, 1.0f, 2.0f },0.0f);
			g2d.setStroke(stroke2);
			g.drawLine(0, h/2, w, h/2);
			g.drawLine(w/2, 0, w/2, h);
			
			
			
			double x = 0; //((WIDTH/2) - ((double)scan.getMaxIndexX()*scale));
			//g.drawLine(x, 0, x, h);
			
			//if(scan!=null){
				
				//x = (((WIDTH/2) - ((double)scan.getMaxIndexX()*scale)));
				//g.drawLine(x, 0, x, h);
				
				
			//}
			
			topRight1 = "Data Points: " + constants.get(dataPoints);	
			topRight2 = "Spin Time:   " + constants.get(spinTime) + " ms";	
			topRight3 = "Read Time:   " + constants.get(readTime) + " ms";	
			
			bottomRight1 = " h: " + h + " w: " + w;
			bottomRight2 = " x: " + Utils.formatFloat(x, 1); 
			
			// draw text 
			if (topRight1 != null) g.drawString(topRight1, (w/2 + 50), 15);
			if (topRight2 != null) g.drawString(topRight2, (w/2 + 50), 30);
			if (topRight3 != null) g.drawString(topRight3, (w/2 + 50), 45);
			
			if (bottomRight1 != null) g.drawString(bottomRight1, (w/2 + 5), h - 10);
			if (bottomRight2 != null) g.drawString(bottomRight2, (w/2 + 5), h - 25);
			if (bottomRight3 != null) g.drawString(bottomRight3, (w/2 + 5), h - 40);
			
			if (topLeft1 != null) g.drawString(topLeft1, 15, 15);
			if (topLeft2 != null) g.drawString(topLeft2, 15, 30);
			if (topLeft3 != null) g.drawString(topLeft3, 15, 45);
		}
	}
}