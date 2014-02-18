
package app.guchagucharr.service;

interface IRunLoggerService
{
	int requestGPS();
	int getMode();
	void setMode(int mode);
	void clearGPS();
	void createLocationManager();
	void clearLocationManager();
	void startLog();
	void stopLog();
}

