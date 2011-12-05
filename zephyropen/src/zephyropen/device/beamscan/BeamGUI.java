package zephyropen.device.beamscan;

import javax.imageio.ImageIO;
import javax.swing.*;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

import zephyropen.api.ApiFactory;
import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.state.State;
import zephyropen.util.Utils;
import zephyropen.util.google.SendGmail;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @author brad.zdanivsky@gmal.com
 */
public class BeamGUI {

	public static ZephyrOpen constants = ZephyrOpen.getReference();
	public static final String beamscan = "beamscan";
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
	public final static int WIDTH = 650;
	public final static int HEIGHT = 300;

	private final String title = "Beam Scan v2.3";
	private JFrame frame = new JFrame(title);
	private JLabel curve = new JLabel();
	private CommPort device = new CommPort();
	private BeamComponent beamCompent = new BeamComponent();

	private java.util.Timer timer = null;
	private static String path = null;
	private boolean isConnected = false;
	private boolean isScanning = false;

	// TODO: TAKE FROM FILE
	private long scanDelay = 2000;

	private String topLeft1 = "NOT Connected";
	private String topLeft2 = "try, device -> connect";
	private String topLeft3 = null;
	private String topRight1 = null;
	private String topRight2 = null;
	private String topRight3 = null;
	private String bottomRight1 = null;
	private String bottomRight2 = null;
	private String bottomRight3 = null;

	private JMenuItem connectItem = new JMenuItem("connect");
	private JMenuItem disconnectItem = new JMenuItem("disconnect");
	private JMenuItem startItem = new JMenuItem("start");
	private JMenuItem stopItem = new JMenuItem("stop");
	private JMenuItem emailItem = new JMenuItem("send email report");
	private JMenuItem scanItem = new JMenuItem("single scan");
	private JMenuItem javaItem = new JMenuItem("get software updates");
	private JMenuItem screenshotItem = new JMenuItem("screen capture");

	private JMenuItem debugItem = new JCheckBoxMenuItem("debugging",
			constants.getBoolean(ZephyrOpen.frameworkDebug));
	private JMenuItem recordingtem = new JCheckBoxMenuItem("recording",
			constants.getBoolean(ZephyrOpen.recording));
	private JMenuItem loggingItem = new JCheckBoxMenuItem("log files",
			constants.getBoolean(ZephyrOpen.loggingEnabled));

	private JMenu userMenue = new JMenu("Scan");
	private JMenu deviceMenue = new JMenu("Device");
	private JMenu helpMenue = new JMenu("Help");

	private ScanResults results = null;
	private int dataPoints = 0;
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
		
	//	if (constants.get(ZephyrOpen.propFile) == null) {
			
			constants.put(ZephyrOpen.deviceName, "beamscan");
			constants.put(ZephyrOpen.user, "beamscan");
			constants.put(ZephyrOpen.propFile, "beamscan.properties");
			constants.put(ZephyrOpen.loggingEnabled, true);
			constants.put(ZephyrOpen.frameworkDebug, true);

	//		constants.info("started without config file");
			constants.info(constants.toString());
			constants.updateConfifFile();
			
	//	}
		
		constants.init();

		// ApiFactory.getReference().remove(ZephyrOpen.zephyropen);
		// if (ApiFactory.getReference().toString() != null)
		//	constants.error("non-null api warning");


	    
		// constants.put(ZephyrOpen.user, "brad");
		// constants.put(State.pack, true);
		// constants.put("drawdelay", "2000");
		
	    // constants.put(ZephyrOpen.deviceName, "beamscan"); //PrototypeFactory...);
		
