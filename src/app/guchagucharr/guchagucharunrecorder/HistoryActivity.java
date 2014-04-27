package app.guchagucharr.guchagucharunrecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
//import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import app.guchagucharr.guchagucharunrecorder.DisplayBlock.eShapeType;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunHistoryLoader;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunLoggerService;
//import android.provider.BaseColumns;
import app.guchagucharr.guchagucharunrecorder.util.ActivityData;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;

public class HistoryActivity extends Activity implements IPageViewController, OnClickListener {
	static final int CONTEXT_MENU_DETAIL_ID = 0;
	static final int CONTEXT_MENU_DELETE_ID = 1;
	static final int CONTEXT_MENU_SHARE_ID = 2;
	static final int CONTEXT_MENU_EDIT_ID = 3;
	
	private ActivityData selectedActivityData = null;
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;
	private ViewGroup componentContainer;
	private PagerHandler handler;
	private HistoryPagerAdapter adapter = null;
	//private RelativeLayout lastMainLayout = null;
	private RelativeLayout lastSubLayout = null;
//	private Button gpxShareButton = null;
//	private String gpxFilePath = null;
	RunHistoryLoader loader = new RunHistoryLoader();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpager_only);
        handler = new PagerHandler( this, this );
        componentContainer = (ViewGroup) findViewById(R.id.viewpager1);
		init();
	}
	@Override
    protected void onResume() {
        dispInfo.init(this, componentContainer, handler, true);
        adapter = new HistoryPagerAdapter(this, this);
        super.onResume();
    }
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//	    //dispInfo.init(this, componentContainer, handler, true);	
//	}
	
	@Override
	public int initPager()
	{
        init();
        this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        this.mViewPager.setAdapter(adapter);
        return 0;
	}
