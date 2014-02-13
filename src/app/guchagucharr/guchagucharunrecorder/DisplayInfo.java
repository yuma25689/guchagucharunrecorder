package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
//import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

/**
 * ��ʏ��擾�p�N���X�̃n���h���B
 * @author 25689
 *
 */
public final class DisplayInfo {
	
	/**
	 * 
	 */
	// Singleton
	private static DisplayInfo instance = new DisplayInfo();
	private DisplayInfo() {}
	public static DisplayInfo getInstance() {
		return instance;
	}
	
	private DroidDisplayInfo _impl = new DroidDisplayInfo();
	
	/**
	 * ���� ���̊֐��͔�����android�ŗL�ɂȂ��Ă��܂������A�d��Ȃ��B
	 * @param activity
	 * @param viewForMeasureBarHeight
	 */
	public void init(Activity activity,
			View viewForMeasureBarHeight,
			Handler handler,
			boolean bTabForceReset)
	{
		_impl.init(activity,viewForMeasureBarHeight,handler,bTabForceReset);
	}
	public int getStatusBarHeight()
	{
		return _impl.getStatusBarHeight();
	}
	
	/**
	 * �T�C�Y�̕␳�l���擾
	 * @return
	 */
	public int getCorrectionXConsiderDensity( int orgX )
	{
		return _impl.getCorrectionXConsiderDensity( orgX );
	}
	public int getCorrectionYConsiderDensity( int orgY )
	{
		return _impl.getCorrectionYConsiderDensity( orgY );
	}
	public double getBkImageWidth()
	{
		return _impl.orgWidthOfBk;
	}
	public double getBkImageHeight()
	{
		return _impl.orgHeightOfBk;
	}
	public boolean isPortrait()
	{
		return _impl.isPortrait();
	}
	///////////////////////////// ���[�e�B���e�B�֐�
	/**
	* �x�[�X�摜��ł̐�΍��W���w�肵���ʒu��\��LayoutParam���쐬����
	* ���A������FILL_PARENT
	* @param left
	* @param top
	* @return LayoutParam
	*/
	public RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
	int left, int top )
	{
		// �w�肳�ꂽ���ʒu�ɑ΂��āA�f�B�X�v���C�T�C�Y���l�������������s��
		int xCorrect = getCorrectionXConsiderDensity(left);
		int yCorrect = getCorrectionYConsiderDensity(top);
		
		// ���ƍ����̎w�肪�Ȃ��̂ŁA�e�𖄂߂�悤�ɐݒ肷��
		RelativeLayout.LayoutParams lp = 
		new RelativeLayout.LayoutParams(
		LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A����̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == isPortrait() )
		{
			lp.topMargin = yCorrect;
			lp.leftMargin = xCorrect;
		}
		else
		{
			lp.leftMargin = yCorrect;
			lp.topMargin = xCorrect;
		}
		// ���̃A�v���P�[�V�����ł́Abottom��right��margin�̓[�������E�E�E�B
		lp.bottomMargin = 0;
		lp.rightMargin = 0;
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		return lp;
	}
	
	
	/**
	* �x�[�X�摜��ł̐�΍��W���w�肵���ʒu��\��LayoutParam���쐬����
	* @param left
	* @param top
	* @param width
	* @param height
	* @return LayoutParam
	*/
	public RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
	int left, int top, int width, int height )
	{
		return createLayoutParamForAbsolutePosOnBk(left, top, width, height, true );
	}
	
	public RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
	int left, int top, int width, int height, boolean bConvertPortraitAndHorz )
	{
		int widthCorrect = 0;
		if( width == LayoutParams.MATCH_PARENT
		|| width == LayoutParams.WRAP_CONTENT )
		{
			widthCorrect = width;
		}
		else
		{
			widthCorrect = getCorrectionXConsiderDensity(width);
		}
		int heightCorrect = 0;
		if( height == LayoutParams.MATCH_PARENT
		|| height == LayoutParams.WRAP_CONTENT )
		{
			heightCorrect = height;
		}
		else
		{
			heightCorrect = getCorrectionYConsiderDensity(height);
		}
		int xCorrect = 0;
		xCorrect = getCorrectionXConsiderDensity(left);
		int yCorrect = 0;
		int topRule = RelativeLayout.ALIGN_PARENT_TOP;
		yCorrect = getCorrectionYConsiderDensity(top);
		
		if( yCorrect < 0 )
		{
			yCorrect = -1 * getCorrectionYConsiderDensity(top);
			topRule = RelativeLayout.ALIGN_PARENT_BOTTOM;
		}
		
		RelativeLayout.LayoutParams lp = null;
		
		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A����̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == isPortrait() || bConvertPortraitAndHorz == false )
		{
			lp = new RelativeLayout.LayoutParams(
			widthCorrect, heightCorrect);
			lp.topMargin = yCorrect;
			lp.leftMargin = xCorrect;
			// ���̃A�v���P�[�V�����ł́Abottom��right��margin�̓[�������E�E�E�B
			lp.bottomMargin = 0;
			lp.rightMargin = 0;
		}
		else
		{
			lp = new RelativeLayout.LayoutParams(
			heightCorrect, widthCorrect);
			lp.topMargin = xCorrect;
			lp.leftMargin = yCorrect;
			// ���̃A�v���P�[�V�����ł́Abottom��right��margin�̓[�������E�E�E�B
			lp.bottomMargin = 0;
			lp.rightMargin = 0;
		}
		
		lp.addRule(topRule);//RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		return lp;
	}
	public RelativeLayout.LayoutParams createLayoutParamForNoPosOnBk(
	int width, int height, boolean bConvertPortraitAndHorz )
	{
		int widthCorrect = 0;
		if( width == LayoutParams.MATCH_PARENT
		|| width == LayoutParams.WRAP_CONTENT )
		{
			widthCorrect = width;
		}
		else
		{
			widthCorrect = getCorrectionXConsiderDensity(width);
			Log.i("width convert", width +"=>" + widthCorrect); 
		}
		int heightCorrect = 0;
		if( height == LayoutParams.MATCH_PARENT
		|| height == LayoutParams.WRAP_CONTENT )
		{
			heightCorrect = height;
		}
		else
		{
			heightCorrect = getCorrectionYConsiderDensity(height);
			Log.i("height convert", height +"=>" + heightCorrect); 
		}
		
		RelativeLayout.LayoutParams lp = null;
		
		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A����̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == isPortrait() || bConvertPortraitAndHorz == false )
		{
			lp = new RelativeLayout.LayoutParams(
			widthCorrect, heightCorrect);
		}
		else
		{
			lp = new RelativeLayout.LayoutParams(
			heightCorrect, widthCorrect);
		}
		// TODO: �������[��
		//lp.addRule(verb);//RelativeLayout.ALIGN_PARENT_TOP);
		
		return lp;
	}
	public int getXNotConsiderDensity( int orgX )
	{
		return _impl.getXNotConsiderDensity(orgX);
	}
	public int getYNotConsiderDensity( int orgY )
	{
		return _impl.getYNotConsiderDensity(orgY);
	}

}
