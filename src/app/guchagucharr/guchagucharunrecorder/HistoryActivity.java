package app.guchagucharr.guchagucharunrecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import app.guchagucharr.guchagucharunrecorder.DisplayBlock.eShapeType;
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunHistoryLoader;
import app.guchagucharr.service.RunHistoryTableContract;
//import android.provider.BaseColumns;
import app.guchagucharr.service.RunHistoryLoader.ActivityData;
import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;

public class HistoryActivity extends Activity implements IPageViewController, OnClickListener {
	private ActivityData selectedActivityData = null;
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;	
	private ViewGroup componentContainer;
	private PagerHandler handler;
	private HistoryPagerAdapter adapter = null;
	//private RelativeLayout lastMainLayout = null;
	private RelativeLayout lastSubLayout = null;
	
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
        dispInfo.init(this, componentContainer, handler, false);
        adapter = new HistoryPagerAdapter(this, this);
        super.onResume();
    }
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
			Vector<ActivityLapData> lapDatas = loader.getHistoryLapData(data.getId());
			for( ActivityLapData lapData: lapDatas )
			{
				distanceTotal += lapData.getDistance();
				timeTotal += lapData.getTime();				
				// speedは、時間と距離から計算したものの方が違和感がなく、圧倒的に精確
				//speedTotal += //lapData.getSpeed();
			}	
			speedTotal = distanceTotal / ( timeTotal / 1000 ); 
			
			// DisplayBlock追加
			String titleDateTime = sdfDateTime.format(new Date(data.getStartDateTime()));
//			String titleDate = sdfDate.format(new Date(data.getDateTime()));
//			String titleTime = sdfTime.format(new Date(data.getDateTime()));
			String title = titleDateTime;//titleDate + System.getProperty("line.separator") + titleTime;
			String gpxExists = null;
			String lapCount = null;
			String gpx = data.getGpxFilePath();
			if( gpx != null )
			{
				File file = new File(gpx);
				if( file.exists() )
				{
					gpxExists = getString(R.string.GPX_EXISTS);
				}
				else
				{
					gpxExists = getString(R.string.GPX_LOSE);
				}
			}
			if( 1 < data.getLapCount() )
			{
				lapCount = getString( R.string.LAP_COUNT_LABEL ) + data.getLapCount();
			}
			String text[] = {
					LapData.createDistanceFormatText( distanceTotal ),
					LapData.createTimeFormatText( timeTotal ),
					//LapData.createSpeedFormatText( speedTotal ),
					LapData.createSpeedFormatTextKmPerH( speedTotal ),
					gpxExists,
					lapCount
			};
			DisplayBlock dispBlock = new DisplayBlock(
					this, data.getId(), dispInfo, title, text, sizeType, eShapeType.SHAPE_BLOCK);
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
	public void updateSubPage(RelativeLayout rl)
	{
		lastSubLayout = rl;
		lastSubLayout.removeAllViews();
		Vector<ActivityLapData> lapData = loader.getHistoryLapData(selectedActivityData.getId());
		if( lapData == null )
		{
			return;
		}
		// NOTICE: 最高で６個しか置けない
		DisplayBlock.eSizeType sizeType = DisplayBlock.getProperSizeTypeFromCount(
				lapData.size());
//		SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//				getString(R.string.datetime_display_format));
		int lastOddPanelID = 0;
		int iPanelCount = 0;
		for( ActivityLapData data : lapData )
		{
			double distance = 0;
			double speed = 0;
			long time = 0;
			distance = data.getDistance();
			time = data.getTime();				
			speed = distance / ( time / 1000 ); 
			
			// DisplayBlock追加
			String title = getString(R.string.LAP_LABEL) + ( data.getLapIndex() + 1 );
			String text[] = {
					LapData.createDistanceFormatText( distance ),
					LapData.createTimeFormatText( time ),
					LapData.createSpeedFormatTextKmPerH( speed ),
			};
			DisplayBlock dispBlock = new DisplayBlock(
					this, data.getId(), dispInfo, title, text, sizeType, eShapeType.SHAPE_HORIZONTAL);
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
		
	}
	@Override
	public DisplayInfo getDispInfo() {
		return dispInfo;
	}
	static final int CONTEXT_MENU_DETAIL_ID = 0;
	static final int CONTEXT_MENU_DELETE_ID = 1;
	static final int CONTEXT_MENU_SHARE_ID = 2;
	 
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	 
	    super.onCreateContextMenu(menu, v, menuInfo);
	 
	    //コンテキストメニューの設定
	    DisplayBlock block = (DisplayBlock) v;
	    menu.setHeaderTitle(block.getTitle());
	    // menu.setHeaderView
	    //menu.setHeaderIcon
	    //Menu.add(int groupId, int itemId, int order, CharSequence title)
	    menu.add(CONTEXT_MENU_DETAIL_ID, (int)block.getRecordId(), 0, R.string.menu_detail);
	    menu.add(CONTEXT_MENU_SHARE_ID, (int)block.getRecordId(), 0, R.string.menu_share);
	    menu.add(CONTEXT_MENU_DELETE_ID, (int)block.getRecordId(), 0, R.string.menu_delete);
	    
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getGroupId()) {
	    case CONTEXT_MENU_DETAIL_ID:
	        // メニュー押下時の操作
	        return true;
	    case CONTEXT_MENU_SHARE_ID:
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
	        	// TODO:GPXファイルの削除
	        	String gpxFile = null;
	        	for( ActivityData data : loader.getHistoryData() )
	        	{
	        		if( data.getId() == item.getItemId() )
	        		{
	        			gpxFile = data.getGpxFilePath();
	        			break;
	        		}
	        	}
	        	if( gpxFile != null )
	        	{
	        		File file = new File( gpxFile );
	        		if( file.exists() )
	        		{
	        			file.delete();
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
						RunHistoryTableContract._ID + "=" + item.getItemId(),null);
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
	    default:
	        return super.onContextItemSelected(item);
	    }
	}
	@Override
	public void onClick(View v) {
		// DispBlockからしか呼ばれないことにしている
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
		
		// NOTICE:ここまで来た場合、ページを削除する?
//		Log.v("page-delete", "page-delele");
//		selectedActivityData = null;
//		adapter.setCount(1);
//		adapter.notifyDataSetChanged();
	}
	
}
