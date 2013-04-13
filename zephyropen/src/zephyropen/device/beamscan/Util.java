package zephyropen.device.beamscan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import zephyropen.api.ZephyrOpen;

public class Util {
	
	static ZephyrOpen constants = ZephyrOpen.getReference();
	
	/** */
	public static void archive(final File[] files) {
		
		constants.info("archive(" + files.length +")....");
		
		new File(constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "archive").mkdirs();
		
	//	checkFolders();
	
		File archive = new File(constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "archive");
		String number = String.valueOf(archive.listFiles().length);
		
		// create folder 
		new File(archive.getAbsoluteFile() + ZephyrOpen.fs + "archive_"+number).mkdirs();
			
		if(files != null){
			if(files.length > 0){		
				String[] names = new String[files.length];
				for(int i = 0 ; i < files.length ; i++){
					names[i] = files[i].getAbsolutePath();	
					names[i] = names[i].replaceFirst("capture", "archive\\\\archive_"+number);
					if( ! new File(files[i].getAbsolutePath()).renameTo(new File(names[i])))
						constants.error("rename fail: " + names[i]);
				}	
			}	
		}
			
		// move log files 
		File[] logs = new File(constants.get(ZephyrOpen.userLog)).listFiles();
		for(int j = 0 ; j < logs.length ; j++){
			String name = logs[j].getAbsolutePath();			
			name = name.replaceFirst("log", "archive\\\\archive_"+number);
			if( ! logs[j].renameTo(new File(name)))
				constants.error("rename fail: " + name);		
		}
	}
	
	/** */
	public static File[] getFrames(String path) {
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File directory, String fileName) {
	            return fileName.endsWith(".png");
	        }
	    };
	    
	    return new File(path).listFiles(filter);	
	}
	
	/** */
	public static File[] getLogs(String path) {
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File directory, String fileName) {
	            return fileName.endsWith(".log");
	        }
	    };
	    
	    return new File(path).listFiles(filter);	
	}
	
	/** 
	public static void checkFolders(){
		File archive = new File(constants.get(ZephyrOpen.userHome) + ZephyrOpen.fs + "archive");
		File[] listing = archive.listFiles();
		
		if(listing == null) return;
		if(listing.length==0) return;
		
		for(int i = 0 ; i < listing.length ; i++)
			if(listing[i].isDirectory())
				if(getFrames(listing[i].getAbsolutePath()).length == 0)
					if(getLogs(listing[i].getAbsolutePath()).length == 0)
						if( ! listing[i].delete()){
							constants.info("can't delete: " + listing[i].getAbsolutePath());
							deleteRecursive(listing[i]);
						}
	}*/
	
	/**
     * By default File#delete fails for non-empty directories, it works like "rm". 
     * We need something a little more brutual - this does the equivalent of "rm -r"
     * @param path Root File Path
     * @return true iff the file and all sub files/directories have been removed
     * @throws FileNotFoundException
     
    public static boolean deleteRecursive(File path) {
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }*/
}
