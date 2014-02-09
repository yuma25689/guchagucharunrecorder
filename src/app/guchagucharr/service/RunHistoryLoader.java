package app.guchagucharr.service;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;

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
	 * @return the historyLapData
	 */
	public SparseArray<Vector<ActivityLapData>> getHistoryLapDatas() {
		return historyLapData;
	}
	/**
	 * @return the historyLapData
	 */
	public Vector<ActivityLapData> getHistoryLapData(int parentId) {
		return historyLapData.get(parentId);
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
					int datetimeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.INSERT_DATETIME );
					int lapCountIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.LAP_COUNT);
					int placeIdIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.PLACE_ID );				
					int nameIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.NAME);
					int gpxIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_PATH);
					data.setId( cursor.getInt( idIndex ) );
					data.setDateTime( cursor.getLong( datetimeIndex ) );
					data.setLapCount( cursor.getLong( lapCountIndex ) );
					data.setPlaceId( cursor.getLong( placeIdIndex ) );
					data.setName( cursor.getString( nameIndex ) );
					data.setGpxFilePath( cursor.getString( gpxIndex ) );
								
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
				int parentIdIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.PARENT_ID);			
				int lapIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_INDEX);
				int lapDistanceIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_DISTANCE);
				int lapTimeIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_TIME);
				int lapSpeedIndex = cursorChild.getColumnIndexOrThrow(RunHistoryTableContract.LAP_SPEED);
				int lapFixedDistanceIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_DISTANCE);
				int lapFixedTimeIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_TIME);
				int lapFixedSpeedIndex = cursorChild.getColumnIndexOrThrow(
						RunHistoryTableContract.LAP_FIXED_SPEED);
				do {
					ActivityLapData dataLap = new ActivityLapData();
					dataLap.setId( cursorChild.getLong( idIndex ) );
					dataLap.setParentId( cursorChild.getInt( parentIdIndex ) );
					dataLap.setLapIndex( cursorChild.getLong( lapIndex ) );
					dataLap.setDistance( cursorChild.getDouble( lapDistanceIndex ) );
					dataLap.setTime( cursorChild.getLong( lapTimeIndex ) );
					dataLap.setSpeed( cursorChild.getDouble( lapSpeedIndex ) );
					dataLap.setFixedDistance( cursorChild.getDouble( lapFixedDistanceIndex ) );
					dataLap.setFixedTime( cursorChild.getLong( lapFixedTimeIndex ) );
					dataLap.setFixedSpeed( cursorChild.getDouble( lapFixedSpeedIndex ) );
				
					
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
	
	
	
	public class ActivityData {
		int id; 
		long dateTime;
		String name;
		long lapCount;
		long placeId;
        String gpxFilePath;
		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}
		/**
		 * @return the dateTime
		 */
		public long getDateTime() {
			return dateTime;
		}
		/**
		 * @param dateTime the dateTime to set
		 */
		public void setDateTime(long dateTime) {
			this.dateTime = dateTime;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the lapCount
		 */
		public long getLapCount() {
			return lapCount;
		}
		/**
		 * @param lapCount the lapCount to set
		 */
		public void setLapCount(long lapCount) {
			this.lapCount = lapCount;
		}
		/**
		 * @return the placeId
		 */
		public long getPlaceId() {
			return placeId;
		}
		/**
		 * @param placeId the placeId to set
		 */
		public void setPlaceId(long placeId) {
			this.placeId = placeId;
		}
		/**
		 * @return the gpxFilePath
		 */
		public String getGpxFilePath() {
			return gpxFilePath;
		}
		/**
		 * @param gpxFilePath the gpxFilePath to set
		 */
		public void setGpxFilePath(String gpxFilePath) {
			this.gpxFilePath = gpxFilePath;
		}
	};
	public class ActivityLapData {
		long id; 
		long dateTime;
		int parentId; 
		long lapIndex; 
		double distance; 
		long time; 
		double speed; 
		double fixedDistance; 
		long fixedTime; 
		double fixedSpeed;
		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(long id) {
			this.id = id;
		}
		/**
		 * @return the dateTime
		 */
		public long getDateTime() {
			return dateTime;
		}
		/**
		 * @param dateTime the dateTime to set
		 */
		public void setDateTime(long dateTime) {
			this.dateTime = dateTime;
		}
		/**
		 * @return the parentId
		 */
		public int getParentId() {
			return parentId;
		}
		/**
		 * @param parentId the parentId to set
		 */
		public void setParentId(int parentId) {
			this.parentId = parentId;
		}
		/**
		 * @return the lapIndex
		 */
		public long getLapIndex() {
			return lapIndex;
		}
		/**
		 * @param lapIndex the lapIndex to set
		 */
		public void setLapIndex(long lapIndex) {
			this.lapIndex = lapIndex;
		}
		/**
		 * @return the distance
		 */
		public double getDistance() {
			return distance;
		}
		/**
		 * @param distance the distance to set
		 */
		public void setDistance(double distance) {
			this.distance = distance;
		}
		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}
		/**
		 * @param time the time to set
		 */
		public void setTime(long time) {
			this.time = time;
		}
		/**
		 * @return the speed
		 */
		public double getSpeed() {
			return speed;
		}
		/**
		 * @param speed the speed to set
		 */
		public void setSpeed(double speed) {
			this.speed = speed;
		}
		/**
		 * @return the fixedDistance
		 */
		public double getFixedDistance() {
			return fixedDistance;
		}
		/**
		 * @param fixedDistance the fixedDistance to set
		 */
		public void setFixedDistance(double fixedDistance) {
			this.fixedDistance = fixedDistance;
		}
		/**
		 * @return the fixedTime
		 */
		public long getFixedTime() {
			return fixedTime;
		}
		/**
		 * @param fixedTime the fixedTime to set
		 */
		public void setFixedTime(long fixedTime) {
			this.fixedTime = fixedTime;
		}
		/**
		 * @return the fixedSpeed
		 */
		public double getFixedSpeed() {
			return fixedSpeed;
		}
		/**
		 * @param fixedSpeed the fixedSpeed to set
		 */
		public void setFixedSpeed(double fixedSpeed) {
			this.fixedSpeed = fixedSpeed;
		} 
	};

}
