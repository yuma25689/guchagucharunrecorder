package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.widget.Toast;
import app.guchagucharr.interfaces.IMainViewController;

public class MainHandler extends Handler {

	static final int INIT_ALL_REFRESH = 103;
	boolean bInitEnd = false;
	Context mActivity = null;
	IMainViewController mViewController = null;
	
	public MainHandler(Context act, IMainViewController vCtrl)
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
                //OkosamaMediaPlayerActivity.getResourceAccessor().initMotionSenser(mActivity);
                //OkosamaMediaPlayerActivity.getResourceAccessor().initSound();
           		if( -1 == mViewController.initControls() )
           		{
           			return;
           		}
           		// 初期化が終わったら、とりあえずリクエストを送る
        		mViewController.requestGPS();
	    		break;
        	}
        	case MessageDef.MSG_CAMERA_END:
        	{
        		String uri = null;
        		if( message.obj != null )
        		{
        			uri = message.obj.toString();
        		}
        		mViewController.endCamera( uri );
        		break;
        	}
		}
	}


}
