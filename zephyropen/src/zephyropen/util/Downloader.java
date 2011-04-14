package zephyropen.util;

//package oculus;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

//import org.red5.logging.Red5LoggerFactory;
//import org.slf4j.Logger;

public class Downloader {

	// private static Logger log = Red5LoggerFactory.getLogger(Downloader.class,
	// "oculus");

	/**
	 * 
	 * Download a given URL to the local disk. Will delete existing file first,
	 * and create directory if required.
	 * 
	 * @param fileAddress
	 *            is the full http://url of the remote file
	 * @param localFileName
	 *            the file name to use on the host
	 * @param destinationDir
	 *            the folder name to put this down load into
	 * @return true if the file is down loaded, false on any error.
	 * 
	 */
	public static boolean FileDownload(final String fileAddress,
			final String localFileName, final String destinationDir) {

		InputStream is = null;
		OutputStream os = null;
		URLConnection URLConn = null;

		// create path to local file
		final String path = destinationDir + File.separator + localFileName;

		// System.out.println("url   : " + fileAddress);
		// System.out.println("local : " + path);

		// create target directory
		new File(destinationDir).mkdirs();

		// delete target first
		new File(path).delete();

		// test is really gone
		if (new File(path).exists()) {
			// System.out.println("can't delete existing file: " + path);
			return false;
		}

		try {

			int ByteRead, ByteWritten = 0;
			os = new BufferedOutputStream(new FileOutputStream(path));

			URLConn = new URL(fileAddress).openConnection();
			is = URLConn.getInputStream();
			byte[] buf = new byte[2048];

			// pull in the bytes
			while ((ByteRead = is.read(buf)) != -1) {
				os.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
				// System.out.println("b: " + ByteRead);
			}

			// System.out.println("saved to local file: " + path + " bytes: " +
			// ByteWritten);

		} catch (Exception e) {
			// System.out.println(e.getMessage());
			return false;
		} finally {
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				// System.out.println(e.getMessage());
				return false;
			}
		}

		// all good
		return true;
	}

	/**
	 * @param srcFolder
	 *            path to the folder to be zipped
	 * @param destZipFile
	 *            path to the final zip file
	 */
	static public boolean zipFolder(String srcFolder, String destZipFile) {
		if (new File(srcFolder).isDirectory()) {

			ZipOutputStream zip = null;
			FileOutputStream fileWriter = null;
			try {
				fileWriter = new FileOutputStream(destZipFile);
				zip = new ZipOutputStream(fileWriter);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}

			addFolderToZip("", srcFolder, zip); //$NON-NLS-1$
			try {
				zip.flush();
				zip.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param zipFile
	 *            the zip file that needs to be unzipped
	 * @param destFolder
	 *            the folder into which unzip the zip file and create the folder
	 *            structure
	 */
	public static boolean unzipFolder(String zipFile, String destFolder) {
		try {
			ZipFile zf = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> zipEnum = zf.entries();
			String dir = destFolder;

			while (zipEnum.hasMoreElements()) {
				ZipEntry item = (ZipEntry) zipEnum.nextElement();

				if (item.isDirectory()) {
					File newdir = new File(dir + File.separator + item.getName());
					newdir.mkdir();
				} else {
					String newfilePath = dir + File.separator + item.getName();
					File newFile = new File(newfilePath);
					if (!newFile.getParentFile().exists()) {
						newFile.getParentFile().mkdirs();
					}

					InputStream is = zf.getInputStream(item);
					FileOutputStream fos = new FileOutputStream(newfilePath);
					int ch;
					while ((ch = is.read()) != -1) {
						fos.write(ch);
					}
					is.close();
					fos.close();
				}
			}
			zf.close();
		} catch (Exception e) {
			return false;
		}

		// all good
		return true;
	}

	static private void addToZip(String path, String srcFile, ZipOutputStream zip) {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[1024];
			int len;
			try {
				FileInputStream in = new FileInputStream(srcFile);
				zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
		File folder = new File(srcFolder);
		String listOfFiles[] = folder.list();
		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				addToZip(path + File.separator + folder.getName(), srcFolder + File.separator
						+ listOfFiles[i], zip);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * test driver
	 * 
	 * unzip methods modified from:
	 * http://jgrasstechtips.blogspot.com/2007/12/zip-and-unzip-folders.html
	 * 
	 */
	public static void main(String[] args) {

		// web url of zip file 
		String webpath = "http://oculus.googlecode.com/files/update_oculus_128.zip";

		// temp file, will be deleted
		String local = "update.zp";

		// what directory to create with zip
		String dir = "michelle";

		// try to down load the file
		if(FileDownload(webpath, local, dir)){
			local = dir + File.separator + local;
			if (unzipFolder(local, dir)) 
				if (!new File(local).delete())
					System.out.println("can't delete downloaded file");
		} else {
			System.out.println("error downloading");
		}
		
		System.out.println("... done");
	}
}
