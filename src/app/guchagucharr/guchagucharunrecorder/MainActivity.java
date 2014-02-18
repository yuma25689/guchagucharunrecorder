package app.guchagucharr.guchagucharunrecorder;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
//import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import app.guchagucharr.interfaces.IMainViewController;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunLogger;
import app.guchagucharr.service.RunLoggerService;
import app.guchagucharr.service.RunningLogStocker;
import app.guchagucharr.service.RunLoggerService.eMode;

/**
 * メインのアクティビティ 開始/終了、履歴、GPS状態表示、ランニング状態表示
 * @author 25689
 */
public class MainActivity extends Activity 
implements 
	// LocationListener,
	IMainViewController,
	OnClickListener,
	OnTouchListener,
	ServiceConnection 
{
	public static String TIMER_NOTIFY = "TimeNotif";
	public static String CURRENT_DURATION = "CurDur";
	public static String LOCATION_DATA = "LocData";
	public static String LOCATION_CHANGE_NOTIFY = "LocChgNotif";
	
	// サービスのトークン
    private RunLogger.ServiceToken mToken = null;

    
	public static DisplayInfo dispInfo = DisplayInfo.getInstance();	
	private RelativeLayout componentContainer;
	private MainHandler handler;
	
	// NOTICE:タイマー処理の一部はサービスに移す案もある
//	private static UpdateTimeDisplayTask timerTask = null;
//	class UpdateTimeDisplayTask extends TimerTask
//	{
//	     @Override
//	     public void run() {
//	         // mHandler through UI Thread to queueing
//	    	 handler.post( new Runnable() {
//	             @Override
//				public void run() {	 
//	                 // update now Time
//	         		try {
//						if( RunLoggerService.getLogStocker() != null 
//								&& RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
//						{
//							long lapTime = new Date().getTime() 
//									- RunLoggerService.getLogStocker().getCurrentLapData().getStartTime();
//							txtTime.setText( LapData.createTimeFormatText( lapTime ) );
//							// NOTICE: 微妙なところだが、ここでタイマーごとにリクエストする
//							// requestGPS();
//							//RunLogger.sService.requestGPS();
//						}
//					} catch (RemoteException e) {
//						e.printStackTrace();
//						Log.e("UpdateTimeDisplayTask",e.getMessage());						
//					}	            	 
//	             }
//	         });
//	     }
//	 }	
	
	// contorls
	// center button
	ImageButton btnCenter = null;
	Region regionCenterBtn = null;
	// GPS button
	ImageButton btnGPS = null;
	// GPS indicator
	ImageView imgGPS = null;
	// history button
	ImageButton btnHistory = null;
	ImageButton btnLap = null;
	// init invisible
	// time label
	static TextView txtTime = null;
	// distance
	static TextView txtDistance = null;
	// speed
	static TextView txtSpeed = null;
	// speed2
	static TextView txtSpeed2 = null;
	// time label
	static TextView txtLap = null;
	// cancel
	ImageButton btnCancel = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// test用
//		Locale locale = new Locale("jp");
//		Locale.setDefault(locale);
//		Configuration config = new Configuration();
//		config.locale = Locale.getDefault();
//		getResources().updateConfiguration(config, null);
		
		setContentView(R.layout.activity_main);
		
        // get the layout
        componentContainer = (RelativeLayout)findViewById(R.id.main_content);

        // -> サービスへ移動のため、ここでは行わない
		//requestGPS();

		// create handler
        handler = new MainHandler( this, this );
        // create resource accessor
        ResourceAccessor.CreateInstance(this);
        // res = ResourceAccessor.getInstance();
	}

	@Override
	protected void onStart() {
	    super.onStart();
        //mToken = RunLogger.bindToService(this, this);
	    
//        if( 0 == RunLogger.getServiceConnectionCount() 
//        || RunLogger.sService == null )
//        {
//        }
	}	
	@Override
    protected void onResume() {
		
        // サービスからの通知を受けるレシーバの作成、登録
        receiver = new ServiceNotifyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(LOCATION_CHANGE_NOTIFY);
        intentFilter.addAction(TIMER_NOTIFY);
        registerReceiver(receiver,intentFilter);

        // 呼ばれる回数が多すぎるかもしれない
	    // Bind( Or Create and Bind) to Service
        mToken = RunLogger.bindToService(this, this);

    	// update display size etc.
		// when end update, send message to handler
		// now, initialize there.
		regionCenterBtn = null;
        dispInfo.init(this, componentContainer, handler,false);
         
        super.onResume();
    }
	
	//@Override
	public void requestGPS()
	{
		// 2014/02/17 move to service
		try {
			RunLogger.sService.requestGPS();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("requestGPS",e.getMessage());
		}
	}
	
	@Override
	protected void onPause()
	{		
//        if (mLocationManager != null) {
//            mLocationManager.removeUpdates(this);
//        }
		if(mToken != null)
		{
			try {
				if( RunLogger.sService.getMode() == RunLoggerService.eMode.MODE_NORMAL.ordinal() )
				{
					RunLogger.sService.stopLog();
					// ログ取得中でない場合は、完全に停止させる
					RunLogger.sService.clearGPS();
					RunLogger.sService.clearLocationManager();					
					// サービスの登録解除
				    RunLogger.unbindFromService(mToken);
				    // サービスの停止
				    RunLogger.stopService(this);
				}
				else
				{
					// ログ取得中
					// サービスとActivityの切り離しのみ
				    RunLogger.unbindFromService(mToken);					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e("onPause","RemoteException");
			}
		    mToken = null;
		}
		
        if( null != receiver )
        {
        	this.unregisterReceiver(receiver);
        	receiver = null;
        }
		
        // clearGPS();
		//android.R.drawable.ic_menu_mylocation
		regionCenterBtn = null;

        super.onPause();	
	}
	@Override
	protected void onStop()
	{
		// TODO: 残念ながら、ここで、いちいちLocationを保存する仕組みにしないと、
		// ランニング中にアプリケーションが終了するだけで、ランニングデータが消えてしまう。
		// GPXファイルを常に書き続けるような作りと、
		// GPXファイルからLocationデータを復帰させるような仕組みがあればいいのではないかと思われる。
		// サービスとの接続を切断
		if(mToken != null)
		{
			try {
				if( RunLogger.sService.getMode() == RunLoggerService.eMode.MODE_NORMAL.ordinal() )
				{
					RunLogger.sService.stopLog();
					// ログ取得中でない場合は、完全に停止させる
					RunLogger.sService.clearGPS();
					RunLogger.sService.clearLocationManager();					
					// サービスの登録解除
				    RunLogger.unbindFromService(mToken);
				    // サービスの停止
				    RunLogger.stopService(this);
				}
				else
				{
					// ログ取得中
					// サービスとActivityの切り離しのみ
				    RunLogger.unbindFromService(mToken);					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e("onPause","RemoteException");
			}
		    mToken = null;
		}
				
        // clearGPS();
        super.onStop();
	}
	@Override
	protected void onDestroy()
	{
		// NOTICE: 本当に不要？
//		if( RunLogger.sService.getMode() == RunLoggerService.eMode.MODE_NORMAL.ordinal() )
//		{
//			RunLogger.sService.clearGPS();
//			// サービスの登録解除
//		    RunLogger.unbindFromService(mToken);
//		    mToken = null;
//		}
		super.onDestroy();		
	}
	
	//@Override
	public void onLocationChanged(Location location) {
		//bGPSCanUse = true;
		Log.v("onLocationChanged - MainActivity","come");
		btnCenter.setEnabled(true);
		try {
			if( false == RunLoggerService.isEmptyLogStocker() 
					&& RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
			{
				txtDistance.setText( LapData.createDistanceFormatText( 
						RunLoggerService.getLogStocker().getCurrentLapData().getDistance() ) );
				txtSpeed.setText( LapData.createSpeedFormatText( location.getSpeed() ) );
				//runLogStocker.getCurrentLapData().getSpeed() ) );
				txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( location.getSpeed() ) );
				//runLogStocker.getCurrentLapData().getSpeed() ) );
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("onLocationChanged - MainActivity", e.getMessage());
		}
		Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));
		
		// TODO: 精度の表示
		if( location.getAccuracy() < 5 )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_bad);
		}
		else if( 10 <= location.getAccuracy() )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_good);
		}
		else if( 5 <= location.getAccuracy() )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_soso);
		}
	}

	// TODO: サービス側から呼ぶ必要があるかどうか確認
	//@Override
