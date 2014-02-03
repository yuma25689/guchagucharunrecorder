package app.guchagucharr.guchagucharunrecorder;

import java.util.Vector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
//import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
//import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.util.SystemUiHider;
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.FileOutputProcessor;
import app.guchagucharr.service.LapData;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class ResultActivity extends Activity implements IPageViewController, OnClickListener {

	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;   // �r���[�y�[�W���[	
	private ViewGroup componentContainer;
	private PagerHandler handler;
	//private RunningLogStocker runLogStocker;
	private final int RESULT_PAGE_NORMAL = 0;
	private final int RESULT_PAGE_LAP = 1;
	
	// �R���g���[��
	// �����̃{�^��
	ImageButton btnCenter = null;
	// GPS�{�^��
	ImageButton btnGPS = null;
	// GPS�C���W�P�[�^
	ImageView imgGPS = null;
	// �����{�^��
	ImageButton btnHistory = null;
	// ����͉B��
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

		setContentView(R.layout.activity_viewpager);
		
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
        // handler�N���X�쐬
        handler = new PagerHandler( this, this );
        // ���C�A�E�g�̎擾
        componentContainer = (ViewGroup) findViewById(R.id.viewpager1);
		final View contentView = componentContainer;//findViewById(R.id.main_content);
	    // �y�[�W���[�r���[(���̒����X���C�h���ĕς���Ă���)
        // �ݒ��dispInfo�����
        // this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        //this.mViewPager.setAdapter(new ResultPagerAdapter(this));
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
		
		
	}

	@Override
    protected void onResume() {
    	// ��ʂ̃T�C�Y���̏����X�V����
		// �I�������handler�b�Z�[�W��������
		// ���݁A�����ŏ��߂ĉ�ʈʒu�̏�����s���Ă���
        dispInfo.init(this, componentContainer, handler, false);
        super.onResume();
    }
		
	@Override
	protected void onPause()
	{
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
	

	// �R���g���[���̏���A�z�u
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
	
	
	public int initPager()
	{
	    // �y�[�W���[�r���[(���̒����X���C�h���ĕς���Ă���)
        // �ݒ��dispInfo�����
        this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        this.mViewPager.setAdapter(new ResultPagerAdapter(this, this));
        
        return 0;
	}
	public int initControls( int position, RelativeLayout rl )
	{
		int ret = 0;
		if( position == RESULT_PAGE_NORMAL )
		{
			//ViewGroup contentView = ((ViewGroup)findViewById(android.R.id.content));
			BitmapFactory.Options bmpoptions = null;
			// �����̃{�^��
			btnCenter = new ImageButton(this);
			btnCenter.setId(CENTER_BUTTON_ID);
			btnCenter.setBackgroundResource( R.drawable.selector_save_button_image );
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
					R.drawable.main_savebutton_normal);
			RelativeLayout.LayoutParams rlBtnCenter 
			= dispInfo.createLayoutParamForNoPosOnBk( 
					bmpoptions.outWidth, bmpoptions.outHeight, true );
			rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
			btnCenter.setLayoutParams(rlBtnCenter);
			btnCenter.setScaleType(ScaleType.FIT_XY);
			btnCenter.setOnClickListener(this);
			rl.addView(btnCenter);
			
			// GPS�{�^��
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
			rl.addView(btnGPS);
	
			// GPS�C���W�P�[�^
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
			rl.addView(imgGPS);
	
			// �����{�^��
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
			rl.addView(btnHistory);
			
			// ���ԕ\�����x��
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
			rl.addView(txtTime);
			
			// ����
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
			rl.addView(txtDistance);
	
			// ���x
			txtSpeed = new TextView(this);
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
			rl.addView(txtSpeed);
			
			// �L�����Z���H
			//btnCancel = new ImageButton(this);
			
			// TODO: ���Ԃ̕\��
			txtDistance.setText( LapData.createDistanceFormatText( 
					ResourceAccessor.getInstance().getLogStocker().getTotalDistance() ) );
			txtTime.setText( LapData.createTimeFormatText(
					ResourceAccessor.getInstance().getLogStocker().getTotalTime() ) );
			// ���x�̓��b�v�̒l����Ȃ��A���̎��̒l��OK
			txtSpeed.setText( LapData.createSpeedFormatText( ResourceAccessor.getInstance().getLogStocker().getTotalSpeed() ) );
			//runLogStocker.getCurrentRapData().getSpeed() ) );
			
//			txtTime.setVisibility(View.GONE);
//			txtDistance.setVisibility(View.GONE);
//			txtSpeed.setVisibility(View.GONE);
	
			btnHistory.setEnabled(false);
		}
		else if( position == RESULT_PAGE_LAP )
		{
			if( dispInfo.isPortrait() )
			{

			}
			else
			{
				
			
			}			
		}
		return ret;
	}
    

	@Override
	public void onClick(View v) {
		if( v == btnGPS )
		{
			// �ۑ���ʂł́AOFF�ɂ������񂶂�Ȃ����Ǝv����
			String providers = Settings.Secure.getString(
					getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			Log.v("GPS", "Location Providers = " + providers);
			if(providers.indexOf("gps", 0) < 0) {
				Toast.makeText(getApplicationContext(), R.string.GPS_OFF, Toast.LENGTH_LONG).show();
			} else {
				// �ݒ��ʂ̌ďo��
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		}
		else if( v == btnCenter )
		{
			// TODO:cliping
			
			// �ۑ�����
			int iRet = ResourceAccessor.getInstance().getLogStocker().save(this);
			
			// Activity�I��
			if( iRet != -1 )
			{
				finish();
			}
		}
	}
}
