package app.guchagucharr.service;

import java.util.Vector;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
//import android.util.Log;

public class FileOutputThread extends Thread {

	private Handler handler;
	private Activity act;
	private final Runnable listener;
	public static final int PROC_TYPE_NONE = 0;
	public static final int PROC_TYPE_EXPORT_GPX = 1;
	private int iProcType = PROC_TYPE_NONE;
	private String strDir;
	private String strFile;
	private Boolean bRet = false;
	Vector<Location> vData = null;
	// SparseArray<LapData> lapData = null;
	
	public FileOutputThread(
			Activity _act,
			Vector<Location> vData_,
			//SparseArray<LapData> lapData_,
			Handler _handler,
			Runnable _listener,
			int _iProcType,
			String _strDir,
			String _strFile
	)
    {
        this.act = _act;
		this.vData = vData_;
		//this.lapData = lapData_;
		this.handler = _handler;
        this.listener = _listener;
        this.iProcType = _iProcType;
        this.strDir = _strDir;
        this.strFile = _strFile;
    }

	public Boolean getResult()
	{
		return bRet;
	}

    @Override
    public void run()
    {
        GPXGenerator processor = 
        	new GPXGenerator(vData,handler);
        switch( iProcType )
        {
        case PROC_TYPE_NONE:
        	break;
        case PROC_TYPE_EXPORT_GPX:
        	processor.createGPXFileFromLocations(act, strDir, strFile);
        	break;
        }
        
        // 終了を通知
        handler.post(listener);
    }
    public String getFileNm() {
		return strFile;
	}
    public int getProcType() {
		return iProcType;
	}
    
}

