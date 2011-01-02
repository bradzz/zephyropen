package zephyropen.swing;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import zephyropen.api.ZephyrOpen;
import edu.stanford.ejalbert.BrowserLauncher;


/**
 * 
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 *
 */
public class AboutTab extends JPanel implements ActionListener {
	
	/** */
	private static final long serialVersionUID = 1L;

	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	
	private JButton zephyropen;
	private JButton celia;
	private JButton brad;

	/** @return a label with copyright info Label */
	public AboutTab(){
		
		super(new GridLayout(2,2)); 
		super.setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		/** setup buttons */
		zephyropen = new JButton(new ImageIcon("images/zephyr80.png")); 
		zephyropen.addActionListener(this);
		zephyropen.setCursor(new Cursor(Cursor.HAND_CURSOR));
		zephyropen.setToolTipText("visit software progect home and download site");
		zephyropen.setHorizontalTextPosition( JButton.RIGHT );
		zephyropen.setBorder(BorderFactory.createEmptyBorder());
		zephyropen.setText("<html><font color=\"333333\" size=\"-1\">&nbsp;&nbsp;&nbsp;Version " +
				ZephyrOpen.VERSION + "<br>&nbsp;&nbsp;&nbsp;" + constants.get(ZephyrOpen.os) + 
				"</font></html>");
		
		brad = new JButton(new ImageIcon("images/brad.jpg")); 	
		brad.addActionListener(this);
		brad.setText("code - brad zdanivsky");
		// <html><font color=\"333333\">brad zdanivsky<br>code"+
		// "<br><u></font><font color=\"blue\">verticalchallenge.org</font></u></html>");
		brad.setToolTipText("visit brad's blog");
		brad.setCursor(new Cursor(Cursor.HAND_CURSOR));
		brad.setBorder( BorderFactory.createEmptyBorder() ); 
			
		celia = new JButton(new ImageIcon("images/celia.jpg")); 
		celia.addActionListener(this);
		celia.setCursor(new Cursor(Cursor.HAND_CURSOR));
		celia.setToolTipText("visit celia's blog");
		celia.setText("graphics - celia chung"); 
		//<html><font color=\"333333\">celia chung<br>graphics"+
		//"<br><u></font><font color=\"blue\">celiachung.carbonmade.com</font></u></html>");
		celia.setBorder( BorderFactory.createEmptyBorder() ); 
		
		/** License note */
		JLabel release = new JLabel("<html><br />&nbsp;&nbsp;&nbsp;&#169;&nbsp;2011&nbsp;Brad&nbsp;Zdanivsky<br /><br /><font color=\"333333\">&nbsp;&nbsp;&nbsp;released and distributed under </font>"
				+"<font color=\"blue\">GPLv2</font></html>");
		release.setFont( new Font("Arial", Font.PLAIN, 10));
		
		/** add with grid layout */	
		add(zephyropen); 
		add(celia);
		add(release);
		add(brad);
		
		/** setting these doesn't stop resize */ 
		setPreferredSize(new Dimension(500,200));
		setMaximumSize(new Dimension(500,200));
		setSize(500, 200);
	}

	/** manage button events, open web browser */
	public void actionPerformed(ActionEvent e) {
		
		BrowserLauncher launcher = null;
					
		try {
						
			launcher = new BrowserLauncher();
			launcher.setNewWindowPolicy(true);
			
			if( e.getSource() == zephyropen ){
				launcher.openURLinBrowser("http://code.google.com/p/zephyropen/");
			} else if (e.getSource() == brad ){
				launcher.openURLinBrowser("http://verticalchallenge.org/");
			} else if( e.getSource() == celia ){
				launcher.openURLinBrowser("http://www.celiadraws.com/");
			}
		} catch (Exception ex) {
			constants.shutdown(ex);
		} 	
	}
}
