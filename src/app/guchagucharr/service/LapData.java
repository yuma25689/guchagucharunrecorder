package app.guchagucharr.service;

import java.util.Vector;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import app.guchagucharr.guchagucharunrecorder.GGRRPreferenceActivity;
import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;

public class LapData {
	
	public LapData() 
	{
		clear();
	}
	@SuppressWarnings("unchecked")
	public LapData(LapData data_) {
		startTime = data_.getStartTime();
		stopTime = data_.getStopTime();
		totalTime = data_.getTotalTime();
		distance = data_.getDistance();
		fixedTime = data_.getFixedTime();
		fixedDistance = data_.getFixedDistance();
		speed = data_.getSpeed();
		name = data_.getName();
		gpxFilePath = data_.getGpxFilePath();
		vSpeedQueue = (Vector<Double>) data_.getSpeedQueue().clone();	
	}
	public void clear()
	{
		startTime = 0;
		stopTime = 0;
		totalTime = 0;
		distance = 0;
		fixedTime = 0;
		fixedDistance = 0;
		speed = 0;
		name = null;
		gpxFilePath = null;
		vSpeedQueue.clear();
	}
	long startTime = 0;
	long stopTime = 0;
	long totalTime = 0;
	double distance = 0;
	long fixedTime = 0;
	double fixedDistance = 0;
	double speed = 0;
	String name = null;
	String gpxFilePath = null;
	final int SPEED_QUEUE_MAX = 100;
	Vector<Double> vSpeedQueue = new Vector<Double>();
	Vector<Double> getSpeedQueue()
	{
		return vSpeedQueue;
	}
	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the stopTime
	 */
	public long getStopTime() {
		return stopTime;
	}
	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	/**
	 * @return the totalTime
	 */
	public long getTotalTime() {
		return stopTime - startTime;//totalTime;
	}
//	public void increaseTime( long time )
//	{
//		totalTime += time;
//	}
	/**
	 * @param totalTime the totalTime to set
	 */
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}
	public void increaseDistance( double distance_ )
	{
		distance += distance_;
	}
	
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}
	/**
	 * @return the speed
	 */
	public double getSpeedAccurateAsPossible() {
		double speedSummary = 0;
		if( vSpeedQueue.isEmpty() )
		{
			return 0;
		}
		for( double speed_ : vSpeedQueue )
		{
			speedSummary += speed_;
		}
		return speedSummary / vSpeedQueue.size();
	}
	public void addSpeedData( double speed_ )
	{
		double sumVal = 0;
		vSpeedQueue.add( speed_ );
		if( SPEED_QUEUE_MAX < vSpeedQueue.size() )
		{
			vSpeedQueue.remove(SPEED_QUEUE_MAX/2);
		}
		for( Double val : vSpeedQueue )
		{
			sumVal += val;
		}
		speed = sumVal / vSpeedQueue.size();
	}
	
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public static String createDistanceFormatText(int currentUnit, double distance)
	{
		String ret = null;
		switch( currentUnit )
		{
		case UnitConversions.DISTANCE_UNIT_KILOMETER:
			if( distance < UnitConversions.KM_TO_M )
			{
				ret = String.format( "%.3f", distance ) 
						+ ResourceAccessor.getInstance().IND_M;
			}
			else
			{
				// NOTICE:微妙な繰り上げ方
				ret = String.format( "%.3f", distance * UnitConversions.M_TO_KM ) 
						+ ResourceAccessor.getInstance().IND_KM;
			}
			break;
		case UnitConversions.DISTANCE_UNIT_MILE:
			double ft = distance * UnitConversions.M_TO_FT;
			if( ft < UnitConversions.MI_TO_FT )
			{
				// 1mileに到達していなかったら、ftで表示？
				ret = String.format( "%.3f", ft ) 
						+ ResourceAccessor.getInstance().IND_FT;
			}
			else
			{
				// NOTICE:微妙な繰り上げ方
				ret = String.format( "%.3f", ft * UnitConversions.FT_TO_MI ) 
						+ ResourceAccessor.getInstance().IND_MILE;
			}
			break;
			
		}
		return ret;
	}
	public static String createDistanceFormatText(int currentUnit, double distance,double totalDistance)
	{
		String ret = null;
		switch( currentUnit )
		{
		case UnitConversions.DISTANCE_UNIT_KILOMETER:
			if( distance < UnitConversions.KM_TO_M )
			{
				ret = String.format( "%.3f", distance ) 
						+ "( / " + String.format( "%.3f", totalDistance )  + ")"						
						+ ResourceAccessor.getInstance().IND_M;
			}
			else
			{
				// NOTICE:微妙な繰り上げ方
				ret = String.format( "%.3f", distance / UnitConversions.KM_TO_M ) 
						+ "( / " + String.format( "%.3f", totalDistance / UnitConversions.KM_TO_M )  + ")"
						+ ResourceAccessor.getInstance().IND_KM;
			}
			break;
		case UnitConversions.DISTANCE_UNIT_MILE:
			double ft = distance * UnitConversions.M_TO_FT;
			double totalFt = totalDistance * UnitConversions.M_TO_FT;
			if( ft < UnitConversions.MI_TO_FT )
			{
				// 1mileに到達していなかったら、ftで表示？
				ret = String.format( "%.3f", ft )
						+ "( / " + String.format( "%.3f", totalFt )  + ")"						
						+ ResourceAccessor.getInstance().IND_FT;
			}
			else
			{
				// NOTICE:微妙な繰り上げ方
				ret = String.format( "%.3f", ft * UnitConversions.FT_TO_MI )
						+ "( / " + String.format( "%.3f", totalFt * UnitConversions.FT_TO_MI )  + ")"
						+ ResourceAccessor.getInstance().IND_MILE;
			}
			break;
			
		}
		
		return ret;
	}
	public static String createTimeFormatText(long time)
	{
		String ret = null;
		// Math.abs�͈ꉞ�e�X�g�p�̂��肾���E�E�E
		long second_all = Math.abs(time) / 1000;
		long second = 0;
		long min = 0;
		long hour = 0;
		String strHour = "00:";
		String strMin = "00:";
		String strSecond = "00";
				
		if( second_all < ResourceAccessor.TIME_MINUTE )
		{
			second = second_all;
			strSecond = String.format( "%02d", second ); //+ ResourceAccessor.IND_SEC;			
	
		}
		else if( second_all < ResourceAccessor.TIME_HOUR )
		{
			min = second_all / ResourceAccessor.TIME_MINUTE;
			second = second_all % ResourceAccessor.TIME_MINUTE;
			strMin = //String.valueOf( min ) + ResourceAccessor.IND_MINUTE;
					String.format( "%02d:", min );
			strSecond = String.format( "%02d", second );// + ResourceAccessor.IND_SEC;			
			
		}
		else
		{
			hour = second_all / ResourceAccessor.TIME_HOUR;
			min = (second_all - hour * ResourceAccessor.TIME_HOUR) / ResourceAccessor.TIME_MINUTE;
			second = second_all % ResourceAccessor.TIME_HOUR % ResourceAccessor.TIME_MINUTE;
			strHour = String.format( "%02d:", hour );// + ResourceAccessor.IND_HOUR;
			strMin = String.format( "%02d:", min );// + ResourceAccessor.IND_MINUTE; 
			strSecond = String.format( "%02d", second ); // + ResourceAccessor.IND_SEC;	
		}
		ret = strHour + strMin + strSecond;
		return ret;
	}
	// 秒速〜メートルは廃止
