package app.guchagucharr.guchagucharunrecorder;

import java.util.Date;

import com.example.guchagucharunrecorder.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.util.SystemUiHider;
import app.guchagucharr.interfaces.IViewController;
import app.guchagucharr.service.RunningLogStocker;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements LocationListener,IViewController, OnClickListener {

	public static DisplayInfo dispInfo = DisplayInfo.getInstance();	
	private RelativeLayout componentContainer;
	private LocationManager mLocationManager;
	private MainHandler handler;
	private RunningLogStocker runLogStocker;
	// private ResourceAccessor res;
	
	// �R���g���[��
	// �����̃{�^��
	ImageButton btnCenter = null;
	// GPS�{�^��
	ImageButton btnGPS = null;
	// GPS�C���W�P�[�^
	ImageView imgGPS = null;
	// �����{�^��
	ImageButton btnHistory = null;
	// �����͉B��
	// ���ԕ\�����x��
	TextView txtTime = null;
	// ����
	TextView txtDistance = null;
	// ���x
	TextView txtSpeed = null;
	// �L�����Z���H
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
        // ���C�A�E�g�̎擾
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
		
		// GPS�̐ݒ�
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
        // handler�N���X�쐬
        handler = new MainHandler( this, this );
        // ���\�[�X�A�N�Z�b�T�̍쐬
        ResourceAccessor.CreateInstance(this);
        // res = ResourceAccessor.getInstance();
		
		// GPS����Ȃ̂ŁAandroid�ɑI��ł��炤�K�v�͂Ȃ�
//		Criteria criteria = new Criteria();
//		    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//		    String bestProvider =
//		    	mLocationManager.getBestProvider(criteria, true);		
	}

	@Override
    protected void onResume() {
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
//                LocationManager.NETWORK_PROVIDER,
                0,
                0,
                this);
        }
    	// ��ʂ̃T�C�Y���̏����X�V����
		// �I�������handler�b�Z�[�W��������
		// ���݁A�����ŏ��߂ĉ�ʈʒu�̏��������s���Ă���
        dispInfo.init(this, componentContainer, handler,false);
                
        super.onResume();
    }	
	@Override
	protected void onPause()
	{
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
        super.onPause();		
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
		if( runLogStocker != null )
		{
			runLogStocker.putLocationLog(location);
		}
		Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));		
	}

	@Override
	public void onProviderDisabled(String provider) {
		imgGPS.setBackgroundResource(R.drawable.gps_bad);		
	}

	@Override
	public void onProviderEnabled(String provider) {
		imgGPS.setBackgroundResource(R.drawable.gps_good);
	}

	// �R���g���[���̏������A�z�u
	static final int CENTER_BUTTON_ID = 1000;
	static final int GPS_BUTTON_ID = 1001;
	static final int GPS_INDICATOR_ID = 1002;
	static final int DISTANCE_TEXT_ID = 1010;
	
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
	
	
	public int initControls()
	{
		int ret = 0;
		//ViewGroup contentView = ((ViewGroup)findViewById(android.R.id.content));
		BitmapFactory.Options bmpoptions = null;
		// �����̃{�^��
		btnCenter = new ImageButton(this);
		btnCenter.setId(CENTER_BUTTON_ID);
		btnCenter.setBackgroundResource( R.drawable.selector_runstart_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_runstartbutton_normal);
		RelativeLayout.LayoutParams rlBtnCenter 
		= createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
		btnCenter.setLayoutParams(rlBtnCenter);
		btnCenter.setScaleType(ScaleType.FIT_XY);
		btnCenter.setOnClickListener(this);
		componentContainer.addView(btnCenter);
		
		// GPS�{�^��
		btnGPS = new ImageButton(this);
		btnGPS.setId(GPS_BUTTON_ID);
		btnGPS.setBackgroundResource( R.drawable.selector_gps_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_gpsbutton_normal);
		RelativeLayout.LayoutParams rlBtnGps 
		= createLayoutParamForNoPosOnBk( 
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

		// GPS�C���W�P�[�^
		imgGPS = new ImageView(this);
		imgGPS.setId(GPS_INDICATOR_ID);
		imgGPS.setBackgroundResource( R.drawable.gps_bad );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.gps_bad);
		RelativeLayout.LayoutParams rlIndGps 
		= createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		//rlIndGps.addRule(RelativeLayout.BELOW, GPS_BUTTON_ID);
		//rlIndGps.addRule(RelativeLayout.CENTER_HORIZONTAL);
		rlIndGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
		rlIndGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;
		rlIndGps.addRule(RelativeLayout.CENTER_VERTICAL );
		imgGPS.setLayoutParams(rlIndGps);
		imgGPS.setScaleType(ScaleType.FIT_XY);
		componentContainer.addView(imgGPS);

		// �����{�^��
		btnHistory = new ImageButton(this);
		btnHistory.setBackgroundResource( R.drawable.selector_history_button_image );
		bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_historybutton_normal);
		RelativeLayout.LayoutParams rlBtnHistory
		= createLayoutParamForNoPosOnBk( 
				bmpoptions.outWidth, bmpoptions.outHeight, true );
		rlBtnHistory.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlBtnHistory.leftMargin = LEFT_TOP_CTRL_1_LEFT_MARGIN;
		rlBtnHistory.addRule(RelativeLayout.CENTER_VERTICAL);
		//rlBtnHistory.topMargin = RIGHT_TOP_CTRL_1_TOP_MARGIN;
		
		btnHistory.setLayoutParams(rlBtnHistory);
		btnHistory.setScaleType(ScaleType.FIT_XY);
		componentContainer.addView(btnHistory);
		
		// �����͉B��
		// ���ԕ\�����x��
		txtTime = new TextView(this);
		RelativeLayout.LayoutParams rlTxtTime
		= createLayoutParamForNoPosOnBk( 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
		rlTxtTime.addRule(RelativeLayout.ABOVE, CENTER_BUTTON_ID);
		rlTxtTime.bottomMargin = CENTER_ABOVE_CTRL_MARGIN;
		rlTxtTime.addRule(RelativeLayout.CENTER_HORIZONTAL);
		txtTime.setLayoutParams(rlTxtTime);
		txtTime.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
		txtTime.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));
		txtTime.setSingleLine();
		txtTime.setText("99:99:99.999");
		txtTime.setTextSize(TIME_TEXTVIEW_FONT_SIZE);		
		componentContainer.addView(txtTime);
		
		// ����
		txtDistance = new TextView(this);
		txtDistance.setId(DISTANCE_TEXT_ID);
		RelativeLayout.LayoutParams rlTxtDistance
		= createLayoutParamForNoPosOnBk( 
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
		txtDistance.setText("42.5353 km");
		txtDistance.setSingleLine();
		txtDistance.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		txtDistance.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
		componentContainer.addView(txtDistance);

		// ���x
		txtSpeed = new TextView(this);
		RelativeLayout.LayoutParams rlTxtSpeed
		= createLayoutParamForNoPosOnBk( 
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
		txtSpeed.setText("12.5 km/h");
		txtSpeed.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
		componentContainer.addView(txtSpeed);
		
		// �L�����Z���H
		//btnCancel = new ImageButton(this);
		
		// TODO: �����Ɛ��䂷��
		txtTime.setVisibility(View.GONE);
		txtDistance.setVisibility(View.GONE);
		txtSpeed.setVisibility(View.GONE);

		btnCenter.setEnabled(false);
		btnHistory.setEnabled(false);
		
		return ret;
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	     switch (status) {
	     case LocationProvider.AVAILABLE:
	    	 Log.v("Status", "AVAILABLE");
	    	 imgGPS.setBackgroundResource(R.drawable.gps_good);
	         break;
	     case LocationProvider.OUT_OF_SERVICE:
	    	 Log.v("Status", "OUT_OF_SERVICE");
	    	 imgGPS.setBackgroundResource(R.drawable.gps_bad);
	    	 break;
	     case LocationProvider.TEMPORARILY_UNAVAILABLE:
	    	 Log.v("Status", "TEMPORARILY_UNAVAILABLE");
	    	 imgGPS.setBackgroundResource(R.drawable.gps_soso);
	    	 break;
	     }		
	}
	
///////////////////////////// ���[�e�B���e�B�֐�
    /**
     * �x�[�X�摜��ł̐�΍��W���w�肵���ʒu��\��LayoutParam���쐬����
     * ���A������FILL_PARENT
     * @param left
     * @param top
     * @return LayoutParam
     */
	public static RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
			int left, int top )
	{
		// �w�肳�ꂽ���ʒu�ɑ΂��āA�f�B�X�v���C�T�C�Y���l�������������s��
		int xCorrect = dispInfo.getCorrectionXConsiderDensity(left);
		int yCorrect = dispInfo.getCorrectionYConsiderDensity(top);
		
		// ���ƍ����̎w�肪�Ȃ��̂ŁA�e�𖄂߂�悤�ɐݒ肷��
		RelativeLayout.LayoutParams lp = 
				new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A�������̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == dispInfo.isPortrait() )
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
	public static RelativeLayout.LayoutParams 
	createLayoutParamForAbsolutePosOnBk(
			int left, int top, int width, int height )
	{
		return createLayoutParamForAbsolutePosOnBk(left, top, width, height, true );
	}
	
	public static RelativeLayout.LayoutParams 
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
			widthCorrect = dispInfo.getCorrectionXConsiderDensity(width);
		}
		int heightCorrect = 0;
		if( height == RelativeLayout.LayoutParams.MATCH_PARENT
		|| height == RelativeLayout.LayoutParams.WRAP_CONTENT )
		{
			heightCorrect = height;
		}
		else
		{
			heightCorrect = dispInfo.getCorrectionYConsiderDensity(height);
		}
		int xCorrect = 0;
			xCorrect = dispInfo.getCorrectionXConsiderDensity(left);
