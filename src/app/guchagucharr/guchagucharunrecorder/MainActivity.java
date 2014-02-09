package app.guchagucharr.guchagucharunrecorder;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
//import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import app.guchagucharr.guchagucharunrecorder.util.SystemUiHider;
import app.guchagucharr.interfaces.IMainViewController;
//import app.guchagucharr.service.RunningLogStocker;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunHistoryTableContract;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements LocationListener,IMainViewController, OnClickListener {

	public static DisplayInfo dispInfo = DisplayInfo.getInstance();	
	private RelativeLayout componentContainer;
	private LocationManager mLocationManager;
	private MainHandler handler;
	//private boolean bGPSCanUse = false;
	private static Timer mTimer = null;
	private static UpdateTimeDisplayTask timerTask = null;
	class UpdateTimeDisplayTask extends TimerTask{
		 
	     @Override
	     public void run() {
	         // mHandler through UI Thread to queueing
	    	 handler.post( new Runnable() {
	             @Override
				public void run() {	 
	                 // update now Time
	         		if( ResourceAccessor.getInstance().getLogStocker() != null 
	         				&& mode == eMode.MODE_MEASURING )
	        		{
	         			long lapTime = new Date().getTime() 
	         					- ResourceAccessor.getInstance().getLogStocker().getCurrentLapData().getStartTime();
	        			txtTime.setText( LapData.createTimeFormatText( lapTime ) );
	        			initGPS();
	        		}	            	 
	             }
	         });
	     }
	 }	
	// private ResourceAccessor res;
	private enum eMode {
		MODE_NORMAL,
		MODE_MEASURING//,
		// MODE_SAVE_OR_CLEAR
	};
	//static eMode mode2; 
	private static eMode mode = eMode.MODE_NORMAL;
	
	// contorls
	// center button
	ImageButton btnCenter = null;
	// GPS button
	ImageButton btnGPS = null;
	// GPS indicator
	ImageView imgGPS = null;
	// history button
	ImageButton btnHistory = null;
	// init invisible
	// time label
	static TextView txtTime = null;
	// distance
	static TextView txtDistance = null;
	// speed
	static TextView txtSpeed = null;
	// speed2
	static TextView txtSpeed2 = null;
	// cancel
	ImageButton btnCancel = null;
	
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
        // get the layout
        componentContainer = (RelativeLayout)findViewById(R.id.main_content);
		final View contentView = componentContainer;//findViewById(R.id.main_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		// GPS setting
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		initGPS();		
        // create handler
        handler = new MainHandler( this, this );
        // create resource accessor
        ResourceAccessor.CreateInstance(this);
        // res = ResourceAccessor.getInstance();
		
		// only use GPS or not
