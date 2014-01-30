package app.guchagucharr.service;

public class RapData {
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
		return totalTime;
	}
	public void increaseTime( long time )
	{
		totalTime += time;
	}
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
	public void addSpeedData( double speed )
	{
		// TODO: ‚±‚ê‚Å‚ÍAŒ”‚ª‘‚¦‚é‚Ù‚ÇŒë·‚ª‘å‚«‚­‚È‚é‚ªA‚Æ‚è‚ ‚¦‚¸‚»‚ê‚Å‚¢‚¢
		speed = ( speed + speed ) / 2;
	}
	
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
