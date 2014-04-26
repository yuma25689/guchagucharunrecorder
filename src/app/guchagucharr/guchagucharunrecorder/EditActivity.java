package app.guchagucharr.guchagucharunrecorder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
//import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.res.ColorStateList;
import android.graphics.Color;
//import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
//import android.widget.DatePicker;
//import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.TimePicker;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.guchagucharunrecorder.util.MyDatePicker;
import app.guchagucharr.guchagucharunrecorder.util.MyTimePicker;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.interfaces.IEditViewController;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunningLogStocker;

public class EditActivity extends Activity 
implements IEditViewController, OnClickListener, OnTouchListener
{
	// タグのID
	// --> write in tags.xml
	//private static final int TEXT_VIEW_LINKED = 1000;
	Calendar mInputDate = null;
	TextView mLastInputDateTimeLabel = null;
	
	// 項目のID
	static final int VALUE_INPUT_CONTROL_ID = 100;
	int iValueInputControlID = VALUE_INPUT_CONTROL_ID;
	
	public static final String KEY_CLMN_DATA_GEN = "KeyOfColumnDataGenerator";
	// データは、ResourceAccessorから取得するものとするので、データのインデックスは不要
	//public static final String KEY_CLMN_DATA_INDEX = "KeyOfColumnDataIndex";
	public static final int EDIT_DATA_NONE = -1;
	public static final int EDIT_DATA_MAIN_TABLE = 1;
	public static final int EDIT_DATA_LAP_TABLE = 2;
	private int iEditDataType = EDIT_DATA_LAP_TABLE;
	private static final int LABEL_PADDING_HORZ = 3;
	// private int iEditDataIndex = -1;
	private IColumnDataGenerator dataGen = null;
	private ColumnData[] clmnInfos = null;
	
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	//private ViewGroup componentContainer;
	private RelativeLayout componentContainer;
	private EditHandler handler;
	private ImageButton btnSave;
	private ImageButton btnCancel;
	
	Region regionSaveBtn = null;
	Boolean bSaveBtnEnableRegionTouched = false;
	Region regionCancelBtn = null;
	Boolean bCancelBtnEnableRegionTouched = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);

		// TODO: ボタンが小さいので、大きくすること。
		btnSave = (ImageButton) findViewById(R.id.edit_savebutton);
		btnSave.setOnClickListener(this);
		btnCancel = (ImageButton) findViewById(R.id.edit_cancelbutton);
		btnCancel.setOnClickListener(this);
		handler = new EditHandler(this,this);
        componentContainer = (RelativeLayout) findViewById(R.id.page_content);
        componentContainer.setBackgroundColor(Color.GREEN);

        Intent intent = getIntent();
        if( intent == null )
        {
        	// TODO: 設定情報が取得できない時点でエラーなので、起動させたくない
        	finish();
        }
        else
        {
        	// 設定情報の取得
        	// 投げる側に、データジェネレータを設定してもらう
        	iEditDataType = intent.getIntExtra(KEY_CLMN_DATA_GEN, EDIT_DATA_NONE);
        	// iEditDataIndex = intent.getIntExtra(KEY_CLMN_DATA_INDEX, -1);
        }
        if( iEditDataType == EDIT_DATA_NONE ) 
        //|| iEditDataIndex == -1 )
        {
        	// TODO: 設定情報が取得できない時点でエラーなので、起動させたくない
        	Toast.makeText(this, R.string.edit_error_cant_launch, Toast.LENGTH_LONG).show();
        	finish();
        }
        else if( iEditDataType == EDIT_DATA_MAIN_TABLE )
        {
        	dataGen = new LapColumnDataGenerator();
        	clmnInfos = dataGen.generate( this, ResourceAccessor.getInstance().getWorkOutDataTmp() );        	
        }
        else if( iEditDataType == EDIT_DATA_LAP_TABLE )
        {
        	dataGen = new LapColumnDataGenerator();
        	clmnInfos = dataGen.generate( this, ResourceAccessor.getInstance().getLapDataTmp() );
        }
	}

	@Override
    protected void onResume() {
        dispInfo.init(this, componentContainer, handler, true);
        super.onResume();
    }
	@Override
	protected void onPause()
	{
        super.onPause();	
	}
	int init()
	{
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
        
		LinearLayout llContent = new LinearLayout(this);
		// NOTICE: 今のところ、縦にならべる固定
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT ));
		// NOTICE:今のところ、種別で分ける必要なし
