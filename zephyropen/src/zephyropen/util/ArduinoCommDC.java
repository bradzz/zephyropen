package zephyropen.util;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author brad.zdanivsky@gmail.com
 */
public class ArduinoCommDC implements SerialPortEventListener {

	// private static Logger log =
	// Red5LoggerFactory.getLogger(ArduinoCommDC.class, "oculus");

	public static final int TIME_OUT = 2000;
	public static final int RESPOND_DELAY = 100;
	public static final long MIN_WRITE_DELAY = 200;
	
	// set this on the firmware! 
	public static final long DEAD_TIME_OUT = 10000;

	// add commands here
	public static byte STOP = 's';
	public static byte FORWARD = 'f'; 
	public static byte BACKWARD = 'b';
	public static byte LEFT = 'l';
	public static byte RIGHT = 'r';
	public static byte COMP = 'c';
	
	public static byte[] SLIDE_LEFT = { 'L', '\n' };
	public static byte[] SLIDE_RIGHT = { 'R', '\n' };
	protected static final byte[] DETACH = { 'd', '\n' };

	// just do on reset instead?
	public final byte[] GET_PRODUCT = { 'x' };
	public final byte[] GET_VERSION = { 'y' };

	// comm port, static because only one instance ?
	private String portName = null;
	private/* static */SerialPort serialPort = null;
	private/* static */InputStream in;
	private/* static */OutputStream out;

	// input buffer
	private byte[] buffer = new byte[64];
	private int buffSize = 0;

	// track write times
	private long lastSent = System.currentTimeMillis();
	private long lastRead = System.currentTimeMillis();

	// WatchDog watchdog = null;

	// /protected CommPort commPort;
	// Settings settings = new Settings();

	protected int speedslow = 45; // Integer.parseInt(settings.readSetting("speedslow"));
	protected int speedmed = 56; // Integer.parseInt(settings.readSetting("speedmed"));
	protected int camservohoriz = 67; // Integer.parseInt(settings.readSetting("camservohoriz"));
	protected int camposmax = 78; // Integer.parseInt(settings.readSetting("camposmax"));
	protected int camposmin = 123; // Integer.parseInt(settings.readSetting("camposmin"));
	protected int nudgedelay = 67; // Integer.parseInt(settings.readSetting("nudgedelay"));
	protected int maxclicknudgedelay = 67; // Integer.parseInt(settings.readSetting("maxclicknudgedelay"));
	protected int maxclickcam = 78; // Integer.parseInt(settings.readSetting("maxclickcam"));
	protected double clicknudgemomentummult = 67; // Double.parseDouble(settings.readSetting("clicknudgemomentummult"));
	protected int steeringcomp = 230; // Integer.parseInt(settings.readSetting("steeringcomp"));

	protected int camservodirection = 0;
	protected int camservopos = camservohoriz;
	protected int camwait = 400;
	protected int camdelay = 50;
	protected int speedfast = 255;
	protected int turnspeed = 255;
	protected int speed = speedfast; // set default to max
	protected int arduinodelay = 4;
	protected String direction = null;
	protected boolean moving = false;
	volatile boolean sliding = false;
	volatile boolean movingforward = false;
	int tempspeed = 999;
	int clicknudgedelay = 0;
	String tempstring = null;
	int tempint = 0;
	long nextcommandtime = 0;
	boolean isconnected = false;

	/**
	 * Constructor but call connect to configure
	 * 
	 * @param str
	 *            is the name of the serial port on the host computer
	 */
	public ArduinoCommDC(String str) {

		// keep port name, need to reconnect
		portName = str;

		// check for lost connection
		new WatchDog().start();
	}