//		}
//		else
//		{
//			xCorrect = dispInfo.getCorrectionYConsiderDensity(top);			
//		}
		int yCorrect = 0;
		int topRule = RelativeLayout.ALIGN_PARENT_TOP;
//		if(dispInfo.isPortrait())
//		{		
			yCorrect = dispInfo.getCorrectionYConsiderDensity(top);
//		}
//		else
//		{
//			yCorrect = dispInfo.getCorrectionXConsiderDensity(left);
//		}

		if( yCorrect < 0 )
		{
			yCorrect = -1 * dispInfo.getCorrectionYConsiderDensity(top);
			topRule = RelativeLayout.ALIGN_PARENT_BOTTOM;
		}
//		else
//		{
//			yCorrect = dispInfo.getCorrectionYConsiderDensity(top);
//		}
		
		RelativeLayout.LayoutParams lp = null;

		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A�������̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == dispInfo.isPortrait() || bConvertPortraitAndHorz == false )
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
	public static RelativeLayout.LayoutParams createLayoutParamForNoPosOnBk(
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
			widthCorrect = dispInfo.getCorrectionXConsiderDensity(width);
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
			heightCorrect = dispInfo.getCorrectionYConsiderDensity(height);
			Log.i("height convert", height +"=>" + heightCorrect); 
		}
		
		RelativeLayout.LayoutParams lp = null;

		// �����ŁA�c���̕ϊ������܂�
		// �\�[�X�R�[�h�ɏ����Ă�����W�A�傫���͏c�p�̂��̂������A�������̏ꍇ�A���p�ɕϊ����č��W��Ԃ�		
		if( true == dispInfo.isPortrait() || bConvertPortraitAndHorz == false )
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

	@Override
	public void onClick(View v) {
		if( v == btnGPS )
		{
			String providers = Settings.Secure.getString(
					getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			Log.v("GPS", "Location Providers = " + providers);
			if(providers.indexOf("gps", 0) < 0) {
				// �ݒ��ʂ̌ďo��
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), R.string.GPS_ON, Toast.LENGTH_LONG).show();
			}
		}
		else if( v == btnCenter )
		{
			Date now = new Date();
			runLogStocker = new RunningLogStocker(now.getTime());
		}
	}
}
