package app.guchagucharr.interfaces;

import android.app.Activity;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.service.RunningLogStocker;

public interface IColumnDataGenerator {
	/**
	 * @param RunnningLogStocker source データのソース
	 * @return 生成されたデータ
	 */
	public ColumnData[] generate(Activity activity, RunningLogStocker source, int index);
}