//		Criteria criteria = new Criteria();
//		    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//		    String bestProvider =
//		    	mLocationManager.getBestProvider(criteria, true);		
	}

	@Override
    protected void onResume() {
    	// update display size etc.
		// when end update, send message to handler
		// now, initialize there.
        dispInfo.init(this, componentContainer, handler,false);
        super.onResume();
    }
	
	@Override
	public void initGPS()
	{
		final long MIN_TIME = 100;
		final long MIN_METER = 1;
        if (mLocationManager != null) {
        	//clearGPS();
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
//                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_METER,
                this);
        }
	}
	
	@Override
	protected void onPause()
	{
//        if (mLocationManager != null) {
//            mLocationManager.removeUpdates(this);
//        }
        // clearGPS();
        super.onPause();	
	}
	@Override
	protected void onStop()
	{
        clearGPS();
        super.onStop();
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	
	@Override
	public void onLocationChanged(Location location) {
		clearGPS();
		//bGPSCanUse = true;
		Log.v("onLocationChanged","come");
		btnCenter.setEnabled(true);
		if( false == ResourceAccessor.getInstance().isEmptyLogStocker() 
				&& mode == eMode.MODE_MEASURING )
		{
//			if( bGPSCanUse == false )
//			{
//				// TODO:create indicator? 
//				txtDistance.setText( getString(R.string.cant_get) );
//				txtTime.setText( getString(R.string.cant_get) );
//				txtSpeed.setText( getString(R.string.cant_get) );				
//			}
//			else
//			{
			Log.v("add","location info");
			ResourceAccessor.getInstance().putLocationLog(location);
			txtDistance.setText( LapData.createDistanceFormatText( 
					ResourceAccessor.getInstance().getLogStocker().getCurrentLapData().getDistance() ) );
			// txtTime.setText( createTimeFormatText( runLogStocker.getCurrentLapData().getTotalTime() ) );
			// speed isn't lap's value. current speed show.
			txtSpeed.setText( LapData.createSpeedFormatText( location.getSpeed() ) );//runLogStocker.getCurrentLapData().getSpeed() ) );
			txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( location.getSpeed() ) );//runLogStocker.getCurrentLapData().getSpeed() ) );
//			}
		}
		Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));
		if( location.getAccuracy() < 2 )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_bad);
		}
		else if( 6 < location.getAccuracy() )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_good);
		}
		else if( 2 < location.getAccuracy() )
		{
			imgGPS.setBackgroundResource(R.drawable.gps_soso);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		//bGPSCanUse = false;
		Log.v("gps","onProviderDisabled");
		//imgGPS.setBackgroundResource(R.drawable.gps_bad);
		//btnCenter.setEnabled(false);
	}

	@Override
	public void onProviderEnabled(String provider) {
		//bGPSCanUse = true;
		Log.v("gps","onProviderEnabled");
		//imgGPS.setBackgroundResource(R.drawable.gps_good);
		//btnCenter.setEnabled(true);
	}

	// init controls
	static final int CENTER_BUTTON_ID = 1000;
	static final int GPS_BUTTON_ID = 1001;
	static final int GPS_INDICATOR_ID = 1002;
	static final int DISTANCE_TEXT_ID = 1010;
	static final int SPEED_TEXT_ID = 1011;
	
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
	
	
	@Override
	public int initControls()
	{
		componentContainer.removeAllViews();
		int ret = 0;
		//ViewGroup contentView = ((ViewGroup)findViewById(android.R.id.content));
		BitmapFactory.Options bmpoptions = null;
		// center button 
		int iCenterButtonImageID = R.drawable.selector_runstop_button_image;
		if( mode == eMode.MODE_NORMAL )
		{
			iCenterButtonImageID = R.drawable.selector_runstart_button_image;
		}
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
		componentContainer.addView(btnCenter);
		
		// GPSbutton
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
		componentContainer.addView(btnGPS);

		// GPSindicator
		imgGPS = new ImageView(this);
		imgGPS.setId(GPS_INDICATOR_ID);
		imgGPS.setBackgroundResource( R.drawable.gps_bad );
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
		componentContainer.addView(imgGPS);

		// history button
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
		componentContainer.addView(btnHistory);
		
		// time label
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
		componentContainer.addView(txtTime);
		
		// distance
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
		txtTime.setLayoutParams(rlTxtTime);
		txtDistance.setLayoutParams(rlTxtDistance);
		txtDistance.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		//txtDistance.setText("42.5353 km");
		txtDistance.setSingleLine();
		txtDistance.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		txtDistance.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
		componentContainer.addView(txtDistance);

		// speed
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
		componentContainer.addView(txtSpeed);

		// speed
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
		componentContainer.addView(txtSpeed2);
		
		// cancel?
		//btnCancel = new ImageButton(this);
		
		if( mode == eMode.MODE_NORMAL )
		{
			// TODO: �����Ɛ��䂷��
			txtTime.setVisibility(View.GONE);
			txtDistance.setVisibility(View.GONE);
			txtSpeed.setVisibility(View.GONE);
			txtSpeed2.setVisibility(View.GONE);
			btnCenter.setEnabled(false);
		}
		else if( mode == eMode.MODE_MEASURING )
		{
			txtDistance.setVisibility(View.VISIBLE);
			txtSpeed.setVisibility(View.VISIBLE);
			txtSpeed2.setVisibility(View.VISIBLE);
			txtTime.setVisibility(View.VISIBLE);			
		}
		
		
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
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// どうやら、宛てにならないようなので、廃止する
//	     switch (status) {
//	     case LocationProvider.AVAILABLE:
//	    	 Log.v("Status", "AVAILABLE");
//	    	 //bGPSCanUse = true;
//	    	 btnCenter.setEnabled(true);
//	    	 imgGPS.setBackgroundResource(R.drawable.gps_good);
//	         break;
//	     case LocationProvider.OUT_OF_SERVICE:
//	    	 Log.v("Status", "OUT_OF_SERVICE");
//	    	 //bGPSCanUse = false;
//	    	 if( mode == eMode.MODE_NORMAL )
//	    		 btnCenter.setEnabled(false);
//	    	 imgGPS.setBackgroundResource(R.drawable.gps_bad);
//	    	 break;
//	     case LocationProvider.TEMPORARILY_UNAVAILABLE:
//	    	 Log.v("Status", "TEMPORARILY_UNAVAILABLE");
//	    	 //bGPSCanUse = false;
//	    	 if( mode == eMode.MODE_NORMAL )
//	    		 btnCenter.setEnabled(false);	    	 
//	    	 //btnCenter.setEnabled(false);
//	    	 imgGPS.setBackgroundResource(R.drawable.gps_soso);
//	    	 break;
//	     }		
	}
	

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
				// Toast.makeText(getApplicationContext(), R.string.GPS_ON, Toast.LENGTH_LONG).show();
				initGPS();
			}
		}
		else if( v == btnHistory )
		{
			// launch activity for save
			Intent intent = new Intent( this, HistoryActivity.class );
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		 
	        startActivity(intent);			
		}
		else if( v == btnCenter )
		{
			// TODO:cliping not button region
			if( mode == eMode.MODE_NORMAL )
			{
				btnCenter.setBackgroundResource(R.drawable.selector_runstop_button_image);
				Date now = new Date();
				long time = now.getTime();
			    if(mTimer == null){
			        timerTask = new UpdateTimeDisplayTask();
			        mTimer = new Timer(true);
			        mTimer.scheduleAtFixedRate( timerTask, 1000, 1000);
			    }				
				ResourceAccessor.getInstance().createLogStocker(time);
				
				txtDistance.setText( LapData.createDistanceFormatText( 0 ) );
				txtTime.setText( LapData.createTimeFormatText( 0 ) );
				txtSpeed.setText( LapData.createSpeedFormatText( 0 ) );
				txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( 0 ) );
				
				txtDistance.setVisibility(View.VISIBLE);
				txtSpeed.setVisibility(View.VISIBLE);
				txtSpeed2.setVisibility(View.VISIBLE);
				txtTime.setVisibility(View.VISIBLE);
				mode = eMode.MODE_MEASURING;
			}
			else if( mode == eMode.MODE_MEASURING )
			{
				// �I��
	            if(mTimer != null){
	                mTimer.cancel();
	                mTimer = null;
	            }		
				ResourceAccessor.getInstance().getLogStocker().stop(new Date().getTime());
				// launch activity for save
				Intent intent = new Intent( this, ResultActivity.class );
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		 
		        startActivity(intent);				
				
				btnCenter.setBackgroundResource(R.drawable.selector_runstart_button_image);
//				txtDistance.setVisibility(View.VISIBLE);
//				txtSpeed.setVisibility(View.VISIBLE);
//				txtTime.setVisibility(View.VISIBLE);
				mode = eMode.MODE_NORMAL;
				clearGPS();
			}
		}
	}

	@Override
	public void clearGPS() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
	}

}
