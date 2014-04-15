package app.guchagucharr.guchagucharunrecorder.util;

public class ActivityData {
	int id; 
	long startDateTime;
	long insertDateTime;
	String name;
	long lapCount;
	long placeId;
    //String gpxFilePath;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
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
	 * @return the lapCount
	 */
	public long getLapCount() {
		return lapCount;
	}
	/**
	 * @param lapCount the lapCount to set
	 */
	public void setLapCount(long lapCount) {
		this.lapCount = lapCount;
	}
	/**
	 * @return the placeId
	 */
	public long getPlaceId() {
		return placeId;
	}
	/**
	 * @param placeId the placeId to set
	 */
	public void setPlaceId(long placeId) {
		this.placeId = placeId;
	}
	/**
	 * @return the gpxFilePath
	 */
//	public String getGpxFilePath() {
//		return gpxFilePath;
//	}
	/**
	 * @param gpxFilePath the gpxFilePath to set
	 */
//	public void setGpxFilePath(String gpxFilePath) {
//		this.gpxFilePath = gpxFilePath;
//	}
};
