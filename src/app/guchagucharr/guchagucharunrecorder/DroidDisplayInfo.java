package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.graphics.Rect;
// import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;
import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewParent;

/**
 * ��ʏ��擾�p�N���X�̎���(android�p)
 * Dip->Pixel�ւ̕ϊ��ɗ��p
 * 1�s�N�Z���~metrics.scaledDensity=1dip
 * @author 25689
 *
 */
public final class DroidDisplayInfo {
	
	DisplayMetrics metrics;

	double orgHeightOfBk = 0;
	double orgWidthOfBk = 0;
	
	int titleBarHeightPixels;
	int statusBarHeightPixels;
	public int getStatusBarHeight()
	{
		return statusBarHeightPixels;
	}
	int clientHeightPixels;
	
	Activity activity;
	Handler handler;
	View viewForMeasureBarHeight;
	// private static Drawable backgroundImgBase = null;
	double widthScaleCorrectDensity = 0;
	double heightScaleCorrectDensity = 0;
	
	public static final int CURRENT_BASE_DEVICE_HEIGHT = 859;
	
	public void clear()
	{
		titleBarHeightPixels = 
		statusBarHeightPixels =
		clientHeightPixels = 0;
		
		widthScaleCorrectDensity =
		heightScaleCorrectDensity = 0;

		metrics = new DisplayMetrics();
	}
	
	
	/**
	 * ����B��ʏ��̎擾�ɕK�v�ȏ������炢�A�����̉�ʏ����X�V����
	 * @param _activity
	 * @param _viewForMeasureBarHeight
	 */
	public void init( Activity _activity,
			View _viewForMeasureBarHeight,
			Handler handler,
			boolean bForceRecreate )
	{
		this.activity = _activity;
		this.viewForMeasureBarHeight = _viewForMeasureBarHeight;
		this.handler = handler;
		if( activity != null )
		{
			// �T�C�Y�̃x�[�X�ƂȂ�摜���擾
			// ���Ƌ����A���̉摜���X�N���[���ƍl�����ʒu�ɑ��̃R���|�[�l���g��z�u����
			// ���̉摜���ł̑��̃R���|�[�l���g�̈ʒu�͕����邪�A�v���O�������ł�density���l�����Ȃ���΂Ȃ�Ȃ�
			// density���l�������ꍇ�́A�␳�l���v�Z����
//	        backgroundImgBase = OkosamaMediaPlayerActivity.getResourceAccessor()
//	        		.getResourceDrawable(R.drawable.background_3);
		}
		updateDisplayMetrics(bForceRecreate);
	}
	
	/**
	 * Activity�̉�ʂ�Metrics������Ɋi�[����
	 */
	public void updateDisplayMetrics(boolean b)
	{
		final Boolean bForceRecreate = b;
		clear();

		if( activity == null )
		{
			return;
		}

	    // �f�B�X�v���C���̎擾
	    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

	    if( viewForMeasureBarHeight == null )
	    {
	    	return;
	    }
	    
	    viewForMeasureBarHeight.post(new Runnable() {
		    @Override
			public void run() {
		        Rect rect = new Rect();
		        viewForMeasureBarHeight.getWindowVisibleDisplayFrame(rect);
		        statusBarHeightPixels = rect.top;
		        
//				int contentViewTopPx = 
//					activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//				titleBarHeightPixels = contentViewTopPx - statusBarHeightPixels;
//			    clientHeightPixels = metrics.heightPixels - titleBarHeightPixels - statusBarHeightPixels;
//			    clientHeightPixels = metrics.heightPixels;		        
			    clientHeightPixels = metrics.heightPixels - statusBarHeightPixels;
				orgHeightOfBk 
					= ControlDefs.APP_BASE_HEIGHT;//(backgroundImgBase.getIntrinsicHeight()); /// metrics.density;
				orgWidthOfBk 
					= ControlDefs.APP_BASE_WIDTH;//(backgroundImgBase.getIntrinsicWidth());// / metrics.density;
				LogWrapper.w("dispInfo", " width=" 
					+ metrics.widthPixels + "height=" + metrics.heightPixels);
				if( isPortrait() )
				{
			        heightScaleCorrectDensity
			        	=  clientHeightPixels 
			        		/ orgHeightOfBk;
			        widthScaleCorrectDensity 
			        =  metrics.widthPixels 
			        		/ orgWidthOfBk;
				}
				else
				{
			        heightScaleCorrectDensity
		        	= metrics.widthPixels 
		        		/ orgHeightOfBk;
			        widthScaleCorrectDensity 
			        = clientHeightPixels
			        		/ orgWidthOfBk;
				}
			
				// handler
				Message msg = Message.obtain();
				msg.what = MessageDef.MSG_INIT_END;
				msg.obj = bForceRecreate;
				// msg.arg1 = DisplayInfo.MSG_INIT_END;
				handler.sendMessage( msg );
		    }
		});	    
	}

	/**
	 * density���l�����Ȃ������ꍇ�̈ʒu���A
	 * density���l�������ꍇ�̍��W�ɒ����ĕԋp����
	 * @param orgY density�l���O�̈ʒu
	 * @return density�l����̈ʒu
	 * 
	 */
	public int getCorrectionXConsiderDensity( int orgX )
	{		
		int ret = 0;
		ret = (int)( widthScaleCorrectDensity * orgX );
		return ret;
	}	
	public int getXNotConsiderDensity( int orgX )
	{		
		int ret = 0;
		ret = (int)( orgX / widthScaleCorrectDensity );
		return ret;
	}	
	/**
	 * density���l�����Ȃ������ꍇ�̈ʒu���A
	 * density���l�������ꍇ�̍��W�ɒ����ĕԋp����
	 * @param orgY density�l���O�̈ʒu
	 * @return density�l����̈ʒu
	 * 
	 */
	public int getCorrectionYConsiderDensity( int orgY )
	{
		int ret = (int)( heightScaleCorrectDensity * orgY );
		return ret;
	}
	public int getYNotConsiderDensity( int orgY )
	{
		int ret = (int)( orgY / heightScaleCorrectDensity );
		return ret;
	}
	
	/**
	 * 
	 * @return �N���C�A���g�̈�̃T�C�Y
	 */
//	public Rect getClientRect()
//	{
//		Rect rect = new Rect();
//		rect.set( 0, 0, metrics.widthPixels, clientHeightPixels );
//		return rect;
//	}
	
	/**
	 * @param activity the activity to set
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	/**
	 * @param viewForMeasureBarHeight the viewForMeasureBarHeight to set
	 */
	public void setViewForMeasureBarHeight(View viewForMeasureBarHeight) {
		this.viewForMeasureBarHeight = viewForMeasureBarHeight;
	}
	
	/**
	 * �c��ǂ���
	 * @return true:�c false:��
	 */
	public boolean isPortrait()
	{
		if( metrics.widthPixels < metrics.heightPixels )
		{
			return true;
		}
		return false;
	}
	
}
