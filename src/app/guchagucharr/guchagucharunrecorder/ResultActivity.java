package app.guchagucharr.guchagucharunrecorder;


import java.text.SimpleDateFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
//import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
//import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.DisplayBlock.eShapeType;
import app.guchagucharr.guchagucharunrecorder.fragments.ChooseActivityTypeDialogFragment;
import app.guchagucharr.guchagucharunrecorder.fragments.ChooseActivityTypeDialogFragment.ChooseActivityTypeCaller;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunLogger;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.TrackIconUtils;
import app.guchagucharr.service.RunLoggerService;

public class ResultActivity extends FragmentActivity //Activity 
implements IPageViewController
, ChooseActivityTypeCaller
, OnClickListener
, OnTouchListener
{
	static final int CONTEXT_MENU_EDIT_ID = 0;
	public static final String WORK_OUT_END = "workoutend";
	public static final String NO_GPS_MODE = "nogpsmode";

	private boolean m_bNoGpsMode = false;
	
	Region regionCenterBtn = null;
	Boolean bCenterBtnEnableRegionTouched = false;
	Region regionCancelBtn = null;
	Boolean bCancelBtnEnableRegionTouched = false;
	int widthTmp = 0;
	int heightTmp = 0;
	
    // 距離の単位
	int mCurrentUnit = UnitConversions.DISTANCE_UNIT_KILOMETER;
	
	// private RelativeLayout lastSubLayout = null;
	
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;	
	private ResultPagerAdapter adapter;
	private ViewGroup componentContainer;
	private PagerHandler handler;
	//private LayoutInflater inflater = null;
	static final int SUB_FIRST_PANEL_ID = 100000;
	
	//private RunningLogStocker runLogStocker;
	private final int RESULT_PAGE_NORMAL = 0;
	private final int RESULT_PAGE_LAP = 1;
	
	ImageButton btnCenter = null;
	ImageButton btnGPS = null;
	//ImageView imgGPS = null;
	EditText editName = null;
	TextView txtTime = null;
	TextView txtDistance = null;
	TextView txtSpeed = null;
	TextView txtSpeed2 = null;
	ImageButton btnCancel = null;
	TextView txtLap = null;
	ImageButton imgDetailExists = null;
	// spinnerもどき
	ImageButton activityTypeButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpager_only);
		
		if( getIntent() != null ) {
			if( getIntent().getIntExtra(ResultActivity.WORK_OUT_END,0) == 1 )
			{
				// 最初に来たフラグクリア
				getIntent().putExtra( ResultActivity.WORK_OUT_END,0 );
				// WorkOut終了時にここに来たときのみ、ユーザに音声通知
				// タイミング微妙だが、終了ボタン押下時に下のActivityでやったらうまくいかなかった・・・
				RunNotificationSoundPlayer.soundActivityFinish(getApplicationContext());
			}
			m_bNoGpsMode = getIntent().getBooleanExtra(NO_GPS_MODE, false);
		}

        handler = new PagerHandler( this, this );
        componentContainer = (ViewGroup) findViewById(R.id.viewpager1);

        // onResumeの時に呼ばれないとやばい
 	   	ViewTreeObserver viewTreeObserver = componentContainer.getViewTreeObserver();
	    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	            widthTmp = componentContainer.getWidth();
	            heightTmp = componentContainer.getHeight();
	        	LogWrapper.w("onGlobalLayout","width = "+ widthTmp + "height = " + heightTmp);
				Message msg = Message.obtain();
				msg.what = MessageDef.MSG_INIT_SIZE_GET;
				handler.sendMessage( msg );	            
	        }
	    });        
	    //inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
    protected void onResume() {
		
        // 設定をメモリに展開しておく
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		mCurrentUnit = Integer.valueOf(pref.getString(GGRRPreferenceActivity.DISTANCE_UNIT_KEY,
				String.valueOf( UnitConversions.DISTANCE_UNIT_KILOMETER ) ) );
		
        handler.clearFlags();
        widthTmp = componentContainer.getWidth();
        heightTmp = componentContainer.getHeight();
		Message msg = Message.obtain();
		msg.what = MessageDef.MSG_INIT_SIZE_GET;
		handler.sendMessage( msg );		
        dispInfo.init(this, componentContainer, handler, true);
		
        //dispInfo.init(this, componentContainer, handler, true);
        super.onResume();
    }
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//	    dispInfo.init(this, componentContainer, handler, true);	
//	}		
	@Override
	protected void onPause()
	{
        super.onPause();	
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}
    private void setActivityTypeIcon(View parent,int value) {
  	  //iconValue = value;
  	  // TrackIconUtils.setIconSpinner(activityTypeIcon, value);
  	  //activityTypeButton.setBackgroundResource(R.drawable.selector_spinner_button_image );
  	  Bitmap source = BitmapFactory.decodeResource(
  			this.getResources(),
  			TrackIconUtils.getIconDrawable(value));
	    	//activityTypeIcon.getAdapter().getItem(0).toString()));
  	  ImageButton parentButton = (ImageButton) parent;
  	  parentButton.setImageBitmap(source);
  	  parentButton.setTag(value);
  	  if( RunLogger.sService != null )
  	  {
	  	  try {
	  		  RunLogger.sService.setActivityTypeCode(value);
	  	  } catch (RemoteException e) {
	  		  e.printStackTrace();
	  		  LogWrapper.e("setIcon to service","error");
	  	  }
  	  }
    }
    
    // newWeightはこのアプリでは使っていないので、消してもOK
	@Override
	public void onChooseActivityTypeDone(View parent,int iconValue) { //, boolean newWeight) {
	    setActivityTypeIcon(parent,iconValue);
	    //activityType.setText(getString(TrackIconUtils.getIconActivityType(value)));
	    
	}

	static final int CENTER_BUTTON_ID = 1000;
	static final int GPS_BUTTON_ID = 1001;
	static final int GPS_INDICATOR_ID = 1002;
	static final int TIME_TXT_ID = 1003;
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
	
	@Override
	public int initPager()
	{
		LogWrapper.w("initPager","come");
		
		if( dispInfo.isPortrait() )
		{
			getWindow().setLayout( 
			dispInfo.getCorrectionXConsiderDensity(
				ControlDefs.APP_DIALOG_WIDTH)
			, dispInfo.getCorrectionYConsiderDensity(
				ControlDefs.APP_DIALOG_HEIGHT)
			);
		}
		else
		{
			getWindow().setLayout( 
				dispInfo.getCorrectionYConsiderDensity(
						ControlDefs.APP_DIALOG_HEIGHT)
				,dispInfo.getCorrectionXConsiderDensity(
						ControlDefs.APP_DIALOG_WIDTH)
			);			
		}
        this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        adapter = new ResultPagerAdapter(this, this);
        this.mViewPager.setAdapter(adapter);

//		getWindow().setLayout( 
//		dispInfo.getCorrectionXConsiderDensity(
//			ControlDefs.APP_DIALOG_WIDTH)
//		, dispInfo.getCorrectionYConsiderDensity(
//			ControlDefs.APP_DIALOG_HEIGHT)
//		);
        
        return 0;
	}
	@Override
	public int initControls( int position, RelativeLayout rl )
	{
		LogWrapper.w("initControls","come");
//		int width = componentContainer.getWidth();
//		int height = componentContainer.getHeight();
		
		int ret = 0;
		if( position == RESULT_PAGE_NORMAL )
		{
			rl.setBackgroundResource(R.drawable.main_background);
			BitmapFactory.Options bmpoptions = null;
			btnCenter = new ImageButton(this);
			btnCenter.setId(CENTER_BUTTON_ID);
			btnCenter.setBackgroundResource( R.drawable.selector_save_button_image );
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
					R.drawable.main_savebutton_normal);
			RelativeLayout.LayoutParams rlBtnCenter 
			= dispInfo.createLayoutParamForNoPosOnBk( 
					bmpoptions.outWidth, bmpoptions.outHeight, false );
			rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
			btnCenter.setLayoutParams(rlBtnCenter);
			btnCenter.setScaleType(ScaleType.FIT_XY);
			btnCenter.setOnClickListener(this);
			//rl.addView(btnCenter);
			addViewToCompContainer(rl,btnCenter);

			
			btnGPS = new ImageButton(this);
			btnGPS.setId(GPS_BUTTON_ID);
			btnGPS.setBackgroundResource( R.drawable.selector_gps_button_image );
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_gpsbutton_normal);
			RelativeLayout.LayoutParams rlBtnGps 
			= dispInfo.createLayoutParamForNoPosOnBk( 
					bmpoptions.outWidth, bmpoptions.outHeight, false );
			rlBtnGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
			rlBtnGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;		
			rlBtnGps.addRule(RelativeLayout.ABOVE, GPS_INDICATOR_ID);
			
			btnGPS.setLayoutParams(rlBtnGps);
			btnGPS.setScaleType(ScaleType.FIT_XY);
			btnGPS.setOnClickListener(this);
			if( m_bNoGpsMode )
			{
				btnGPS.setVisibility(View.INVISIBLE);
			}
			//rl.addView(btnGPS);
			addViewToCompContainer(rl,btnGPS);
			
	