//		if( iEditDataType == EDIT_DATA_NONE )
//        {
//			
//        }
//		else if( iEditDataType == EDIT_DATA_MAIN_TABLE )
//		{
//		}
//		else if( iEditDataType == EDIT_DATA_LAP_TABLE )
//		{
//			// ラップテーブル編集
//			// データの取得
//			for( ColumnData clmn : clmnInfos )
//			{
//			}			
//		}
		// メインテーブル編集
		for( ColumnData clmn : clmnInfos )
		{
			if( clmn.isHidden())
			{
				// 隠しカラムの場合は、表示する必要なし
				continue;
			}
			// とりあえず、１カラムにつき１LinearLayoutで
			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			
			LinearLayout.LayoutParams llForLabel = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			LinearLayout.LayoutParams llForContent = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			llForContent.weight = 1;
			// TODO: 各コントロールの配置
			if( null != clmn.getLabelBefore() )
			{
				TextView lblBefore = new TextView(this);
				lblBefore.setText( clmn.getLabelBefore());
				
				lblBefore.setLayoutParams(llForLabel);			
				// TODO: 背景色、文字色、フォントサイズ等の設定
				lblBefore.setPadding(LABEL_PADDING_HORZ, LABEL_PADDING_HORZ, 
						LABEL_PADDING_HORZ, LABEL_PADDING_HORZ);
				
				// TODO:背景色:青
				lblBefore.setBackgroundColor(Color.CYAN);
				lblBefore.setTextColor(Color.BLACK);
				ll.addView(lblBefore);
			}
			// contents
			iValueInputControlID++;
			clmn.setItemValueControlID(iValueInputControlID);
			if( clmn.isEditable() )
			{
				if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
				{
					// 日時型の場合
					if( clmn.getText() != null && 0 < clmn.getText().length() )
					{
						TextView lblText = new TextView(this);
						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
								getString(R.string.datetime_display_format));
						lblText.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
						lblText.setLayoutParams(llForContent);
						lblText.setBackgroundColor(Color.DKGRAY);
						lblText.setTextColor(Color.WHITE);
						lblText.setId(iValueInputControlID);
						ll.addView(lblText);

						Button btnDate = new Button(this);
						btnDate.setBackgroundResource(R.drawable.selector_edit_button_image);
						// OnClickListenerでどの項目か識別できるように、タグを設定
						btnDate.setTag(clmn);
						btnDate.setTag(R.id.TEXT_VIEW_LINKED, lblText);
						//btnDate.setText(R.string.date_button_caption);
						btnDate.setOnClickListener(this);
						ll.addView(btnDate);
					}
				}
				// 時間型
				else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_TIME )
				{
					// 時間型の場合
					if( clmn.getText() != null && 0 < clmn.getText().length() )
					{
						TextView lblText = new TextView(this);
						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
								getString(R.string.time_display_format));
						lblText.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
						lblText.setLayoutParams(llForContent);
						lblText.setBackgroundColor(Color.DKGRAY);
						lblText.setTextColor(Color.WHITE);
						lblText.setId(iValueInputControlID);						
						ll.addView(lblText);

						Button btnDate = new Button(this);
						// OnClickListenerでどの項目か識別できるように、タグを設定
						btnDate.setTag(clmn);
						btnDate.setTag(R.id.TEXT_VIEW_LINKED, lblText);
						btnDate.setBackgroundResource(R.drawable.selector_edit_button_image);						
						//btnDate.setText(R.string.date_button_caption);
						btnDate.setOnClickListener(this);
						
						ll.addView(btnDate);
					}
				}
				else
				{
					EditText edt = new EditText(this);
					if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_INTEGER 
					|| clmn.getEditMethod() == ColumnData.EDIT_METHDO_REAL )
					{
						edt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
						edt.setText( clmn.getText());
					}
