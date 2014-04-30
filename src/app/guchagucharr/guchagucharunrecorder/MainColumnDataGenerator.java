package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.provider.BaseColumns;
import app.guchagucharr.guchagucharunrecorder.util.ActivityData;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.SQLiteContract;

public class MainColumnDataGenerator implements IColumnDataGenerator {
	static final int MAIN_TABLE_COLUMN_COUNT = 10;

	@Override
	public ColumnData[] generate(Activity activity, ActivityData data) {
//		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
//		+ RunHistoryTableContract.START_DATETIME + " INTEGER,"        		
//		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
//		+ RunHistoryTableContract.NAME + " TEXT,"
//		+ RunHistoryTableContract.LAP_COUNT + " INTEGER,"
//		+ RunHistoryTableContract.PLACE_ID + " INTEGER"
		
		// boolean hidden, String columnName, String labelBefore,
		// String labelAfter, String dataType, String text, String hint
		ColumnData[] columns = {
		new ColumnData( activity, true, false, BaseColumns._ID, null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getId()), null ),
		// TODO: ここも、できたらFix版と分けた方が良いかもしれない
		new ColumnData( activity, RunHistoryTableContract.START_DATETIME, 
				R.string.label_start_date_time, null,
				SQLiteContract.INTEGER, String.valueOf(data.getStartDateTime())
				, R.string.hint_startdatetime
				, ColumnData.EDIT_METHDO_DATETIME
				),
		new ColumnData( activity, true, false, RunHistoryTableContract.INSERT_DATETIME, 
				null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getInsertDateTime()), null ),
				//R.string.label_insert_date_time, null,
//				SQLiteContract.INTEGER, String.valueOf(data.getInsertDateTime())
//				, R.string.hint_insertdatetime
//				, ColumnData.EDIT_METHDO_DATETIME			
//				),
		new ColumnData( activity, RunHistoryTableContract.NAME, 
				R.string.label_lapname, null,
				SQLiteContract.TEXT, data.getName(), R.string.hint_lapname ),
		new ColumnData( activity, true, false, RunHistoryTableContract.LAP_COUNT, 
				null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getLapCount()), null ),
		new ColumnData( activity, true, false, RunHistoryTableContract.PLACE_ID,
				// TODO:値の設定
				null, null,
				SQLiteContract.INTEGER, String.valueOf(data.getPlaceId()), null )
		};
		return columns;
	}

	@Override
	public ColumnData[] generate(Activity activity, ActivityLapData data) {
		
		return null;
	}

}
