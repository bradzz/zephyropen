package zephyropen.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
	private static Long total = 0L;

	/**  */
	public MergeLogs() {

		if (!jdbcOpen()) {
			constants.error("no database connection");
			return;
		}

		File[] files = getFiles();
		for (int i = 0; i < files.length; i++) {

			constants.info(i + " : " + files[i].getName());

			total += counter;
			counter = 0L;
			 
			readXML(files[i]);

			// files[i].renameTo(new File(constants.get(ZephyrOpen.userLog) +
			// ZephyrOpen.fs + files[i].getName() + ".done"));
			// files[i].delete();
		}
		
		constants.info("total : " + total ); 

		jdbcClose();
	}

	// setup files
	public File[] getFiles() {

		// get all file names
		File[] all = (new File(constants.get(ZephyrOpen.userLog))).listFiles();

		System.out.println(constants.get(ZephyrOpen.userLog));
		System.out.println("found files = " + all.length);
		
		int size = 0;
		for (int i = 0; i < all.length; i++)
			if (all[i].getName().endsWith(".xml"))
				size++;

		System.out.println("found xml files = " + size);

		File[] xml = new File[size];
		int c = 0;
		for (int i = 0; i < all.length; i++)
			if (all[i].isFile()) 
				if (all[i].getName().endsWith(".xml")) 
					xml[c++] = all[i];
			
		return xml;
	}

	private boolean jdbcOpen() {

		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String url = "jdbc:mysql://localhost/" + constants.get(ZephyrOpen.user);
			conn = DriverManager.getConnection(url, "root", "");

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

	/*
	 * CREATE TABLE `backup`.`heart` ( `time` BIGINT NOT NULL , `heart` INT NOT
	 * NULL ) ENGINE = MYISAM ;
	 */

	private boolean insert(Command command) {

		if (FilterFactory.filter(command)) {

			try {

				Statement st = conn.createStatement();
				st.executeUpdate("INSERT INTO HEART VALUES ("
						+ command.get(ZephyrOpen.TIME_MS) + ", "
						+ command.get(PrototypeFactory.heart) + ")");

			} catch (Exception ex) {
				// constants.error("insert(): " + ex.getMessage());
				return false;
			}
		} else {
			// constants.error("filtered: " + command.toString());
			return false;
		}

		return true;
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
			while ((line = lnreader.readLine()) != null) {
				command = xmlParser.parse(line);
				if (command != null) {
					if (insert(command))
						counter++;
				}
			}

			constants.info("lines read : " + lnreader.getLineNumber());
			constants.info("sql insert : " + counter);
			constants.info("dropped    : " + (lnreader.getLineNumber() - counter));
			constants.info(file.getName() + " took seconds: " + (System.currentTimeMillis() - start) / 1000);

		} catch (Exception e) {
			constants.error("readXML() : " + e.getMessage(), this);
		} finally {
			try {
				freader.close();
				lnreader.close();
			} catch (IOException e) {
				constants.error("readXML() : " + e.getMessage(), this);
			}
		}
	}

	/** Find log file by naming convention given in args[] */
	public static void main(String[] args) throws Exception {

		constants.init(args[0]);

		// username
		new MergeLogs();

		System.out.println("... done");

	}
}
