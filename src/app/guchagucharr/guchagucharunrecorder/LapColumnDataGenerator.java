package app.guchagucharr.guchagucharunrecorder;

import java.util.Vector;

import android.provider.BaseColumns;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.interfaces.IColumnDataGenerator;
import app.guchagucharr.service.RunHistoryTableContract;
import app.guchagucharr.service.RunningLogStocker;
import app.guchagucharr.service.SQLiteContract;

public class LapColumnDataGenerator implements IColumnDataGenerator {

	static final int LAP_TABLE_COLUMN_COUNT = 10;
	@Override
	public Vector<ColumnData> generate(RunningLogStocker source) {
		
		// boolean hidden, String columnName, String labelBefore,
		// String labelAfter, String dataType, String text, String hint
		ColumnData[] columns = {
		new ColumnData( true, BaseColumns._ID, null, null, SQLiteContract.INTEGER, null, null ),
		new ColumnData( false, RunHistoryTableContract.START_DATETIME, 
				getString(R.string.label_start_date_time), "",
				SQLiteContract.INTEGER, "", getString(R.string.hint_startdatetime ) ),
		new ColumnData( false, RunHistoryTableContract.INSERT_DATETIME, 
				getString(R.string.label_insert_date_time), "",
				SQLiteContract.INTEGER, "", getString(R.string.hint_insertdatetime ) ),
		new ColumnData( true, RunHistoryTableContract.PARENT_ID, 
				null, null,
				SQLiteContract.INTEGER, "", null ),
		new ColumnData( true, RunHistoryTableContract.NAME, 
				getString(R.string.label_lapname), null,
				SQLiteContract.TEXT, "", getString(R.string.hint_lapname ) ),
		new ColumnData( true, RunHistoryTableContract.LAP_INDEX, 
				null, null,
				SQLiteContract.INTEGER, "", null ),
		};
			        + RunHistoryTableContract.LAP_DISTANCE + " REAL,"
			        + RunHistoryTableContract.LAP_TIME + " INTEGER,"
			        + RunHistoryTableContract.LAP_SPEED + " REAL,"
			        + RunHistoryTableContract.LAP_FIXED_DISTANCE + " REAL,"
			        + RunHistoryTableContract.LAP_FIXED_TIME + " INTEGER,"
			        + RunHistoryTableContract.LAP_FIXED_SPEED + " REAL,"
			        + RunHistoryTableContract.GPX_FILE_PATH + " TEXT" 	// 2014/02/17 lapに移動                      
			
		}
		
		for( int i=0; i < LAP_TABLE_COLUMN_COUNT; i++ )
		{
			
			ColumnData data;
			data.setColumnName(columnName)
		
		}
		+ RunHistoryTableContract.START_DATETIME + " INTEGER,"                		
		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
		+ RunHistoryTableContract.PARENT_ID + " INTEGER,"
		+ RunHistoryTableContract.NAME + " TEXT,"
		+ RunHistoryTableContract.LAP_INDEX + " INTEGER,"
        + RunHistoryTableContract.LAP_DISTANCE + " REAL,"
        + RunHistoryTableContract.LAP_TIME + " INTEGER,"
        + RunHistoryTableContract.LAP_SPEED + " REAL,"
        + RunHistoryTableContract.LAP_FIXED_DISTANCE + " REAL,"
        + RunHistoryTableContract.LAP_FIXED_TIME + " INTEGER,"
        + RunHistoryTableContract.LAP_FIXED_SPEED + " REAL,"
        + RunHistoryTableContract.GPX_FILE_PATH + " TEXT" 	// 2014/02/17 lapに移動                      
		+ ");"
   ;
		
		return null;
	}

}
