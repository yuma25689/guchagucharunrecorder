package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
//import android.widget.RelativeLayout;

/**
 * 画面情報取得用クラスのハンドル。
 * @author 25689
 *
 */
public final class DisplayInfo {
	
	// Singleton
	private static DisplayInfo instance = new DisplayInfo();
	private DisplayInfo() {}
	public static DisplayInfo getInstance() {
		return instance;
	}
	
	private DroidDisplayInfo _impl = new DroidDisplayInfo();
	
	/**
	 * 初期化 この関数は微妙にandroid固有になってしまったが、仕方ない。
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
	
	/**
	 * サイズの補正値を取得
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
	
}