//			imgGPS = new ImageView(this);
//			imgGPS.setId(GPS_INDICATOR_ID);
//			imgGPS.setBackgroundResource( R.drawable.gps_bad );
//			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.gps_bad);
//			RelativeLayout.LayoutParams rlIndGps 
//			= dispInfo.createLayoutParamForNoPosOnBk( 
//					bmpoptions.outWidth, bmpoptions.outHeight, true );
//			rlIndGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
//			rlIndGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;
//			rlIndGps.addRule(RelativeLayout.CENTER_VERTICAL );
//			imgGPS.setLayoutParams(rlIndGps);
//			imgGPS.setScaleType(ScaleType.FIT_XY);
//			rl.addView(imgGPS);
	
			editName = new EditText(this);
			RelativeLayout.LayoutParams rlEditName
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
			rlEditName.addRule(RelativeLayout.ALIGN_TOP);
			rlEditName.topMargin = CENTER_ABOVE_CTRL_MARGIN;
			rlEditName.addRule(RelativeLayout.CENTER_HORIZONTAL);
			editName.setLayoutParams(rlEditName);
//			editName.setBackgroundColor(
//					ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
//			editName.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));
			editName.setSingleLine();
			//txtTime.setText("99:99:99.999");
			//txtTime.setTextSize(TIME_TEXTVIEW_FONT_SIZE);		
			// rl.addView(editName);
			addViewToCompContainer(rl,editName);			
			
			
			txtTime = new TextView(this);
			txtTime.setId(TIME_TXT_ID);
			RelativeLayout.LayoutParams rlTxtTime
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
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
//			rlTxtTime.addRule(RelativeLayout.ABOVE, CENTER_BUTTON_ID);
//			rlTxtTime.bottomMargin = CENTER_ABOVE_CTRL_MARGIN;
//			rlTxtTime.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtTime.setLayoutParams(rlTxtTime);
			txtTime.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
			txtTime.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));
			txtTime.setSingleLine();
			//txtTime.setText("99:99:99.999");
			txtTime.setTextSize(TIME_TEXTVIEW_FONT_SIZE);		
			//rl.addView(txtTime);
			addViewToCompContainer(rl,txtTime);
			
			// cancel button
			if( btnCancel == null )
				btnCancel = new ImageButton(this);
			//btnCancel.setId();
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
			addViewToCompContainer(rl,btnCancel);
			
