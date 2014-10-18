
package app.guchagucharr.service;

interface IRunLoggerService
{
	long getTimeInMillis();
	int requestGPS();
	int getMode();
	void setMode(int mode);
	int getNoGpsMode();
	void setNoGpsMode(int noGpsModeFlg);
	int getActivityTypeCode();
	void setActivityTypeCode(int activityTypeCode);
	void clearGPS();
	void createLocationManager();
	void clearLocationManager();
	void startLog();
	void stopLog();
}

