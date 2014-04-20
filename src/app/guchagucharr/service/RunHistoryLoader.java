package app.guchagucharr.service;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;
import app.guchagucharr.guchagucharunrecorder.util.ActivityData;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;

public class RunHistoryLoader {
	
	ArrayList<ActivityData> historyData = new ArrayList<ActivityData>(); 
	SparseArray<Vector<ActivityLapData>> historyLapData = new SparseArray<Vector<ActivityLapData>>(); 
	
	/**
	 * @return the historyData
	 */
	public ArrayList<ActivityData> getHistoryData() {
		return historyData;
	}
	/**
	 * @return the historyData
	 */
	public ActivityData getHistoryData(int id) {
		for( ActivityData data : historyData )
		{
			if( data.getId() == id )
			{
				return data;
			}
		}
		return null;
	}
	/**
	 * @return the historyLapData
	 */
	public SparseArray<Vector<ActivityLapData>> getHistoryLapDatas() {
		return historyLapData;
	}
	/**
	 * @return the historyLapData
	 */
	public Vector<ActivityLapData> getHistoryLapDatas(int parentId) {
		return historyLapData.get(parentId);
	}
	/**
	 * @return the historyLapData
	 */
	public ActivityLapData getHistoryLapData(int parentId, int recordId) {
		for( ActivityLapData data : historyLapData.get(parentId) )
		{
			if( data.getId() == recordId )
			{
				return data;
			}
		}
		return null;
	}
	public void clear()
	{
		historyData.clear();
		historyLapData.clear();
	}
	public int load(Activity activity)
	{
		clear();
		// ContentProvider検索
		try {
			// 親テーブルの検索
			String history = RunHistoryTableContract.CONTENT_URI_STRING 
					+ "/" + RunHistoryTableContract.HISTORY_TABLE_NAME;
			Log.v("uri",history);			
			Cursor cursor = activity.getContentResolver().query(
					Uri.parse( history ),
				    null,//mProjection,
				    null,//mSelectionClause,
				    null,//mSeletionArgs,
				    RunHistoryTableContract.INSERT_DATETIME + " desc" );//mSortOrder);
			
			// 全レコードループ
			if( 0 < cursor.getCount() )
			{
				//カーソルを移動(レコードの先頭に)
				cursor.moveToFirst();
				do {
					ActivityData data = new ActivityData();
					int idIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
					int startDatetimeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.START_DATETIME );
					int insertDatetimeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.INSERT_DATETIME );
					int lapCountIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.LAP_COUNT);
					int placeIdIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.PLACE_ID );				
					int nameIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.NAME);
					data.setId( cursor.getInt( idIndex ) );
					data.setStartDateTime( cursor.getLong( startDatetimeIndex ) );
					data.setInsertDateTime( cursor.getLong( insertDatetimeIndex ) );
					data.setLapCount( cursor.getLong( lapCountIndex ) );
					data.setPlaceId( cursor.getLong( placeIdIndex ) );
					data.setName( cursor.getString( nameIndex ) );
					//int gpxIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_PATH);
					//data.setGpxFilePath( cursor.getString( gpxIndex ) );
								
					historyData.add( data );			
				} while( cursor.moveToNext() );
			}
			//カーソルを閉じる
			cursor.close();
		} catch( IllegalArgumentException ex ) {
			ex.printStackTrace();
			Log.e("Error",ex.getMessage());
			return -1;
		}
		try 
		{
			String historyLap = RunHistoryTableContract.CONTENT_URI_STRING 
					+ "/" + RunHistoryTableContract.HISTORY_LAP_TABLE_NAME;
			Log.v("uri",historyLap);
			Cursor cursorChild = activity.getContentResolver().query(
					Uri.parse( historyLap ),
				    null,//mProjection,
				    null,//RunHistoryTableContract.PARENT_ID + "=" + id,//mSelectionClause,
				    null,//mSeletionArgs,
				    RunHistoryTableContract.PARENT_ID + "," 
				    + RunHistoryTableContract.LAP_INDEX);//mSortOrder);
			
			// 全レコードループ
			if( 0 < cursorChild.getCount() )
			{
				Vector<ActivityLapData> vLapData = new Vector<ActivityLapData>();
				
				cursorChild.moveToFirst();
				int idIndex = cursorChild.getColumnIndexOrThrow(BaseColumns._ID);				
				int startDateTimeIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.START_DATETIME);			
				int insertDateTimeIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.INSERT_DATETIME);			
				int parentIdIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.PARENT_ID);			
				int lapIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_INDEX);
				int lapDistanceIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_DISTANCE);
				int lapTimeIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_TIME);
				int lapSpeedIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_SPEED);
				int lapFixedDistanceIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_DISTANCE);
				int lapFixedTimeIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_TIME);
				int lapNameIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.NAME);
				int lapFixedSpeedIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_SPEED);
				int gpxIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_PATH);
				int gpxFixedIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_PATH);
				do {
					ActivityLapData dataLap = new ActivityLapData();
					dataLap.setId( cursorChild.getLong( idIndex ) );
					dataLap.setStartDateTime( cursorChild.getLong( startDateTimeIndex ) );
					dataLap.setInsertDateTime( cursorChild.getLong( insertDateTimeIndex ) );
					dataLap.setParentId( cursorChild.getInt( parentIdIndex ) );
					dataLap.setLapIndex( cursorChild.getLong( lapIndex ) );
					dataLap.setDistance( cursorChild.getDouble( lapDistanceIndex ) );
					dataLap.setTime( cursorChild.getLong( lapTimeIndex ) );
					dataLap.setSpeed( cursorChild.getDouble( lapSpeedIndex ) );
					dataLap.setFixedDistance( cursorChild.getDouble( lapFixedDistanceIndex ) );
					dataLap.setFixedTime( cursorChild.getLong( lapFixedTimeIndex ) );
					dataLap.setFixedSpeed( cursorChild.getDouble( lapFixedSpeedIndex ) );
					dataLap.setName( cursorChild.getString( lapNameIndex ));
					dataLap.setGpxFilePath( cursorChild.getString( gpxIndex ) );				
					dataLap.setGpxFixedFilePath( cursorChild.getString( gpxIndex ) );				
					
					if( vLapData.isEmpty() == false
					&& dataLap.getParentId() != vLapData.lastElement().getParentId() )
					{
						historyLapData.put( vLapData.lastElement().getParentId(), vLapData );
						vLapData = new Vector<ActivityLapData>();
						vLapData.add(dataLap);
					}
					else
					{
						vLapData.add(dataLap);
					}
				} while( cursorChild.moveToNext() );
				historyLapData.put( vLapData.lastElement().getParentId(), vLapData );
			}
			if( cursorChild != null )
			{
				cursorChild.close();
			}
		} catch( IllegalArgumentException ex ) {
			ex.printStackTrace();
			Log.e("Error",ex.getMessage());
			return -1;
		}
		
		return 0;
	}
}
