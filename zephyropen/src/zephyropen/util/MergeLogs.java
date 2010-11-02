package zephyropen.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import zephyropen.api.PrototypeFactory;
import zephyropen.api.ZephyrOpen;
import zephyropen.command.Command;
import zephyropen.state.FilterFactory;
import zephyropen.xml.Parser;
import zephyropen.xml.XMLParser;

/**
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 */
public class MergeLogs {

	/** framework configuration */
	private final static ZephyrOpen constants = ZephyrOpen.getReference();
	private Connection conn;
	private static Long counter = 0L;

	/**  */
	public MergeLogs(String dirName) {

		if (!jdbcOpen()) {
			constants.error("no database connection");
			return;
		}

		
		File[] files = getFiles(constants.get(ZephyrOpen.userLog), ".log");
		for (int i = 0; i < files.length; i++) {

			constants.info(i + " : " + files[i].getName());
			readLog(new File(constants.get(ZephyrOpen.userLog) + "/" + files[i].getName()));
			files[i].renameTo(new File(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + files[i].getName() + ".done"));
			files[i].delete();
			Utils.delay(30000);
			System.gc();
		}
		
		/*
		File[] files = getFiles(constants.get(ZephyrOpen.userLog), ".xml");
		for (int i = 0; i < files.length; i++) {

			constants.info(i + " : " + files[i].getName());
			readXML(new File(constants.get(ZephyrOpen.userLog) + "/" + files[i].getName()));
			
			files[i].renameTo(new File(constants.get(ZephyrOpen.userLog) + ZephyrOpen.fs + files[i].getName() + ".done"));
			files[i].delete();
			
			Utils.delay(30000);
		}*/

		jdbcClose();
	}

	// setup files
	public File[] getFiles(String dirName, String ext) {

		// get all file names
		File[] all = (new File(dirName)).listFiles();

		int size = 0;
		for (int i = 0; i < all.length; i++) {
			if (all[i].getName().endsWith(ext))
				if (all[i].getName().indexOf("_config") == -1)
					if (all[i].getName().indexOf("_debug") == -1)
						size++;
		}

		// System.out.println("found files = " + all.length + "\n" + ext + " = " + size);

		File[] xml = new File[size];
		int c = 0;
		for (int i = 0; i < all.length; i++) {
			if (all[i].isFile()) {
				if (all[i].getName().endsWith(ext))
					if (all[i].getName().indexOf("_config") == -1)
						if (all[i].getName().indexOf("_debug") == -1)
							xml[c++] = all[i];
			}
		}

		return xml;
	}

	private boolean jdbcOpen() {

		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String url = "jdbc:mysql://localhost/backup";
			conn = DriverManager.getConnection(url, "brent", "zdy");

		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			return false;
		}