//	public void onProviderDisabled(String provider) {
//		//bGPSCanUse = false;
//		Log.v("gps","onProviderDisabled");
//		//imgGPS.setBackgroundResource(R.drawable.gps_bad);
//		//btnCenter.setEnabled(false);
//		// TODO: GPSが切れたとき。ここに来るかどうか要確認＆来たら、メッセージ表示、ワークアウト終了も考慮
//		String providers = Settings.Secure.getString(
//				getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//		if(providers.indexOf("gps", 0) < 0) 
//		{
//			imgGPS.setBackgroundResource(R.drawable.gps_not_arrow);
//		}		
//	}

//	@Override
//	public void onProviderEnabled(String provider) {
//		//bGPSCanUse = true;
//		Log.v("gps","onProviderEnabled");
//		//imgGPS.setBackgroundResource(R.drawable.gps_good);
//		//btnCenter.setEnabled(true);
//	}

	// init controls
	static final int CENTER_BUTTON_ID = 1000;
	static final int GPS_BUTTON_ID = 1001;
	static final int GPS_INDICATOR_ID = 1002;
	static final int DISTANCE_TEXT_ID = 1010;
	static final int SPEED_TEXT_ID = 1011;
	static final int LAP_BUTTON_ID = 1012;
	
	static final int LEFT_TOP_CTRL_1_LEFT_MARGIN = 20;
	static final int LEFT_TOP_CTRL_1_TOP_MARGIN = 40;
	static final int RIGHT_TOP_CTRL_1_RIGHT_MARGIN = 20;
	static final int RIGHT_TOP_CTRL_1_TOP_MARGIN = 40;
	static final int LEFT_CENTER_CTRL_MARGIN = 20;
	static final int RIGHT_CENTER_CTRL_MARGIN = 20;
	static final int CENTER_ABOVE_CTRL_MARGIN = -15;
	static final int CENTER_BELOW_CTRL_MARGIN = -15;
	//static final int CENTER_BOTTOM_CTRL_MARGIN = 20;

	//static final int TIME_TEXTVIEW_WIDTH = 150;
	//static final int TIME_TEXTVIEW_HEIGHT = 60;
	static final int TIME_TEXTVIEW_FONT_SIZE = 30;

	//static final int DISTANCE_TEXTVIEW_WIDTH = 150;
	//static final int DISTANCE_TEXTVIEW_HEIGHT = 60;
	static final int DISTANCE_TEXTVIEW_FONT_SIZE = 25;

	//static final int SPEED_TEXTVIEW_WIDTH = 150;
	//static final int SPEED_TEXTVIEW_HEIGHT = 60;
	static final int SPEED_TEXTVIEW_FONT_SIZE = 25;
	
	static final int LAP_TEXTVIEW_FONT_SIZE = 25;
	
	private void addViewToCompContainer( View v )
	{
		if( v.getParent() != null )
		{	
			((ViewGroup)v.getParent()).removeView(v);
		}
		componentContainer.addView(v);		
	}
	
	@Override
	public int initControls()
	{
		// TODO:サービスにつながれていないときに、ここに来てはいけない
		// モードは、サービス上に常に保持
//        if( mTimer != null) 
//        {
//			mode = eMode.MODE_MEASURING;
//        }
		
		componentContainer.removeAllViews();
		int ret = 0;
		//ViewGroup contentView = ((ViewGroup)findViewById(android.R.id.content));
		BitmapFactory.Options bmpoptions = null;
		// center button 
		int iCenterButtonImageID = R.drawable.selector_runstop_button_image;
		try {
			if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
			{
				iCenterButtonImageID = R.drawable.selector_runstart_button_image;
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( btnCenter == null )
			btnCenter = new ImageButton(this);
		btnCenter.setId(CENTER_BUTTON_ID);
		btnCenter.setBackgroundResource( iCenterButtonImageID );//R.drawable.selector_runstart_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_runstartbutton_normal);
		RelativeLayout.LayoutParams rlBtnCenter 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
		btnCenter.setLayoutParams(rlBtnCenter);
		btnCenter.setScaleType(ScaleType.FIT_XY);
		btnCenter.setOnClickListener(this);
		btnCenter.setOnTouchListener(this);
		addViewToCompContainer(btnCenter);
//		// ボタンのリージョンを設定する		
//        Path path = new Path();
//        int x_cbtn = btnCenter.getLeft();
//        int y_cbtn =  btnCenter.getTop();
//        int width_cbtn = btnCenter.getWidth();
//        int height_cbtn = btnCenter.getHeight();
//        // 頂点
//        path.moveTo(x_cbtn + width_cbtn / 2
//        		, y_cbtn);
//        // 左の先へ
//        path.lineTo( x_cbtn, y_cbtn + height_cbtn / 2);
//        // 下の先へ
//        path.lineTo( x_cbtn + width_cbtn / 2
//        		, y_cbtn + height_cbtn );
//        // 右の先へ
//        path.lineTo( x_cbtn + width_cbtn
//        		, y_cbtn + height_cbtn / 2 );
//        // 頂点へ戻る？
////        path.moveTo(x_cbtn + width_cbtn / 2
////        		, y_cbtn );
//        path.close();
//        RectF rectF = new RectF();
//        path.computeBounds(rectF, true);	        
//        regionCenterBtn = new Region();
//        regionCenterBtn.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
		
		
		// GPSbutton
		if( btnGPS == null )		
			btnGPS = new ImageButton(this);
		btnGPS.setId(GPS_BUTTON_ID);
		btnGPS.setBackgroundResource( R.drawable.selector_gps_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_gpsbutton_normal);
		RelativeLayout.LayoutParams rlBtnGps 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				// LEFT_TOP_CTRL_1_LEFT_MARGIN, LEFT_TOP_CTRL_1_TOP_MARGIN, 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		//rlBtnGps.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlBtnGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
		rlBtnGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;		
		rlBtnGps.addRule(RelativeLayout.ABOVE, GPS_INDICATOR_ID);
		//rlBtnGps.addRule(RelativeLayout.CENTER_VERTICAL);
		//rlBtnGps.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		//rlBtnGps.topMargin = LEFT_TOP_CTRL_1_TOP_MARGIN;
		
		btnGPS.setLayoutParams(rlBtnGps);
		btnGPS.setScaleType(ScaleType.FIT_XY);
		btnGPS.setOnClickListener(this);
		addViewToCompContainer(btnGPS);

		// GPSindicator
		if( imgGPS == null )
		{
			imgGPS = new ImageView(this);
		}
		imgGPS.setId(GPS_INDICATOR_ID);
		String providers = Settings.Secure.getString(
				getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(providers.indexOf("gps", 0) < 0) 
		{
			imgGPS.setBackgroundResource(R.drawable.gps_not_arrow);
		}
		else
		{
//			try {
//				RunLogger.sService.createLocationManager();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//				Log.e("createLocationManager",e.getMessage());
//			}			
			// TODO: 未受信の画像作成？
			imgGPS.setBackgroundDrawable(null);
			//imgGPS.setBackgroundResource(R.drawable.gps_no_responce);
		}
		//imgGPS.setBackgroundResource( R.drawable.gps_bad );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.gps_bad);
		RelativeLayout.LayoutParams rlIndGps 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		//rlIndGps.addRule(RelativeLayout.BELOW, GPS_BUTTON_ID);
		//rlIndGps.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlIndGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
		rlIndGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;
		rlIndGps.addRule(RelativeLayout.CENTER_VERTICAL );
		imgGPS.setLayoutParams(rlIndGps);
		imgGPS.setScaleType(ScaleType.FIT_XY);
		addViewToCompContainer(imgGPS);

		// history button
		if( btnHistory == null )
			btnHistory = new ImageButton(this);
		btnHistory.setBackgroundResource( R.drawable.selector_history_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_historybutton_normal);
		RelativeLayout.LayoutParams rlBtnHistory
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlBtnHistory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlBtnHistory.leftMargin = LEFT_TOP_CTRL_1_LEFT_MARGIN;
		rlBtnHistory.addRule(RelativeLayout.CENTER_VERTICAL);
		//rlBtnHistory.topMargin = RIGHT_TOP_CTRL_1_TOP_MARGIN;
		
		btnHistory.setLayoutParams(rlBtnHistory);
		btnHistory.setScaleType(ScaleType.FIT_XY);
		btnHistory.setOnClickListener(this);
		addViewToCompContainer(btnHistory);
		
		// time label
		if( txtTime == null )
			txtTime = new TextView(this);
		RelativeLayout.LayoutParams rlTxtTime
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
		rlTxtTime.addRule(RelativeLayout.ABOVE, CENTER_BUTTON_ID);
		rlTxtTime.bottomMargin = CENTER_ABOVE_CTRL_MARGIN;
		rlTxtTime.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtTime.setLayoutParams(rlTxtTime);
		txtTime.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtTime.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));
		txtTime.setSingleLine();
		//txtTime.setText("99:99:99.999");
		txtTime.setTextSize(TIME_TEXTVIEW_FONT_SIZE);		
		addViewToCompContainer(txtTime);
		
		// distance
		if( txtDistance == null )
			txtDistance = new TextView(this);
		txtDistance.setId(DISTANCE_TEXT_ID);
		RelativeLayout.LayoutParams rlTxtDistance
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
//		rlTxtDistance.addRule(RelativeLayout.LEFT_OF, CENTER_BUTTON_ID);
//		rlTxtDistance.rightMargin = LEFT_CENTER_CTRL_MARGIN;
//		rlTxtDistance.addRule(RelativeLayout.CENTER_VERTICAL);
		rlTxtDistance.addRule(RelativeLayout.BELOW, CENTER_BUTTON_ID);
		rlTxtDistance.topMargin = CENTER_BELOW_CTRL_MARGIN;
		rlTxtDistance.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtDistance.setLayoutParams(rlTxtDistance);
		txtDistance.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		//txtDistance.setText("42.5353 km");
		txtDistance.setSingleLine();
		txtDistance.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		txtDistance.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
		addViewToCompContainer(txtDistance);

		// speed
		if( txtSpeed == null )		
			txtSpeed = new TextView(this);
		txtSpeed.setId(SPEED_TEXT_ID);
		RelativeLayout.LayoutParams rlTxtSpeed
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
//		rlTxtSpeed.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
//		rlTxtSpeed.leftMargin = LEFT_CENTER_CTRL_MARGIN;
//		rlTxtSpeed.addRule(RelativeLayout.CENTER_VERTICAL);
		rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
		rlTxtSpeed.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtSpeed.setLayoutParams(rlTxtSpeed);
		txtSpeed.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtSpeed.setTextSize(SPEED_TEXTVIEW_FONT_SIZE);
		txtSpeed.setSingleLine();
		//txtSpeed.setText("12.5 km/h");
		txtSpeed.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		addViewToCompContainer(txtSpeed);

		// speed
		if( txtSpeed2 == null )		
			txtSpeed2 = new TextView(this);
		RelativeLayout.LayoutParams rlTxtSpeed2
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
		rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
		rlTxtSpeed2.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtSpeed2.setLayoutParams(rlTxtSpeed2);
		txtSpeed2.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtSpeed2.setTextSize(SPEED_TEXTVIEW_FONT_SIZE);
		txtSpeed2.setSingleLine();
		//txtSpeed.setText("12.5 km/h");
		txtSpeed2.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		addViewToCompContainer(txtSpeed2);
		
		// next Lap button
		if( btnLap == null )
			btnLap = new ImageButton(this);
		btnLap.setId(LAP_BUTTON_ID);
		// TODO: next lapみたいな文言
		btnLap.setBackgroundResource( R.drawable.selector_history_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
				R.drawable.main_historybutton_normal);
		RelativeLayout.LayoutParams rlBtnLap
		= dispInfo.createLayoutParamForNoPosOnBk( 
				// LEFT_TOP_CTRL_1_LEFT_MARGIN, LEFT_TOP_CTRL_1_TOP_MARGIN, 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		//rlBtnLap.addRule(RelativeLayout.ALIGN_RIGHT );
		//rlBtnLap.addRule(RelativeLayout.ALIGN_BOTTOM );
		
		btnLap.setLayoutParams(rlBtnLap);
		btnLap.setScaleType(ScaleType.FIT_XY);
		btnLap.setOnClickListener(this);
		addViewToCompContainer(btnLap);
		
		// lap label
		if( txtLap == null )
			txtLap = new TextView(this);
		RelativeLayout.LayoutParams rlTxtLap
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
		rlTxtLap.addRule(RelativeLayout.BELOW, LAP_BUTTON_ID);
		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
		rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtLap.setLayoutParams(rlTxtLap);
		txtLap.setBackgroundColor(ResourceAccessor.getInstance().getColor(
				R.color.theme_color_cantedit));
		txtLap.setTextSize(LAP_TEXTVIEW_FONT_SIZE);
		txtLap.setSingleLine();
		txtLap.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		addViewToCompContainer(txtLap);
		
		// cancel?
		//btnCancel = new ImageButton(this);
		
		try {
			if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
			{
				txtTime.setVisibility(View.GONE);
				txtDistance.setVisibility(View.GONE);
				txtSpeed.setVisibility(View.GONE);
				txtSpeed2.setVisibility(View.GONE);
				btnLap.setVisibility(View.GONE);
				txtLap.setVisibility(View.GONE);
				btnCenter.setEnabled(false);
			}
			else if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
			{
				txtDistance.setVisibility(View.VISIBLE);
				txtSpeed.setVisibility(View.VISIBLE);
				txtSpeed2.setVisibility(View.VISIBLE);
				txtTime.setVisibility(View.VISIBLE);		
				btnLap.setVisibility(View.VISIBLE);
				if( 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
				{
					txtLap.setVisibility(View.VISIBLE);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("initControls",e.getMessage());
		}
		
		
		// TODO: limit
		Cursor c = getContentResolver().query(
				Uri.parse("content://" 
				+ RunHistoryTableContract.AUTHORITY + "/" 
				+ RunHistoryTableContract.HISTORY_TABLE_NAME ), null, null, null, null);
		
		if( c != null && 0 < c.getCount() )
		{
			btnHistory.setEnabled(true);
		}
		else
		{
			btnHistory.setEnabled(false);
		}
		c.close();
		
		return ret;
	}
	
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// どうやら、宛てにならないようなので、廃止する
//	}
	

	@Override
	public void onClick(View v) {
		if( v == btnGPS )
		{
			String providers = Settings.Secure.getString(
					getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			Log.v("GPS", "Location Providers = " + providers);
			if(providers.indexOf("gps", 0) < 0) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			} else {
				// Toast.makeText(getApplicationContext(), 
				// R.string.GPS_ON, Toast.LENGTH_LONG).show();
				requestGPS();
				//RunLogger.sService.requestGPS();
			}
		}
		else if( v == btnHistory )
		{
			// launch activity for save
			Intent intent = new Intent( this, HistoryActivity.class );
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		 
	        startActivity(intent);			
		}
		else if( v == btnLap )
		{
			// 次のラップへ
			RunLoggerService.getLogStocker().nextLap(new Date().getTime());
			txtLap.setVisibility(View.VISIBLE);
			txtLap.setText(getString(R.string.LAP_LABEL) 
					+ ( RunLoggerService.getLogStocker().getStockedLapCount() + 1));
		}
		else if( v == btnCenter )
		{
			try {
				if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
				{
					btnCenter.setBackgroundResource(R.drawable.selector_runstop_button_image);
					Date now = new Date();
					long time = now.getTime();
					// logging start
					RunLogger.sService.startLog();					
//				    if(mTimer == null){
//				        timerTask = new UpdateTimeDisplayTask();
//				        mTimer = new Timer(true);
//				        mTimer.scheduleAtFixedRate( timerTask, 1000, 1000);
//				    }				
				    RunLoggerService.createLogStocker(time);
					
					txtDistance.setText( LapData.createDistanceFormatText( 0 ) );
					txtTime.setText( LapData.createTimeFormatText( 0 ) );
					txtSpeed.setText( LapData.createSpeedFormatText( 0 ) );
					txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( 0 ) );
					
					txtDistance.setVisibility(View.VISIBLE);
					txtSpeed.setVisibility(View.VISIBLE);
					txtSpeed2.setVisibility(View.VISIBLE);
					txtTime.setVisibility(View.VISIBLE);
					btnLap.setVisibility(View.VISIBLE);			
					if( 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
					{
						txtLap.setVisibility(View.VISIBLE);
					}
					RunLogger.sService.setMode( eMode.MODE_MEASURING.ordinal() );
				}
				else if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
				{
					// logging end
					RunLogger.sService.stopLog();
					clearGPS();		            
		            RunningLogStocker.setRunHistorySaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
		            RunningLogStocker.setOutputGPXSaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
					RunLoggerService.getLogStocker().stop(new Date().getTime());
					// launch activity for save
					Intent intent = new Intent( this, ResultActivity.class );
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		 
			        startActivity(intent);				
					
					btnCenter.setBackgroundResource(R.drawable.selector_runstart_button_image);
	//				txtDistance.setVisibility(View.VISIBLE);
	//				txtSpeed.setVisibility(View.VISIBLE);
	//				txtTime.setVisibility(View.VISIBLE);
					btnLap.setVisibility(View.GONE);						
					RunLogger.sService.setMode( eMode.MODE_NORMAL.ordinal() );
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e("btnCenter","clicked error");
			}
		}
	}

	@Override
	public void clearGPS() {
		// サービスのクリアGPS
		try {
			RunLogger.sService.clearGPS();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("clearGPS",e.getMessage());
		}
	}

	
	///////////// Service Connection
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if( v == btnCenter )
		{
	        // タッチされた座標の取得
	        int x1 = (int)event.getX();
	        int y1 = (int)event.getY();
			if( regionCenterBtn == null )
			{
				// ボタンのリージョンを設定する
		        // ->おそらく、座標は不要
		        Path path = new Path();
		        int x_cbtn = 0;//btnCenter.getLeft();
		        int y_cbtn = 0;//btnCenter.getTop();
		        int width_cbtn = btnCenter.getWidth();
		        int height_cbtn = btnCenter.getHeight();
		        // 頂点
		        path.moveTo(x_cbtn + width_cbtn / 2
		        		, y_cbtn);
		        // 左の先へ
		        path.lineTo( x_cbtn, y_cbtn + height_cbtn / 2);
		        // 下の先へ
		        path.lineTo( x_cbtn + width_cbtn / 2
		        		, y_cbtn + height_cbtn );
		        // 右の先へ
		        path.lineTo( x_cbtn + width_cbtn
		        		, y_cbtn + height_cbtn / 2 );
		        // 頂点へ戻る？
	//	        path.moveTo(x_cbtn + width_cbtn / 2
	//	        		, y_cbtn );
		        path.close();
		        RectF rectF = new RectF();
		        path.computeBounds(rectF, true);	        
		        regionCenterBtn = new Region();
		        regionCenterBtn.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
			}
	        if( false == regionCenterBtn.contains( x1, y1 ))
	        {
	        	// ボタンの領域でない部分がタッチされていたら
		        // OnTouchをキャンセルする
		        return true;
	        }
		}
		return false;
	}
	IntentFilter intentFilter;
	BroadcastReceiver receiver;
	
	/**
	 * サービスからのintentのレシーバ
	 * @author 25689
	 *
	 */
	class ServiceNotifyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			// サービスからintentを受け取ったら
			if( MainActivity.LOCATION_CHANGE_NOTIFY.equals( intent.getAction() ) )
			{
				// LocationChangeの場合
				onLocationChanged((Location) intent.getParcelableExtra(LOCATION_DATA));
			}
			else if( MainActivity.TIMER_NOTIFY.equals( intent.getAction() ) )
			{
				// NOTICE: BroadcastReceiverでUI処理ができるのかは未確認
				txtTime.setText( intent.getStringExtra(CURRENT_DURATION) );
			}
		}
	}

}
