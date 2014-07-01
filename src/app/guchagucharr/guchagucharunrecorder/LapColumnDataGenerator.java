package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.provider.BaseColumns;
import app.guchagucharr.guchagucharunrecorder.util.ActivityData;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.SQLiteContract;

public class LapColumnDataGenerator implements IColumnDataGenerator {

	@Override
	public ColumnData[] generate(Activity activity, ActivityData data) {
		// インタフェースで必須の関数なので、この関数がこのクラスにもあるが、
		// これは本当はこのクラスには必要なしのダミー関数
		return null;
	}

	static final int LAP_TABLE_COLUMN_COUNT = 10;
	@Override
	public ColumnData[] generate(Activity activity, ActivityLapData data) {//RunningLogStocker source, int index) {
		
		// boolean hidden, String columnName, String labelBefore,
		// String labelAfter, String dataType, String text, String hint
		ColumnData[] columns = {
		new ColumnData( activity, true, false, BaseColumns._ID, null, null, SQLiteContract.INTEGER, 
				String.valueOf(data.getId()), null ),
		// TODO: ここも、できたらFix版と分けた方が良いかもしれない
		new ColumnData( activity, RunHistoryTableContract.START_DATETIME, 
				R.string.label_start_date_time, null,
				SQLiteContract.INTEGER, String.valueOf(data.getStartDateTime())
				, R.string.hint_startdatetime
				, ColumnData.EDIT_METHDO_DATETIME
				),
		new ColumnData( activity,true, false, RunHistoryTableContract.INSERT_DATETIME, 
				//R.string.label_insert_date_time,
//				null,SQLiteContract.INTEGER, String.valueOf(data.getInsertDateTime())
//				, R.string.hint_insertdatetime
//				, ColumnData.EDIT_METHDO_DATETIME			
//				),
				null, null, SQLiteContract.INTEGER, String.valueOf(data.getInsertDateTime()), null ),
		new ColumnData( activity, true, false, RunHistoryTableContract.PARENT_ID,
				null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getParentId()), null ),
		new ColumnData( activity, RunHistoryTableContract.NAME, 
				R.string.label_lapname, null,
				SQLiteContract.TEXT, data.getName(), R.string.hint_lapname ),
		new ColumnData( activity, true, false, RunHistoryTableContract.LAP_INDEX, 
				null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getLapIndex()), null ),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_DISTANCE,
				// TODO:単位は設定によって切替
				R.string.label_lap_distance, R.string.label_unit_distance,
				SQLiteContract.REAL, 
						String.valueOf(data.getDistance()), R.string.hint_lapdistance
				,ColumnData.EDIT_METHDO_DISTANCE),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_TIME,
				R.string.label_lap_time, null,
				SQLiteContract.INTEGER, 
						String.valueOf(data.getTime())
						, R.string.hint_lap_time,
						ColumnData.EDIT_METHDO_TIME),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_SPEED,
				// TODO:単位は設定によって切替
				R.string.label_lap_speed, R.string.label_unit_speed,
				SQLiteContract.REAL, String.valueOf(data.getDistance() / data.getTime() )//data.getSpeed())
				, R.string.hint_lap_speed,
				ColumnData.EDIT_METHDO_SPEED),
		new ColumnData( activity, RunHistoryTableContract.LAP_FIXED_DISTANCE,
				// TODO:単位は設定によって切替
				R.string.label_lap_distance_fixed, R.string.label_unit_distance,
				SQLiteContract.REAL, String.valueOf(getFixedDistance(data)),
				R.string.hint_lapdistance_fixed 
				, ColumnData.EDIT_METHDO_DISTANCE
				),
		new ColumnData( activity, RunHistoryTableContract.LAP_FIXED_TIME,
				// TODO:編集方法
				R.string.label_lap_time_fixed, null,
				SQLiteContract.INTEGER, 
					String.valueOf(getFixedTime(data))
					, R.string.hint_lap_time_fixed
						, ColumnData.EDIT_METHDO_TIME
				),
				// TODO:速度は入力不可に
		new ColumnData( activity, true, false, RunHistoryTableContract.LAP_FIXED_SPEED,
				null, null,
				//R.string.label_lap_speed_fixed, R.string.label_unit_speed,
				SQLiteContract.REAL, String.valueOf(getFixedDistance(data) / getFixedTime(data) ), null //String.valueOf(data.getSpeed())
				//, R.string.hint_lap_speed_fixed 
				//, ColumnData.EDIT_METHDO_REAL				
				),
		new ColumnData( activity, false, RunHistoryTableContract.GPX_FILE_PATH, 
				R.string.label_gpxfilepath, null,
				SQLiteContract.TEXT, data.getGpxFilePath(),
				R.string.hint_gpxfilepath ),		
		new ColumnData( activity, RunHistoryTableContract.GPX_FILE_PATH_FIXED, 
				R.string.label_gpxfilepath_fixed, null,
				SQLiteContract.TEXT, data.getGpxFilePath(),
				R.string.hint_gpxfilepath_fixed ),				
		};
		
		return columns;
	}
	
	public double getFixedDistance( ActivityLapData data )
	{
		return data.getFixedDistance() != 0 ? 
				data.getFixedDistance() :
				data.getDistance();
	}
	public long getFixedTime( ActivityLapData data )
	{
		return data.getFixedTime() != 0 ? 
				data.getFixedTime() :
				data.getTime();
	}

}
