package app.guchagucharr.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.Toast;

// ランニング中のデータを貯めるのクラス
public class RunningLogStocker {

	private final int MAX_LOCATION_LOG_CNT = 72000;	
	// 一番速くても0.1秒周期でしかログを取得できないはずなので、最高で2.4時間分
	// ただし、メーターの方も1m以上はなれないと計測されない制御があるので、
	// 0.1秒周期も何か乗り物に乗っていない限り無理だと思う
	static long removeMilli( long val )
	{
		return val * 1000;
	}
	// 1970年からの時刻(ms)
	long totalStartTime = 0;
	long totalStopTime = 0;
	double firstCorrectDistance = 0;	// GPSで計測する前の誤差となる距離
	public double getTotalDistance()
	{
		double ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getDistance();
		}
		return ret;
	}
	public long getTotalTime()
	{
		long ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getTotalTime();
		}
		return ret;
	}
	/**
	 * 
	 * @return m/s
	 */
	public double getTotalSpeed()
	{
		double ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getSpeedAccurateAsPossible();
		}
		ret /= getLapCount();
		return ret;
	}
	public LapData getLapData(int index)
	{
		return lapData.get(index);
	}
	public int getLapCount()
	{
		return iLap+1;
	}
	
	int iLap = 0;	// lap(from0)
	LapData currentLapData = new LapData();
	
	SparseArray<LapData> lapData = new SparseArray<LapData>();
	Vector<Location> vLocation = new Vector<Location>();
	Location prevLocation = null;
	public LapData getCurrentLapData()
	{
		return currentLapData;
	}

	public RunningLogStocker(long time)
	{
		lapData.clear();
		currentLapData.clear();
		iLap = 0;
		totalStartTime = time;
		currentLapData.setStartTime(time);
	}	
	public void putLocationLog( Location location )
	{
		if( vLocation.isEmpty() )
		{
			// 空の時=初回、かつlap1(lap2以降)
			// その時のSpeedと、時間で距離を計算し、GPSでまだ計測されていない範囲の距離とする
			// TODO: たぶん、あまり正確ではないので、チャンスがあったら別のやり方を考慮
			// ->とりあえず、何もしない
			//long diffTime = location.getTime() - currentLapData.getStartTime();
			//firstCorrectDistance = location.getSpeed() * diffTime * removeMilli(diffTime);
			//currentLapData.increaseTime(diffTime);
			//currentLapData.increaseDistance(firstCorrectDistance);
		}
		else
		{
			// ここで、このラップの各値について計算するが、パフォーマンスを考慮して、
			// なるべく過去の値は見なくてもいいようにする
			//float[] result = new float[1];
			//float result = prevLocation.distanceTo(location);
//			Location.distanceBetween(
//					prevLocation.getLatitude(),//vLocation.lastElement().getLatitude(),
//					prevLocation.getLongitude(),//vLocation.lastElement().getLongitude(),
//					location.getLatitude(),
//					location.getLongitude(), result);
			//distance = result[0];
			currentLapData.increaseDistance(prevLocation.distanceTo(location));
			// long time = location.getTime() - vLocation.lastElement().getTime();
			// currentLapData.increaseTime(location.getTime() - prevLocation.getTime());//vLocation.lastElement().getTime());
			currentLapData.addSpeedData(location.getSpeed());
		}
        // Log.v("Speed", String.valueOf(location.getSpeed()));
		if( MAX_LOCATION_LOG_CNT < vLocation.size() )
		{
			// マックス値を超えたら、真ん中らへんから抜いていく
			vLocation.remove(MAX_LOCATION_LOG_CNT/2);
		}
		// 要素追加
		vLocation.add(location);
		prevLocation = new Location(location);
	}
	public void nextLap(Long time)
	{
		lapData.put(iLap, currentLapData);
		iLap++;
		currentLapData.clear();
		prevLocation = new Location( vLocation.lastElement() );
		// 時間だけ書き換える
		prevLocation.setTime(time);
	}
	public void stop( long time )
	{		
		totalStopTime = time;
		currentLapData.setStopTime(time);
		lapData.put(iLap, currentLapData);		
	}
	
	public ContentValues createContentValues(int tableID, 
			long insertTime, String strExtra, long lngExtra, int iExtra )
	{
		ContentValues ret = null;
		if( tableID == RunHistoryTableContract.HISTORY_TABLE_ID)
		{
			ret = new ContentValues();
			ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime);
			ret.put(RunHistoryTableContract.NAME, strExtra);
			ret.put(RunHistoryTableContract.LAP_COUNT, getLapCount() );
			// TODO: 場所の登録はまだ未実装
			ret.put(RunHistoryTableContract.PLACE_ID, -1);

		}
		else
		{
			ret = new ContentValues();
    		ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime );
    		ret.put( RunHistoryTableContract.PARENT_ID, lngExtra );
    		ret.put( RunHistoryTableContract.LAP_INDEX, iExtra );
            ret.put( RunHistoryTableContract.LAP_DISTANCE, lapData.get(iExtra).getDistance() );
            ret.put( RunHistoryTableContract.LAP_TIME, lapData.get(iExtra).getTotalTime() );
            ret.put( RunHistoryTableContract.LAP_SPEED, lapData.get(iExtra).getSpeed() );
            // TODO: 未実装
            ret.put( RunHistoryTableContract.LAP_FIXED_DISTANCE, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_TIME, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_SPEED, 0 );
            ret.put( RunHistoryTableContract.FILE_NAME, strExtra );
			
		}
		
		return ret;
		
	}
	

	public int insertRunHistoryLog(
			Activity activity,
			String logname, 
			RunningLogStocker log)
	{
		int insertCount = -1;
		activity.getContentResolver().insert(
				Uri.parse("content://" 
				+ RunHistoryTableContract.AUTHORITY + "/" 
				+ RunHistoryTableContract.HISTORY_TRANSACTION )
				,null);
        //SQLiteDatabase db = databaseHelper.getReadableDatabase();
        //db.beginTransaction();
        try {
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        	ContentValues values = null;
        	Date date = new Date();
        	long time = date.getTime();
        	values = log.createContentValues(
        			RunHistoryTableContract.HISTORY_TABLE_ID
        			, time, sdf.format( date ), 0, 0);
        	if( values == null )
        	{
        		Toast.makeText(activity, "failed to save the runnning data.", 
        				Toast.LENGTH_LONG).show();
                return -1;
        	}
        	Uri uriRet = activity.getContentResolver().insert(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/" 
        					+ RunHistoryTableContract.HISTORY_TABLE_NAME ), values);
            long id = Integer.parseInt(uriRet.getPathSegments().get(1)); 
        	if( -1 == id )
        	{
        		Toast.makeText(activity, "failed to save the runnning data.", 
        				Toast.LENGTH_LONG).show();
                return -1;        		
        	}
        	else
        	{
        		insertCount = 1;
        		for( int iLap=0; iLap < log.getLapCount(); iLap++ )
        		{
	            	values = log.createContentValues(RunHistoryTableContract.HISTORY_LAP_TABLE_ID, 
	            			time, 
	            			null,	// TODO: GPXファイルのパス
	            			id,	// 親のid
	            			iLap);	// lap index
	            	if( values == null )
	            	{
	            		Toast.makeText(activity, "failed to save the runnning data.", 
	            				Toast.LENGTH_LONG).show();
	                    return -1;
	            	}
	            	// long lapId = 
	            	//db.insert(RunHistoryTableContract.HISTORY_LAP_TABLE_NAME, null, values);
	            	//Uri uriRet = 
	            	activity.getContentResolver().insert(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/" 
        					+ RunHistoryTableContract.HISTORY_LAP_TABLE_NAME ), values);
	            	
	            	insertCount++;
        		}
                //db.setTransactionSuccessful();
        		activity.getContentResolver().insert(
        				Uri.parse("content://" 
        				+ RunHistoryTableContract.AUTHORITY + "/" 
        				+ RunHistoryTableContract.HISTORY_COMMIT )
        				,null);
        		
        	}
        	
        	
        } finally {
        	//db.endTransaction();
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
    				,null);
        	
        }
        return insertCount;
	}
	
	/**
	 * 取得されたログデータの保存
	 * @return
	 */
	public int save(Activity activity)
	{
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	Date date = new Date();
    	String strDateTime = sdf.format( date );
		
		// gpxデータへの変換、保存
		
		// databaseへの保存
		int iRet = insertRunHistoryLog(activity, strDateTime, this );
		
		return iRet;
	}
}
