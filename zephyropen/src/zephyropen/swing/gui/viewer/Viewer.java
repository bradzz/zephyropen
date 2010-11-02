package zephyropen.swing.gui.viewer;

import zephyropen.command.Command;
import zephyropen.swing.TabbedFrame;

public interface Viewer {
	
	public void update(Command command);
	
	public void poll();
	
	public TabbedFrame getFrame();
	
}
