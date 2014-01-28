package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.widget.Toast;
import app.guchagucharr.interfaces.IViewController;

public class MainHandler extends Handler {

	static final int INIT_ALL_REFRESH = 103;
	static final int MEDIA_SCAN_TARGET_CREATED = 107;
	boolean bInitEnd = false;
	Context mActivity = null;
	IViewController mViewController = null;
	
	public MainHandler(Context act, IViewController vCtrl)
	{
		mActivity = act;
		mViewController = vCtrl;
	}
	
    //メッセージ受信
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
        		
        		// TODO:ディスプレイの作成
        		mViewController.initControls();
	    		break;
        	}
		}
	}


}
