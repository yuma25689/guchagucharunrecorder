
package app.guchagucharr.service;

interface IRunLoggerService
{
	long getTimeInMillis();
	int requestGPS();
	int getMode();
	void setMode(int mode);
	void clearGPS();
	void createLocationManager();
	void clearLocationManager();
	void startLog();
	void stopLog();
}

