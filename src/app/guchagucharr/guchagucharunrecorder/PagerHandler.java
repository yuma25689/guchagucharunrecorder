package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import app.guchagucharr.interfaces.IPageViewController;

public class PagerHandler extends Handler {
	Context mActivity = null;
	IPageViewController mViewController = null;
	boolean bSizeGet = false;
	boolean bInitEnd =false;
	boolean bInitControlsEnd = false;
	
	public PagerHandler(Context act, IPageViewController vCtrl)
	{
		mActivity = act;
		mViewController = vCtrl;
	}
	
	void clearFlags()
	{
		bSizeGet = false;
		bInitEnd = false;
		bInitControlsEnd = false;
	}
	
    //���b�Z�[�W��M
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
        		bInitEnd = true;
        		if( bSizeGet && bInitControlsEnd == false )
        		{
        			bInitControlsEnd =true;
        			mViewController.initPager();
        		}
	    		break;
        	}
        	case MessageDef.MSG_INIT_SIZE_GET:
        		bSizeGet = true;
        		if( bInitEnd && bInitControlsEnd == false )
        		{
        			bInitControlsEnd =true;
        			mViewController.initPager();
        		}
        		break;
		}
	}
}