//			if( false == m_bNoGpsMode )
//			{
			
			// ����
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
	        	rlTxtDistance.addRule(RelativeLayout.BELOW, CENTER_BUTTON_ID);
	        	rlTxtDistance.topMargin = CENTER_BELOW_CTRL_MARGIN;
	        	rlTxtDistance.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        }
	        else
	        {
	        	// 横向き
	        	rlTxtDistance.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
	        	rlTxtDistance.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
	    		rlTxtDistance.addRule(RelativeLayout.CENTER_VERTICAL);
	        }
			
//			rlTxtDistance.addRule(RelativeLayout.BELOW, CENTER_BUTTON_ID);
//			rlTxtDistance.topMargin = CENTER_BELOW_CTRL_MARGIN;
//			rlTxtDistance.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtDistance.setLayoutParams(rlTxtDistance);
			txtDistance.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
			//txtDistance.setText("42.5353 km");
			txtDistance.setSingleLine();
			txtDistance.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
			txtDistance.setTextSize(DISTANCE_TEXTVIEW_FONT_SIZE);
			// rl.addView(txtDistance);
			addViewToCompContainer(rl,txtDistance);
			
	
			// ���x
			txtSpeed = new TextView(this);
			txtSpeed.setId(SPEED_TEXT_ID);
			RelativeLayout.LayoutParams rlTxtSpeed
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
	        if( true == dispInfo.isPortrait() )
	        {
	        	// 縦向き
	    		rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
	    		rlTxtSpeed.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        }
	        else
	        {
	        	// 横向き
	    		rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
	    		rlTxtSpeed.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
	    		rlTxtSpeed.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
	        }
			