	/** open port, enable read and write, enable events */
	public void connect() {
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(ArduinoCommDC.class.getName(), TIME_OUT);
			serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open streams
			out = serialPort.getOutputStream();
			in = serialPort.getInputStream();

			// register for serial events
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		// setup delay
		try {
			Thread.sleep(TIME_OUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// all good, ready for commands
		isconnected = true;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				// System.out.println("DA: " + in.available());
				// loop on data avail
				byte[] input = new byte[32];
				int read = in.read(input);
				for (int j = 0; j < read; j++) {

					// print() or println() from ardunio code
					if ((input[j] == '>') || (input[j] == 13) || (input[j] == 10)) {

						print();

						// reset
						buffSize = 0;

						// track input from arduino
						lastRead = System.currentTimeMillis();

					} else if (input[j] == '<') {

						// start of message
						buffSize = 0;

					} else {

						// buffer until ready to parse
						buffer[buffSize++] = input[j];
					}
				}
			} catch (IOException e) {
				System.out.println("event : " + e.getLocalizedMessage());
			}
		}
	}

	private void print() {

		if (buffSize == 0)
			return;

		System.out.print("delta : " + getReadDelta());
		System.out.print(" [" + buffSize + "] ");
		String responce = "";
		for (int i = 0; i < buffSize; i++)
			responce += (char) buffer[i];

		// for (int i = 0; i < buffSize; i++)
		// System.out.println(i + " : " + (char)buffer[i] + " : " + buffer[i]);

		System.out.println(responce.trim());
	}

	/** @return the time since last write() operation */
	public long getWriteDelta() {
		return System.currentTimeMillis() - lastSent;
	}

	/** @return the time since last read operation */
	public long getReadDelta() {
		return System.currentTimeMillis() - lastRead;
	}
	
	/** inner class to check if getting responses */
	private class WatchDog extends Thread {

		public WatchDog() {
			System.out.println("starting watchdog thread");
			this.setDaemon(true);
		}

