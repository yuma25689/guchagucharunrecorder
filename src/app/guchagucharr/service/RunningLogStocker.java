package app.guchagucharr.service;

import java.util.Vector;

import android.location.Location;
import android.util.SparseArray;

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
	double totalDistance = 0;	// m
	double totalTime = 0;		// ms
	double totalSpeed = 0;		// m/s(average)
	
	int iRap = 0;	// rap(from0)
	RapData currentRapData = new RapData();
	
	SparseArray<RapData> rapTime = new SparseArray<RapData>();
	Vector<Location> vLocation = new Vector<Location>();
	Location prevLocation = null;
	public RapData getCurrentRapData()
	{
		return currentRapData;
	}

	public RunningLogStocker(long time)
	{
		currentRapData.clear();
		iRap = 0;
		totalStartTime = time;
		currentRapData.setStartTime(time);
	}	
	public void putLocationLog( Location location )
	{
		if( vLocation.isEmpty() )
		{
			// 空の時=初回、かつrap1(rap2以降)
			// その時のSpeedと、時間で距離を計算し、GPSでまだ計測されていない範囲の距離とする
			// TODO: たぶん、あまり正確ではないので、チャンスがあったら別のやり方を考慮
			long diffTime = location.getTime() - currentRapData.getStartTime();
			firstCorrectDistance = location.getSpeed() * diffTime * removeMilli(diffTime);
			currentRapData.increaseTime(diffTime);
			currentRapData.increaseDistance(firstCorrectDistance);
		}
		else
		{
			// ここで、このラップの各値について計算するが、パフォーマンスを考慮して、
			// なるべく過去の値は見なくてもいいようにする
			float[] result = new float[3];
			//float distance = 0;
			Location.distanceBetween(
					prevLocation.getLatitude(),//vLocation.lastElement().getLatitude(),
					prevLocation.getAltitude(),//vLocation.lastElement().getAltitude(),
					location.getLatitude(),
					location.getAltitude(), result);
			//distance = result[0];
			currentRapData.increaseDistance(result[0]);
			// long time = location.getTime() - vLocation.lastElement().getTime();
			currentRapData.increaseTime(location.getTime() - prevLocation.getTime());//vLocation.lastElement().getTime());
			currentRapData.addSpeedData(location.getSpeed());
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
	public void nextRap(Long time)
	{
		iRap++;
		currentRapData.clear();
		prevLocation = new Location( vLocation.lastElement() );
		// 時間だけ書き換える
		prevLocation.setTime(time);
	}
	public void stop( long time )
	{
		totalStopTime = time;
	}
	
	/**
	 * 取得されたログデータの保存
	 * @return
	 */
	public boolean save()
	{
		boolean bRet = false;
		
		// gpxデータへの変換、保存
		
		// databaseへの保存
		
		
		return bRet;
	}
}
