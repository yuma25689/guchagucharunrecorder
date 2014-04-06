/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.guchagucharr.service;

//import android.app.Notification;
//import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
//import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.MainActivity;
//import app.guchagucharr.guchagucharunrecorder.MainActivity.eMode;
//import android.os.Vibrator;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * 2014/03/14 サービス起動の段階で、リクエスト開始
 * @author 25689
 *
 */
public class RunLoggerService extends Service 
implements LocationListener
{	
	// 2014/03/14 MyTracksで利用しているLocationClientの利用
	private LocationClient locationClient;
	private float requestLocationUpdatesDistance;
	private long requestLocationUpdatesTime;
	private final ConnectionCallbacks connectionCallbacks = 
		new ConnectionCallbacks() {
	    @Override
	    public void onDisconnected() {}

	    @Override
	    public void onConnected(Bundle bunlde) {
	    	Log.w("onConnected","come");
	      handler.post(new Runnable() {
	        @Override
	        public void run() {
	          if ( locationClient.isConnected()) {
	            LocationRequest locationRequest = new LocationRequest().setPriority(
	                LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(requestLocationUpdatesTime)
	                .setFastestInterval(requestLocationUpdatesTime)
	                .setSmallestDisplacement(requestLocationUpdatesDistance);
	            locationClient.requestLocationUpdates(
	                locationRequest, RunLoggerService.this, 
	                handler.getLooper());
	          }
	        }
	      });
	    }
	  };
	  private final OnConnectionFailedListener
      onConnectionFailedListener = new OnConnectionFailedListener() {
          @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {}
      };
	  
	//private long lastGetLocationTime = 0;
	private Handler handler;
	private static Timer mTimer = null;
	private static UpdateTimeDisplayTask timerTask = null;
	class UpdateTimeDisplayTask extends TimerTask
	{
	     @Override
	     public void run() {
	    	 Log.v("UpdateTimeDisplayTask","come");
	         // mHandler through UI Thread to queueing
	    	 handler.post( new Runnable() {
	             @Override
	             public void run() {	 
	    	 
	            	 // update now Time
	            	 try {
						if( RunLoggerService.getLogStocker() != null
								&& RunLogger.sService != null
								&& RunLogger.sService.getMode() == eMode.MODE_MEASURING.ordinal() )
						{
							long lapTime = getTimeInMillis()//new Date().getTime() 
									- RunLoggerService.getLogStocker().getCurrentLapData().getStartTime();
							long totalTime = lapTime;
							if( RunLoggerService.getLogStocker().getLapData(0) != null)
							{
								totalTime = getTimeInMillis()//new Date().getTime() 
									- RunLoggerService.getLogStocker().getLapData(0).getStartTime();
							}
							
							//clearGPS();
							// NOTICE: 微妙なところだが、ここでタイマーごとにリクエストする?
							//requestGPS();
							//RunLogger.sService.requestGPS();
		
					        // Send intent to activity
					        Intent activityNotifyIntent = new Intent();
					        activityNotifyIntent.putExtra( MainActivity.CURRENT_DURATION, LapData.createTimeFormatText( lapTime ));
					        activityNotifyIntent.putExtra( MainActivity.TOTAL_DURATION, LapData.createTimeFormatText( totalTime ));
					        activityNotifyIntent.setAction(
					        		MainActivity.TIMER_NOTIFY);
					        getBaseContext().sendBroadcast(activityNotifyIntent);
							
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						Log.e("UpdateTimeDisplayTask",e.getMessage());						
					}
	             }
		     });
	     }
	 }	
	
	public long getTimeInMillis()
	{
		//Calendar calendar = Calendar.getInstance();
		return Calendar.getInstance().getTimeInMillis();
	}
	
	// public static ResourceAccessor resourceAccessor;
	//private LocationManager mLocationManager;	
	//private RunningLogStocker runLogStocker = null;	
    //private WakeLock mWakeLock;
    //private int mServiceStartId = -1;
    //private boolean mServiceInUse = false;
	// private ResourceAccessor res;
	public enum eMode {
		MODE_NORMAL,
		MODE_MEASURING//,
		// MODE_SAVE_OR_CLEAR
	};
	//static eMode mode2; 
	private static eMode mode = eMode.MODE_NORMAL;

	private static Notification notif = null;
	public static void setNotification( Notification notif_ )
	{
		notif = notif_;
	}
	public eMode getMode()
	{
		return mode;
	}
	public void setMode(eMode mode_)
	{
		mode = mode_;
	}
	public static RunningLogStocker runLogStocker = null;
	public static void createLogStocker()//long time)
	{
		runLogStocker = new RunningLogStocker();//time);
	}
	public static RunningLogStocker getLogStocker()
	{
		return runLogStocker;
	}
	public static void putLocationLog( Location data )
	{
		runLogStocker.putLocationLog(data);
	}
	public static boolean isEmptyLogStocker()
	{
		if( runLogStocker == null )
			return true;
		
		return false;
	}
	public static void clearRunLogStocker()
	{
		runLogStocker = null;
	}
	
    public RunLoggerService()//boolean enableLocationClient)
    {
    	// メンバ変数の初期化
        handler = new Handler(); 
    }

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // コンストラクタ
        Log.w("RunLoggerService-onCreate","come");
        // ロケーションクライアントの作成
        // NOTICE:タイミング的には、サービスができた瞬間から計測開始という感じになるかもしれない
	    locationClient = new LocationClient(this, connectionCallbacks, onConnectionFailedListener);
	    locationClient.connect();
    }
    public static int NOTIF_ID = 100;
    
    @Override
    public void onStart(Intent intent, int startID) 
    {
        Log.w("RunLoggerService-onStart","come");    	
    	// 今のところ、特に何もしない
    	// もしやるにしても、Notificationの表示くらいかもしれない
    }

    @Override
    public void onDestroy() {
        Log.w("RunLoggerService-onDestroy","come");    	
//        mWakeLock.release();
//		NotificationManager mNotificationManager =
//				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.cancel(NOTIF_ID);
		Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();    	
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        //mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        //mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //mServiceStartId = startId;
        Log.w("RunLoggerService-onStartCommand","come");
        // TODO:サービスが強制終了==>再起動と来た場合、ここで復旧するのもいいかもしれない
        // ただし、どちらかというと、onLocationChangedで復旧した方がいいかも？
        // (テストした感じサービス再起動が起こっても、メンバ変数が全てクリアされてしまうだけで、
        // onLocationChangedは来ている)
        return START_STICKY;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        // mServiceInUse = false;
        // stopSelf(mServiceStartId);
        return true;
    }
    /**
     * 
     * @return 1:GPS not arrowed 0:normal
     */
    public int requestGPS()
    {
		if( locationClient != null )
		{
			if( false == locationClient.isConnected() )
			{
				locationClient.connect();
			}
		}
    	return 0;
    }
    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still
     * has a remote reference to the stub.
     */
    static class ServiceStub extends IRunLoggerService.Stub {
        WeakReference<RunLoggerService> mService;
        
        ServiceStub(RunLoggerService service) {
            mService = new WeakReference<RunLoggerService>(service);
        }

		@Override
		public int requestGPS() throws RemoteException {
			// TODO Auto-generated method stub
			return mService.get().requestGPS();
		}
		
		@Override
		public int getMode() throws RemoteException {
			// TODO Auto-generated method stub
			return mService.get().getMode().ordinal();
		}
		@Override
		public void setMode(int mode) throws RemoteException {
			mService.get().setMode(eMode.values()[mode]);
		}
		@Override
		public void clearGPS() throws RemoteException {
			mService.get().clearGPS();
		}

		@Override
		public void createLocationManager() throws RemoteException {
			//mService.get().createLocationManager();
		}

		@Override
		public void clearLocationManager() throws RemoteException {
			// mService.get().clearLocationManager();
			
		}

		@Override
		public void startLog() throws RemoteException {
			mService.get().startLog();
			Log.v("startLog","come");
		}

		@Override
		public void stopLog() throws RemoteException {
			mService.get().stopLog();
			Log.v("stopLog","come");
		}

		@Override
		public long getTimeInMillis() throws RemoteException {
			//Log.v("getTimeInMillis","come");
			return mService.get().getTimeInMillis();
		}
		
    }
    
	private final IBinder mBinder = new ServiceStub(this);

	void clearGPS()
	{
	    if (locationClient != null) {
	    	locationClient.disconnect();
	    }
	}
	
	void startLog()
	{		
	    //if(mTimer == null){
        timerTask = new UpdateTimeDisplayTask();
        mTimer = new Timer(true);
        mTimer.scheduleAtFixedRate( timerTask, 1000, 1000);
        //clearGPS();
        requestGPS();
	    //}
        //writeModeToTmpFile( activity, eMode.MODE_MEASURING );
        // NOTICE:サービスが落ちないように、Foreground化する。
        // あまりこれで絶対大丈夫という感じもないが・・・テストした感じでは、落ちなくなった。
        // TODO: 復旧のテスト用にコメント化中 2014/04/06
//        if( notif != null)
//        	startForeground(NOTIF_ID,notif);
	}
	void stopLog()
	{
		Log.v("stopLog","come");
	    if(mTimer != null){
	        mTimer.cancel();
	        mTimer = null;
	        timerTask = null;
	    }
	    // TODO: このメソッドのラッパーをRunLoggerに作り、そっちに移す
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_ID);
	    
	}
	@Override
	public void onLocationChanged(Location location) {
		Log.v("GPS","onLocationChanged");
		// NOTICE: 受診毎に切断
		// TODO:サービス再起動時の復旧処理を、ここに入れればうまく行きそうに感じる
		// clearGPS();
		if( false == isEmptyLogStocker() 
				&& mode == eMode.MODE_MEASURING )
		{
			// TODO: 精度は、設定に
			// 50m以上の誤差がある場合は、切り捨てる
			if( 50 < location.getAccuracy() )
			{
				Log.v("get location data but not stock","because over 50 accuracy");
				return;
			}
			Log.v("add","location info");
			// NOTICE: この関数でほとんど全てのログを取っているようなもの
			putLocationLog(location);
		}
        // Send intent to activity
        Intent activityNotifyIntent = new Intent();
        activityNotifyIntent.putExtra(MainActivity.LOCATION_DATA, location);
        activityNotifyIntent.setAction(
        		MainActivity.LOCATION_CHANGE_NOTIFY);
        getBaseContext().sendBroadcast(activityNotifyIntent);
        //lastGetLocationTime = location.getTime();
	}

	  /**
	   * Requests location updates. This is an ongoing request, thus the caller
	   * needs to check the status of {@link #isAllowed}.
	   * 
	   * @param minTime the minimal time
	   * @param minDistance the minimal distance
	   * @param locationListener the location listener
	   */
	  public void requestLocationUpdates(
	      final long minTime, final float minDistance, final LocationListener locationListener) {
	    handler.post(new Runnable() {
	        @Override
	      public void run() {
	        requestLocationUpdatesTime = minTime;
	        requestLocationUpdatesDistance = minDistance;
	        // requestLocationUpdates = locationListener;
	        connectionCallbacks.onConnected(null);
	      }
	    });
	  }

	  /**
	   * Removes location updates.
	   * 
	   * @param locationListener the location listener
	   */
	  public void removeLocationUpdates(final LocationListener locationListener) {
	    handler.post(new Runnable() {
	        @Override
	      public void run() {
	        // requestLocationUpdates = null;
	        if (locationClient != null && locationClient.isConnected()) {
	          locationClient.removeLocationUpdates(locationListener);
	        }
	      }
	    });
	  }
}
