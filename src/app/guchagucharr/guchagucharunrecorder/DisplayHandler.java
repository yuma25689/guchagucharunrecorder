package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import app.guchagucharr.interfaces.IPageViewController;

public class DisplayHandler extends Handler {
	Context mActivity = null;
	IPageViewController mViewController = null;
	
	public DisplayHandler(Context act, IPageViewController vCtrl)
	{
		mActivity = act;
		mViewController = vCtrl;
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
        		// �f�B�X�v���C�̍쐬
        		mViewController.initPager();
	    		break;
        	}
		}
	}
}
