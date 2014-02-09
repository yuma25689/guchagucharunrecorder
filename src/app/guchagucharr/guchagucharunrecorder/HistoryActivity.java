package app.guchagucharr.guchagucharunrecorder;

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
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import app.guchagucharr.interfaces.IPageViewController;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.RunHistoryLoader;
import app.guchagucharr.service.RunHistoryTableContract;
//import android.provider.BaseColumns;
import app.guchagucharr.service.RunHistoryLoader.ActivityData;
import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;

public class HistoryActivity extends Activity implements IPageViewController {
	private DisplayInfo dispInfo = DisplayInfo.getInstance();
	private ViewPager mViewPager;	
	private ViewGroup componentContainer;
	private PagerHandler handler;
	
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
        super.onResume();
    }
	@Override
	public int initPager()
	{
        init();		
        this.mViewPager = (ViewPager)this.findViewById(R.id.viewpager1);
        this.mViewPager.setAdapter(new HistoryPagerAdapter(this, this));
        
        return 0;
	}
//	do {
//		double distance = cursor.getDouble( lapDistanceIndex );
//		distanceTotal += distance;
//		double speed = cursor.getDouble( lapSpeedIndex );
//		speedTotal += speed;
//		long time = cursor.getLong( lapTimeIndex );
//		timeTotal += time;
	
	static final int FIRST_PANEL_ID = 1500;
	
	@Override
	public int initControls( int position, RelativeLayout rl )
	{
		ArrayList<ActivityData> mainData = loader.getHistoryData();
		DisplayBlock.eSizeType sizeType = DisplayBlock.getProperSizeTypeFromCount(
				mainData.size());
		SimpleDateFormat sdfDateTime = new SimpleDateFormat(getString(R.string.datetime_display_format));
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
			String titleDateTime = sdfDateTime.format(new Date(data.getDateTime()));
//			String titleDate = sdfDate.format(new Date(data.getDateTime()));
//			String titleTime = sdfTime.format(new Date(data.getDateTime()));
			String title = titleDateTime;//titleDate + System.getProperty("line.separator") + titleTime;
			String text[] = {
					LapData.createDistanceFormatText( distanceTotal ),
					LapData.createTimeFormatText( timeTotal ),
					//LapData.createSpeedFormatText( speedTotal ),
					LapData.createSpeedFormatTextKmPerH( speedTotal )
			};
			DisplayBlock dispBlock = new DisplayBlock(this, data.getId(), dispInfo, title, text, sizeType);
			if( iPanelCount == 0 )
			{
				RelativeLayout.LayoutParams lp = (LayoutParams) dispBlock.getLayoutParams();
				lp.addRule(RelativeLayout.ALIGN_LEFT);
				lp.addRule(RelativeLayout.ALIGN_TOP);
				dispBlock.setId(FIRST_PANEL_ID);
				lastOddPanelID = dispBlock.getId();
			}
			else
			{
				dispBlock.setId(FIRST_PANEL_ID+iPanelCount);
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
			final double COLOR_RANGE = 100;
			double rate = COLOR_RANGE / mainData.size();
			int iMinus = (int) (iPanelCount * rate);
			dispBlock.setBackgroundColorAsStateList(0xFF, 0, 20 + iMinus, 155 + iMinus);
			//dispBlock.setBackgroundColor(Color.argb(0xFF, 0, 20 + iMinus, 155 + iMinus));
			rl.addView(dispBlock);
			iPanelCount++;
		}
		return 0;
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
	    menu.add(CONTEXT_MENU_DELETE_ID, (int)block.getRecordId(), 0, R.string.menu_delete);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getGroupId()) {
	    case CONTEXT_MENU_DETAIL_ID:
	        // メニュー押下時の操作
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
	        } finally {
	        	// TODO: 削除失敗のメッセージ出力
	        	Log.e("delete failed!","???");
	        	//db.endTransaction();
	    		getContentResolver().insert(
	    				Uri.parse("content://" 
	    				+ RunHistoryTableContract.AUTHORITY + "/" 
	    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
	    				,null);
	        	
	        }
	    	
	        return true;
	    default:
	        return super.onContextItemSelected(item);
	    }
	}
	
}