//	do {
//		double distance = cursor.getDouble( lapDistanceIndex );
//		distanceTotal += distance;
//		double speed = cursor.getDouble( lapSpeedIndex );
//		speedTotal += speed;
//		long time = cursor.getLong( lapTimeIndex );
//		timeTotal += time;
	static final int MAIN_FIRST_PANEL_ID = 1500;
	static final int SUB_FIRST_PANEL_ID = 100000;

	@Override
	public int initControls( int position, RelativeLayout rl )
	{
//		int width = componentContainer.getWidth();
//		int height = componentContainer.getHeight();
		if( position == 0 )
		{
			updateMainPage( rl );
		}
		else if( position == 1 )
		{
			updateSubPage( rl );
		}
		
		return 0;
	}
	public void updateMainPage(RelativeLayout rl)
	{
		//lastMainLayout = rl;
		// 勝手にこんなことしていいんでしょうか・・・。
		//lastMainLayout.removeAllViews();
		ArrayList<ActivityData> mainData = loader.getHistoryData();
		DisplayBlock.eSizeType sizeType = DisplayBlock.getProperSizeTypeFromCount(
				mainData.size());
		SimpleDateFormat sdfDateTime = new SimpleDateFormat(
				getString(R.string.datetime_display_format));
		SimpleDateFormat sdfDateTimeAfter = null;
//		SimpleDateFormat sdfDate = new SimpleDateFormat(getString(R.string.date_display_format));
//		SimpleDateFormat sdfTime = new SimpleDateFormat(getString(R.string.time_display_format));
		int lastEvenPanelID = 0;
		int lastOddPanelID = 0;
		int iPanelCount = 0;
		for( ActivityData data : mainData )
		{
			double distanceTotal = 0;
			double speedTotal = 0;
			long timeTotal = 0;
			Vector<ActivityLapData> lapDatas = loader.getHistoryLapDatas(data.getId());
			for( ActivityLapData lapData: lapDatas )
			{
				distanceTotal += lapData.getDistance();
				timeTotal += lapData.getTime();				
				// speedは、時間と距離から計算したものの方が違和感がなく、圧倒的に精確
				//speedTotal += //lapData.getSpeed();
			}	
			speedTotal = distanceTotal / ( timeTotal * UnitConversions.MS_TO_S ); 
			// DisplayBlock追加
//			String titleDateTime = sdfDateTime.format(data.getStartDateTime())
//					+ getString(R.string.to) + sdfDateTime.format(data.getStartDateTime() + timeTotal);
//			String titleDate = sdfDate.format(new Date(data.getDateTime()));
//			String titleTime = sdfTime.format(new Date(data.getDateTime()));
			//String title = titleDateTime;//titleDate + System.getProperty("line.separator") + titleTime;
			//Locale locale = Locale.getDefault();
			Calendar calStart = Calendar.getInstance();//TimeZone.getDefault(),
					//locale);//TimeZone.getTimeZone("UTC"));
			calStart.setTimeInMillis(data.getStartDateTime());
			//calStart.set(Calendar.HOUR, 0);
			calStart.set(Calendar.HOUR_OF_DAY, 0);
			calStart.set(Calendar.MINUTE, 0);
			calStart.set(Calendar.SECOND, 0);
			calStart.set(Calendar.MILLISECOND, 0);
			Calendar calEnd = Calendar.getInstance();//TimeZone.getDefault(),
					//locale);//TimeZone.getTimeZone("UTC"));
			calEnd.setTimeInMillis(data.getStartDateTime() + timeTotal);
			//calEnd.set(Calendar.HOUR, 0);
			calEnd.set(Calendar.HOUR_OF_DAY, 0);			
			calEnd.set(Calendar.MINUTE, 0);
			calEnd.set(Calendar.SECOND, 0);
			calEnd.set(Calendar.MILLISECOND, 0);
			if( calStart.equals(calEnd) == false )
			{
				calStart.set(Calendar.DATE, 0);
				calEnd.set(Calendar.DATE, 0);
				if( calStart.equals(calEnd) == false )
				{
					sdfDateTimeAfter = new SimpleDateFormat(
							getString(R.string.datetime_display_format));					
				}
				else
				{
					sdfDateTimeAfter = new SimpleDateFormat(
							getString(R.string.datetime_display_format2));
				}
			}
			else
			{
				sdfDateTimeAfter = new SimpleDateFormat(
						getString(R.string.time_display_format));
			}
			String[] title = {sdfDateTime.format(data.getStartDateTime()),
					sdfDateTimeAfter.format(data.getStartDateTime() + timeTotal)};
			String lapCount = null;
			// TODO: 後でラップのデータを見てちゃんと表示する
//			String gpx = data.getGpxFilePath();
//			if( gpx != null )
//			{
//				File file = new File(gpx);
//				if( file.exists() )
//				{
//					gpxExists = getString(R.string.GPX_EXISTS);
//				}
//				else
//				{
//					gpxExists = getString(R.string.GPX_LOSE);
//				}
//			}
			if( 1 < data.getLapCount() )
			{
				lapCount = getString( R.string.LAP_COUNT_LABEL ) + data.getLapCount();
			}
			String text[] = {
					LapData.createDistanceFormatText( distanceTotal ),
					LapData.createTimeFormatText( timeTotal ),
					//LapData.createSpeedFormatText( speedTotal ),
					LapData.createSpeedFormatTextKmPerH( speedTotal ),
					//gpxExists,
					lapCount
			};
			DisplayBlock dispBlock = new DisplayBlock(
					this, 
					dispInfo.getXNotConsiderDensity(componentContainer.getWidth()),
					dispInfo.getYNotConsiderDensity(componentContainer.getHeight()),
					data.getId(),
					dispInfo, title, text, null, sizeType, eShapeType.SHAPE_BLOCK);
			dispBlock.setData(data);
			if( iPanelCount == 0 )
			{
				RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
				lp.addRule(RelativeLayout.ALIGN_LEFT);
				lp.addRule(RelativeLayout.ALIGN_TOP);
				dispBlock.setId(MAIN_FIRST_PANEL_ID);
				lastOddPanelID = dispBlock.getId();
			}
			else
			{
				dispBlock.setId(MAIN_FIRST_PANEL_ID+iPanelCount);
				if( dispInfo.isPortrait() )
				{
					if( iPanelCount % 2 == 0 )
					{
						// ここにくるのは、奇数枚
						RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
						if( lastOddPanelID != 0 )
						{
							lp.addRule(RelativeLayout.BELOW, lastOddPanelID);
						}
						lastOddPanelID = dispBlock.getId();
					}
					else
					{
						// 偶数枚
						RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
						if( lastOddPanelID != 0 )
						{
							lp.addRule(RelativeLayout.RIGHT_OF, lastOddPanelID);
						}
						if( lastEvenPanelID != 0 )
						{
							lp.addRule(RelativeLayout.BELOW, lastEvenPanelID);
						}
						lastEvenPanelID = dispBlock.getId();
					}
				} else {
					// 横向きの場合
					// 最高で２行にする
					if( DisplayBlock.getBlockCountOnOnePage(sizeType) / 2 <= iPanelCount ) 
					{
						// ここにくるのは、2行目
						RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
						lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
						if( DisplayBlock.getBlockCountOnOnePage(sizeType) / 2 < iPanelCount )
						{
							lp.addRule(RelativeLayout.RIGHT_OF, MAIN_FIRST_PANEL_ID+iPanelCount-1);
						}
					}
					else
					{
						// ここにくるのは、1行目
						RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
						lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
						if( 0 < iPanelCount )
						{
							lp.addRule(RelativeLayout.RIGHT_OF, MAIN_FIRST_PANEL_ID+iPanelCount-1);
						}
					}
				}
			}
			// 下に行くほど薄くする
			final double COLOR_RANGE = 80;
			double rate = COLOR_RANGE / mainData.size();
			int iMinus = (int) (iPanelCount * rate);
			dispBlock.setBackgroundColorAsStateList(0xFF, 0, 20 + iMinus, 70 + iMinus);
			//dispBlock.setBackgroundColor(Color.argb(0xFF, 0, 20 + iMinus, 155 + iMinus));
			dispBlock.setOnClickListener(this);
			rl.addView(dispBlock);
			iPanelCount++;
		}
		return;
	}
	public void updateSubPage(RelativeLayout rlBase)
	{
		lastSubLayout = rlBase;
		RelativeLayout rl = (RelativeLayout) rlBase.findViewById(R.id.page_content1);
		rl.removeAllViews();
		//RelativeLayout rl2 = (RelativeLayout) rlBase.findViewById(R.id.page_content2);
		// NOTICE: とりあえず、下段のビューは廃止
		//rl2.setVisibility(View.GONE);
		Vector<ActivityLapData> lapData = loader.getHistoryLapDatas(selectedActivityData.getId());
		if( lapData == null || lapData.size() == 0 )
		{
			return;
		}
		
		// TODO: 暫定版なので後で直すこと
//		if( null == lapData.get(0).getGpxFilePath() )
//		{
//			rl2.setVisibility(View.GONE);
//		}
//		else
//		{
//			File file = new File(lapData.get(0).getGpxFilePath());
//			if( file != null && file.exists() )
//			{
//				rl2.setVisibility(View.VISIBLE);
//				gpxShareButton = (Button) rl2.findViewById(R.id.gpx_share_button);
//				gpxShareButton.setOnClickListener(this);
//				gpxFilePath = lapData.get(0).getGpxFilePath();
//				bGPXExists = true;
//			}
//			else
//			{
//				rl2.setVisibility(View.GONE);
//			}
//		}
		// NOTICE: 最高で６個しか置けない
		DisplayBlock.eSizeType sizeType = DisplayBlock.getProperSizeTypeFromCount(
				lapData.size());
//		SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//				getString(R.string.datetime_display_format));
		int lastOddPanelID = 0;
		int iPanelCount = 0;
		
		// 全てのラップデータをループ
		for( ActivityLapData data : lapData )
		{
			double distance = 0;
			double speed = 0;
			long time = 0;
			distance = data.getDistance();
			time = data.getTime();				
			speed = distance / ( time * UnitConversions.MS_TO_S ); 
			String gpxFilePath = data.getGpxFilePath();
			//boolean bGPXExists = false;			
//			if( gpxFilePath != null )
//			{
//				File file = new File(gpxFilePath);
//				if( file.exists() )
//				{
//					rl2.setVisibility(View.VISIBLE);
//	//				gpxShareButton = (Button) rl2.findViewById(R.id.gpx_share_button);
//	//				gpxShareButton.setOnClickListener(this);
//					//bGPXExists = true;
//				}
//			}
			// DisplayBlock追加
			String[] title = {data.getName()}; //getString(R.string.LAP_LABEL) + ( data.getLapIndex() + 1 );
			if( title[0] == null )
			{
				title[0] = getString(R.string.no_title);//getString(R.string.LAP_LABEL) + ( data.getLapIndex() + 1 );
			}
			String text[] = {
					LapData.createDistanceFormatText( distance ),
					LapData.createTimeFormatText( time ),
					LapData.createSpeedFormatTextKmPerH( speed ),
			};
			DisplayBlock dispBlock = new DisplayBlock(
					this, 
					dispInfo.getXNotConsiderDensity(componentContainer.getWidth()),
					dispInfo.getYNotConsiderDensity(componentContainer.getHeight()),					
					data.getId(),
					dispInfo,
					title,
					text,
					gpxFilePath,
					sizeType, 
					eShapeType.SHAPE_HORIZONTAL);
			dispBlock.setData(data);
			
			if( dispInfo.isPortrait() )
			{
				if( iPanelCount == 0 )
				{
					RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
					lp.addRule(RelativeLayout.ALIGN_LEFT);
					lp.addRule(RelativeLayout.ALIGN_TOP);
					dispBlock.setId(SUB_FIRST_PANEL_ID);
					lastOddPanelID = dispBlock.getId();
				}
				else
				{
					dispBlock.setId(SUB_FIRST_PANEL_ID+iPanelCount);
					RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
					if( lastOddPanelID != 0 )
					{
						lp.addRule(RelativeLayout.BELOW, lastOddPanelID);
					}
					lastOddPanelID = dispBlock.getId();
				}
			}
			else
			{
				// 横向き
				dispBlock.setId(SUB_FIRST_PANEL_ID+iPanelCount);
				if( iPanelCount % 2 == 0 ) 
				{
					RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
					if( iPanelCount == 0 )
					{
						lp.addRule(RelativeLayout.ALIGN_TOP);
					}
					else
					{
						lp.addRule(RelativeLayout.BELOW, SUB_FIRST_PANEL_ID+iPanelCount-2);
					}
				}
				else if( iPanelCount % 2 == 1 )
				{
					RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();					
					lp.addRule(RelativeLayout.BELOW, SUB_FIRST_PANEL_ID+iPanelCount-2);
					lp.addRule(RelativeLayout.RIGHT_OF, SUB_FIRST_PANEL_ID+iPanelCount-1);
				}					
//				else
//				{
//					RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
//					lp.addRule(RelativeLayout.ALIGN_BOTTOM);
//					if( DisplayBlock.getBlockCountOnOnePage(sizeType) / 2 < iPanelCount )
//					{
//						lp.addRule(RelativeLayout.RIGHT_OF, SUB_FIRST_PANEL_ID+iPanelCount-1);
//					}					
//				}				
			}
			// 下に行くほど薄くする
			final double COLOR_RANGE = 80;
			double rate = COLOR_RANGE / lapData.size();
			int iMinus = (int) (iPanelCount * rate);
			dispBlock.setBackgroundColorAsStateList(0xFF, 20 + iMinus, 70 + iMinus, 0 );
			rl.addView(dispBlock);
			iPanelCount++;
		}
		return;
	}
	public void init()
	{
		// データをローダにロード
		int iRet = loader.load(this);
		if( iRet != 0 )
		{
			// TODO: エラー発生を通知
			finish();
		}
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
	}
	@Override
	public DisplayInfo getDispInfo() {
		return dispInfo;
	}
	 
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{ 
	    super.onCreateContextMenu(menu, v, menuInfo);
	 
	    //コンテキストメニューの設定
	    DisplayBlock block = (DisplayBlock) v;
	    menu.setHeaderTitle(block.getTitle()[0]);
	    
		if( v instanceof DisplayBlock )
		{
			DisplayBlock dispBlock = (DisplayBlock)v;
			
			if(dispBlock.getData() == null )
			{
			}
			else
			{
				// dataの種別で処理を分ける
				if( dispBlock.getData() instanceof ActivityData )
				{
				    menu.add(CONTEXT_MENU_DETAIL_ID, (int)block.getRecordId(), 0,
				    		R.string.menu_detail);
				    menu.add(CONTEXT_MENU_SHARE_ID, (int)block.getRecordId(), 0,
				    		R.string.menu_share);
				    menu.add(CONTEXT_MENU_DELETE_ID, (int)block.getRecordId(), 0,
				    		R.string.menu_delete);					
				}
				else if( dispBlock.getData() instanceof ActivityLapData )
				{
				    menu.add(CONTEXT_MENU_EDIT_ID, selectedActivityData.getId(),
				    		(int)block.getRecordId(), R.string.menu_edit);
				    menu.add(CONTEXT_MENU_SHARE_ID, selectedActivityData.getId(),
				    		(int)block.getRecordId(), R.string.menu_share);
				    menu.add(CONTEXT_MENU_DELETE_ID, selectedActivityData.getId(),
				    		(int)block.getRecordId(), R.string.menu_delete);					
				}
			}
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getGroupId()) {
	    case CONTEXT_MENU_DETAIL_ID:
	        // 詳細メニュー
	    	// 今のところ、最上位のデータを想定
	    	selectedActivityData = (ActivityData) loader.getHistoryData(item.getGroupId());
			// TODO:ページが1ページしかない場合、ページの拡張を行う
			if( adapter.getCount() == 1 )
			{
				adapter.setCount(2);
			}
			else if( adapter.getCount() == 2 )
			{
				if( null != lastSubLayout )
				{
					updateSubPage(lastSubLayout);
				}
			}
			// adapter.notifyDataSetChanged();
			// NOTICE: とりあえず、自動ページ移動はする？
			mViewPager.arrowScroll(View.FOCUS_RIGHT);
	        return true;
	    case CONTEXT_MENU_SHARE_ID:
	    	// TODO: GPXかテキストかを選択すること！
	        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
	        intent.setType("text/plain");

			SimpleDateFormat sdfDateTime = new SimpleDateFormat(
					getString(R.string.datetime_display_format));	        
	        String text = new String();
	        double distanceTotal = 0;
	        long timeTotal = 0;
			Vector<ActivityLapData> lapDatas = loader.getHistoryLapDatas(item.getItemId());
			for( ActivityLapData lapData: lapDatas )
			{
				distanceTotal += lapData.getDistance();
				timeTotal += lapData.getTime();				
				// speedは、時間と距離から計算したものの方が違和感がなく、圧倒的に精確
				//speedTotal += //lapData.getSpeed();
			}
			double speedTotal = distanceTotal / ( timeTotal * UnitConversions.MS_TO_S );
			String name = null;
			String titleDateTime = null;
        	if( null != loader.getHistoryData(item.getItemId()) )
        	{
        		ActivityData data = loader.getHistoryData(item.getItemId());
    			name = data.getName();
    			if( name != null && name.isEmpty() == false )
    			{
    				name += System.getProperty("line.separator");
    			}
    			titleDateTime = sdfDateTime.format(data.getStartDateTime())
    					+ getString(R.string.to) + sdfDateTime.format(data.getStartDateTime() + timeTotal);
        	}
	        text = name
	        + titleDateTime + System.getProperty("line.separator")
	        + getString(R.string.distance_label) + LapData.createDistanceFormatText( distanceTotal ) + System.getProperty("line.separator")
	        + getString(R.string.time_label) + LapData.createTimeFormatText( timeTotal ) + System.getProperty("line.separator")
	        + getString(R.string.speed_label) + LapData.createSpeedFormatTextKmPerH( speedTotal ) + System.getProperty("line.separator")
	        ;
	        intent.putExtra(Intent.EXTRA_TEXT, text);
	        startActivity(Intent.createChooser(
	                intent, getString(R.string.Share)));
	    	return true;
	    case CONTEXT_MENU_DELETE_ID:
	        // メニュー押下時の操作
	    	// トランザクションの考慮
			getContentResolver().insert(
					Uri.parse("content://"
					+ RunHistoryTableContract.AUTHORITY + "/"
					+ RunHistoryTableContract.HISTORY_TRANSACTION )
					,null);
	        try {
	        	// 全てのGPXファイルを削除？
	        	String gpxFile = null;
	        	for( ActivityLapData data : loader.getHistoryLapDatas(item.getItemId()) )
	        	{
        			gpxFile = data.getGpxFilePath();
    	        	if( gpxFile != null )
    	        	{
    	        		File file = new File( gpxFile );
    	        		if( file.exists() )
    	        		{
    	        			file.delete();
    	        		}
    	        	}
	        	}
	        	
	        	int iRet = this.getContentResolver().delete(
						Uri.parse("content://" 
						+ RunHistoryTableContract.AUTHORITY + "/" 
						+ RunHistoryTableContract.HISTORY_LAP_TABLE_NAME  ), 
						RunHistoryTableContract.PARENT_ID + "=" + item.getItemId(),null);
	        	if( iRet <= 0 )
	        	{
	        		return false;
	        	}
	        	iRet = this.getContentResolver().delete(
						Uri.parse("content://" 
						+ RunHistoryTableContract.AUTHORITY + "/"
						+ RunHistoryTableContract.HISTORY_TABLE_NAME ), 
						BaseColumns._ID + "=" + item.getItemId(),null);
	        	if( iRet <= 0 )
	        	{
	        		return false;
	        	}	        	
        		getContentResolver().insert(
        				Uri.parse("content://" 
        				+ RunHistoryTableContract.AUTHORITY + "/" 
        				+ RunHistoryTableContract.HISTORY_COMMIT )
        				,null);
	        } catch(Exception e){
	        	e.printStackTrace();
	        	Log.e("delete failed!",e.getMessage());
		        Toast.makeText(this, R.string.Delete_failed, Toast.LENGTH_LONG).show();
	        	return false;
	        } finally {
	        	//db.endTransaction();
	    		getContentResolver().insert(
	    				Uri.parse("content://" 
	    				+ RunHistoryTableContract.AUTHORITY + "/" 
	    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
	    				,null);
	        	
	        }
	        // 削除しました。メッセージ
	        Toast.makeText(this, R.string.Deleted, Toast.LENGTH_LONG).show();
	        // データをリロードする
	        loader.load(this);
	        // ページ更新
	        // やばいかも。
	        //updateMainPage(lastMainLayout);
			adapter.notifyDataSetChanged();
	    	
	        return true;
	    case CONTEXT_MENU_EDIT_ID:
	        // 編集メニュー
			// launch activity for save
			Intent intentEdit = new Intent( this, EditActivity.class );
			// データをそのままどこかに格納する？
			intentEdit.putExtra(EditActivity.KEY_CLMN_DATA_GEN, EditActivity.EDIT_DATA_LAP_TABLE);
			// ActivityLapData lapData4Edit = new ActivityLapData();
			ActivityLapData lapData4Edit 
				= loader.getHistoryLapData(item.getItemId(),item.getOrder());//= RunLoggerService.getLogStocker().getLapData(item.getItemId());
			ResourceAccessor.getInstance().setLapDataTmp(lapData4Edit);
			intentEdit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intentEdit);	    	
	    	return true;
	    default:
	        return super.onContextItemSelected(item);
	    }
	}
	@Override
	public void onClick(View v) {

		try
		{
			if( v != null )
			{
				v.setEnabled(false);
			}
			if( v instanceof DisplayBlock )
			{
				DisplayBlock dispBlock = (DisplayBlock)v;
				
				if(dispBlock.getData() == null )
				{
				}
				else
				{
					// dataの種別で処理を分ける
					if( dispBlock.getData() instanceof ActivityData )
					{
						// ラップじゃない方のデータが表示されたら、ラップの表示が必要になる
						selectedActivityData = (ActivityData) dispBlock.getData();
						// TODO:ページが1ページしかない場合、ページの拡張を行う
						if( adapter.getCount() == 1 )
						{
							adapter.setCount(2);
						}
						else if( adapter.getCount() == 2 )
						{
							if( null != lastSubLayout )
							{
								updateSubPage(lastSubLayout);
							}
						}
						// adapter.notifyDataSetChanged();
						// NOTICE: とりあえず、自動ページ移動はする？
						mViewPager.arrowScroll(View.FOCUS_RIGHT);
						// mViewPager.setCurrentItem(0);
						return;
					}
					else if( dispBlock.getData() instanceof ActivityLapData )
					{
						
						
						return;
					}
				}
			}
			else
			{
				// TODO: 後で調整
//				if( v == gpxShareButton )
//				{
//					// GPXの共有を行
//			        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);//ACTION_SEND);
//			        intent.addCategory(Intent.CATEGORY_DEFAULT);
//			        intent.addCategory(Intent.CATEGORY_BROWSABLE);
//			        intent.setDataAndType(Uri.fromFile(new File(gpxFilePath)), "application/gpx+xml");
//			        // intent.putExtra(Intent.EXTRA_TEXT, gpxFilePath);
//			        startActivity(Intent.createChooser(
//			                intent, getString(R.string.gpx_share)));
//					
//				}
			}
		} finally {
			if( v != null )
			{
				//v.setEnabled(true);
			}
		}
		
		// NOTICE:ここまで来た場合、ページを削除する?
//		Log.v("page-delete", "page-delele");
//		selectedActivityData = null;
//		adapter.setCount(1);
//		adapter.notifyDataSetChanged();
	}
	
}