//					else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
//					{
//						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//								getString(R.string.datetime_display_format));
//						edt.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
//						edt.setInputType(EditorInfo.TYPE_CLASS_DATETIME
//								|EditorInfo.TYPE_DATETIME_VARIATION_NORMAL);
//					}
//					else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_TIME )
//					{
//						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//								getString(R.string.time_display_format));
//						edt.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
//						edt.setInputType(EditorInfo.TYPE_CLASS_DATETIME
//								|EditorInfo.TYPE_DATETIME_VARIATION_TIME);
//					}
					edt.setId(iValueInputControlID);					
					edt.setLayoutParams(llForContent);				
					ll.addView(edt);
				}
			}
			else
			{
				TextView lblText = new TextView(this);
				if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
				{
					SimpleDateFormat sdfDateTime = new SimpleDateFormat(
							getString(R.string.datetime_display_format));
//					SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
//					dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));					
					try {
						lblText.setText( sdfDateTime.format(sdfDateTime.parse(clmn.getText()) ));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_TIME )
				{
					SimpleDateFormat sdfDateTime = new SimpleDateFormat(
							getString(R.string.time_display_format));
					lblText.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
				}
				else
				{
					lblText.setText( clmn.getText());
				}
				lblText.setLayoutParams(llForContent);
				lblText.setBackgroundColor(Color.DKGRAY);
				lblText.setTextColor(Color.WHITE);
				lblText.setId(iValueInputControlID);						
				
				ll.addView(lblText);				
			}
			if( null != clmn.getLabelAfter() )
			{
				TextView lblAfter = new TextView(this);
				lblAfter.setText( clmn.getLabelAfter());
				
				lblAfter.setLayoutParams(llForLabel);	
				lblAfter.setPadding(LABEL_PADDING_HORZ, LABEL_PADDING_HORZ, 
						LABEL_PADDING_HORZ, LABEL_PADDING_HORZ);
				
				// TODO:背景色:青
				lblAfter.setBackgroundColor(Color.CYAN);				
				lblAfter.setTextColor(Color.BLACK);
				// TODO: 背景色、文字色、フォントサイズ等の設定
				ll.addView(lblAfter);
			}
			
			llContent.addView(ll);
		}
		addViewToCompContainer(componentContainer, llContent);
        return 0;
	}
