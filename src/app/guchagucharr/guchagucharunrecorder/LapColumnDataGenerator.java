package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.provider.BaseColumns;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunningLogStocker;
import app.guchagucharr.service.SQLiteContract;

public class LapColumnDataGenerator implements IColumnDataGenerator {

	static final int LAP_TABLE_COLUMN_COUNT = 10;
	@Override
	public ColumnData[] generate(Activity activity, RunningLogStocker source, int index) {
		
		// boolean hidden, String columnName, String labelBefore,
		// String labelAfter, String dataType, String text, String hint
		ColumnData[] columns = {
		new ColumnData( activity, true, false, BaseColumns._ID, null, null, SQLiteContract.INTEGER, null, null ),
		new ColumnData( activity, RunHistoryTableContract.START_DATETIME, 
				R.string.label_start_date_time, null,
				// TODO: 値の設定
				SQLiteContract.INTEGER, "", R.string.hint_startdatetime ),
		new ColumnData( activity, RunHistoryTableContract.INSERT_DATETIME, 
				R.string.label_insert_date_time, null,
				// TODO: 値の設定
				SQLiteContract.INTEGER, "", R.string.hint_insertdatetime ),
		new ColumnData( activity, true, false, RunHistoryTableContract.PARENT_ID, 
				null, null,
				SQLiteContract.INTEGER, "", null ),
		new ColumnData( activity, RunHistoryTableContract.NAME, 
				R.string.label_lapname, null,
				SQLiteContract.TEXT, "", R.string.hint_lapname ),
		new ColumnData( activity, true, false, RunHistoryTableContract.LAP_INDEX, 
				null, null,
				SQLiteContract.INTEGER, "", null ),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_DISTANCE,
				// TODO:単位は設定によって切替
				R.string.label_lap_distance, R.string.label_unit_distance,
				SQLiteContract.REAL, String.valueOf(source.getLapData(index).getDistance()), R.string.hint_lapdistance ),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_TIME,
				// TODO:編集方法
				R.string.label_lap_time, null,
				SQLiteContract.INTEGER, String.valueOf(
						source.getLapData(index).getStopTime() - source.getLapData(index).getStartTime())
						, R.string.hint_lap_time ),
		new ColumnData( activity, false, RunHistoryTableContract.LAP_SPEED,
				// TODO:単位は設定によって切替
				R.string.label_lap_speed, R.string.label_unit_speed,
				SQLiteContract.REAL, String.valueOf(source.getLapData(index).getSpeed())
				, R.string.hint_lap_speed ),
		new ColumnData( activity, RunHistoryTableContract.LAP_FIXED_DISTANCE,
				// TODO:単位は設定によって切替
				R.string.label_lap_distance_fixed, R.string.label_unit_distance,
				SQLiteContract.REAL, String.valueOf(source.getLapData(index).getDistance()),
				R.string.hint_lapdistance_fixed ),
		new ColumnData( activity, RunHistoryTableContract.LAP_FIXED_TIME,
				// TODO:編集方法
				R.string.label_lap_time_fixed, null,
				SQLiteContract.INTEGER, String.valueOf(
						source.getLapData(index).getStopTime() - source.getLapData(index).getStartTime())
						, R.string.hint_lap_time_fixed ),
		new ColumnData( activity, RunHistoryTableContract.LAP_FIXED_SPEED,
				// TODO:単位は設定によって切替
				R.string.label_lap_speed_fixed, R.string.label_unit_speed,
				SQLiteContract.REAL, String.valueOf(source.getLapData(index).getSpeed())
				, R.string.hint_lap_speed_fixed ),
		new ColumnData( activity, false, RunHistoryTableContract.GPX_FILE_PATH, 
				R.string.label_gpxfilepath, null,
				SQLiteContract.TEXT, source.getLapData(index).getGpxFilePath(),
				R.string.hint_gpxfilepath ),				
		new ColumnData( activity, RunHistoryTableContract.GPX_FILE_PATH, 
				R.string.label_gpxfilepath_fixed, null,
				SQLiteContract.TEXT, source.getLapData(index).getGpxFilePath(),
				R.string.hint_gpxfilepath_fixed ),				
		};
		
		return columns;
	}

}
