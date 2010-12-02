package zephyropen.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.util.ftp.FTPManager;
import zephyropen.util.google.GoogleChart;
import zephyropen.util.google.ScreenShot;

/** Create a multiple tab frame for all the graphs for this device */
public class TabbedFrame extends AbstractFrame implements ComponentListener, KeyListener {

	/** swing requires */
	private static final long serialVersionUID = 1L;

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	protected static ApiFactory apiFactory = ApiFactory.getReference();

	/** add space around the icon image */
	public static final int X_EDGE = 16;
	public static final int Y_EDGE = 50;

	public static final int X_EDGE_OSX = 20;
	public static final int Y_EDGE_OSX = 60;

	/** how often to send an ftp update or recording */
	private static final int MOD = 30;

	private int counter = 1;

	private final JTabbedPane tabbedPane = new JTabbedPane();

	private GoogleChart[] components = null;

	private FTPManager ftpManager = null;

	private boolean isOSX = false;

	/**
	 * place the 'parts' onto tabbed frame
	 * 
	 * @param api
	 */
	public TabbedFrame(GoogleChart[] parts) {

		setLayout(new GridLayout(1, 1));
		components = parts;

		// start with defaults
		setPreferredSize(new Dimension(GoogleChart.DEFAULT_X_SIZE, GoogleChart.DEFAULT_Y_SIZE));
		setSize(new Dimension(GoogleChart.DEFAULT_X_SIZE, GoogleChart.DEFAULT_Y_SIZE));

		/** add the components onto a pane */
		createTabs();

		/** place on Panel */
		add(tabbedPane);

		/** listen for key presses for screen shots etc */
		tabbedPane.addKeyListener(this);

		/** listen for re-size events */
		frame.addComponentListener(this);

		/** add listener for tab selection changes */
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				updateSelected();
			}
		});

		/** see is ftp is available */
		ftpManager = FTPManager.getReference();

		if (constants.get(ZephyrOpen.os).startsWith("Mac")) {

			constants.put(ZephyrOpen.xSize, String.valueOf(frame.getWidth() - X_EDGE_OSX));
			constants.put(ZephyrOpen.ySize, String.valueOf(frame.getHeight() - Y_EDGE_OSX));
			isOSX = true;

		}
	}

	/** place in tabs with icons */
	public void createTabs() {

		String filename = null;
		for (int i = 0; i < components.length; i++) {

			/** center the image */
			((JLabel) components[i]).setHorizontalAlignment(JLabel.CENTER);

			/** set the size needed for the image */
			components[i].setPreferredSize(new Dimension(frame.getWidth(),
					frame.getWidth()));

			filename = "images" + System.getProperty("file.separator")
					+ components[i].getTitle() + ".png";

			if (new File(filename).exists()) {

				tabbedPane.addTab(null, new ImageIcon(filename), components[i]);

			} else {

				constants.info("path not found: " + filename, this);

				/** else no icon for you ! */
				tabbedPane.addTab(components[i].getTitle(), null, components[i]);

			}
		}

		/** add nag tab of copyright info */
		tabbedPane.addTab(null, new ImageIcon("images/about.png"), new AboutTab());
	}

	/** Re-draw the graph only the current tab */
	@Override
	public void updateSelected() {

		/** don't refresh the status, not an icon label */
		if (tabbedPane.getSelectedIndex() == tabbedPane.getTabCount() - 1)
			return;

		/** update current icon */
		components[tabbedPane.getSelectedIndex()].updateIcon();

		// count updates
		counter++;

		if (counter % MOD == 0)
			for (int i = 0; i < tabbedPane.getTabCount() - 1; i++)
				ftpManager.upload(components[i]);

		if (constants.getBoolean(ZephyrOpen.recording))
			if (counter % MOD == 0)
				for (int i = 0; i < tabbedPane.getTabCount() - 1; i++)
					new ScreenShot(components[i]);
	}

	/** manage re-size events here */
	public void componentResized(ComponentEvent e) {

		if (isOSX) {

			constants.put(ZephyrOpen.xSize, String.valueOf(frame.getWidth() - X_EDGE_OSX));
			constants.put(ZephyrOpen.ySize, String.valueOf(frame.getHeight() - Y_EDGE_OSX));

		} else {

			constants.put(ZephyrOpen.xSize, String.valueOf(frame.getWidth() - X_EDGE));
			constants.put(ZephyrOpen.ySize, String.valueOf(frame.getHeight() - Y_EDGE));

		}

		updateSelected();
	}

	/**
	 * Manage key inputs
	 */
	public void keyTyped(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_TYPED) {

			char c = Character.toLowerCase(e.getKeyChar());

			if (c == ' ') {
				new ScreenShot(components[tabbedPane.getSelectedIndex()]);
				return;
			}

			if (c == 'x') {
				components[tabbedPane.getSelectedIndex()].getState().reset();
				return;
			}

			// TODO: REVIEW this!
			// don't let several proc's do this at once
			// if (constants.getBoolean(ZephyrOpen.filelock)) {

			// put this in some command or keyboard manager

			if (c == 'r')
				constants.put(ZephyrOpen.recording, "true");

			else if (c == 's')
				constants.put(ZephyrOpen.recording, "false");

			else if (c == 'd')
				constants.put(ZephyrOpen.ftpEnabled, "false");

			else if (c == 'o') {
				constants.put(ZephyrOpen.loggingEnabled, "false");
				constants.info("logging off, still have locked file", this);
			}

			else if (c == 'p') {
				constants.put(ZephyrOpen.loggingEnabled, "true");
				constants.info("logging on, still have locked file", this);
			}

			else if (c == 'f')
				if (ftpManager.ftpConfigured())
					constants.put(ZephyrOpen.ftpEnabled, "true");

		}
	}

	/** not used currently */
	public void componentHidden(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
}