//			rlTxtSpeed.addRule(RelativeLayout.BELOW, DISTANCE_TEXT_ID);
//			rlTxtSpeed.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtSpeed.setLayoutParams(rlTxtSpeed);
			txtSpeed.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
			txtSpeed.setTextSize(SPEED_TEXTVIEW_FONT_SIZE);
			txtSpeed.setSingleLine();
			//txtSpeed.setText("12.5 km/h");
			txtSpeed.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
			//rl.addView(txtSpeed);
			addViewToCompContainer(rl,txtSpeed);
	
			
			// speed
			txtSpeed2 = new TextView(this);
			RelativeLayout.LayoutParams rlTxtSpeed2
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
			
	        if( true == dispInfo.isPortrait() )
	        {
	        	// 縦向き
	    		rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
	    		rlTxtSpeed2.addRule(RelativeLayout.CENTER_HORIZONTAL);        	
	        }
	        else
	        {
	        	// 横向き
	    		rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
	    		rlTxtSpeed2.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID);
	    		rlTxtSpeed2.leftMargin = CENTER_RIGHT_CTRL_MARGIN;
	        }
			
//			rlTxtSpeed2.addRule(RelativeLayout.BELOW, SPEED_TEXT_ID);
//			rlTxtSpeed2.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtSpeed2.setLayoutParams(rlTxtSpeed2);
			txtSpeed2.setBackgroundColor(ResourceAccessor.getInstance().getColor(R.color.theme_color_cantedit));
			txtSpeed2.setTextSize(SPEED_TEXTVIEW_FONT_SIZE);
			txtSpeed2.setSingleLine();
			//txtSpeed.setText("12.5 km/h");
			txtSpeed2.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
			//rl.addView(txtSpeed2);
			addViewToCompContainer(rl,txtSpeed2);
			
			// lap label
			txtLap = new TextView(this);
			RelativeLayout.LayoutParams rlTxtLap
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
	        if( true == dispInfo.isPortrait() )
	        {
	        	// 縦向き
	    		rlTxtLap.addRule(RelativeLayout.ABOVE, TIME_TXT_ID);
	    		rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        }
	        else
	        {
	        	// 横向き
	    		rlTxtLap.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	    		rlTxtLap.topMargin = CENTER_TOP_CTRL_MARGIN;
	    		rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
	        }
//			rlTxtLap.addRule(RelativeLayout.ABOVE,TIME_TXT_ID);
//			rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtLap.setLayoutParams(rlTxtLap);
			txtLap.setBackgroundColor(ResourceAccessor.getInstance().getColor(
					R.color.theme_color_cantedit));
			txtLap.setTextSize(LAP_TEXTVIEW_FONT_SIZE);
			txtLap.setSingleLine();
			txtLap.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
			// rl.addView(txtLap);
			addViewToCompContainer(rl,txtLap);			

			// Spinnerのカスタマイズが難しいので、Spinnerのインタフェースはボタンにする
			if( activityTypeButton == null )
			{
				activityTypeButton = new ImageButton(this);
			}
			activityTypeButton.setBackgroundResource(R.drawable.selector_spinner_button_image );
		    Bitmap source = BitmapFactory.decodeResource(
			        this.getResources(),
			        TrackIconUtils.getIconDrawable(RunLoggerService.getActivityTypeCode()));
		    activityTypeButton.setImageBitmap(source);
		    // 種別はTagに設定
		    activityTypeButton.setTag(RunLoggerService.getActivityTypeCode());
			
			// activityTypeIcon.setBackgroundResource(R.drawable.selector_history_button_image );
			//activityTypeIcon.setId(GPS_INDICATOR_ID);
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
					R.drawable.main_historybutton_normal);		
//			RelativeLayout.LayoutParams rlActTypeSpn
//			= new RelativeLayout.LayoutParams(0,0);
			RelativeLayout.LayoutParams rlActType
