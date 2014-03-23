package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import app.guchagucharr.interfaces.IEditViewController;
//import android.util.Log;
//import android.widget.Toast;
import app.guchagucharr.interfaces.IMainViewController;

public class EditHandler extends Handler {

	static final int INIT_ALL_REFRESH = 103;
	boolean bInitEnd = false;
	Context mActivity = null;
	IEditViewController mViewController = null;
	
	public EditHandler(Context act, IEditViewController vCtrl)
	{
		mActivity = act;
		mViewController = vCtrl;
	}
	
    @Override
	public void handleMessage(Message message) {
    	if( mActivity == null )
    	{
    		return;
    	}
    	switch( message.what )
		{
        	case MessageDef.MSG_INIT_END:
        	{
           		if( -1 == mViewController.initControls() )
           		{
           			return;
           		}
	    		break;
        	}
		}
	}


}