		public void run() {
			while (true) {				
				if(getReadDelta() > DEAD_TIME_OUT) {
					if (isconnected) {
					
						System.out.println("in delta: " + (System.currentTimeMillis() - lastRead));
						System.err.println("no info coming back from arduino, resting: " + portName);

						// reset 
						disconnect();
						connect();
							
						// lastRead = System.currentTimeMillis();
						
						if(isconnected){
							System.out.println("re-connected, stopping bot..");
							//TODO: send current state, speed, comp etc  ??/
							stopGoing();
						}
					}
				}

				try {
					// check often
					Thread.sleep(RESPOND_DELAY + 100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void disconnect() {
		try {
			in.close();
			out.close();
			isconnected = false;
		} catch (Exception e) {
			System.out.println("close(): " + e.getMessage());
		}
		serialPort.close();
	}

	/**
	 * Send a multi byte command to the arduino with protection for trying to
	 * send too quickly
	 * 
	 * @param command
	 *            is a finalized byte array of messages to send
	 */
	private synchronized void sendCommand(final byte[] command) {

		if (!isconnected) {
			System.err.println("not connected, try connecting...");
			connect();
			return;
		}

		if (getWriteDelta() < MIN_WRITE_DELAY) {
			try {

				System.out.println("sending too fast: " + getWriteDelta());

				// only wait as long as needed to make next time slot 
				Thread.sleep((MIN_WRITE_DELAY - getWriteDelta()) + 1);

				System.out.println("delta now: " + getWriteDelta());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {

			// send bytes
			out.write(command);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// track last write
		lastSent = System.currentTimeMillis();
	}

	public void sendcommand(int command1, int command2) {
		System.err.println("depri");
	}

	/** */
	public void stopGoing() {
		moving = false;
		movingforward = false;

		new Thread(new Runnable() {
			public void run() {
				try {
					final byte[] command = {STOP, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/** */
	public void goForward() {
		new Thread(new Runnable() {
			public void run() {
				try {
					final byte[] command = {FORWARD, (byte)speed, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		moving = true;
		movingforward = true;
	}

	/** */
	public void goBackward() {
		new Thread(new Runnable() {
			public void run() {
				try {
					final byte[] command = {BACKWARD, (byte)speed, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		moving = true;
		movingforward = false;
	}

	/** */
	public void turnRight() {
		new Thread(new Runnable() {
			public void run() {
				try {
					int tmpspeed = turnspeed;
					int boost = 10;
					if (speed < turnspeed && (speed + boost) < speedfast) 
						tmpspeed = speed + boost;
					
					// send it
					final byte[] command = {RIGHT, (byte)tmpspeed, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		moving = true;
	}

	/** */
	public void turnLeft() {
		new Thread(new Runnable() {
			public void run() {
				try {
					int tmpspeed = turnspeed;
					int boost = 10;
					if (speed < turnspeed && (speed + boost) < speedfast) 
						tmpspeed = speed + boost;
					
					// send it 
					final byte[] command = {LEFT, (byte)tmpspeed, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		moving = true;
	}

	public void camGo() {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (camservodirection != 0) {
						sendcommand(4, camservopos);
						Thread.sleep(camdelay);
						camservopos += camservodirection;
						if (camservopos > camposmax) {
							camservopos = camposmax;
							camservodirection = 0;
						}
						if (camservopos < camposmin) {
							camservopos = camposmin;
							camservodirection = 0;
						}
					}
					Thread.sleep(250);
					sendcommand(8, -1); // release
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void camCommand(String str) {
		if (str.equals("stop")) {
			camservodirection = 0;
		}
		if (str.equals("up")) {
			camservodirection = 1;
			camGo();
		}
		if (str.equals("down")) {
			camservodirection = -1;
			camGo();
		}
		if (str.equals("horiz")) {
			camHoriz();
		}
		if (str.equals("downabit")) {
			camservopos -= 5;
			if (camservopos < camposmin) {
				camservopos = camposmin;
			}
			new Thread(new Runnable() {
				public void run() {
					try {
						// arduinoDelay(-1); // testing this here
						sendcommand(4, camservopos);
						Thread.sleep(camwait);
						sendcommand(8, -1); // release
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		if (str.equals("upabit")) {
			camservopos += 5;
			if (camservopos > camposmax) {
				camservopos = camposmax;
			}
			new Thread(new Runnable() {
				public void run() {
					try {
						// arduinoDelay(-1); // testing this here
						sendcommand(4, camservopos);
						Thread.sleep(camwait);
						sendcommand(8, -1); // release
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void camHoriz() {

		new Thread(new Runnable() {
			public void run() {
				try {
					// arduinoDelay(-1); // testing this here
					sendcommand(4, camservohoriz);
					camservopos = camservohoriz;
					Thread.sleep(camwait);
					sendcommand(8, -1); // release
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	/** detach the cam servo */
	public void camHoldStill() { 
		new Thread(new Runnable() {
			public void run() {
				try {
					sendCommand(DETACH);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/** Set the speed on the bot */
	public void speedset(String str) {
		if (str.equals("slow")) {
			speed = speedslow;
		}
		if (str.equals("med")) {
			speed = speedmed;
		}
		if (str.equals("fast")) {
			speed = speedfast;
		}
		if (movingforward) {	
			goForward(); 
		}
	}

	public void nudge(String dir) {
		direction = dir;
		new Thread(new Runnable() {
			public void run() {
				try {
					int n = nudgedelay;
					if (direction.equals("right")) {
						turnRight();
					}
					if (direction.equals("left")) {
						turnLeft();
					}
					if (direction.equals("forward")) {
						goForward();
						movingforward = false;
						n *= 4;
					}
					if (direction.equals("backward")) {
						goBackward();
						n *= 4;
					}
					Thread.sleep(n);
					if (movingforward == true) {
						goForward();
					} else {
						stopGoing();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void slide(String dir) {
		if (sliding == false) {
			sliding = true;
			direction = dir;
			tempspeed = 999;
			new Thread(new Runnable() {
				public void run() {
					try {
						int distance = 300;
						int turntime = 500;
						tempspeed = speed;
						speed = speedfast;
						if (direction.equals("right")) {
							turnLeft();
						} else {
							turnRight();
						}
						Thread.sleep(turntime);
						if (sliding == true) {
							goBackward();
							Thread.sleep(distance);
							if (sliding == true) {
								if (direction.equals("right")) {
									turnRight();
								} else {
									turnLeft();
								}
								Thread.sleep(turntime);
								if (sliding == true) {
									goForward();
									Thread.sleep(distance);
									if (sliding == true) {
										stopGoing();
										sliding = false;
										speed = tempspeed;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void slidecancel() {
		if (sliding == true) {
			if (tempspeed != 999) {
				speed = tempspeed;
				sliding = false;
			}
		}
	}

	public Integer clickSteer(String str) {
		tempstring = str;
		tempint = 999;
		String xy[] = tempstring.split(" ");
		if (Integer.parseInt(xy[1]) != 0) {
			tempint = clickCam(Integer.parseInt(xy[1]));
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					String xy[] = tempstring.split(" ");
					if (Integer.parseInt(xy[0]) != 0) {
						if (Integer.parseInt(xy[1]) != 0) {
							Thread.sleep(camwait);
						}
						clickNudge(Integer.parseInt(xy[0]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return tempint;
	}

	public void clickNudge(Integer x) {
		if (x > 0) {
			direction = "right";
		} else {
			direction = "left";
		}
		clicknudgedelay = maxclicknudgedelay * (Math.abs(x)) / 320;
		/*
		 * multiply clicknudgedelay by multiplier multiplier increases to
		 * CONST(eg 2) as x approaches 0, 1 as approaches 320
		 * ((320-Math.abs(x))/320)*1+1
		 */
		double mult = Math.pow(((320.0 - (Math.abs(x))) / 320.0), 3)
				* clicknudgemomentummult + 1.0;
		// System.out.println("clicknudgedelay-before: "+clicknudgedelay);
		clicknudgedelay = (int) (clicknudgedelay * mult);
		// System.out.println("n: "+clicknudgemomentummult+" mult: "+mult+" clicknudgedelay-after: "+clicknudgedelay);
		new Thread(new Runnable() {
			public void run() {
				try {
					tempspeed = speed;
					speed = speedfast;
					if (direction.equals("right")) {
						turnRight();
					} else {
						turnLeft();
					}
					Thread.sleep(clicknudgedelay);
					speed = tempspeed;
					if (movingforward == true) {
						goForward();
					} else {
						stopGoing();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public Integer clickCam(Integer y) {
		Integer n = maxclickcam * y / 240;
		camservopos -= n;
		if (camservopos > camposmax) {
			camservopos = camposmax;
		}
		if (camservopos < camposmin) {
			camservopos = camposmin;
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					sendcommand(4, camservopos);
					Thread.sleep(camwait + clicknudgedelay);
					sendcommand(8, -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return camservopos;
	}

	public void camToPos(Integer n) {
		camservopos = n;
		new Thread(new Runnable() {
			public void run() {
				try {
					sendcommand(4, camservopos);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void updateSteeringComp() {
		
		// new commandSender(command).start();
		
		new Thread(new Runnable() {
			public void run() {
				try {
					//TODO: is there left and right comp values ? 
					final byte[] command = {COMP, (byte)steeringcomp, '\n'};
					sendCommand(command);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/* test driver */
	public static void main(String[] args) throws Exception {

		FindPort find = new FindPort();
		String portstr = find.search(FindPort.OCULUS_DC);
		if (portstr != null) {

			ArduinoCommDC dc = new ArduinoCommDC(portstr);
			
			dc.connect();
			if (!dc.isconnected) {
				System.out.println("can't connect to: " + portstr);
				System.exit(0);
			}

			System.out.println("connected oculus on: " + portstr);

			dc.updateSteeringComp();
			dc.goBackward();

			Thread.sleep(5650);

			dc.goBackward();

			Thread.sleep(7700);
			
			dc.turnLeft();

			Thread.sleep(300);

			dc.turnRight();

			Thread.sleep(4760);

			dc.stopGoing();

			Thread.sleep(60000);
		}

		System.out.println(".. done");

		// force exit
		System.exit(0);
	}
}
