package app.guchagucharr.guchagucharunrecorder;


import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
//import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.util.CameraView;
import app.guchagucharr.interfaces.IMainViewController;
import app.guchagucharr.service.GPXGeneratorSync;
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
	// サービスからの通知用？
	public static String TIMER_NOTIFY = "TimeNotif";
	public static String CURRENT_DURATION = "CurDur";
	public static String TOTAL_DURATION = "TtlDur";
	public static String LOCATION_DATA = "LocData";
	public static String LOCATION_CHANGE_NOTIFY = "LocChgNotif";

	// カメラ用
	CameraView cameraView = null;
	boolean bCameraMode = false;
	// サービスのトークン
    private RunLogger.ServiceToken mToken = null;

    // 画面初期化用
	public static DisplayInfo dispInfo = DisplayInfo.getInstance();	
	private RelativeLayout componentContainer;
	private MainHandler handler;
	
	// NOTICE:タイマー処理の一部はサービスに移す案もある
	// private Timer mTimer = null;	
	
	boolean bUseGPS = true;
	// contorls
	// center button
	ImageButton btnCenter = null;
	Region regionCenterBtn = null;
	Boolean bCenterBtnEnableRegionTouched = false;
	Region regionLapBtn = null;
	Boolean bLapBtnEnableRegionTouched = false;
	Region regionCancelBtn = null;
	Boolean bCancelBtnEnableRegionTouched = false;
	// GPS button
	ImageButton btnGPS = null;
	// GPS indicator
	ImageView imgGPS = null;
	// history button
	ImageButton btnHistory = null;
	ImageButton btnLap = null;
	ImageButton btnCamera = null;
	// init invisible
	// time label
	static TextView txtTime = null;
	static TextView txtTimeOfLap = null;
	// distance
	static TextView txtDistance = null;
	static TextView txtDistanceOfLap = null;
	// speed
	static TextView txtSpeed = null;
	// speed2
	static TextView txtSpeed2 = null;
	// lap label
	static TextView txtLap = null;
	// location count label
	static TextView txtLocationCount = null;
	// cancel
	ImageButton btnCancel = null;
	
	/**
	 * Activityができたとき
	 * インスタンス作成等
	 * 画面初期化は、onResumeで始まる
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // get the layout
        componentContainer = (RelativeLayout)findViewById(R.id.main_content);
		// create handler
        handler = new MainHandler( this, this );
        // create resource accessor
        ResourceAccessor.CreateInstance(this);
        // res = ResourceAccessor.getInstance();
	}

	@Override
    protected void onResume() {
		
//		  private void checkGooglePlayServices() {
//			    int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//			    if (code != ConnectionResult.SUCCESS) {
//			      Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
//			          code, this, GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
//
//			              @Override
//			            public void onCancel(DialogInterface dialogInterface) {
//			              finish();
//			            }
//			          });
//			      if (dialog != null) {
//			        dialog.show();
//			        return;
//			      }
//			    }
		
		// 03/25 Activityがnullな可能性？
		ResourceAccessor.getInstance().setActivity(this);
		
        // サービスからの通知を受けるレシーバの作成、登録
        receiver = new ServiceNotifyReceiver();
        intentFilter = new IntentFilter();
        // 位置情報の取得時
        intentFilter.addAction(LOCATION_CHANGE_NOTIFY);
        // 時間
        intentFilter.addAction(TIMER_NOTIFY);
        registerReceiver(receiver,intentFilter);

    	// update display size etc.
		// when end update, send message to handler
		// now, initialize there.
		regionCenterBtn = null;
		regionLapBtn = null;
		regionCancelBtn = null;
        dispInfo.init(this, componentContainer, handler,false);

        // NOTICE: Resume毎につなぎ直すと、多すぎるかも？
        // Bind( Or Create and Bind) to Service
        mToken = RunLogger.bindToService(this, this);
        super.onResume();
    }
	// TODO onResumeよりもっと下位のイベントがあれば、それを設定して、
	// isArrowGPSSetting == true であれば requestGPSをコール
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
	    dispInfo.init(this, componentContainer, handler, true);	

	    // requestGPS();
	}

	@Override
	public void requestGPS()
	{
		// 2014/02/17 move to service
		try {
			if(RunLogger.sService == null 
			|| isArrowGPSSetting() == false )
			{
				return;
			}
			RunLogger.sService.clearGPS();
			RunLogger.sService.requestGPS();
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("requestGPS",e.getMessage());
		}
	}
	
	void stopService() throws RemoteException
	{
		Log.w("stopService","come");
		clearGPS();
		RunLogger.sService.stopLog();
		// ログ取得中でない場合は、完全に停止させる
		// RunLogger.sService.clearGPS();
		RunLogger.sService.clearLocationManager();					
		// サービスの登録解除
	    RunLogger.unbindFromService(mToken);
	    // サービスの停止
	    RunLogger.stopService(this);		
	}
	
	
	@Override
	protected void onPause()
	{		
		// サービスのトークンを保持していれば
		if(mToken != null)
		{
			try {
				// 実際に、サービスがnullでなければ
				if( RunLogger.sService != null )
				{
					if( RunLogger.sService.getMode() 
							== RunLoggerService.eMode.MODE_NORMAL.ordinal() )
					{
						// 今、計測中でなければ、サービスを止める
						Log.w("onPause2Stopservice","come");
						stopService();
					}
					else
					{
						// 計測中
						// サービスとActivityの切り離しのみ
					    RunLogger.unbindFromService(mToken);					
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e("onPause","RemoteException");
			}
			// どっちにしても、トークンをnullにする
		    mToken = null;
		}
		
		// レシーバの登録解除
        if( null != receiver )
        {
        	this.unregisterReceiver(receiver);
        	receiver = null;
        }
		
        // clearGPS();
		//android.R.drawable.ic_menu_mylocation
		regionCenterBtn = null;
		regionLapBtn = null;
		regionCancelBtn = null;

		clearCamera();
        super.onPause();	
	}
//	@Override
//	protected void onStop()
//	{
//		// stopService
//				
//        // clearGPS();
//        super.onStop();
//	}
//	@Override
//	protected void onDestroy()
//	{
//		// NOTICE: 本当に不要？
////		if( RunLogger.sService.getMode() == RunLoggerService.eMode.MODE_NORMAL.ordinal() )
////		{
////			RunLogger.sService.clearGPS();
////			// サービスの登録解除
////		    RunLogger.unbindFromService(mToken);
////		    mToken = null;
////		}
//		super.onDestroy();		
//	}

	/**
	 * 取得した位置情報を画面に表示する処理
	 * @param speed
	 */
	public void updateLogDisplay(double speed)
	{
		Log.v("updateLogDisplay - MainActivity", "come");
		try {
			if( false == RunLoggerService.isEmptyLogStocker() 
			&& RunLogger.sService != null
			&& RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
			{
				// 位置情報が取得されていて、サービス起動中で、計測中の場合
				if( false == RunLoggerService.isEmptyLogStocker()
				&& 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
				{
					// ラップが2周目以降の場合は、ラップのデータも表示する
					txtDistance.setText( LapData.createDistanceFormatText( 
							RunLoggerService.getLogStocker().getCurrentLapData().getDistance()
						)
					);
					txtDistanceOfLap.setText(LapData.createDistanceFormatText( 
						RunLoggerService.getLogStocker().getTotalDistance()
						+ RunLoggerService.getLogStocker().getCurrentLapData().getDistance()
						)
					);
				}
				else
				{
					// ラップが１周目の場合は、ラップのデータは表示しない
					txtDistance.setText( LapData.createDistanceFormatText( 
						RunLoggerService.getLogStocker().getCurrentLapData().getDistance() )
					);	
				}
				txtSpeed.setText( LapData.createSpeedFormatText( speed ) );//location.getSpeed() ) );
				//runLogStocker.getCurrentLapData().getSpeed() ) );
				txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( speed ) );//location.getSpeed() ) );
				//runLogStocker.getCu rrentLapData().getSpeed() ) );
				txtLocationCount.setText( String.valueOf(RunLoggerService.getLogStocker().getLocationDataCount() ) );
				//getLocationData().size() ) );
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("updateLogDisplay - MainActivity", e.getMessage());
		}		
	}
	//@Override
	/**
	 * 位置情報更新がサービスから通知されたときに呼ばれるはず
	 * @param location
	 */
	public void onLocationChanged(Location location) {
		//bGPSCanUse = true;
		Log.v("onLocationChanged - MainActivity","come");
		if( btnCenter != null && btnCenter.isEnabled() == false )
		{
			// 中央ボタンが使用不可だったら、位置情報を受信した時点で
			// 中央ボタンを使用可能にする
			btnCenter.setEnabled(true);
		}
		
		// 画面側では、サービスにストックされている位置情報の表示のみを行う
		updateLogDisplay( location.getSpeed() );
		Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));

        // 精度の表示
        if( imgGPS != null )
        {
			// TODO: 精度の数値表示
        	if( false == isArrowGPSSetting() )
        	{
        		imgGPS.setBackgroundResource(R.drawable.gps_not_arrow);
        	}
        	else if( 50 < location.getAccuracy() )
			{
				imgGPS.setBackgroundResource(R.drawable.gps_bad);
			}
			else if( 30 >= location.getAccuracy() )
			{
				imgGPS.setBackgroundResource(R.drawable.gps_soso);
			}
			else if( 10 >= location.getAccuracy() )
			{
				imgGPS.setBackgroundResource(R.drawable.gps_good);
			}
        }
	}
	// init controls
	static final int CENTER_BUTTON_ID = 1000;
	static final int GPS_BUTTON_ID = 1001;
	static final int GPS_INDICATOR_ID = 1002;
	static final int TIME_TEXT_ID = 1003;
	static final int DISTANCE_LAP_TEXT_ID = 1009;
	static final int DISTANCE_TEXT_ID = 1010;
	static final int SPEED_TEXT_ID = 1011;
	static final int LAP_BUTTON_ID = 1012;
	static final int CAMERA_BUTTON_ID = 1013;
	static final int CANCEL_BUTTON_ID = 1014;
	
	static final int LEFT_TOP_CTRL_1_LEFT_MARGIN = 20;
	static final int LEFT_TOP_CTRL_1_TOP_MARGIN = 40;
	static final int RIGHT_TOP_CTRL_1_RIGHT_MARGIN = 20;
	static final int RIGHT_TOP_CTRL_1_TOP_MARGIN = 40;
	static final int LEFT_CENTER_CTRL_MARGIN = 20;
	static final int RIGHT_CENTER_CTRL_MARGIN = 20;
	static final int CENTER_ABOVE_CTRL_MARGIN = -15;
	static final int CENTER_BELOW_CTRL_MARGIN = -15;
	static final int CENTER_LEFT_CTRL_MARGIN = 15;
	static final int CENTER_RIGHT_CTRL_MARGIN = 15;
	static final int CENTER_TOP_CTRL_MARGIN = 15;
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
	
	/**
	 * このViewのメインビューに子Viewを追加する
	 * @param v
	 */
	private void addViewToCompContainer( View v )
	{
		if( v.getParent() != null )
		{	
			((ViewGroup)v.getParent()).removeView(v);
		}
		componentContainer.addView(v);		
	}
	/**
	 * このViewのメインビューから子Viewを削除する
	 * @param v
	 */
	private void removeViewFromCompContainer( View v )
	{
		if( v.getParent() != null 
		&& componentContainer == v.getParent())
		{
			componentContainer.removeView(v);
		}
	}
	/**
	 * 計測時に一時的に作成されるGPXファイルが存在するかどうかチェックする
	 * @param activity
	 * @return
	 */
	private boolean isTmpGpxFileExists(Activity activity)
	{
		// フォルダ取得
		File tmpDir = activity.getFilesDir();
		// 一時ファイル名作成
		String gpxFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;
		// ファイルがあるかどうか
		File gpxFile = new File( gpxFilePath );
		return gpxFile.exists(); 
	}	
	
	/**
	 * 端末の設定でGPSが許可されているかどうか調べる
	 * @return
	 */
	public boolean isArrowGPSSetting()
	{
		String providers = Settings.Secure.getString(
				getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(providers.indexOf("gps", 0) < 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * GPSの精度を表すインジケータの画像を初期化する
	 */
	void resetGpsIndicator()
	{
		if(false == isArrowGPSSetting()) 
		{
			imgGPS.setBackgroundResource(R.drawable.gps_not_arrow);
		}
		else
		{
			// TODO: 未受信の画像作成？
			imgGPS.setBackgroundResource(R.drawable.gps_no_responce);
		}
		
	}
	
	/**
	 * 画面のサイズ取得後の画面初期化処理
	 */
	@Override
	public int initControls()
	{
		Log.v("initControls","come");
		if( RunLogger.sService == null )
		{
			// CONFIRM:サービスができてからここにくる想定？
			Log.w("sService","null");
			return -1;
		}
		try {
			if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
			{
				int iMode = RunLogger.getModeFromTmpFile(this);
				if( iMode == eMode.MODE_MEASURING.ordinal() )
				{
					// 内部モードは通常モードなのに、
					// ファイルに格納されたモードが計測モード
					// ファイルの方を正とみなし、復旧処理を行う
					Toast.makeText(this, "recovery process come", Toast.LENGTH_LONG ).show();
				}
			}
//				// ノーマルモードなのに、GPXがある
//				// ->前に落ちたと見なし、復帰処理に入る
//					//if( RunLoggerService.isEmptyLogStocker() )
//					//{				
//				Log.v("recovery process","come");
//					RunLoggerService.clearRunLogStocker();
//					RunLoggerService.createLogStocker();
//				//}
//				// TODO: モードをファイル管理にし、モードを復帰？
//				// TODO:-----------GPXの断片から、ログストッカーの内容を復帰
//					
//				//if( RunLoggerService.isEmptyLogStocker() )
//				//{
//				long time = RunLogger.sService.getTimeInMillis();
//				if( 0 != RunLogger.startLog(this, time) )
////						if( false == RunLoggerService.getLogStocker().start(this,time) )
//				{
//					RunLoggerService.clearRunLogStocker();
//					Toast.makeText(this, R.string.cant_start_workout_because_error, Toast.LENGTH_LONG).show();
//					return -1;
//				}
//				
//				RunLogger.sService.setMode(eMode.MODE_MEASURING.ordinal());
//			}
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("",e.getMessage());
		}
		// 全てのビューをクリアする
		componentContainer.removeAllViews();
		int ret = 0;
		BitmapFactory.Options bmpoptions = null;
		// center button
		// 現在のモードによって、中央ボタンの画像を切り替える
		int iCenterButtonImageID = R.drawable.selector_runstop_button_image;
		try {
			if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
			{
				// 計測前
				iCenterButtonImageID = R.drawable.selector_runstart_button_image;
			}
			else if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
			{
				// 計測中
				iCenterButtonImageID = R.drawable.selector_runstop_button_image;
			}			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( btnCenter == null )
			btnCenter = new ImageButton(this);
		btnCenter.setId(CENTER_BUTTON_ID);
		btnCenter.setBackgroundResource( iCenterButtonImageID );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
				R.drawable.main_runstartbutton_normal);
		RelativeLayout.LayoutParams rlBtnCenter 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, false );
		rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
		btnCenter.setLayoutParams(rlBtnCenter);
		btnCenter.setScaleType(ScaleType.FIT_XY);
		btnCenter.setOnClickListener(this);
		btnCenter.setOnTouchListener(this);
		addViewToCompContainer(btnCenter);

		// GPSbutton
		if( btnGPS == null )		
			btnGPS = new ImageButton(this);
		btnGPS.setId(GPS_BUTTON_ID);
		btnGPS.setBackgroundResource( R.drawable.selector_gps_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_gpsbutton_normal);
		RelativeLayout.LayoutParams rlBtnGps 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, false );
		rlBtnGps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rlBtnGps.rightMargin = RIGHT_TOP_CTRL_1_RIGHT_MARGIN;
		
		rlBtnGps.addRule(RelativeLayout.ABOVE, GPS_INDICATOR_ID);
		
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
		resetGpsIndicator();
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.gps_bad);
		RelativeLayout.LayoutParams rlIndGps 
		= dispInfo.createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlIndGps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rlIndGps.rightMargin = RIGHT_TOP_CTRL_1_RIGHT_MARGIN;
		rlIndGps.addRule(RelativeLayout.CENTER_VERTICAL );
		imgGPS.setLayoutParams(rlIndGps);
		imgGPS.setScaleType(ScaleType.FIT_XY);
		addViewToCompContainer(imgGPS);
		
		// CONFIRM: これの存在は割と微妙
		// location count label
		if( txtLocationCount == null )
			txtLocationCount = new TextView(this);
		RelativeLayout.LayoutParams rlLocationCount
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
		rlLocationCount.addRule(RelativeLayout.BELOW, GPS_INDICATOR_ID);
		rlLocationCount.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rlLocationCount.rightMargin = RIGHT_TOP_CTRL_1_RIGHT_MARGIN;
		
		txtLocationCount.setLayoutParams(rlLocationCount);
		txtLocationCount.setBackgroundColor(ResourceAccessor.getInstance().getColor(
				R.color.theme_color_cantedit));
		txtLocationCount.setTextSize(LAP_TEXTVIEW_FONT_SIZE);
		txtLocationCount.setSingleLine();
		txtLocationCount.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		addViewToCompContainer(txtLocationCount);

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
		txtTime.setId(TIME_TEXT_ID);
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
			rlTxtTime.addRule(RelativeLayout.ABOVE, CENTER_BUTTON_ID);
			rlTxtTime.bottomMargin = CENTER_ABOVE_CTRL_MARGIN;
			rlTxtTime.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
			rlTxtTime.addRule(RelativeLayout.LEFT_OF, CENTER_BUTTON_ID);
			rlTxtTime.rightMargin = CENTER_LEFT_CTRL_MARGIN;
			rlTxtTime.addRule(RelativeLayout.CENTER_VERTICAL);        	
        }
		txtTime.setLayoutParams(rlTxtTime);
		txtTime.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtTime.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));
		txtTime.setSingleLine();
		//txtTime.setText("99:99:99.999");
		txtTime.setTextSize(TIME_TEXTVIEW_FONT_SIZE);
		addViewToCompContainer(txtTime);
		
		// timeOfLap label
		if( txtTimeOfLap == null )
			txtTimeOfLap = new TextView(this);
		RelativeLayout.LayoutParams rlTxtTimeOfLap
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
        	rlTxtTimeOfLap.addRule(RelativeLayout.ABOVE, TIME_TEXT_ID);
        	rlTxtTimeOfLap.bottomMargin = CENTER_ABOVE_CTRL_MARGIN;
        	rlTxtTimeOfLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
        	rlTxtTimeOfLap.addRule(RelativeLayout.ABOVE, TIME_TEXT_ID);
        	rlTxtTimeOfLap.addRule(RelativeLayout.LEFT_OF, CENTER_BUTTON_ID);			
        	rlTxtTimeOfLap.rightMargin = CENTER_LEFT_CTRL_MARGIN;
			//rlTxtTime.addRule(RelativeLayout.CENTER_VERTICAL);        	
        }
		txtTimeOfLap.setLayoutParams(rlTxtTimeOfLap);
		txtTimeOfLap.setBackgroundColor(
				ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtTimeOfLap.setTextColor(
				ResourceAccessor.getInstance().getColor(R.color.text_color_important));
		txtTimeOfLap.setSingleLine();
		//txtTime.setText("99:99:99.999");
		txtTimeOfLap.setTextSize(TIME_TEXTVIEW_FONT_SIZE);
		addViewToCompContainer(txtTimeOfLap);

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
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
    		rlTxtDistance.addRule(RelativeLayout.BELOW, DISTANCE_LAP_TEXT_ID);
    		rlTxtDistance.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		rlTxtDistance.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
    		rlTxtDistance.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
    		rlTxtDistance.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
        	//rlTxtDistance.addRule(RelativeLayout.BELOW, DISTANCE_LAP_TEXT_ID);        	
    		rlTxtDistance.addRule(RelativeLayout.CENTER_VERTICAL);
        }
		txtDistance.setLayoutParams(rlTxtDistance);
		txtDistance.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		//txtDistance.setText("42.5353 km");
		txtDistance.setSingleLine();
		txtDistance.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		txtDistance.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
		addViewToCompContainer(txtDistance);
		
		// distanceOfLap
		if( txtDistanceOfLap == null )
			txtDistanceOfLap = new TextView(this);
		txtDistanceOfLap.setId(DISTANCE_LAP_TEXT_ID);
		RelativeLayout.LayoutParams rlTxtDistanceOfLap
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
        	rlTxtDistanceOfLap.addRule(RelativeLayout.BELOW, CENTER_BUTTON_ID);
        	rlTxtDistanceOfLap.topMargin = CENTER_BELOW_CTRL_MARGIN;
        	rlTxtDistanceOfLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
        	rlTxtDistanceOfLap.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
    		rlTxtDistanceOfLap.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
    		//rlTxtDistanceOfLap.addRule(RelativeLayout.CENTER_VERTICAL);
    		rlTxtDistanceOfLap.addRule(RelativeLayout.ABOVE, DISTANCE_TEXT_ID);    		
        }
        txtDistanceOfLap.setLayoutParams(rlTxtDistanceOfLap);
        txtDistanceOfLap.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		//txtDistanceOfLap.setText("42.5353 km");
        txtDistanceOfLap.setSingleLine();
        txtDistanceOfLap.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
        txtDistanceOfLap.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
		addViewToCompContainer(txtDistanceOfLap);
		
		// speed
		if( txtSpeed == null )		
			txtSpeed = new TextView(this);
		txtSpeed.setId(SPEED_TEXT_ID);
		RelativeLayout.LayoutParams rlTxtSpeed
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
    		rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		rlTxtSpeed.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
    		rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		//rlTxtSpeed.addRule(RelativeLayout.CENTER_HORIZONTAL);
    		rlTxtSpeed.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
    		rlTxtSpeed.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
        }
		
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
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
    		rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		rlTxtSpeed2.addRule(RelativeLayout.CENTER_HORIZONTAL);        	
        }
        else
        {
        	// 横向き
    		rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		//rlTxtSpeed2.addRule(RelativeLayout.CENTER_HORIZONTAL);        	
    		rlTxtSpeed2.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
    		rlTxtSpeed2.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
        }
		
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
		btnLap.setBackgroundResource( R.drawable.selector_next_button_image );
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
		btnLap.setOnTouchListener(this);
		addViewToCompContainer(btnLap);
		
		// lap label
		if( txtLap == null )
			txtLap = new TextView(this);
		RelativeLayout.LayoutParams rlTxtLap
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        if( true == dispInfo.isPortrait() )
        {
        	// 縦向き
    		rlTxtLap.addRule(RelativeLayout.BELOW, LAP_BUTTON_ID);
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else
        {
        	// 横向き
    		rlTxtLap.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    		rlTxtLap.topMargin = CENTER_TOP_CTRL_MARGIN;
    		//rlTxtSpeed.topMargin = CENTER_BELOW_CTRL_MARGIN;
    		rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
		
		txtLap.setLayoutParams(rlTxtLap);
		txtLap.setBackgroundColor(ResourceAccessor.getInstance().getColor(
				R.color.theme_color_cantedit));
		txtLap.setTextSize(LAP_TEXTVIEW_FONT_SIZE);
		txtLap.setSingleLine();
		txtLap.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		addViewToCompContainer(txtLap);
		
		// next Lap button
		if( btnCamera == null )
			btnCamera = new ImageButton(this);
		btnCamera.setId(CAMERA_BUTTON_ID);
		btnCamera.setBackgroundResource( R.drawable.selector_camera_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
				R.drawable.main_camerabutton_normal);
		RelativeLayout.LayoutParams rlbtnCamera
		= dispInfo.createLayoutParamForNoPosOnBk( 
				// LEFT_TOP_CTRL_1_LEFT_MARGIN, LEFT_TOP_CTRL_1_TOP_MARGIN, 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlbtnCamera.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
		rlbtnCamera.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
		
		btnCamera.setLayoutParams(rlbtnCamera);
		btnCamera.setScaleType(ScaleType.FIT_XY);
		btnCamera.setOnClickListener(this);
		addViewToCompContainer(btnCamera);

		// Cancelボタン
		if( btnCancel == null )
			btnCancel = new ImageButton(this);		
		btnCancel.setBackgroundResource( R.drawable.selector_cancel_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
				R.drawable.main_cancelbutton_normal);
		RelativeLayout.LayoutParams rlBtnCancel
		= dispInfo.createLayoutParamForNoPosOnBk( 
				// LEFT_TOP_CTRL_1_LEFT_MARGIN, LEFT_TOP_CTRL_1_TOP_MARGIN, 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlBtnCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT );
		rlBtnCancel.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
		
		btnCancel.setLayoutParams(rlBtnCancel);
		btnCancel.setScaleType(ScaleType.FIT_XY);
		btnCancel.setOnClickListener(this);
		btnCancel.setOnTouchListener(this);
		addViewToCompContainer(btnCancel);
				
		try {
			if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
			{
				txtTime.setVisibility(View.GONE);
				txtTimeOfLap.setVisibility(View.GONE);
				txtDistance.setVisibility(View.GONE);
				txtDistanceOfLap.setVisibility(View.GONE);
				txtSpeed.setVisibility(View.GONE);
				txtSpeed2.setVisibility(View.GONE);
				btnLap.setVisibility(View.GONE);
				btnCamera.setVisibility(View.GONE);
				btnCancel.setVisibility(View.GONE);
				txtLap.setVisibility(View.GONE);
				btnCenter.setEnabled(false);
				txtLocationCount.setVisibility(View.GONE);
			}
			else if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
			{
				txtDistance.setVisibility(View.VISIBLE);
				txtSpeed.setVisibility(View.VISIBLE);
				txtSpeed2.setVisibility(View.VISIBLE);
				txtTime.setVisibility(View.VISIBLE);		
				btnLap.setVisibility(View.VISIBLE);
				btnCamera.setVisibility(View.VISIBLE);
				btnCancel.setVisibility(View.VISIBLE);
				btnCamera.setEnabled(true);
				btnCancel.setEnabled(true);

				if( false == RunLoggerService.isEmptyLogStocker()
				&& 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
				{
					txtLap.setVisibility(View.VISIBLE);
					txtTimeOfLap.setVisibility(View.VISIBLE);
					txtDistanceOfLap.setVisibility(View.VISIBLE);
				}
				else
				{
					txtLap.setVisibility(View.GONE);
					txtTimeOfLap.setVisibility(View.GONE);
					txtDistanceOfLap.setVisibility(View.GONE);					
				}
				if( false == RunLoggerService.isEmptyLogStocker() 
				&& 0 < RunLoggerService.getLogStocker().getLocationDataCount() )//getLocationData().isEmpty() == false )
				{
					updateLogDisplay(RunLoggerService.getLogStocker().getCurrentLocation().getSpeed() );//getLocationData().lastElement().getSpeed());//0);
				}
				txtLocationCount.setVisibility(View.VISIBLE);
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
	
	@Override
	public void onClick(View v) {

		try {
			if( v != null )
			{
				v.setEnabled(false);
			}
			
			if( v == btnGPS )
			{
				if(false == isArrowGPSSetting()) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				} else {
					requestGPS();
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
				if( false == bLapBtnEnableRegionTouched )
				{
					return;
				}
				// 次のラップへ
				try {
					RunLoggerService.getLogStocker().nextLap(this,RunLogger.sService.getTimeInMillis());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				txtDistance.setText( LapData.createDistanceFormatText( 0 ) );
				//txtTime.setText( LapData.createTimeFormatText( 0 ) );
				txtSpeed.setText( LapData.createSpeedFormatText( 0 ) );
				txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( 0 ) );
				
				//new Date().getTime());
				txtTimeOfLap.setVisibility(View.VISIBLE);
				txtTimeOfLap.setText( LapData.createTimeFormatText( 0 ) );
				txtDistanceOfLap.setVisibility(View.VISIBLE);
				txtDistanceOfLap.setText( LapData.createDistanceFormatText( 0 ) );

				txtLap.setVisibility(View.VISIBLE);
				txtLap.setText(getString(R.string.LAP_LABEL) 
						+ ( RunLoggerService.getLogStocker().getStockedLapCount() + 1));
			}
			else if( v == btnCancel )
			{
				if( false == bCancelBtnEnableRegionTouched )
				{
					return;
				}
				try {
					// 全てクリアする
					// TODO:確認ダイアログ
					if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
					{
						RunLogger.sService.setMode( eMode.MODE_NORMAL.ordinal() );
						// モードをファイルに書き込み
						RunLogger.writeModeToTmpFile(this,eMode.MODE_NORMAL);						
						// logging end
						RunLogger.sService.stopLog();
						clearGPS();
			            RunningLogStocker.setRunHistorySaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
			            RunningLogStocker.setOutputGPXSaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
						RunLoggerService.getLogStocker().stop(this, RunLogger.sService.getTimeInMillis());//new Date().getTime());
						
						btnCenter.setBackgroundResource(R.drawable.selector_runstart_button_image);
						txtTime.setVisibility(View.GONE);
						txtTimeOfLap.setVisibility(View.GONE);
						txtDistance.setVisibility(View.GONE);
						txtDistanceOfLap.setVisibility(View.GONE);
						txtSpeed.setVisibility(View.GONE);
						txtSpeed2.setVisibility(View.GONE);
						txtLap.setVisibility(View.GONE);
						btnCenter.setEnabled(false);
						txtLocationCount.setVisibility(View.GONE);
						btnLap.setVisibility(View.GONE);
						btnCamera.setVisibility(View.GONE);
						btnCancel.setVisibility(View.GONE);
						resetGpsIndicator();
						//imgGPS.setBackgroundResource(R.drawable.gps_no_responce);
						if( bUseGPS )
						{
							requestGPS();
						}
						Log.v("btnCancel","clicked");
						return;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					Log.e("btnCancel","clicked error");
				}
			}			
			else if( v == btnCamera )
			{
				// カメラビュー
				this.showCamera();
			}
			else if( v == btnCenter )
			{
				if( false == bCenterBtnEnableRegionTouched )
				{
					return;
				}
				try {
					if( RunLogger.sService.getMode() == eMode.MODE_NORMAL.ordinal() )
					{
						// 計測前の場合
						// 位置情報を全てクリアする
						RunLoggerService.clearRunLogStocker();
					    RunLoggerService.createLogStocker();

					    // サービスから開始時間を取得
						long time = RunLogger.sService.getTimeInMillis();
						// 位置情報取得開始
						if( 0 != RunLogger.startLog(this,time) )
						{
							// ログ取得開始に失敗したら、ログをクリアして戻る
							RunLoggerService.clearRunLogStocker();
							// モードをファイルに書き込み
							RunLogger.writeModeToTmpFile(this,eMode.MODE_NORMAL);
							Toast.makeText(this, R.string.cant_start_workout_because_error, Toast.LENGTH_LONG).show();
							return;
						}
//						if( false == RunLoggerService.getLogStocker().start(this,time) )
//						{
//							// ログ取得開始に失敗したら、ログをクリアして戻る
//							RunLoggerService.clearRunLogStocker();
//							Toast.makeText(this, R.string.cant_start_workout_because_error, Toast.LENGTH_LONG).show();
//							return;
//						}
						
						// 中央ボタンを計測中のものにする
						btnCenter.setBackgroundResource(R.drawable.selector_runstop_button_image);
						
						// サービスの方でも、ログ取得開始？
						// CONFIRM:なぜログ開始関数が２つ？
						// ==>できたらまとめること。
						// RunLogger.sService.startLog();		

						// 画面表示のクリア
						// TODO:クリア関数があれば、それを利用
						txtDistance.setText( LapData.createDistanceFormatText( 0 ) );
						txtTime.setText( LapData.createTimeFormatText( 0 ) );
						txtSpeed.setText( LapData.createSpeedFormatText( 0 ) );
						txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( 0 ) );
						txtLocationCount.setText("0");
						
						txtDistance.setVisibility(View.VISIBLE);
						txtSpeed.setVisibility(View.VISIBLE);
						txtSpeed2.setVisibility(View.VISIBLE);
						txtTime.setVisibility(View.VISIBLE);
						btnLap.setVisibility(View.VISIBLE);
						btnCamera.setVisibility(View.VISIBLE);
						btnCancel.setVisibility(View.VISIBLE);
						btnCancel.setEnabled(true);						
						if( 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
						{
							txtLap.setVisibility(View.VISIBLE);
							txtTimeOfLap.setVisibility(View.VISIBLE);
							txtDistanceOfLap.setVisibility(View.VISIBLE);
						}
						txtLocationCount.setVisibility(View.VISIBLE);
						// RunLogger.sService.setMode( eMode.MODE_MEASURING.ordinal() );
					}
					else if( RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
					{
						RunLogger.sService.setMode( eMode.MODE_NORMAL.ordinal() );
						// モードをファイルに書き込み
						RunLogger.writeModeToTmpFile(this,eMode.MODE_NORMAL);						
						// logging end
						RunLogger.sService.stopLog();
						clearGPS();		            
			            RunningLogStocker.setRunHistorySaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
			            RunningLogStocker.setOutputGPXSaveResult(RunningLogStocker.SAVE_NOT_TRY,RunLoggerService.getLogStocker());
						RunLoggerService.getLogStocker().stop(this, RunLogger.sService.getTimeInMillis());//new Date().getTime());
						
						btnCenter.setBackgroundResource(R.drawable.selector_runstart_button_image);
						txtTime.setVisibility(View.GONE);
						txtTimeOfLap.setVisibility(View.GONE);
						txtDistance.setVisibility(View.GONE);
						txtDistanceOfLap.setVisibility(View.GONE);
						txtSpeed.setVisibility(View.GONE);
						txtSpeed2.setVisibility(View.GONE);
						txtLap.setVisibility(View.GONE);
						btnCenter.setEnabled(false);
						txtLocationCount.setVisibility(View.GONE);
						btnLap.setVisibility(View.GONE);
						btnCamera.setVisibility(View.GONE);
						btnCancel.setVisibility(View.GONE);
		
						if( RunLoggerService.getLogStocker().getCurrentLocation() == null )
						{
							return;
						}
						// launch activity for save
						Intent intent = new Intent( this, ResultActivity.class );
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	 
				        startActivity(intent);				
						
//						btnCenter.setBackgroundResource(R.drawable.selector_runstart_button_image);
//		//				txtDistance.setVisibility(View.VISIBLE);
//		//				txtSpeed.setVisibility(View.VISIBLE);
//		//				txtTime.setVisibility(View.VISIBLE);
//						btnLap.setVisibility(View.GONE);
//						btnCamera.setVisibility(View.GONE);
//						btnCancel.setVisibility(View.GONE);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					Log.e("btnCenter","clicked error");
				}
			}
		} finally {
			if( v != null )
			{
				if( v == btnGPS || v == btnLap || v==btnCancel)
				{
					v.setEnabled(true);
				}
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
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		// サービスが接続されたとき
		Log.v("onServiceConnected",String.valueOf(name));
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// クラッシュ時くらいしかこなかったはず
		Log.e("onServiceDisconnected",String.valueOf(name));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Region region = null;
		//Boolean bRegionFlag = null;
		if( v == btnCenter 
		|| v == btnLap 
		|| v == btnCancel )
		{
			if( btnCenter == v )
			{
				region = regionCenterBtn;
				//bRegionFlag = bCenterBtnEnableRegionTouched;
 
			}
			else if( v == btnLap )
			{
				region = regionLapBtn;
				//bRegionFlag = bLapBtnEnableRegionTouched;
			}
			else if( v == btnCancel )
			{
				region = regionCancelBtn;
			}
	        // タッチされた座標の取得
	        int x1 = (int)event.getX();
	        int y1 = (int)event.getY();
			if( region == null )
			{
				// ボタンのリージョンを設定する
		        // ->おそらく、座標は不要
		        Path path = new Path();
		        int x_cbtn = 0;//btnCenter.getLeft();
		        int y_cbtn = 0;//btnCenter.getTop();
		        int width_cbtn = v.getWidth();
		        int height_cbtn = v.getHeight();
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
		        region = new Region();
		        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
				if( btnCenter == v )
				{
					bCenterBtnEnableRegionTouched = true;
				}
				else if( v == btnLap )
				{
					bLapBtnEnableRegionTouched = true;
				}
				else if( v == btnCancel )
				{
					bCancelBtnEnableRegionTouched = true;
				}
			}
	        if( false == region.contains( x1, y1 ))
	        {
	        	// ボタンの領域でない部分がタッチされていたら
		        // OnTouchをキャンセルする
				if( btnCenter == v )
				{
					bCenterBtnEnableRegionTouched = false;
				}
				else if( v == btnLap )
				{
					bLapBtnEnableRegionTouched = false;
				}
				else if( v == btnCancel )
				{
					bCancelBtnEnableRegionTouched = false;
				}
 
	        	// onTouchを返さないと、他の制御がおかしくなりそうなので、onTouchは返した後、
	        	// pressイベントで無理矢理制御する
		        //return true;
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
				// BroadcastReceiverから
				txtTime.setText( intent.getStringExtra(TOTAL_DURATION) );
				if( false == RunLoggerService.isEmptyLogStocker()
				&& 0 < RunLoggerService.getLogStocker().getStockedLapCount() )
				{				
					txtTimeOfLap.setText( intent.getStringExtra(CURRENT_DURATION) );
				}
			}
		}
	}
	public void showCamera()
	{
		// サーフィスビューのクリア方法
		cameraView = new CameraView(this);
		cameraView.setHandler(handler);
		RelativeLayout.LayoutParams rl
		= dispInfo.createLayoutParamForNoPosOnBk( 
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true );
		// NOTICE: 下記は、おそらく不要
		rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		// レイアウトの設定
		cameraView.setLayoutParams(rl);
		
		addViewToCompContainer( cameraView );
		bCameraMode = true;
	}
	
	public void clearCamera()
	{
		if( cameraView != null )
		{
			// TODO:サーフィスビューのクリア方法
			cameraView.setVisibility( View.GONE );
			removeViewFromCompContainer( cameraView );
			btnCamera.setEnabled(true);
			bCameraMode = false;
			cameraView = null;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			// 
			if( bCameraMode )
			{
				// カメラが表示されていた場合、カメラを削除するだけで、アプリは落とさない
				clearCamera();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void endCamera(String uri)
	{
		clearCamera();
		if( uri != null )
		{
			if( RunLoggerService.getLogStocker() != null)
			{
				RunLoggerService.getLogStocker().addImageUri(uri);
			}
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
      }
      
      @Override
      public boolean onPrepareOptionsMenu(Menu menu) {
        // updateMenuItems(isGpsStarted, isRecording);
        return super.onPrepareOptionsMenu(menu);
      }
      
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
        //Intent intent;
        switch (item.getItemId()) {
          //case ID_MENU_RECOVERY:
            //return true;
          default:
            return super.onOptionsItemSelected(item);
        }
      }
	

}
