package app.guchagucharr.interfaces;

import java.util.Vector;

import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.service.RunningLogStocker;

public interface IColumnDataGenerator {
	/**
	 * @param RunnningLogStocker source データのソース
	 * @return 生成されたデータ
	 */
	public Vector<ColumnData> generate(RunningLogStocker source);
}