//	@Override
	public int initControls()
	{		
		int ret = 0;
		init();
		return ret;
	}
    

	@Override
	public void onClick(View v) {
		try {
			if( v != null )
			{
				v.setEnabled(false);
			}
			if( v == btnSave )
			{
				// TODO: 保存処理を行う
				updateData(this);
        		Toast.makeText(this, R.string.data_updated, 
        				Toast.LENGTH_LONG).show();
        		finish();

			}
			else if( v == btnCancel )
			{
				// 保存せずに終了する
				// TODO: 確認ダイアログ
				finish();
			}
			else
			{
				// 日付ボタンかもしれない
				// 判定
				if( v.getTag() != null && v.getTag() instanceof ColumnData )
				{
					ColumnData clmn = (ColumnData) v.getTag();
					if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
					{
						// 日時型のボタンが押されたはず
						mLastInputDateTimeLabel = (TextView)v.getTag(R.id.TEXT_VIEW_LINKED);
						View viewDlg = getLayoutInflater().inflate(R.layout.dt_picker, null);
						Calendar tmpDate = Calendar.getInstance();

						if( clmn.getText() != null )
						{
							tmpDate.setTime(new Date(Long.parseLong(clmn.getText())));
						}
						final MyDatePicker DPicker = (MyDatePicker)viewDlg.findViewById( R.id.date_picker );
						DPicker.init(
							tmpDate.get(Calendar.YEAR),
							tmpDate.get(Calendar.MONTH),
							tmpDate.get(Calendar.DAY_OF_MONTH),
							null);
						final MyTimePicker TPicker = (MyTimePicker)viewDlg.findViewById( R.id.time_picker );
						TPicker.setCurrentHour(	tmpDate.get(Calendar.HOUR) );
						TPicker.setCurrentMinute( tmpDate.get(Calendar.MINUTE) );
						TPicker.setCurrentSecond( tmpDate.get(Calendar.SECOND) );
		
				    	new AlertDialog.Builder(this)
						.setTitle(getString(R.string.INPUTDLG_TITLE_DATETIME))
						.setView(viewDlg)
						.setPositiveButton( android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) 
								{
									if( mLastInputDateTimeLabel != null )
									{
										mInputDate = Calendar.getInstance();
										mInputDate.set( DPicker.getYear() 
												,DPicker.getMonth()
												,DPicker.getDayOfMonth()
												,TPicker.getCurrentHour()
												,TPicker.getCurrentMinute()
												,TPicker.getCurrentSecond()
										);
										long lngDT = mInputDate.getTimeInMillis();
										
										// TODO: 元のデータに設定するかどうか
										// String.valueOf(lngDT
										
										// TextViewに設定する
										SimpleDateFormat sdfDateTime = new SimpleDateFormat(
												getString(R.string.datetime_display_format));
										mLastInputDateTimeLabel.setText( sdfDateTime.format(lngDT) );
										// TODO:clmnのTextも更新必要

									}
								};			
							}
						)
						.setNegativeButton( android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dlg,
											int which )
									{}
								}
						)
						.create()
						.show();
						
					}
					else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_TIME )
					{
						// 時間型のボタンが押されたはず
						mLastInputDateTimeLabel = (TextView)v.getTag(R.id.TEXT_VIEW_LINKED);						
						Calendar tmpDate = Calendar.getInstance();
						if( clmn.getText() != null )
						{
							tmpDate.setTime(new Date(Long.parseLong(clmn.getText())));
						}
						View viewDlg = getLayoutInflater().inflate(R.layout.t_picker, null);

						final MyTimePicker TPicker = (MyTimePicker)viewDlg.findViewById( R.id.time_picker );
						TPicker.setCurrentHour(	tmpDate.get(Calendar.HOUR) );
						TPicker.setCurrentMinute( tmpDate.get(Calendar.MINUTE) );
						TPicker.setCurrentSecond( tmpDate.get(Calendar.SECOND) );
		
				    	new AlertDialog.Builder(this)
						.setTitle(getString(R.string.INPUTDLG_TITLE_TIME))
						.setView(viewDlg)
						.setPositiveButton( android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) 
								{
									if( mLastInputDateTimeLabel != null )
									{
										mInputDate = Calendar.getInstance();
										mInputDate.set( 0 
												,0
												,0
												,TPicker.getCurrentHour()
												,TPicker.getCurrentMinute()
												,TPicker.getCurrentSecond()
										);
										long lngDT = mInputDate.getTimeInMillis();
										
										// TODO: 元のデータに設定するかどうか
										// String.valueOf(lngDT
										
										// TextViewに設定する
										SimpleDateFormat sdfDateTime = new SimpleDateFormat(
												getString(R.string.time_display_format));
										mLastInputDateTimeLabel.setText( sdfDateTime.format(lngDT) );

									}
								};			
							}
						)
						.setNegativeButton( android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dlg,
											int which )
									{}
								}
						)
						.create()
						.show();
						
