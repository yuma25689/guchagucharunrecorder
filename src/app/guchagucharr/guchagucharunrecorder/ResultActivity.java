package app.guchagucharr.guchagucharunrecorder;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
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
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.LapData;

public class ResultActivity extends Activity implements IPageViewController, OnClickListener {

	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;   // �r���[�y�[�W���[	
	private ViewGroup componentContainer;
	private PagerHandler handler;
	private LayoutInflater inflater = null;
	
	//private RunningLogStocker runLogStocker;
	private final int RESULT_PAGE_NORMAL = 0;
	private final int RESULT_PAGE_LAP = 1;
	
	ImageButton btnCenter = null;
	ImageButton btnGPS = null;
	ImageView imgGPS = null;
	TextView txtTime = null;
	TextView txtDistance = null;
	TextView txtSpeed = null;
	TextView txtSpeed2 = null;
	ImageButton btnCancel = null;
	TextView txtLap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_viewpager_only);
		
        handler = new PagerHandler( this, this );
        componentContainer = (ViewGroup) findViewById(R.id.viewpager1);

	    inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
	}

	@Override
    protected void onResume() {
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
        this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        this.mViewPager.setAdapter(new ResultPagerAdapter(this, this));
        
        return 0;
	}
	@Override
	public int initControls( int position, RelativeLayout rl )
	{
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
					bmpoptions.outWidth, bmpoptions.outHeight, true );
			rlBtnCenter.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rlBtnCenter.addRule(RelativeLayout.CENTER_VERTICAL);
			btnCenter.setLayoutParams(rlBtnCenter);
			btnCenter.setScaleType(ScaleType.FIT_XY);
			btnCenter.setOnClickListener(this);
			rl.addView(btnCenter);
			
			btnGPS = new ImageButton(this);
			btnGPS.setId(GPS_BUTTON_ID);
			btnGPS.setBackgroundResource( R.drawable.selector_gps_button_image );
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.main_gpsbutton_normal);
			RelativeLayout.LayoutParams rlBtnGps 
			= dispInfo.createLayoutParamForNoPosOnBk( 
					bmpoptions.outWidth, bmpoptions.outHeight, true );
			rlBtnGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
			rlBtnGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;		
			rlBtnGps.addRule(RelativeLayout.ABOVE, GPS_INDICATOR_ID);
			
			btnGPS.setLayoutParams(rlBtnGps);
			btnGPS.setScaleType(ScaleType.FIT_XY);
			btnGPS.setOnClickListener(this);
			rl.addView(btnGPS);
	
			imgGPS = new ImageView(this);
			imgGPS.setId(GPS_INDICATOR_ID);
			imgGPS.setBackgroundResource( R.drawable.gps_bad );
			bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(R.drawable.gps_bad);
			RelativeLayout.LayoutParams rlIndGps 
			= dispInfo.createLayoutParamForNoPosOnBk( 
					bmpoptions.outWidth, bmpoptions.outHeight, true );
			rlIndGps.addRule(RelativeLayout.RIGHT_OF, CENTER_BUTTON_ID );
			rlIndGps.leftMargin = RIGHT_CENTER_CTRL_MARGIN;
			rlIndGps.addRule(RelativeLayout.CENTER_VERTICAL );
			imgGPS.setLayoutParams(rlIndGps);
			imgGPS.setScaleType(ScaleType.FIT_XY);
			rl.addView(imgGPS);
	
			txtTime = new TextView(this);
			txtTime.setId(TIME_TXT_ID);
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
			rl.addView(txtSpeed);
			
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
			rl.addView(txtSpeed2);
			
			// lap label
			txtLap = new TextView(this);
			RelativeLayout.LayoutParams rlTxtLap
			= dispInfo.createLayoutParamForNoPosOnBk( 
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
			rlTxtLap.addRule(RelativeLayout.ABOVE,TIME_TXT_ID);
			rlTxtLap.addRule(RelativeLayout.CENTER_HORIZONTAL);
			txtLap.setLayoutParams(rlTxtLap);
			txtLap.setBackgroundColor(ResourceAccessor.getInstance().getColor(
					R.color.theme_color_cantedit));
			txtLap.setTextSize(LAP_TEXTVIEW_FONT_SIZE);
			txtLap.setSingleLine();
			txtLap.setTextColor(ResourceAccessor.getInstance().getColor(R.color.text_color_important));		
			rl.addView(txtLap);
			
			// �L�����Z���H
			//btnCancel = new ImageButton(this);
			
			// TODO: ���Ԃ̕\��
			txtDistance.setText( LapData.createDistanceFormatText( 
					ResourceAccessor.getInstance().getLogStocker().getTotalDistance() ) );
			txtTime.setText( LapData.createTimeFormatText(
					ResourceAccessor.getInstance().getLogStocker().getTotalTime() ) );
			txtSpeed.setText( LapData.createSpeedFormatText( 
					ResourceAccessor.getInstance().getLogStocker().getTotalDistance() 
					/ ResourceAccessor.getInstance().getLogStocker().getTotalTime() ) );
					// ResourceAccessor.getInstance().getLogStocker().getTotalSpeed() ) );
			txtSpeed2.setText( LapData.createSpeedFormatTextKmPerH( 
					ResourceAccessor.getInstance().getLogStocker().getTotalDistance() 
					/ ResourceAccessor.getInstance().getLogStocker().getTotalTime() ) );
					//ResourceAccessor.getInstance().getLogStocker().getTotalSpeed() ) );
			if( 1 < ResourceAccessor.getInstance().getLogStocker().getStockedLapCount() )
			{
				txtLap.setText(getString(R.string.LAP_COUNT_LABEL) 
					+ ResourceAccessor.getInstance().getLogStocker().getStockedLapCount() );
			}
			else
			{
				txtLap.setVisibility(View.GONE);
			}
			
		}
		else if( position == RESULT_PAGE_LAP )
		{
			final int BLOCK_PADDING = 3;
			// 時間
			// 距離
			// 速度
			// 上記３つOr２つを縦に並べたものを1ブロックにする
			// とりあえずブロックの大きさは、文字が全て入るコントロールの最小の大きさとする
			int iContentCount = ResourceAccessor.getInstance().getLogStocker().getStockedLapCount();
			final int CONTENT_COUNT_ONE = 1;
			final int CONTENT_COUNT_SOSO = 5;
			final int CONTENT_COUNT_MANY = 10;
			int iWidth = 0;
			int iHeight = 0;
			if( dispInfo.isPortrait() )
			{
				iWidth = ControlDefs.APP_BASE_WIDTH;
				iHeight = ControlDefs.APP_BASE_HEIGHT;
				inflater.inflate( R.layout.page_vscrollable, rl );
				RelativeLayout rlContent = (RelativeLayout) rl.findViewById( R.id.page_content );
				// 数によって、並べ方を変える
				if( iContentCount == CONTENT_COUNT_ONE )
				{
					// 地図も表示してみる？
					// それに加えて、キロごとのデータも？
				}
				else if( CONTENT_COUNT_MANY <= iContentCount )
				{
					int iEvenLineLeftPadding = 0;
					int iOddLineLeftPadding = 30;
					// ->ラップではあまりない？
					// スクロールビューなので、何も考えずに全てのブロックを積む
					// ひょっとしたらパフォーマンスの問題が出るかもしれない
					// 不規則な大きさのブロックで組んだようなレイアウトにする
					int iYearBlockWidth = 50;
					int iYearBlockHeight = 50;
					
				}
				
				
			}
			else
			{
				iWidth = ControlDefs.APP_BASE_HEIGHT;
				iHeight = ControlDefs.APP_BASE_WIDTH;
				
				inflater.inflate( R.layout.page_hscrollable, rl );
				RelativeLayout rlContent = (RelativeLayout) rl.findViewById( R.id.page_content );				
			}
			rl.setBackgroundColor(Color.WHITE);
		}
		return ret;
	}
    

	@Override
	public void onClick(View v) {
		if( v == btnGPS )
		{
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
			
			// TODO: arg2 -> GPX save setting create 
			ResourceAccessor.getInstance().getLogStocker().save(this,true);
			
		}
	}

	@Override
	public DisplayInfo getDispInfo() {
		return dispInfo;
	}
}