//			= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
//					RelativeLayout.LayoutParams.WRAP_CONTENT);
			= dispInfo.createLayoutParamForNoPosOnBk(
					bmpoptions.outWidth,
					bmpoptions.outHeight, true );
			rlActType.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			rlActType.leftMargin = LEFT_TOP_CTRL_1_LEFT_MARGIN;
			rlActType.addRule(RelativeLayout.CENTER_VERTICAL);
			// activityTypeIcon.setLayoutParams(rlActTypeSpn);
			activityTypeButton.setLayoutParams(rlActType);
			activityTypeButton.setOnClickListener(new View.OnClickListener() {

			    @Override
			    public void onClick(View v) {
			        //if (event.getAction() == MotionEvent.ACTION_UP) {
			    	int iCurrentCd = RunLoggerService.getActivityTypeCode();//nActivityTypeInitIcon;
			    	try {
			    		iCurrentCd = Integer.parseInt((String)activityTypeButton.getTag());
			    	} catch( Exception ex )
			    	{
			    		iCurrentCd = RunLoggerService.getActivityTypeCode();//nActivityTypeInitIcon;
			    	}
			        	ChooseActivityTypeDialogFragment act 
			        	= ChooseActivityTypeDialogFragment.newInstance(
			        		  //activityType.getText().toString()
			        			activityTypeButton,
			        			iCurrentCd//nActivityTypeInitIcon
			        		  );
			        	act.show(
			        			getSupportFragmentManager(),
			        			ChooseActivityTypeDialogFragment.CHOOSE_ACTIVITY_TYPE_DIALOG_TAG);
			        //}
			    	// activityTypeIcon.performClick();
			    }
			});
			