//	public static String createSpeedFormatText(double speed)
//	{
//		String ret = null;
//
//		// m/s
//		ret = String.format("%.2f", speed) + ResourceAccessor.getInstance().IND_MPERS;
//		return ret;
//	}
	// TODO: 距離の単位が設定されたものになるように修正必要
	public static String createSpeedFormatTextKmPerH(int currentUnit, double speed)
	{
		String ret = null;
		double meterperhour = speed * 60 * 60;
		
		ret = String.format("%.2f", meterperhour / 1000 ) + ResourceAccessor.getInstance().IND_KMPERHOUR;
		return ret;
	}
	/**
	 * @return the gpxFilePath
	 */
	public String getGpxFilePath() {
		return gpxFilePath;
	}
	/**
	 * @param gpxFilePath the gpxFilePath to set
	 */
	public void setGpxFilePath(String gpxFilePath) {
		this.gpxFilePath = gpxFilePath;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the fixedTime
	 */
	public long getFixedTime() {
		return fixedTime;
	}
	/**
	 * @param fixedTime the fixedTime to set
	 */
	public void setFixedTime(long fixedTime) {
		this.fixedTime = fixedTime;
	}
	/**
	 * @return the fixedDistance
	 */
	public double getFixedDistance() {
		return fixedDistance;
	}
	/**
	 * @param fixedDistance the fixedDistance to set
	 */
	public void setFixedDistance(double fixedDistance) {
		this.fixedDistance = fixedDistance;
	}
}
