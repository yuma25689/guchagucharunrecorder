package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.graphics.Rect;
// import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewParent;

/**
 * 画面情報取得用クラスの実装(android用)
 * Dip->Pixelへの変換に利用
 * 1ピクセル×metrics.scaledDensity=1dip
 * @author 25689
 *
 */
public final class DroidDisplayInfo {
	
	DisplayMetrics metrics;

	double orgHeightOfBk = 0;
	double orgWidthOfBk = 0;
	
	int titleBarHeightPixels;
	int statusBarHeightPixels;
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
	 * 初期化。画面情報の取得に必要な情報をもらい、内部の画面情報を更新する
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
			// サイズのベースとなる画像を取得
			// 割と強引だが、この画像をスクリーンと考えた位置に他のコンポーネントを配置する
			// この画像内での他のコンポーネントの位置は分かるが、プログラム内ではdensityを考慮しなければならない
			// densityを考慮した場合の、補正値を計算する
//	        backgroundImgBase = OkosamaMediaPlayerActivity.getResourceAccessor()
//	        		.getResourceDrawable(R.drawable.background_3);
		}
		updateDisplayMetrics(bForceRecreate);
	}
	
	/**
	 * Activityの画面のMetricsを内部に格納する
	 */
	public void updateDisplayMetrics(boolean b)
	{
		final Boolean bForceRecreate = b;
		clear();

		if( activity == null )
		{
			return;
		}

	    // ディスプレイ情報の取得
	    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

	    if( viewForMeasureBarHeight == null )
	    {
	    	return;
	    }
	    
	    // ステータスバーと、タイトルバーの高さを取得
	    // Viewが非表示の状態だと、取れない。
	    viewForMeasureBarHeight.post(new Runnable() {
		    @Override
			public void run() {
		    	// こうすることで、Viewが始まっていない場合でも、Viewが開始してから情報を取得できる？
		        Rect rect = new Rect();
		        // Viewからステータスバーの高さを取得
		        viewForMeasureBarHeight.getWindowVisibleDisplayFrame(rect);
		        statusBarHeightPixels = rect.top;
		        
		        // ステータスバーとタイトルバーの合計？をWindowから取得
		        // =>ステータスバーとタイトルバーは隠せているはずなので不要
//				int contentViewTopPx = 
//					activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//				// タイトルバーの高さを取得
//				titleBarHeightPixels = contentViewTopPx - statusBarHeightPixels;
//			    // クライアント領域の高さを取得
//			    clientHeightPixels = metrics.heightPixels - titleBarHeightPixels - statusBarHeightPixels;
		        // クライアント領域の高さを取得
			    clientHeightPixels = metrics.heightPixels;		        
		        // 実際のDisplayの高さと横幅と、背景画像の高さと横幅との比率を求める
				// ベース画像の、元（プログラムに入ってくる前）の高さと幅
				orgHeightOfBk 
					= ControlDefs.APP_BASE_HEIGHT;//(backgroundImgBase.getIntrinsicHeight()); /// metrics.density;
				orgWidthOfBk 
					= ControlDefs.APP_BASE_WIDTH;//(backgroundImgBase.getIntrinsicWidth());// / metrics.density;
				// プログラム内でのクライアント領域のサイズと、元の画像のサイズとの比率を求める
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
			
				// handlerに通知する
				Message msg = Message.obtain();
				msg.what = MessageDef.MSG_INIT_END;
				msg.obj = bForceRecreate;
				// msg.arg1 = DisplayInfo.MSG_INIT_END;
				handler.sendMessage( msg );
		    }
		});	    
	}

	/**
	 * densityを考慮しなかった場合の位置を、
	 * densityを考慮した場合の座標に直して返却する
	 * @param orgY density考慮前の位置
	 * @return density考慮後の位置
	 * 
	 */
	public int getCorrectionXConsiderDensity( int orgX )
	{		
		int ret = 0;
		ret = (int)( widthScaleCorrectDensity * orgX );
		return ret;
	}	
	/**
	 * densityを考慮しなかった場合の位置を、
	 * densityを考慮した場合の座標に直して返却する
	 * @param orgY density考慮前の位置
	 * @return density考慮後の位置
	 * 
	 */
	public int getCorrectionYConsiderDensity( int orgY )
	{
		int ret = (int)( heightScaleCorrectDensity * orgY );
		return ret;
	}
	
	/**
	 * 
	 * @return クライアント領域のサイズ
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
	 * 縦向きかどうか
	 * @return true:縦 false:横
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
