package app.guchagucharr.interfaces;

import android.app.Activity;
import app.guchagucharr.guchagucharunrecorder.util.ActivityData;
import app.guchagucharr.guchagucharunrecorder.util.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;

public interface IColumnDataGenerator {
	/**
	 * @param RunnningLogStocker source データのソース
	 * @return 生成されたデータ
	 */
	public ColumnData[] generate(Activity activity, ActivityData data );//RunningLogStocker source, int index);
	public ColumnData[] generate(Activity activity, ActivityLapData data );
}
