package app.guchagucharr.service;

import java.util.Vector;

import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;

public class LapData {
	public void clear()
	{
		startTime = 0;
		stopTime = 0;
		totalTime = 0;
		distance = 0;
		speed = 0;
	}
	long startTime = 0;
	long stopTime = 0;
	long totalTime = 0;
	double distance = 0;
	double speed = 0;
	final int SPEED_QUEUE_MAX = 100;
	Vector<Double> vSpeedQueue = new Vector<Double>();
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
		// TODO: �����B�덷�������Ԃ���Ǝv���邵�A�ő匏���̎w������
		double sumVal = 0;
		vSpeedQueue.add( speed_ );
		if( SPEED_QUEUE_MAX < vSpeedQueue.size() )
		{
			// �T���v����������������A�܂񒆂��炢�̃T���v���𔲂��H
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
	public static String createDistanceFormatText(double distance)
	{
		String ret = null;
		final double DISTANCE_KM = 1000;
		// TODO: �ݒ�ɂ���āAm/s��km/hour��؂�ւ�
		
		if( distance < DISTANCE_KM )
		{
			ret = String.format( "%.0f", distance ) + ResourceAccessor.IND_M;
		}
		else
		{
			ret = String.format( "%.3f", distance / DISTANCE_KM ) + ResourceAccessor.IND_KM;
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
	public static String createSpeedFormatText(double speed)
	{
		String ret = null;

		// m/s
		ret = String.format("%.2f", speed) + ResourceAccessor.IND_MPERS;
		return ret;
	}
	public static String createSpeedFormatTextKmPerH(double speed)
	{
		String ret = null;
		double meterperhour = speed * 60 * 60;
		
		ret = String.format("%.2f", meterperhour / 1000 ) + ResourceAccessor.IND_KMPERHOUR;
		return ret;
		
	}
}
