package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.content.Intent;
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
import android.view.ViewGroup;
//import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import app.guchagucharr.interfaces.IEditViewController;
import app.guchagucharr.service.RunHistoryLoader.ActivityData;
import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;

public class EditActivity extends Activity 
implements IEditViewController, OnClickListener, OnTouchListener
{
	public static final String KEY_CLMN_DATA_GEN = "KeyOfColumnDataGenerator";
	public static final String KEY_CLMN_DATA_INDEX = "KeyOfColumnDataIndex";
	public static final int EDIT_DATA_NONE = -1;
	public static final int EDIT_DATA_MAIN_TABLE = 1;
	public static final int EDIT_DATA_LAP_TABLE = 2;
	private int iEditDataType = EDIT_DATA_LAP_TABLE;
	// private int iEditDataIndex = -1;
//	private IColumnDataGenerator dataGen = null;
	
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewGroup componentContainer;
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

		handler = new EditHandler(this,this);
        componentContainer = (ViewGroup) findViewById(R.id.page_content);
        
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
        
		// TODO: 各コントロールの配置
		if( iEditDataType == EDIT_DATA_NONE )
        {
			
        }
		else if( iEditDataType == EDIT_DATA_MAIN_TABLE )
		{
			// メインテーブル編集
			// データの取得
			ActivityData data = ResourceAccessor.getInstance().getWorkOutDataTmp();
		}
		else if( iEditDataType == EDIT_DATA_LAP_TABLE )
		{
			// ラップテーブル編集
			// データの取得
			ActivityLapData data = ResourceAccessor.getInstance().getLapDataTmp();
			
		}
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