//						final TimePickerDialog timePickerDialog = new TimePickerDialog(this,
//				            new TimePickerDialog.OnTimeSetListener() {
//				                @Override
//				                public void onTimeSet(MyTimePicker1 view, int hourOfDay, int minute) {
//				                	// TODO: 設定
//									mInputDate = Calendar.getInstance();
//									mInputDate.set( 0 
//											,0
//											,0
//											,hourOfDay
//											,minute
//											,0	// TODO: 秒数の入力
//									);
//									long lngDT = mInputDate.getTimeInMillis();
//									// TextViewに設定する
//									SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//											getString(R.string.datetime_display_format));
//									mLastInputDateTimeLabel.setText( sdfDateTime.format(lngDT) );
//				                }
//				            }, tmpDate.get(Calendar.HOUR), tmpDate.get(Calendar.MINUTE), true);
//				        timePickerDialog.show();					
//					}
					}
				}
			}
		} finally {
			if( v != null )
			{
				v.setEnabled(true);
			}
		}
	}

	private void addViewToCompContainer( RelativeLayout rl, ViewGroup v )
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
		if( v == btnSave
		|| v == btnCancel )
		{
			if( btnSave == v )
			{
				region = regionSaveBtn;
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
				if( btnSave == v )
				{
					bSaveBtnEnableRegionTouched = true;
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
				if( btnSave == v )
				{
					bSaveBtnEnableRegionTouched = false;
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
	public void controlToData(ColumnData[] clmns)
	{
		if( clmns != null )
		{
			ActivityLapData lapData = ResourceAccessor.getInstance().getLapDataTmp();
			for( int i=0; i < clmns.length; i++ )
			{
				String strValue = null;
				if( clmns[i].getItemValueControlID() != -1 )
				{
					// 項目値の設定されたコントロールがある場合
					// そこから値を取得する
					View v = componentContainer.findViewById(clmns[i].getItemValueControlID());
					if( v != null )
					{
						if( v instanceof TextView )
						{
							TextView txt = (TextView) v;
							if( txt.getText() != null )
							{
								strValue = txt.getText().toString();
							}
						}
					}
				}
				else
				{
					strValue = clmns[i].getText();
				}
				if( strValue == null )
				{
					continue;
				}
//				else if( clmns[i].isEditable() == false || clmns[i].isHidden() )
//				{
//					
//				}
				SimpleDateFormat sdfDateTime = new SimpleDateFormat(
						getString(R.string.datetime_display_format));
				SimpleDateFormat sdfTime = new SimpleDateFormat(
						getString(R.string.time_display_format));
	
				try {
					if( clmns[i].getColumnName() == RunHistoryTableContract.START_DATETIME )
					{
						lapData.setStartDateTime(sdfDateTime.parse(strValue).getTime());
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.INSERT_DATETIME )
					{
						lapData.setInsertDateTime(sdfDateTime.parse(strValue).getTime());
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.PARENT_ID )
					{
						lapData.setParentId(Integer.parseInt(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_INDEX )
					{
						lapData.setLapIndex(Integer.parseInt(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_DISTANCE )
					{
						lapData.setDistance(Double.parseDouble(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_TIME )
					{
						// TODO: これはきっとおかしい
						lapData.setTime(sdfTime.parse(strValue).getTime());
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_SPEED )
					{
						lapData.setSpeed(Double.parseDouble(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_FIXED_DISTANCE )
					{
						lapData.setDistance(Double.parseDouble(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_FIXED_TIME )
					{
						// TODO: これはきっとおかしい
						lapData.setTime(sdfTime.parse(strValue).getTime());
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.LAP_FIXED_SPEED )
					{
						lapData.setSpeed(Double.parseDouble(strValue));
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.NAME )
					{
						lapData.setName(strValue);
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.GPX_FILE_PATH )
					{
						lapData.setGpxFilePath(strValue);
					}
					else if( clmns[i].getColumnName() == RunHistoryTableContract.GPX_FILE_PATH_FIXED )
					{
						lapData.setGpxFixedFilePath(strValue);
					}
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("parse error", "get lap data from edit");
				}
				// lapDataを再設定
				ResourceAccessor.getInstance().setLapDataTmp(lapData);
			}
		}
		return;
	}
	
	public ContentValues createContentValues(Activity activity, int tableID)
	{
		ContentValues ret = null;
//		if( tableID == RunHistoryTableContract.HISTORY_TABLE_ID)
//		{
//			ret = new ContentValues();
//			ret.put(RunHistoryTableContract.START_DATETIME, totalStartTime);
//			ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime);
//			ret.put(RunHistoryTableContract.NAME, strExtra[0]);
//			ret.put(RunHistoryTableContract.LAP_COUNT, getStockedLapCount() );
//            //ret.put( RunHistoryTableContract.GPX_FILE_PATH, strExtra[1] );
//			// TODO: place id under construction
//			ret.put(RunHistoryTableContract.PLACE_ID, -1);
//		}
//		else 
		if( tableID == RunHistoryTableContract.HISTORY_LAP_TABLE_ID)
		{
			ActivityLapData lapData = ResourceAccessor.getInstance().getLapDataTmp();
			ret = new ContentValues();
			ret.put(RunHistoryTableContract.START_DATETIME, lapData.getStartDateTime());
    		ret.put(RunHistoryTableContract.INSERT_DATETIME, lapData.getInsertDateTime() );
    		ret.put( RunHistoryTableContract.PARENT_ID, lapData.getParentId() );
    		ret.put( RunHistoryTableContract.LAP_INDEX, lapData.getLapIndex() );
            ret.put( RunHistoryTableContract.LAP_DISTANCE, lapData.getDistance() );
            ret.put( RunHistoryTableContract.LAP_TIME, lapData.getTime() );
            ret.put( RunHistoryTableContract.LAP_SPEED, lapData.getSpeed() );
            ret.put( RunHistoryTableContract.LAP_FIXED_DISTANCE, lapData.getFixedDistance() );
            ret.put( RunHistoryTableContract.LAP_FIXED_TIME, lapData.getFixedTime() );
            ret.put( RunHistoryTableContract.LAP_FIXED_SPEED, lapData.getFixedSpeed() );
            ret.put( RunHistoryTableContract.NAME, lapData.getName() );
            ret.put( RunHistoryTableContract.GPX_FILE_PATH, lapData.getGpxFilePath() );
            ret.put( RunHistoryTableContract.GPX_FILE_PATH_FIXED, lapData.getGpxFixedFilePath() );
		}
		
		return ret;
		
	}
	public int updateData(
			Activity activity )
	{
		int tableID = RunHistoryTableContract.HISTORY_TABLE_ID;
		String tableName = RunHistoryTableContract.HISTORY_TABLE_NAME;
        if( iEditDataType == EDIT_DATA_LAP_TABLE )
        {
    		tableID = RunHistoryTableContract.HISTORY_LAP_TABLE_ID;
    		tableName = RunHistoryTableContract.HISTORY_LAP_TABLE_NAME;        	
        }

		int iCount = -1;
		activity.getContentResolver().insert(
				Uri.parse("content://" 
				+ RunHistoryTableContract.AUTHORITY + "/" 
				+ RunHistoryTableContract.HISTORY_TRANSACTION )
				,null);
        try {
        	ContentValues values = null;
        	values = createContentValues(
        			activity, tableID );
        			//RunHistoryTableContract.HISTORY_LAP_TABLE_ID);
        	if( values == null )
        	{
        		Toast.makeText(activity, "failed to update data.", 
        				Toast.LENGTH_LONG).show();
                return -1;
        	}
        	iCount = 
        	activity.getContentResolver().update(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/"
        					+ tableName
        					//+ RunHistoryTableContract.HISTORY_LAP_TABLE_NAME 
        					), values, null, null);
            //db.setTransactionSuccessful();
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_COMMIT )
    				,null);
        		
        } finally {
        	//db.endTransaction();
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
    				,null);
        	
        }
        return iCount;
	}
	
}
