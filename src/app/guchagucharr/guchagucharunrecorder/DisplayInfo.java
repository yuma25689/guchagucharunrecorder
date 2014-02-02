package app.guchagucharr.guchagucharunrecorder;

import java.io.Serializable;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
//import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

/**
 * 画面情報取得用クラスのハンドル。
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
	///////////////////////////// ユーティリティ関数
	/**
	* ベース画像上での絶対座標を指定した位置を表すLayoutParamを作成する
	* 幅、高さはFILL_PARENT
	* @param left
	* @param top
	* @return LayoutParam
	*/
	public RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
	int left, int top )
	{
		// 指定された左位置に対して、ディスプレイサイズを考慮した調整を行う
		int xCorrect = getCorrectionXConsiderDensity(left);
		int yCorrect = getCorrectionYConsiderDensity(top);
		
		// 幅と高さの指定がないので、親を埋めるように設定する
		RelativeLayout.LayoutParams lp = 
		new RelativeLayout.LayoutParams(
		LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// ここで、縦横の変換をかます
		// ソースコードに書いてある座標、大きさは縦用のものがだが、横向きの場合、横用に変換して座標を返す		
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
		// このアプリケーションでは、bottomとrightのmarginはゼロだが・・・。
		lp.bottomMargin = 0;
		lp.rightMargin = 0;
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		return lp;
	}
	
	
	/**
	* ベース画像上での絶対座標を指定した位置を表すLayoutParamを作成する
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
		if( width == RelativeLayout.LayoutParams.MATCH_PARENT
		|| width == RelativeLayout.LayoutParams.WRAP_CONTENT )
		{
			widthCorrect = width;
		}
		else
		{
			widthCorrect = getCorrectionXConsiderDensity(width);
		}
		int heightCorrect = 0;
		if( height == RelativeLayout.LayoutParams.MATCH_PARENT
		|| height == RelativeLayout.LayoutParams.WRAP_CONTENT )
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
		
		// ここで、縦横の変換をかます
		// ソースコードに書いてある座標、大きさは縦用のものがだが、横向きの場合、横用に変換して座標を返す		
		if( true == isPortrait() || bConvertPortraitAndHorz == false )
		{
			lp = new RelativeLayout.LayoutParams(
			widthCorrect, heightCorrect);
			lp.topMargin = yCorrect;
			lp.leftMargin = xCorrect;
			// このアプリケーションでは、bottomとrightのmarginはゼロだが・・・。
			lp.bottomMargin = 0;
			lp.rightMargin = 0;
		}
		else
		{
			lp = new RelativeLayout.LayoutParams(
			heightCorrect, widthCorrect);
			lp.topMargin = xCorrect;
			lp.leftMargin = yCorrect;
			// このアプリケーションでは、bottomとrightのmarginはゼロだが・・・。
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
		if( width == RelativeLayout.LayoutParams.MATCH_PARENT
		|| width == RelativeLayout.LayoutParams.WRAP_CONTENT )
		{
			widthCorrect = width;
		}
		else
		{
			widthCorrect = getCorrectionXConsiderDensity(width);
			Log.i("width convert", width +"=>" + widthCorrect); 
		}
		int heightCorrect = 0;
		if( height == RelativeLayout.LayoutParams.MATCH_PARENT
		|| height == RelativeLayout.LayoutParams.WRAP_CONTENT )
		{
			heightCorrect = height;
		}
		else
		{
			heightCorrect = getCorrectionYConsiderDensity(height);
			Log.i("height convert", height +"=>" + heightCorrect); 
		}
		
		RelativeLayout.LayoutParams lp = null;
		
		// ここで、縦横の変換をかます
		// ソースコードに書いてある座標、大きさは縦用のものがだが、横向きの場合、横用に変換して座標を返す		
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
		// TODO: 複数ルール
		//lp.addRule(verb);//RelativeLayout.ALIGN_PARENT_TOP);
		
		return lp;
	}

}
