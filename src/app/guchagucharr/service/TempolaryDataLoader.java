package app.guchagucharr.service;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;

public class TempolaryDataLoader {
	
	ArrayList<TempolaryData> dataArray = new ArrayList<TempolaryData>();
	/**
	 * @return the historyData
	 */
	public ArrayList<TempolaryData> getData() {
		return dataArray;
	}
	public void clear()
	{
		dataArray.clear();
	}
	public int load(Context context)
	{
		clear();
		// ContentProvider検索
		try {
			// 親テーブルの検索
			String tempTblUri = RunHistoryTableContract.CONTENT_URI_STRING 
					+ "/" + RunHistoryTableContract.TEMPOLARY_INFO_TABLE_NAME;
			LogWrapper.v("uri",tempTblUri);			
			Cursor cursor = context.getContentResolver().query(
					Uri.parse( tempTblUri ),
				    null,//mProjection,
				    null,//mSelectionClause,
				    null,//mSeletionArgs,
				    null );
				    //RunHistoryTableContract.INSERT_DATETIME + " desc" );//mSortOrder);
			// 全レコードループ
			if( 0 < cursor.getCount() )
			{
				//カーソルを移動(レコードの先頭に)
				cursor.moveToFirst();
				//do {
				// 必ず１つしかないものとする
				// NOTICE やってから気づいたが、テーブルにせずBundle等でもいいかもしれない。
					TempolaryData data = new TempolaryData();
					int currentModeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.CURRENT_MODE );
					int startDatetimeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.START_DATETIME );
					int gpxDirIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_DIR);
					int activityTypeCodeIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.ACTIVITY_TYPE);
					data.setCurrentMode( cursor.getInt( currentModeIndex ) );
					data.setStartDateTime( cursor.getLong( startDatetimeIndex ) );
					data.setGpxDir( cursor.getString( gpxDirIndex ) );
					data.setActivityTypeCode( cursor.getInt( activityTypeCodeIndex ) );
					//int gpxIndex = cursor.getColumnIndexOrThrow(RunHistoryTableContract.GPX_FILE_PATH);
					//data.setGpxFilePath( cursor.getString( gpxIndex ) );
								
					dataArray.add( data );			
				//} while( cursor.moveToNext() );
			}
			//カーソルを閉じる
			cursor.close();
		} catch( IllegalArgumentException ex ) {
			ex.printStackTrace();
			LogWrapper.e("Tempolary table load Error",ex.getMessage());
			return -1;
		}
		
		return 0;
	}
	
	
	
	public class TempolaryData {
		int id; 
		long startDateTime;
		String gpxDir;
		int currentMode;
		int activityTypeCode;
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
		 * @return the startDateTime
		 */
		public long getStartDateTime() {
			return startDateTime;
		}
		/**
		 * @param startDateTime the startDateTime to set
		 */
		public void setStartDateTime(long startDateTime) {
			this.startDateTime = startDateTime;
		}
		/**
		 * @return the gpxDir
		 */
		public String getGpxDir() {
			return gpxDir;
		}
		/**
		 * @param gpxDir the gpxDir to set
		 */
		public void setGpxDir(String gpxDir) {
			this.gpxDir = gpxDir;
		}
		/**
		 * @return the currentMode
		 */
		public int getCurrentMode() {
			return currentMode;
		}
		/**
		 * @param currentMode the currentMode to set
		 */
		public void setCurrentMode(int currentMode) {
			this.currentMode = currentMode;
		}
		/**
		 * @return the activityTypeCode
		 */
		public int getActivityTypeCode() {
			return activityTypeCode;
		}
		/**
		 * @param activityTypeCode the activityTypeCode to set
		 */
		public void setActivityTypeCode(int activityTypeCode) {
			this.activityTypeCode = activityTypeCode;
		}
	};

}