		return true;
	}

	private void jdbcClose() {

		try {

			conn.close();

		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void insert(Command command) {

		if (command.get(ZephyrOpen.TIME_MS) == null)
			return;

		if (FilterFactory.filter(command)) {

			try {
				
				counter++;

				Statement st = conn.createStatement();
				st.executeUpdate("INSERT INTO HEART VALUES ("
						+ command.get(ZephyrOpen.TIME_MS) + ", "
						+ command.get(PrototypeFactory.heart) + ")");

			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}

		// else constants.error("filtered: " + command.toString());
	}
 
	private void insert(long timestamp, int hr) {

		// System.out.println("inserting: " + timestamp + " " + hr);
		
		if( (hr > 30 ) && (hr < 140)){
		try {
			
			counter++;

			Statement st = conn.createStatement();
			st.executeUpdate("INSERT INTO HEART VALUES (" + timestamp + ", " + hr + ")");

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
		} else constants.error("filtered: " + hr);
	}

	/** */
	public void readXML(File file) {

		/** Basic XML parser */
		Parser xmlParser = new XMLParser();

		constants.info("opening: " + file.getName());

		LineNumberReader lnreader = null;
		FileReader freader = null;
		try {

			freader = new FileReader(file);
			lnreader = new LineNumberReader(freader);
			String line = "";
			Command command;

			long start = System.currentTimeMillis();
			constants.info("reading: " + file.getName() + new Date(start));
			while ((line = lnreader.readLine()) != null) {

				//if( lnreader.getLineNumber() > 2000000 ){
					
				command = xmlParser.parse(line);
				if (command != null)
					insert(command);

				if (lnreader.getLineNumber() % 50000 == 0) {

					System.out.println(counter + " " + Runtime.getRuntime().freeMemory() + " : " + lnreader.getLineNumber());
					Utils.delay(1000);

				}
				
				/*
				if( Runtime.getRuntime().freeMemory() < 500000 ){
					constants.info("resting, gc() ... ");
					System.gc();
					Utils.delay(ZephyrOpen.ONE_MINUTE);
					constants.info("... back");
				}
				*/
				
			}

			constants.info("lines read:" + lnreader.getLineNumber());
			constants.info(file.getName() + " took seconds: " + (System.currentTimeMillis() - start) / 1000);

		} catch (Exception e) {

			constants.error(e.getMessage(), this);
			System.out.println(e.getStackTrace());

		} finally {
			try {
				freader.close();
				lnreader.close();
			} catch (IOException e) {
				constants.error(e.getMessage(), this);
				System.out.println(e.getStackTrace());
			}
		}
	}

	/** */
	public void readLog(File file) {

		constants.info("opening: " + file.lastModified() + " " + file.getName());

		LineNumberReader lnreader = null;
		FileReader freader = null;
		try {

			freader = new FileReader(file);
			lnreader = new LineNumberReader(freader);
			String line = "";
			int old = 0;

			long start = System.currentTimeMillis();

			while ((line = lnreader.readLine()) != null) {

				long time = getTime(line);
				int hr = 0;

				// 12345, com5, 66, 255
				if (line.indexOf("COM") > 0) {

					hr = getHeart(line);

				} else {

					hr = getHeartOld(line);
					old++;
					
				}

				if (hr > 30 && hr < 130)
					insert(time, hr);

				if (lnreader.getLineNumber() % 10000 == 0) {
					System.out.println("lines read:" + lnreader.getLineNumber());
				}
			}
			
			constants.info("lines read:" + lnreader.getLineNumber());
			constants.info("old: " + old);
			constants.info(file.getName() + " took seconds: " + (System.currentTimeMillis() - start) / 1000);

		} catch (Exception e) {

			constants.error(e.getMessage(), this);
			System.out.println(e.getStackTrace());

		} finally {
			try {
				freader.close();
				lnreader.close();
			} catch (IOException e) {
				constants.error(e.getMessage(), this);
				System.out.println(e.getStackTrace());
			}
		}
	}

	/** */
	public static long getTime(String line) {
		long ms = Long.parseLong(line.substring(0, line.indexOf(',')));
		return ms;
	}

	/** 1234, com5, 56, 888 */
	public static int getHeart(String line) {
		
		String text = "";
		int value = 0;

		try{ 
			
		
		int first = line.indexOf(',');
		int second = line.indexOf(',', first+1);
		int third = line.indexOf(',', second+1);

		/*
		if (first == -1 || second == -1 || third == -1) {
			constants.error("line error: " + line);
			return 0;
		}
		*/

		text = line.substring(second+1, third);
		
		//System.out.println(line + "\ntext: " + text);
		
		value = Integer.valueOf(text.trim());

		} catch (Exception e) {
			System.out.println("error text: " + text);
			return 0;
		}
		
		return value;
	}

	/** 1234, 56, 88 */
	public static int getHeartOld(String line) {

		String hr;
		
		try {
			
			int first = line.indexOf(',')+1;
			int second = line.indexOf(',', first)+1;

			hr = line.substring(first, second).trim();
			
		} catch (Exception e) {
			return 0;
		}
		
		return Integer.valueOf(hr);
	}

	/** */
	public static String getDate(long ms) {
		return new Date(ms).toString();
	}

	/** Find log file by naming convention given in args[] */
	public static void main(String[] args) throws Exception {

		constants.init(args[0]);

		// user, pass, deviceName
		new MergeLogs(args[0]);

		System.out.println("... done");

	}

}