		new BeamGUI();
	}

	/** create the swing GUI */
	public BeamGUI() {

		path = constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "capture";

		// create log dir if not there
		if ((new File(path)).mkdirs())
			constants.info("created: " + path);

		/** Resister listener */
		startItem.addActionListener(listener);
		stopItem.addActionListener(listener);
		screenshotItem.addActionListener(listener);
		scanItem.addActionListener(listener);
		disconnectItem.addActionListener(listener);
		connectItem.addActionListener(listener);
		emailItem.addActionListener(listener);
		javaItem.addActionListener(listener);

		debugItem.addItemListener(debugListener);
		recordingtem.addItemListener(recodingListener);
		loggingItem.addItemListener(loggingListener);

		/** Add to menu */
		deviceMenue.add(connectItem);
		userMenue.add(recordingtem);
		userMenue.add(loggingItem);
		userMenue.add(screenshotItem);

		helpMenue.add(debugItem);
		helpMenue.add(emailItem);
		helpMenue.add(javaItem);

		/** Create the menu bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(userMenue);
		menuBar.add(deviceMenue);
		menuBar.add(helpMenue);

		/** Create frame */
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1));
		frame.add(beamCompent);
		frame.add(curve);
		frame.setSize(WIDTH + 6, (HEIGHT * 2) + 48); // room for the menu
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		/** register shutdown hook */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				device.close();
			}
		});
	}

	/** Methods to create Image corresponding to the frame. */
	public static void screenCapture(final Component component) {
		new Thread() {
			public void run() {
				final String fileName = path + ZephyrOpen.fs
						+ System.currentTimeMillis() + ".png";
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

	/** run on timer */
	private class ScanTask extends TimerTask {
		@Override
		public void run() {
			singleScan();
		}
	}

	public ItemListener debugListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				constants.put(ZephyrOpen.frameworkDebug, false);

			if (e.getStateChange() == ItemEvent.SELECTED)
				constants.put(ZephyrOpen.frameworkDebug, true);

			constants.updateConfifFile();
		}
	};

	public ItemListener loggingListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				constants.put(ZephyrOpen.loggingEnabled, false);

			if (e.getStateChange() == ItemEvent.SELECTED)
				constants.put(ZephyrOpen.loggingEnabled, true);

			constants.updateConfifFile();
		}
	};

	public ItemListener recodingListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				constants.put(ZephyrOpen.recording, false);

			if (e.getStateChange() == ItemEvent.SELECTED)
				constants.put(ZephyrOpen.recording, true);

			constants.updateConfifFile();
		}
	};

	private void openBrowser() {
		BrowserLauncher launcher = null;
		try {
			launcher = new BrowserLauncher();
		} catch (BrowserLaunchingInitializingException e) {
			return;
		} catch (UnsupportedOperatingSystemException e) {
			return;
		}
		launcher.setNewWindowPolicy(true);
		launcher.openURLinBrowser("http://www.java.com/en/download/manual.jsp");
		launcher.openURLinBrowser("http://code.google.com/p/zephyropen/");
		launcher.openURLinBrowser("http://verticalchallenge.org");
	}

	private void sendReport() {
		new Thread() {
			public void run() {

				String text = "number of files in " + path + " "
						+ new File(path).list().length + "\r\n";

				text += "number of files in "
						+ constants.get(ZephyrOpen.userLog)
						+ " "
						+ new File(constants.get(ZephyrOpen.userLog)).list().length
						+ "\r\n";

				FileInputStream fstream;
				try {
					
					fstream = new FileInputStream(constants.get(ZephyrOpen.propFile));
					BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
					String strLine;
					text += "-- prop file -- \r\n";
					while ((strLine = br.readLine()) != null) text += strLine + "\r\n";
					br.close();

				} catch (Exception e) {
					return;
				}
			
				/*
				try {
					
					fstream = new FileInputStream(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName) + "_debug.log");
					BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream)));
					String strLine;
					text += "-- debug log -- \r\n";
					while ((strLine = br.readLine()) != null)
						text += strLine + "\r\n";
					br.close();

				} catch (Exception e) {
					return;
				}
				*/
				
				// System.out.println(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName) + "_debug.log");
				
				new SendGmail("beamscanner@gmail.com", "beam-scan").sendMessage("debug info", text); 
				
				//, constants.get(ZephyrOpen.userLog));
			}
		}.start();
	}

	/** Listen for menu */
	private ActionListener listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();

			if (source.equals(javaItem)) {
				openBrowser();
			} else if (source.equals(emailItem)) {
				sendReport();
			} else if (source.equals(scanItem)) {
				new Thread() {
					public void run() {
						if (timer == null)
							singleScan();
					}
				}.start();
			} else if (source.equals(connectItem)) {
				if (!isConnected) {
					isConnected = device.connect();
					if (isConnected) {
						topLeft1 = "CONNECTED";
						topLeft2 = "Port: " + device.getPortName();
						topLeft3 = "Version: " + device.getVersion();
						bottomRight2 = null;
						beamCompent.repaint();
					}
				}
			} else if (source.equals(disconnectItem)) {

				device.close();
				isConnected = false;
				isScanning = false;
				if (timer != null)
					timer.cancel();
				topLeft1 = "DISCONNECTED";
				topLeft2 = "try: Device -> connect";
				topLeft3 = null;
				bottomRight1 = null;
				bottomRight2 = null;
				bottomRight2 = "usb device disconected";
				userMenue.remove(stopItem);
				deviceMenue.add(connectItem);
				deviceMenue.remove(disconnectItem);
				beamCompent.repaint();
				return;

			} else if (source.equals(screenshotItem)) {
				new Thread() {
					public void run() {
						Utils.delay(300);
						screenCapture(frame);
					}
				}.start();
			} else if (source.equals(startItem)) {
				timer = new java.util.Timer();
				timer.scheduleAtFixedRate(new ScanTask(), 0, scanDelay);
				isScanning = true;
				userMenue.remove(startItem);
			} else if (source.equals(stopItem)) {
				timer.cancel();
				timer = null;
				isScanning = false;
				userMenue.remove(stopItem);
			}

			updateMenu();

		}
	};

	public void updateMenu() {
		if (isConnected) {
			topLeft1 = "CONNECTED";
			topLeft2 = "Port: " + device.getPortName();
			topLeft3 = "Version: " + device.getVersion();
			bottomRight2 = null;
			beamCompent.repaint();
			if (isScanning) {
				userMenue.remove(startItem);
				userMenue.remove(scanItem);
				userMenue.add(stopItem);
			} else {
				userMenue.add(startItem);
				userMenue.add(scanItem);
			}

			deviceMenue.add(disconnectItem);
			deviceMenue.remove(connectItem);

		} else {
			deviceMenue.add(connectItem);
			userMenue.remove(startItem);
			userMenue.remove(scanItem);
			deviceMenue.remove(disconnectItem);
		}
	}

	/** take one slice if currently connected */
	public void singleScan() {

		if (!isConnected) {
			topLeft1 = "FAULT";
			topLeft2 = "not connected";
			topLeft3 = "try connecting first";
			topRight1 = null;
			topRight2 = null;
			topRight3 = null;
			beamCompent.repaint();
			return;
		}

		results = device.sample();

		dataPoints = results.points.size();
		scale = (double) WIDTH / dataPoints;
		xCenterpx = (((double) WIDTH) * 0.25);
		yCenterpx = (((double) WIDTH) * 0.75);

		int[] slice = results.getSlice(constants.getInteger("lowLevel") + 5);
		if (slice == null)
			return;

		// constants.put("yellowSlice", 100);
		constants.put(yellowX1, slice[0]);
		constants.put(yellowX2, slice[1]);
		constants.put(yellowY1, slice[2]);
		constants.put(yellowY2, slice[3]);

		// compute pixels
		yellowX1px = (WIDTH / 2) - (xCenterpx - ((double) slice[0] * scale));
		yellowX2px = (WIDTH / 2) - (xCenterpx - ((double) slice[1] * scale));
		yellowY1px = (HEIGHT / 2) - (yCenterpx - ((double) slice[2] * scale));
		yellowY2px = (HEIGHT / 2) - (yCenterpx - ((double) slice[3] * scale));

		// topRight1 = "yellow (" + Utils.formatFloat(yellowX1px, 0) + ", " +
		// Utils.formatFloat(yellowX2px,0)
		// + ")(" + Utils.formatFloat(yellowY1px,0) + ", " +
		// Utils.formatFloat(yellowY2px,0) + ")";

		slice = results.getSlice(100);
		if (slice == null)
			return;
		// constants.put("orangeSlice", 300);
		constants.put(orangeX1, slice[0]);
		constants.put(orangeX2, slice[1]);
		constants.put(orangeY1, slice[2]);
		constants.put(orangeY2, slice[3]);
		orangeX1px = (WIDTH / 2) - (xCenterpx - ((double) slice[0] * scale));
		orangeX2px = (WIDTH / 2) - (xCenterpx - ((double) slice[1] * scale));
		orangeY1px = (HEIGHT / 2) - (yCenterpx - ((double) slice[2] * scale));
		orangeY2px = (HEIGHT / 2) - (yCenterpx - ((double) slice[3] * scale));

		// topRight2 = "orange (" + Utils.formatFloat(orangeX1px, 0) + ", " +
		// Utils.formatFloat(orangeX2px,0)
		// + ")(" + Utils.formatFloat(orangeY1px,0) + ", " +
		// Utils.formatFloat(orangeY2px,0) + ")";

		slice = results.getSlice(400);
		if (slice == null)
			return;
		// constants.put("redSlice", 800);
		constants.put("redX1", slice[0]);
		constants.put("redX2", slice[1]);
		constants.put("redY1", slice[2]);
		constants.put("redY2", slice[3]);
		redX1px = (WIDTH / 2) - (xCenterpx - ((double) slice[0] * scale));
		redX2px = (WIDTH / 2) - (xCenterpx - ((double) slice[1] * scale));
		redY1px = (HEIGHT / 2) - (yCenterpx - ((double) slice[2] * scale));
		redY2px = (HEIGHT / 2) - (yCenterpx - ((double) slice[3] * scale));

		// opRight3 = "red (" + Utils.formatFloat(redX1px, 0) + ", " +
		// Utils.formatFloat(redX2px,0)
		// + ")(" + Utils.formatFloat(redY1px,0) + ", " +
		// Utils.formatFloat(redY2px,0) + ")";

		bottomRight1 = "filtered: " + results.getFilered();

		curve.setIcon(lineGraph(results.points));
		beamCompent.repaint();
		if (constants.getBoolean(ZephyrOpen.recording))
			screenCapture(frame);
	}

	/** create graph */
	public static Icon lineGraph(Vector<Integer> points) {

		BeamLineGraph chart = new BeamLineGraph(); // "beam", "ma",
													// com.googlecode.charts4j.Color.BLUEVIOLET);
		Icon icon = null;
		for (int j = 0; j < points.size(); j += 2)
			chart.add(String.valueOf(points.get(j)));

		try {
			String str = chart.getURLString(WIDTH, HEIGHT,
					"data: " + (points.size() / 5));

			System.out.println(constants.toString());
			System.out.println(str);

			if (str != null) {
				icon = new ImageIcon(new URL(str));
			}
		} catch (final Exception e) {
			constants.error(e.getMessage());
		}
		return icon;
	}

	/** draw cross section chart */
	public class BeamComponent extends JComponent {
		private static final long serialVersionUID = 1L;

		// TODO: TAKE FROM PROPS
		private boolean drawLines = false;

		public void paint(Graphics g) {
			final int w = getWidth();
			final int h = getHeight();

			// nonSystem.out.println("reddaw");

			g.setColor(Color.lightGray);
			g.fillOval((int) yellowX1px, (int) yellowY1px, (int) yellowX2px
					- (int) yellowX1px, (int) yellowY2px - (int) yellowY1px);
			if (drawLines) {
				g.drawLine((int) yellowX1px, 0, (int) yellowX1px, h);
				g.drawLine((int) yellowX2px, 0, (int) yellowX2px, h);
				g.drawLine(0, (int) yellowY1px, w, (int) yellowY1px);
				g.drawLine(0, (int) yellowY2px, w, (int) yellowY2px);
			}

			g.setColor(Color.ORANGE);
			g.fillOval((int) orangeX1px, (int) orangeY1px, (int) orangeX2px
					- (int) orangeX1px, (int) orangeY2px - (int) orangeY1px);
			if (drawLines) {
				g.drawLine((int) orangeX1px, 0, (int) orangeX1px, h);
				g.drawLine((int) orangeX2px, 0, (int) orangeX2px, h);
				g.drawLine(0, (int) orangeY1px, w, (int) orangeY1px);
				g.drawLine(0, (int) orangeY2px, w, (int) orangeY2px);
			}

			g.setColor(Color.RED);
			g.fillOval((int) redX1px, (int) redY1px, (int) redX2px
					- (int) redX1px, (int) redY2px - (int) redY1px);
			if (drawLines) {
				g.drawLine((int) redX1px, 0, (int) redX1px, h);
				g.drawLine((int) redX2px, 0, (int) redX2px, h);
				g.drawLine(0, (int) redY1px, w, (int) redY1px);
				g.drawLine(0, (int) redY2px, w, (int) redY2px);
			}

			// draw grid
			g.setColor(Color.BLACK);
			g.drawLine(0, h - 1, w, h - 1);
			Graphics2D g2d = (Graphics2D) g;
			Stroke stroke2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 6.0f, 2.0f,
							1.0f, 2.0f }, 0.0f);
			g2d.setStroke(stroke2);
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);

			if (results != null) {
				topRight1 = new java.util.Date().toString();
				topRight2 = "Data Points: " + results.points.size();
				topRight3 = "Spin Time:   " + results.scanTime() + " ms";

				// TODO:
				bottomRight2 = " x: " + results.getMaxIndexX() + " y: "
						+ results.getMaxIndexY();
			}

			// bottomRight1 = " h: " + h + " w: " + w;

			// draw text
			if (topRight1 != null)
				g.drawString(topRight1, (w / 2 + 5), 15);
			if (topRight2 != null)
				g.drawString(topRight2, (w / 2 + 5), 30);
			if (topRight3 != null)
				g.drawString(topRight3, (w / 2 + 5), 45);

			if (bottomRight1 != null)
				g.drawString(bottomRight1, (w / 2 + 5), h - 10);
			if (bottomRight2 != null)
				g.drawString(bottomRight2, (w / 2 + 5), h - 25);
			if (bottomRight3 != null)
				g.drawString(bottomRight3, (w / 2 + 5), h - 40);

			if (topLeft1 != null)
				g.drawString(topLeft1, 15, 15);
			if (topLeft2 != null)
				g.drawString(topLeft2, 15, 30);
			if (topLeft3 != null)
				g.drawString(topLeft3, 15, 45);
		}
	}
}