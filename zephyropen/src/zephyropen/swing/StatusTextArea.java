package zephyropen.swing;

import javax.swing.JTextArea;

import zephyropen.api.API;
import zephyropen.api.ApiFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.util.Utils;

public class StatusTextArea extends JTextArea implements API {

	private static final long serialVersionUID = 1L;
	private static ApiFactory apiFactory = ApiFactory.getReference();
	
	public StatusTextArea(){
		
		super();
		setAutoscrolls(true);
		setEditable(false);
		
		/** register with all relevant message types */
		apiFactory.add(this);
		
		//apiFactory.add(ZephyrOpen.discovery, this);
		//apiFactory.add(ZephyrOpen.command, this);
		//apiFactory.add(ZephyrOpen.launch, this);
		
	}
	
	public void execute(Command command) {
		
		String list = command.list();
		append( /*Utils.getTime() + " " + */ command.getType() + " : " + list + "\n");
		setCaretPosition(getDocument().getLength());
	}
	
	public String getDeviceName() {
		return ZephyrOpen.status;
	}

	public String getAddress() {
		return ZephyrOpen.getReference().get(ZephyrOpen.address);
	}
	
	public long getDelta() {
		return 0;
	}
}
