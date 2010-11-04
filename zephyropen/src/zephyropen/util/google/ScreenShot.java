package zephyropen.util.google;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import zephyropen.api.ZephyrOpen;

public class ScreenShot extends Thread {

	/** framework configuration */
	protected static ZephyrOpen constants = ZephyrOpen.getReference();
	
	private String filename = null;
	private URL url = null;

	/** send current icon to log file */
	public ScreenShot(GoogleChart googleLabel){ 

		url = googleLabel.getURL(GoogleChart.DEFAULT_X_SIZE, GoogleChart.DEFAULT_Y_SIZE);

		if (url == null) {
			constants.error("screenshot failed, null URL: " + googleLabel.getName());
			return;
		}
		
		String path = constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "screenshots" 
			+ ZephyrOpen.fs + constants.get(ZephyrOpen.deviceName);
		
		System.out.println( "user " + constants.get(ZephyrOpen.userHome) );
		

		// create log dir if not there
		if((new File(path)).mkdirs())
			System.err.println("created: " + path);

		// create file name that will be unique 
		filename = path + ZephyrOpen.fs + googleLabel.getTitle() + "_" + System.currentTimeMillis() + ".png";

		// do it
		this.start();
	}

	@Override
	public void run() {

		constants.info("snip to: " + filename, this);

		try {

			URLConnection uc = url.openConnection();
			String contentType = uc.getContentType();
			int contentLength = uc.getContentLength();

			if (contentType.startsWith("text/") || contentLength == -1) {
				constants.error("report is comming back as text?", this);
				return;
			}

			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();

			if (offset != contentLength) {
				constants.error("Only read " + offset + " bytes; Expected "
						+ contentLength + " bytes", this);
			}

			FileOutputStream out = new FileOutputStream(filename);
			out.write(data);
			out.flush();
			out.close();

		} catch (IOException e) {
			constants.error(e.getMessage(), this);
		}
	}
}