package app.guchagucharr.guchagucharunrecorder.util;

import app.guchagucharr.service.LapData;

public class ActivityLapData {
	
	public void valueOf( LapData dataSrc )
	{
		startDateTime = dataSrc.getStartTime();
		//insertDateTime;
		//int parentId;
		//lapIndex = dataSrc.ge 
		distance = dataSrc.getDistance(); 
		time = dataSrc.getTotalTime(); 
		speed = dataSrc.getSpeed(); 
//		fixedDistance = 0; 
//		fixedTime = 0;
//		fixedSpeed = 0;
		name = dataSrc.getName();
		gpxFilePath = dataSrc.getGpxFilePath();
		// gpxFixedFilePath = dataSrc.getGpxFixedFilePath();
		
	}
	
	
	long id = -1;
	long startDateTime;
	long insertDateTime;
	int parentId; 
	long lapIndex; 
	double distance; 
	long time; 
	double speed; 
	double fixedDistance = 0;
	long fixedTime = 0;
	double fixedSpeed = 0;
	String name;
	String gpxFilePath;
	String gpxFixedFilePath;
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the dateTime
	 */
	public long getStartDateTime() {
		return startDateTime;
	}
	/**
	 * @param dateTime the dateTime to set
	 */
	public void setStartDateTime(long dateTime) {
		this.startDateTime = dateTime;
	}
	/**
	 * @return the dateTime
	 */
	public long getInsertDateTime() {
		return insertDateTime;
	}
	/**
	 * @param dateTime the dateTime to set
	 */
	public void setInsertDateTime(long dateTime) {
		this.insertDateTime = dateTime;
	}
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	/**
	 * @return the lapIndex
	 */
	public long getLapIndex() {
		return lapIndex;
	}
	/**
	 * @param lapIndex the lapIndex to set
	 */
	public void setLapIndex(long lapIndex) {
		this.lapIndex = lapIndex;
	}
	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
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
	 * @return the fixedSpeed
	 */
	public double getFixedSpeed() {
		return fixedSpeed;
	}
	/**
	 * @param fixedSpeed the fixedSpeed to set
	 */
	public void setFixedSpeed(double fixedSpeed) {
		this.fixedSpeed = fixedSpeed;
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
	 * @return the gpxFixedFilePath
	 */
	public String getGpxFixedFilePath() {
		return gpxFixedFilePath;
	}
	/**
	 * @param gpxFixedFilePath the gpxFixedFilePath to set
	 */
	public void setGpxFixedFilePath(String gpxFixedFilePath) {
		this.gpxFixedFilePath = gpxFixedFilePath;
	}
	/**
	 * 適切な距離を返却(ユーザ入力の距離があれば、それを。そうでなければ、計測されたものを)
	 * @return
	 */
	public double getAppropriteDistance()
	{
		if( fixedDistance == 0 )
		{
			return distance;
		}
		else
		{
			return fixedDistance;
		}
	}

	/**
	 * 適切な時間を返却(ユーザ入力の時間があれば、それを。そうでなければ、計測されたものを)
	 * @return
	 */
	public long getAppropriteTime()
	{
		if( fixedTime == 0 )
		{
			return time;
		}
		else
		{
			return fixedTime;
		}
	}
	
};
