package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import app.guchagucharr.interfaces.IPageViewController;

public class PagerHandler extends Handler {
	Context mActivity = null;
	IPageViewController mViewController = null;
	
	public PagerHandler(Context act, IPageViewController vCtrl)
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
        		// ディスプレイの作成
        		mViewController.initPager();
	    		break;
        	}
		}
	}
}
