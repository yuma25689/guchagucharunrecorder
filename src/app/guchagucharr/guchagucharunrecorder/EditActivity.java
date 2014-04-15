package app.guchagucharr.guchagucharunrecorder;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
//import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
//import android.graphics.drawable.BitmapDrawable;
//import android.content.IntentFilter;
//import android.location.Criteria;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
//import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.interfaces.IEditViewController;

public class EditActivity extends Activity 
implements IEditViewController, OnClickListener, OnTouchListener
{
	// タグのID
	private static final int TEXT_VIEW_LINKED = 1;
	
	
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
//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//
//	}

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
			if( clmn.isEditable() )
			{
//				// TODO: 日付型等の対応
//				if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
//				{
//					// 日時型の場合
//					if( clmn.getText() != null && 0 < clmn.getText().length() )
//					{
//						TextView lblText = new TextView(this);
//						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//								getString(R.string.datetime_display_format));
//						lblText.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
//						lblText.setLayoutParams(llForContent);
//						lblText.setBackgroundColor(Color.DKGRAY);
//						lblText.setTextColor(Color.WHITE);
//						ll.addView(lblText);
//
//						Button btnDate = new Button(this);
//						// OnClickListenerでどの項目か識別できるように、タグを設定
//						btnDate.setTag(clmn.getColumnName());
//						btnDate.setTag(TEXT_VIEW_LINKED, lblText);
//						//btnDate.setText(R.string.date_button_caption);
//						btnDate.setOnClickListener(this);
//						
//						ll.addView(btnDate);
//					}
//				}
//				else
				{
					EditText edt = new EditText(this);
					if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_INTEGER 
					|| clmn.getEditMethod() == ColumnData.EDIT_METHDO_REAL )
					{
						edt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
						edt.setText( clmn.getText());
					}
					else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_DATETIME )
					{
						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
								getString(R.string.datetime_display_format));
						edt.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
						edt.setInputType(EditorInfo.TYPE_CLASS_DATETIME
								|EditorInfo.TYPE_DATETIME_VARIATION_NORMAL);
					}
					else if( clmn.getEditMethod() == ColumnData.EDIT_METHDO_TIME )
					{
						SimpleDateFormat sdfDateTime = new SimpleDateFormat(
								getString(R.string.datetime_display_format));
						edt.setText( sdfDateTime.format(Long.parseLong(clmn.getText())) );
						edt.setInputType(EditorInfo.TYPE_CLASS_DATETIME
								|EditorInfo.TYPE_DATETIME_VARIATION_TIME);
					}
					edt.setLayoutParams(llForContent);				
					ll.addView(edt);
				}
			}
			else
			{
				TextView lblText = new TextView(this);
				lblText.setText( clmn.getText());
				lblText.setLayoutParams(llForContent);
				lblText.setBackgroundColor(Color.DKGRAY);
				lblText.setTextColor(Color.WHITE);
				
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
			}
			else if( v == btnCancel )
			{
				// 保存せずに終了する
				// TODO: 確認ダイアログ
				finish();
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
}
