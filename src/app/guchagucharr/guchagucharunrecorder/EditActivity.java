package app.guchagucharr.guchagucharunrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
//import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;
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
import app.guchagucharr.guchagucharunrecorder.fragments.ChooseActivityTypeDialogFragment;
import app.guchagucharr.guchagucharunrecorder.fragments.ChooseActivityTypeDialogFragment.ChooseActivityTypeCaller;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.guchagucharunrecorder.util.MyDatePicker;
import app.guchagucharr.guchagucharunrecorder.util.MyTimePicker;
import app.guchagucharr.guchagucharunrecorder.util.TrackIconUtils;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;
import app.guchagucharr.guchagucharunrecorder.util.XmlUtil;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.interfaces.IEditViewController;
import app.guchagucharr.service.GPXGeneratorSync;
import app.guchagucharr.service.RunHistoryLoader;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunLoggerService;

public class EditActivity extends FragmentActivity //Activity 
implements IEditViewController, OnClickListener, OnTouchListener
,ChooseActivityTypeCaller
{
	// EditTextの値変更監視
	public class UITextWatcher implements TextWatcher {
		String valueName;
		public UITextWatcher(String valueName_)
		{
			valueName = valueName_;
		}
		public void afterTextChanged(Editable arg) {
			for( ColumnData clmn : clmnInfos )
			{
				if( valueName.equals( clmn.getColumnName() ) )
				{
					clmn.setText( arg.toString() );
					break;
				}
			}
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}	
	// タグのID
	// --> write in tags.xml
	//private static final int TEXT_VIEW_LINKED = 1000;
	Calendar mInputDate = null;
	TextView mLastInputDateTimeLabel = null;
	//ColumnData mLastInputColumnData = null;
	String mLastInputColumnName = null;
	
	
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
	private ColumnData[] clmnInfosOrg = null;
	
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
        	dataGen = new MainColumnDataGenerator();
        	clmnInfos = dataGen.generate( this, ResourceAccessor.getInstance().getWorkOutDataTmp() );
        	clmnInfosOrg = dataGen.generate( this, ResourceAccessor.getInstance().getWorkOutDataTmp() );
        }
        else if( iEditDataType == EDIT_DATA_LAP_TABLE )
        {
        	dataGen = new LapColumnDataGenerator();
        	clmnInfos = dataGen.generate( this, ResourceAccessor.getInstance().getLapDataTmp() );
        	clmnInfosOrg = dataGen.generate( this, ResourceAccessor.getInstance().getLapDataTmp() );
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
				// Edit可能な項目
				if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
				{
					// 日時型の場合
					if( clmn.getText() != null && 0 < clmn.getText().length() )
					{
						TextView lblText = new TextView(this);
						lblText.setText( ColumnData.getFormatText(this, clmn.getEditMethod(), clmn.getText()));
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
								//sdfDateTime.format(Long.parseLong(clmn.getText())) );
						lblText.setText( ColumnData.getFormatText(this, clmn.getEditMethod(), clmn.getText()));						
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
				// 距離
				else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DISTANCE )
				{
					// 距離型の場合
					if( clmn.getText() != null && 0 < clmn.getText().length() )
					{
						EditText edt = new EditText(this);
						
						edt.setText( 
							String.format( "%.3f", Double.parseDouble(clmn.getText()) ) 
						);
								//ColumnData.getFormatText(this, clmn.getEditMethod(), clmn.getText()));						
						edt.setLayoutParams(llForContent);
						edt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
						edt.addTextChangedListener(new UITextWatcher(clmn.getColumnName()));
						ll.addView(edt);
					}
				}
				// Activity Type設定用コントロールは特別にする
				else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_ACTIVITY_TYPE )
				{
					// Activity種別の入力の場合
					if( clmn.getText() != null && 0 < clmn.getText().length() )
					{
						ImageButton activityTypeButton = new ImageButton(this);
						activityTypeButton.setBackgroundResource(R.drawable.selector_spinner_button_image );
						int iIconCd = TrackIconUtils.ACTIVITY_TYPE_NONE;
						try {
							iIconCd = Integer.parseInt(clmn.getText());
						} 
						catch( Exception ex )
						{
							iIconCd = TrackIconUtils.ACTIVITY_TYPE_NONE;
						}
					    Bitmap source = BitmapFactory.decodeResource(
						        getResources(),
						        TrackIconUtils.getIconDrawable(iIconCd));
					    activityTypeButton.setImageBitmap(source);
					    // 種別はTagに設定
					    activityTypeButton.setTag(iIconCd);
					    activityTypeButton.setTag(R.id.COLUMN_NAME_ID, clmn.getColumnName() );
						
						BitmapFactory.Options bmpoptions 
						= ResourceAccessor.getInstance().getBitmapSizeFromMineType(
								R.drawable.main_historybutton_normal);		
						RelativeLayout.LayoutParams rlActType
						= dispInfo.createLayoutParamForNoPosOnBk(
								bmpoptions.outWidth,
								bmpoptions.outHeight, true );
						// activityTypeIcon.setLayoutParams(rlActTypeSpn);
						activityTypeButton.setLayoutParams(rlActType);
						activityTypeButton.setOnClickListener(new View.OnClickListener() {

						    @Override
						    public void onClick(View v) {
						    	int iCurrentCd = TrackIconUtils.ACTIVITY_TYPE_NONE;
						    	try {
						    		iCurrentCd = (Integer)v.getTag();
						    	} catch( Exception ex )
						    	{
						    		iCurrentCd = TrackIconUtils.ACTIVITY_TYPE_NONE;
						    	}
					        	ChooseActivityTypeDialogFragment act 
					        	= ChooseActivityTypeDialogFragment.newInstance(
					        			v,
					        			iCurrentCd
					        		  );
					        	act.show(
					        			getSupportFragmentManager(),
					        			ChooseActivityTypeDialogFragment.CHOOSE_ACTIVITY_TYPE_DIALOG_TAG);
						    }
						});
						//activityTypeButton.setLayoutParams(llForContent);
						activityTypeButton.setId(iValueInputControlID);
						ll.addView(activityTypeButton);
					}
				}
				else
				{
					EditText edt = new EditText(this);
					if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_INTEGER 
					|| clmn.getEditMethod() == ColumnData.EDIT_METHDO_REAL )
					{
						edt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
					}
					edt.setText( clmn.getText());
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
					edt.addTextChangedListener(new UITextWatcher(clmn.getColumnName()));
					ll.addView(edt);
				}
			}
			else
			{
				// Edit不可能
				TextView lblText = new TextView(this);
				lblText.setText( ColumnData.getFormatText(this, clmn.getEditMethod(), clmn.getText()));
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
				// データ更新処理を行う
				// TODO: まだテーブルに保存されていない時の処理
				int iRetUpdate = updateData(this);
				if( 0 < iRetUpdate )
				{
			        if( iEditDataType == EDIT_DATA_MAIN_TABLE 
			        && isUpdateColumn( RunHistoryTableContract.ACTIVITY_TYPE ) )
			    	{
						// 活動タイプが編集されていたら、GPXファイルの活動タイプも編集する
			        	boolean bGpxEditSuccess = false;
			    		RunHistoryLoader loader = new RunHistoryLoader();
			    		int id = Integer.parseInt(clmnInfos[0].getText());
			    		loader.loadPartialData(this, id);
			    		Vector<ActivityLapData> lapData = loader.getHistoryLapDatas(id);
			    		
			    		for( ActivityLapData data : lapData )
			    		{
			    			// 全てのラップデータをループ
			    			// 対象のGPXは、ユーザが修正したものが格納されていたら、
			    			// そっちを使うが、デフォルトは編集されていないものを使う
			    			String targetGpx = data.getGpxFilePath();
			    			if( data.getGpxFixedFilePath() != null
			    			&& data.getGpxFixedFilePath() != data.getGpxFilePath() )
			    			{
			    				targetGpx = data.getGpxFixedFilePath();
			    			}
			    			
							ColumnData clmn = getColumnDataFromColumnName(
									clmnInfos, RunHistoryTableContract.ACTIVITY_TYPE );
							// typeを文字列に変換
							Integer activityTypeCode = 
											Integer.parseInt(clmn.getText());
			    			
							bGpxEditSuccess = GPXGeneratorSync.updateActivityType(this,targetGpx,activityTypeCode);
							// TODO:エラー処理
							if( false == bGpxEditSuccess )
							{
								LogWrapper.e("ActivityType EditError","ActivityType EditError Occured");
							}
			    		}
			    	}
					
	        		Toast.makeText(this, R.string.data_updated, 
	        				Toast.LENGTH_LONG).show();
	        		finish();
				}
				else if(iRetUpdate == 0)
				{
	        		Toast.makeText(this, R.string.data_not_updated,
	        				Toast.LENGTH_LONG).show();					
				}
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
						mLastInputColumnName = clmn.getColumnName();
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
						// TPicker.setIsTimeInputView(true);
						TPicker.setCurrentHour(	tmpDate.get(Calendar.HOUR_OF_DAY) );
						TPicker.setCurrentMinute( tmpDate.get(Calendar.MINUTE) );
						TPicker.setCurrentSecond( tmpDate.get(Calendar.SECOND) );
		
				    	new AlertDialog.Builder(this)
						.setTitle(getString(R.string.INPUTDLG_TITLE_DATETIME))
						.setView(viewDlg)
						.setPositiveButton( android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) 
								{
									if( mLastInputDateTimeLabel != null 
									&& mLastInputColumnName != null )
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
												getString(R.string.datetime_display_format_full));
										mLastInputDateTimeLabel.setText( sdfDateTime.format(lngDT) );
										for( ColumnData clmn : clmnInfos )
										{
											if( mLastInputColumnName.equals( clmn.getColumnName() ) )
											{
												clmn.setText( String.valueOf( lngDT ));
												break;
											}
										}
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
						mLastInputColumnName = clmn.getColumnName();						
						//Calendar tmpDate = Calendar.getInstance();
						int hour = 0;
						int minute = 0;
						int second = 0;
						if( clmn.getText() != null )
						{
							long millisec = Long.parseLong(clmn.getText());
							hour = (int)UnitConversions.getHourFromMilliSec(millisec);							
							minute = (int)(UnitConversions.getMinuteFromMilliSec(millisec)
									- UnitConversions.getHourFromMilliSec(millisec) * 60);							
							second = (int)(UnitConversions.getSecondFromMilliSec(millisec)
									- UnitConversions.getMinuteFromMilliSec(millisec) * 60);
							//tmpDate.setTime(new Date(Long.parseLong(clmn.getText())));
						}
						View viewDlg = getLayoutInflater().inflate(R.layout.t_picker, null);

						final MyTimePicker TPicker = (MyTimePicker)viewDlg.findViewById( R.id.time_picker );
						TPicker.setIsTimeInputView(true);
						TPicker.setCurrentHour(	hour );//tmpDate.get(Calendar.HOUR_OF_DAY) );
						TPicker.setCurrentMinute( minute );//tmpDate.get(Calendar.MINUTE) );
						TPicker.setCurrentSecond( second );//tmpDate.get(Calendar.SECOND) );
		
				    	new AlertDialog.Builder(this)
						.setTitle(getString(R.string.INPUTDLG_TITLE_TIME))
						.setView(viewDlg)
						.setPositiveButton( android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) 
								{
									if( mLastInputDateTimeLabel != null 
									&& mLastInputColumnName != null
									)
									{
//										mInputDate = Calendar.getInstance();
//										mInputDate.set( 0 
//												,0
//												,0
//												,TPicker.getCurrentHour()
//												,TPicker.getCurrentMinute()
//												,TPicker.getCurrentSecond()
//										);
//										long lngDT = mInputDate.getTimeInMillis();
										
										long millisec = UnitConversions.createMillisec(
											TPicker.getCurrentHour()
											,TPicker.getCurrentMinute()
											,TPicker.getCurrentSecond()
										);
										// TextViewに設定する
										//SimpleDateFormat sdfDateTime = new SimpleDateFormat(
										//		getString(R.string.time_display_format));
										mLastInputDateTimeLabel.setText( //sdfDateTime.format(lngDT) );
												UnitConversions.getWorkoutTimeString(EditActivity.this, 
														millisec));
										for( ColumnData clmn : clmnInfos )
										{
											if( mLastInputColumnName.equals( clmn.getColumnName() ) )
											{
												clmn.setText( String.valueOf( millisec ));
												break;
											}
										}
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
						getString(R.string.datetime_display_format_full));
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
					LogWrapper.e("parse error", "get lap data from edit");
				}
				// lapDataを再設定
				ResourceAccessor.getInstance().setLapDataTmp(lapData);
			}
		}
		return;
	}
	
	public ContentValues createContentValuesForUpdate(Activity activity, int tableID)
	{
		ContentValues ret = null;
		if( tableID == RunHistoryTableContract.HISTORY_TABLE_ID)
		{
			ret = new ContentValues();
			// updateなので、編集不可の項目を含める必要はない
			// IDを含めると、エラーに？
			//ret.put(BaseColumns._ID,clmnInfos[0].getText() );
			ret.put(RunHistoryTableContract.START_DATETIME, clmnInfos[1].getText() );
			//ret.put(RunHistoryTableContract.INSERT_DATETIME, clmnInfos[2].getText());
			ret.put(RunHistoryTableContract.NAME, clmnInfos[3].getText());
			//ret.put(RunHistoryTableContract.LAP_COUNT, clmnInfos[4].getText() );
			//ret.put(RunHistoryTableContract.PLACE_ID, clmnInfos[5].getText() );
			ret.put(RunHistoryTableContract.ACTIVITY_TYPE, clmnInfos[6].getText() );
		}
		else 
		if( tableID == RunHistoryTableContract.HISTORY_LAP_TABLE_ID)
		{
			ActivityLapData lapData = ResourceAccessor.getInstance().getLapDataTmp();
			ret = new ContentValues();
			// updateなので、編集不可の項目を含める必要はない
			// IDを含めると、エラーに？			
			// ret.put(BaseColumns._ID,clmnInfos[0].getText() );			
			ret.put(RunHistoryTableContract.START_DATETIME, clmnInfos[1].getText() );
    		//ret.put(RunHistoryTableContract.INSERT_DATETIME, clmnInfos[2].getText() );
            ret.put( RunHistoryTableContract.NAME, clmnInfos[3].getText() );
    		//ret.put( RunHistoryTableContract.PARENT_ID, clmnInfos[4].getText() );
    		//ret.put( RunHistoryTableContract.LAP_INDEX, clmnInfos[5].getText() );
            //ret.put( RunHistoryTableContract.LAP_DISTANCE, clmnInfos[6].getText() );
            //ret.put( RunHistoryTableContract.LAP_TIME, clmnInfos[7].getText() );
            //ret.put( RunHistoryTableContract.LAP_SPEED, clmnInfos[8].getText() );
            ret.put( RunHistoryTableContract.LAP_FIXED_DISTANCE, clmnInfos[9].getText() );
            ret.put( RunHistoryTableContract.LAP_FIXED_TIME, clmnInfos[10].getText() );
            ret.put( RunHistoryTableContract.LAP_FIXED_SPEED, clmnInfos[11].getText() );
            //ret.put( RunHistoryTableContract.GPX_FILE_PATH, clmnInfos[12].getText() );
            ret.put( RunHistoryTableContract.GPX_FILE_PATH_FIXED, clmnInfos[13].getText() );
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
        	values = createContentValuesForUpdate(
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
        					), values, BaseColumns._ID + "=" + clmnInfos[0].getText(), null);
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
	/**
	 * 指定されたカラムの値が、更新されているかどうかを調べる
	 * @param clmnName
	 * @return true:更新されている false:更新されていない、もしくはエラー？
	 */
	private boolean isUpdateColumn(String clmnName)
	{
    	ColumnData clmn = getColumnDataFromColumnName( clmnInfos, clmnName);
    	ColumnData clmnOrg = getColumnDataFromColumnName( clmnInfosOrg, clmnName);
    	if( clmn != null && clmnOrg != null )
    	{
    		if( clmn.getText() != null && clmn.getText().equals(clmnOrg.getText() ) )
    		{
    			return false;
    		}
    		else if( clmn.getText() == null && clmnOrg.getText() == null )
    		{
    			return false;
    		}
    		else
    		{
    			// ここに来たら、更新されているはず？
    			return true;
    		}
    	}		
		return false;
	}

    private void setActivityTypeIcon(View parent,int value) {
    	// コントロールの値を更新
    	Bitmap source = BitmapFactory.decodeResource(
    			this.getResources(),
    			TrackIconUtils.getIconDrawable(value));
    	ImageButton parentButton = (ImageButton)parent;
    	parentButton.setImageBitmap(source);
    	parentButton.setTag(value);
    	
    	// DB値との同期となるデータの更新
    	// ボタンから、そのカラム名を取得
    	String clmnName = (String) parentButton.getTag(R.id.COLUMN_NAME_ID);
    	ColumnData clmn = getColumnDataFromColumnName( clmnInfos, clmnName);
    	if( clmn != null )
    	{
    		clmn.setText( String.valueOf(value) );
    	}
    }
    /**
     * 対象のカラム配列から、指定された名前のカラムを取得する
     * @param clmnArray
     * @param name
     * @return null:なし ColumnData:検索されたカラム
     */
    ColumnData getColumnDataFromColumnName( ColumnData[] clmnArray, String name )
    {
    	if( name == null ) return null;
		for( ColumnData clmn : clmnArray )
		{
			// カラム名から、対象カラムを検索、見つかったらその値を更新
			if( name.equals( clmn.getColumnName() ) )
			{
				return clmn;
			}
		}
		return null;
    }
    
	@Override
	public void onChooseActivityTypeDone(View parent,int iconValue) {
		setActivityTypeIcon(parent,iconValue);
	}
	
}
