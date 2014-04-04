package app.guchagucharr.guchagucharunrecorder.util;

import java.io.File;
import java.util.ArrayList;

public class FileUtil {
	public static ArrayList<String> searchFiles(String dir_path, String ext)//, boolean search_subdir)
	{
	  final File dir = new File(dir_path);
	  if( dir.exists() == false )
	  {
		  return null;
	  }
	  
	  ArrayList<String> find_files = new ArrayList<String>();
	  final File[] files = dir.listFiles();
	  if(null != files){
	    for(int i = 0; i < files.length; ++i) {
//	      if(!files[i].isFile()){
//	        if(search_subdir){
//	          ArrayList<String> sub_files = searchFiles(files[i].getPath(), expr, search_subdir);
//	          find_files.addAll(sub_files);
//	        }
//	        continue;
//	      }
	
		  final String filename = files[i].getName();
		  if((null == ext) || filename.endsWith(ext)){
		    find_files.add(dir.getPath() + "/" + filename);
	      }
	    }
	  }
	  return find_files;
	}
}
