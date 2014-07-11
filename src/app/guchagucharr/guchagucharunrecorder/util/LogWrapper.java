package app.guchagucharr.guchagucharunrecorder.util;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;

public class LogWrapper {
	//String sLogDir = mLogDir;
	private final static String LOGDIR 
		= Environment.getExternalStorageDirectory().getPath()
		+ "/" + "GGRRLog";

	private final static String SDFILE = LOGDIR+"/"+ "log.txt";
	private static boolean enableSDLog = true;
	private static boolean enable = true;
	private static boolean enableV = true;
	private static boolean enableI = true;

	static private BufferedWriter prepareSDLog()
	{
		try {
			mkdir_p(LOGDIR);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		BufferedWriter bw = null;
		try {
			FileOutputStream file = new FileOutputStream(SDFILE, true);
			
			bw = new BufferedWriter(new OutputStreamWriter(
					file, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bw;
	}
	static private String createTimeText()
	{
		String ret = null;
		Calendar now = Calendar.getInstance();
		//StackTraceElement[] ste = (new Throwable()).getStackTrace();
//		ret = ste[1].getMethodName()
//			   + "("
//			 	+ ste[1].getFileName() + ":" + ste[1].getLineNumber()
//			   + ") " + text;
		ret = (now.get(Calendar.YEAR))+"/"+(now.get(Calendar.MONTH)+1)
				+"/"+now.get(Calendar.DATE)
				+" "+now.get(Calendar.HOUR)
				+":"+now.get(Calendar.MINUTE)
				+":"+now.get(Calendar.SECOND)
				;
		return ret;
	}
	static private void trySDLog(String tag, String msg)
	{
		if( enableSDLog )
		{
			BufferedWriter bw = prepareSDLog();
			if( null == bw )
			{
				return;
			}
			try {
				bw.append(tag+"\t"+createTimeText()+"\t"+msg+"\n");
				bw.close();
				bw = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	static public void e(String tag, String msg) {
		if (!enable) return;
		trySDLog(tag,msg);
		Log.e(tag,msg);
	}
	static public void w(String tag, String msg) {
		if (!enable) return;
		trySDLog(tag,msg);
		Log.w(tag,msg);
	}
	static public void v(String tag, String msg) {
		if( enableV == false )
		{
			return;
		}
		if (!enable) return;
		trySDLog(tag,msg);
		Log.v(tag,msg);
	}
	static public void i(String tag, String msg) {
		if( enableI == false )
		{
			return;
		}
		if (!enable) return;
		trySDLog(tag,msg);
		Log.i(tag,msg);
	}

	public static boolean mkdir_p(File dir) throws IOException {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("File.mkdirs() failed.");
			}
			return true;
		} else if (!dir.isDirectory()) {
			throw new IOException("Cannot create path. " + dir.toString() + " already exists and is not a directory.");
		} else {
			return false;
		}
	}

	public static boolean mkdir_p(String dir) throws IOException {
		return mkdir_p(new File(dir));
	}
}
