package app.guchagucharr.service;

import android.app.Activity;
import android.os.Handler;
//import android.util.Log;

public class FileOutputThread extends Thread {

	private Handler handler;
	private Activity act;
	private final Runnable listener;
	public static final int PROC_TYPE_NONE = 0;
	public static final int PROC_TYPE_EXPORT_GPX = 1;
	private int iProcType = PROC_TYPE_NONE;
	private String strPath;
	private Boolean bRet = false;
	RunningLogStocker stocker = null;
	// SparseArray<LapData> lapData = null;
	
	public FileOutputThread(
			Activity _act,
			//Vector<Location> vData_,
			RunningLogStocker stocker,
			//SparseArray<LapData> lapData_,
			Handler _handler,
			Runnable _listener,
			int _iProcType,
			String _strPath
	)
    {
        this.act = _act;
		this.stocker = stocker;
		//this.lapData = lapData_;
		this.handler = _handler;
        this.listener = _listener;
        this.iProcType = _iProcType;
        this.strPath = _strPath;
    }

	public Boolean getResult()
	{
		return bRet;
	}

    @Override
    public void run()
    {
    	int iRet = 0;
        GPXGenerator generator = 
        	new GPXGenerator(stocker.getLocationData(),handler);
        switch( iProcType )
        {
        case PROC_TYPE_NONE:
        	break;
        case PROC_TYPE_EXPORT_GPX:
        	iRet = generator.createGPXFileFromLocations(act, strPath);
        	if( iRet == GPXGenerator.RETURN_OK )
        	{
        		bRet = true;
        	}
        	break;
        }
        
        // 終了を通知
        handler.post(listener);
    }
    public int getProcType() {
		return iProcType;
	}
    
}