//		    activityTypeIcon.setAdapter(TrackIconUtils.getIconSpinnerAdapter(this, nActivityTypeInitIcon));
//			addViewToCompContainer(activityTypeIcon);
			addViewToCompContainer(rl,activityTypeButton);
			
			if( m_bNoGpsMode == false )
			{
				// 画像でいいかと思ったが、押したら反応するようにする
				imgDetailExists = new ImageButton(this);
				//imgDetailExists.setId(GPS_INDICATOR_ID);
				imgDetailExists.setBackgroundResource( R.drawable.ind_detail_exist );
				bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.ind_detail_exist);
				RelativeLayout.LayoutParams rlIndDetail
				= dispInfo.createLayoutParamForNoPosOnBk( 
						bmpoptions.outWidth, bmpoptions.outHeight, true );
				rlIndDetail.leftMargin = RIGHT_CENTER_CTRL_MARGIN;
				rlIndDetail.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
				rlIndDetail.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
				imgDetailExists.setLayoutParams(rlIndDetail);
				imgDetailExists.setScaleType(ScaleType.FIT_XY);
				imgDetailExists.setOnClickListener(this);			
				rl.addView(imgDetailExists);
			}
			SimpleDateFormat sdfDateTime = new SimpleDateFormat(
					getString(R.string.datetime_display_format));
			editName.setText( 
					TrackIconUtils.getActivityTypeNameFromCode(
							this,RunLoggerService.getActivityTypeCode())
					);//getString( R.string.default_activity_name ) ); 
					//sdfDateTime.format(RunLoggerService.getLogStocker().getLapData(0).getStartTime()) 
					//+ "-" + sdfDateTime.format(RunLoggerService.getLogStocker().getLastLapData().getStopTime()));//RunLogger.sService.getTimeInMillis()));
			// NOTICE:テスト用			
			String gpsLastTime = "";
			
			if( false == m_bNoGpsMode ) {
				
				txtDistance.setText( LapData.createDistanceFormatText(
						mCurrentUnit,
						RunLoggerService.getLogStocker().getTotalDistance() ) );
				txtSpeed.setText( LapData.createSpeedFormatTextKmPerH(
						mCurrentUnit,
						RunLoggerService.getLogStocker().getTotalDistance() 
						/ RunLoggerService.getLogStocker().getTotalTime() ) );
						// RunLoggerService.getLogStocker().getTotalSpeed() ) );
//				txtSpeed2.setText( LapData.createSpeedFormatText( 
//						RunLoggerService.getLogStocker().getTotalDistance() 
//						/ RunLoggerService.getLogStocker().getTotalTime() ) );
						//RunLoggerService.getLogStocker().getTotalSpeed() ) );
				gpsLastTime = LapData.createTimeFormatText(
						RunLoggerService.getLogStocker().getCurrentLocation().getTime() 
						- RunLoggerService.getLogStocker().getLapData(0).getStartTime() );				
			}
			else
			{
				txtDistance.setText( LapData.createDistanceFormatText(
						mCurrentUnit,
						RunLoggerService.getLogStocker().getTotalDistance() ) );
				txtSpeed.setText( LapData.createSpeedFormatTextKmPerH(
						mCurrentUnit,
						RunLoggerService.getLogStocker().getTotalDistance() 
						/ RunLoggerService.getLogStocker().getTotalTime() ) );
				
			}
			
			txtTime.setText( LapData.createTimeFormatText(
					RunLoggerService.getLogStocker().getTotalTime() ) + " " + gpsLastTime );
			
			if( 1 < RunLoggerService.getLogStocker().getStockedLapCount() )
			{
				txtLap.setText(getString(R.string.LAP_COUNT_LABEL) 
					+ RunLoggerService.getLogStocker().getStockedLapCount() );
			}
			else
			{
				txtLap.setVisibility(View.GONE);
			}
		}
		else if( position == RESULT_PAGE_LAP )
		{
			String title[] = {""};
			if( 1 < RunLoggerService.getLogStocker().getStockedLapCount() )
			{
				title = new String[RunLoggerService.getLogStocker().getStockedLapCount()];
			}			
			// NOTICE: 最高で６個しか置けない
			DisplayBlock.eSizeType sizeType = DisplayBlock.getProperSizeTypeFromCount(
					RunLoggerService.getLogStocker().getStockedLapCount());
			int lastOddPanelID = 0;
			int iPanelCount = 0;
			for( int i=0; i < RunLoggerService.getLogStocker().getStockedLapCount(); i++ )
			{
				LapData lapData = RunLoggerService.getLogStocker().getLapData(i);
				if( lapData == null )
				{
					break;
				}
				double distance = 0;
				double speed = 0;
				long time = 0;
				distance = lapData.getFixedDistance() == 0 ? 
						lapData.getDistance() : lapData.getFixedDistance();
				time = lapData.getFixedTime() == 0 ?
						lapData.getTotalTime() : lapData.getFixedTime();
				speed = distance / ( time * UnitConversions.MS_TO_S ); 
				
				// DisplayBlock追加
				if( 1 < RunLoggerService.getLogStocker().getStockedLapCount() )
				{
					title[i] = String.valueOf( i + 1 );
				}
				String text[] = {
						LapData.createDistanceFormatText( mCurrentUnit, distance ),
						LapData.createTimeFormatText( time ),
						LapData.createSpeedFormatTextKmPerH( mCurrentUnit, speed ),
				};
				DisplayBlock dispBlock = new DisplayBlock(
						this,
						dispInfo.getXNotConsiderDensity(componentContainer.getWidth()),
						dispInfo.getYNotConsiderDensity(componentContainer.getHeight()),
						i,
						dispInfo, title, text, 
						// null,
						lapData.getGpxFilePath(),	
						// TODO: ここから別アクティビティに飛べるようになってしまうが、そうすると保存忘れのリスクは上がる
						// できるだけ保存忘れが発生しないようにすること
						sizeType, eShapeType.SHAPE_HORIZONTAL);
				if( iPanelCount == 0 )
				{
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dispBlock.getLayoutParams();
					lp.addRule(RelativeLayout.ALIGN_LEFT);
					lp.addRule(RelativeLayout.ALIGN_TOP);
					dispBlock.setId(SUB_FIRST_PANEL_ID);
					lastOddPanelID = dispBlock.getId();
				}
				else
				{
					dispBlock.setId(SUB_FIRST_PANEL_ID+iPanelCount);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dispBlock.getLayoutParams();
					if( lastOddPanelID != 0 )
					{
						lp.addRule(RelativeLayout.BELOW, lastOddPanelID);
					}
					lastOddPanelID = dispBlock.getId();
				}
				// 下に行くほど薄くする
				final double COLOR_RANGE = 80;
				double rate = COLOR_RANGE / RunLoggerService.getLogStocker().getStockedLapCount();
				int iMinus = (int) (iPanelCount * rate);
				dispBlock.setBackgroundColorAsStateList(0xFF, 20 + iMinus, 70 + iMinus, 0 );
				//rl.addView(dispBlock);
				addViewToCompContainer(rl,dispBlock);
				
				iPanelCount++;
			}
			//lastSubLayout = rl;			
		}
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
				String providers = Settings.Secure.getString(
						getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				LogWrapper.v("GPS", "Location Providers = " + providers);
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
				
				// TODO: arg2 -> GPX save setting create 
				RunLoggerService.getLogStocker().save(this,editName.getText().toString(),true);
				
			}
			else if( v == btnCancel )
			{
				// 保存せずに終了する
				// TODO: 確認ダイアログ
				finish();
			}
			else if( v == imgDetailExists )
			{
//				if( adapter.getCount() == 1 )
//				{
//					adapter.setCount(2);
//				}
//				else if( adapter.getCount() == 2 )
//				{
//					if( null != lastSubLayout )
//					{
//						initControls(RESULT_PAGE_LAP,lastSubLayout);
//					}
//				}				
				mViewPager.arrowScroll(View.FOCUS_RIGHT);
				LogWrapper.v("imgDetail","click");
			}
		} finally {
			if( v != null )
			{
				if( btnGPS == v )
				{
					v.setEnabled(true);
				}
			}
		}
	}

	@Override
	public DisplayInfo getDispInfo() {
		return dispInfo;
	}
	private void addViewToCompContainer( RelativeLayout rl, View v )
	{
		if( v.getParent() != null )
		{	
			((ViewGroup)v.getParent()).removeView(v);
		}
		rl.addView(v);		
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Region region = null;
		//Boolean bRegionFlag = null;
		if( v == btnCenter 
		|| v == btnCancel )
		{
			if( btnCenter == v )
			{
				region = regionCenterBtn;
				//bRegionFlag = bCenterBtnEnableRegionTouched;
 
			}
			else if( v == btnCancel )
			{
				region = regionCancelBtn;
				//bRegionFlag = bCancelBtnEnableRegionTouched;
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
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	 
	    super.onCreateContextMenu(menu, v, menuInfo);
	 
	    //コンテキストメニューの設定
	    DisplayBlock block = (DisplayBlock) v;
	    menu.setHeaderTitle(block.getTitle()[0]);
	    // menu.setHeaderView
	    //menu.setHeaderIcon
	    //Menu.add(int groupId, int itemId, int order, CharSequence title)
	    //menu.add(CONTEXT_MENU_DETAIL_ID, (int)block.getRecordId(), 0, R.string.menu_detail);
	    menu.add(CONTEXT_MENU_EDIT_ID, (int)block.getRecordId(), 0, R.string.menu_edit);
	    //menu.add(CONTEXT_MENU_DELETE_ID, (int)block.getRecordId(), 0, R.string.menu_delete);
	    
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getGroupId()) {
	    case CONTEXT_MENU_EDIT_ID:
	        // 編集メニュー
			// launch activity for save
			Intent intent = new Intent( this, EditActivity.class );
			// データをそのままどこかに格納する？
			// TODO: その場合、LapDataから、テーブル用のデータに変換してから設定する
			// intent.set
			intent.putExtra(EditActivity.KEY_EDIT_MODE, EditActivity.EDIT_DATA_NOT_SAVED_DATA);
			intent.putExtra(EditActivity.KEY_LAP_INDEX, item.getItemId());
			intent.putExtra(EditActivity.KEY_CLMN_DATA_GEN, EditActivity.EDIT_DATA_LAP_TABLE);
			// TODO:編集データ識別子の設定 向こう側で取得できれば何でもいいが、案外悩みどころ
			// intent.putExtra(EditActivity.KEY_CLMN_DATA_INDEX, item.getItemId());
			ActivityLapData lapData4Edit = new ActivityLapData();
			LapData lapData = RunLoggerService.getLogStocker().getLapData(item.getItemId());
			lapData4Edit.valueOf( lapData );
			ResourceAccessor.getInstance().setLapDataTmp(lapData4Edit);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intent);
	    	return true;
	    default:
	        return super.onContextItemSelected(item);
	    }
	}

}
