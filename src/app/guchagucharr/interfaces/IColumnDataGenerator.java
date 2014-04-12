package app.guchagucharr.interfaces;

import android.app.Activity;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.service.RunHistoryLoader.ActivityData;
import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;
import app.guchagucharr.service.RunningLogStocker;

public interface IColumnDataGenerator {
	/**
	 * @param RunnningLogStocker source データのソース
	 * @return 生成されたデータ
	 */
	public ColumnData[] generate(Activity activity, ActivityData data );//RunningLogStocker source, int index);
	public ColumnData[] generate(Activity activity, ActivityLapData data );
}
