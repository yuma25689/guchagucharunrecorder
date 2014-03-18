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
import android.app.Service;
import android.content.Intent;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
//import android.provider.Settings;
import android.util.Log;
import app.guchagucharr.guchagucharunrecorder.MainActivity;
import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;
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
	private final LocationClient locationClient;
	private float requestLocationUpdatesDistance;
	private long requestLocationUpdatesTime;
	private final ConnectionCallbacks connectionCallbacks = 
		new ConnectionCallbacks() {
	    @Override
	    public void onDisconnected() {}

	    @Override
	    public void onConnected(Bundle bunlde) {
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

	  
	private long lastGetLocationTime = 0;
	private Handler handler;	
	private Timer mTimer = null;	
	private UpdateTimeDisplayTask timerTask = null;
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
        handler = new Handler(); 
		//if (enableLocationClient) {
		    locationClient = new LocationClient(ResourceAccessor.getInstance().getActivity(), connectionCallbacks, onConnectionFailedListener);
		    locationClient.connect();
//		} else {
//			locationClient = null;
//		}
        
    }

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
//        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
//        mCardId = StorageInfo.getCardId(this);        
        //registerExternalStorageListener();

//        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
//        mWakeLock.setReferenceCounted(false);
        // createLocationManager();
    }

    @Override
    public void onDestroy() {
//        mWakeLock.release();
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
        return START_STICKY;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        //mServiceInUse = false;
        // stopSelf(mServiceStartId);
        return true;
    }
    /**
     * 
     * @return 1:GPS not arrowed 0:normal
     */
    public int requestGPS()
    {
////    	Looper looper = Looper.getMainLooper();
////    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 0, listener, looper);
//		final long MIN_TIME = 900;
//		final float MIN_METER = 1f;
//		String providers = Settings.Secure.getString(
//				getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//		if( providers.indexOf("gps", 0) < 0 )
//		{
//			// GPSが許可されていないと思われる
//			Log.v("initGPS", "GPS not allowed.");
//			return 1;
//		}
//		
//        if (mLocationManager != null ) {
//        	// clearGPS();
//            mLocationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
////                LocationManager.NETWORK_PROVIDER,
//                MIN_TIME,
//                MIN_METER,
//                this);
//            // 最後に取得してからMIN_TIME*8以上たっていたら、A-GPSの更新を促してみる
//            if( lastGetLocationTime == 0 
//            || MIN_TIME*1000 < getTimeInMillis() - lastGetLocationTime )
//            {
//            	// A-GPS情報の削除
//            	// Bundleである項目だけを指定できるが、nullで全て削除
//            	// ->どうも、やるとやばそう
//            	//mLocationManager.sendExtraCommand("gps", "delete_aiding_data", null);
//            	// これでA-GPS情報のダウンロードを促すらしい
//            	//Bundle bundle = new Bundle();
//            	//mLocationManager.sendExtraCommand("gps", "force_xtra_injection", null);
//            	// NTPサーバから、現在時刻を更新してもらうのを促す？
//            	//mLocationManager.sendExtraCommand("gps", "force_time_injection", null);
//            	//lastGetLocationTime = getTimeInMillis();
//            	Log.v("a-gps reset","a-gps reset occur" );
//            }
//        }
//
		if( locationClient != null )
		{
			if( false == locationClient.isConnected() )
			{
				locationClient.connect();
			}
		}

    	return 0;
    }
    
            
//            // Notification�N���X�̍쐬
//            Notification status = new Notification();
//            // 
//            status.tickerText = ticket;
//            // Notification�N���X�ɁA�r���[��ݒ�
//            status.contentView = views;
//            // Notification���풓������H
//            //status.flags |= Notification.FLAG_ONGOING_EVENT;
////            status.ledARGB = 0xffffff00;
////            status.ledOnMS = 300;
////            status.ledOffMS = 1000;
//            // status.flags |= Notification.DEFAULT_LIGHTS;
//                        
//            // Notification�̃A�C�R����ݒ�
//            status.icon = R.drawable.stat_notify_musicplayer;
//            SharedPreferences prefs = getSharedPreferences(
//                    MusicSettingsActivity.PREFERENCES_FILE, MODE_PRIVATE);            
//            boolean bVib = prefs.getBoolean(MusicSettingsActivity.KEY_ENABLE_MEDIA_CHANGE_VIBRATE, false);
//
//            if( bVib )
//            {
//	            // �o�C�u����΍Đ����ł��邱�ƂɋC�Â��̂�
////                status.flags |= Notification.DEFAULT_VIBRATE;
////                status.vibrate = new long[]{250,50,750,10};
//                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//                String sVib = prefs.getString(MusicSettingsActivity.KEY_VIBRATE_INTENSITY, "");
//                //Log.e("Vib",sVib);
//                long nVib = 0;
//                if( sVib != null && sVib.length() > 0 )
//                {
//                	nVib = Long.parseLong(sVib);
//                }
//                vibrator.vibrate(nVib);
//            }
//            // �N���b�N���ɔ��s�����C���e���g�H���낤���H
//            // �^�C�~���O���w�肵�Ĕ��s�ł���C���e���g
//            // ����͑����ANotification���N���b�N���ꂽ�Ƃ�
//            Intent clickIntent = new Intent();
//            clickIntent.setClassName(
//            		"okosama.app", "okosama.app.OkosamaMediaPlayerActivity");
//            status.contentIntent = PendingIntent.getActivity(this, 0,
//            		// TODO: Activity�ύX
//            		clickIntent
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
//            // statusbar��Notification�\��
//            startForeground(PLAYBACKSERVICE_STATUS, status);
//            if (!mIsSupposedToBePlaying) {
//            	// ����͂����炭�v���C���t���O�Ƃ��ė��p����Ă���
//            	// �܂����ꂪ�����Ă��Ȃ����
//            	// ���p���t���O�𗧂Ă�
//                mIsSupposedToBePlaying = true;
//                // �Đ���Ԃ̕ύX��ʒm����
//                notifyChange(PLAYSTATE_CHANGED);
//            }


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

//	void createLocationManager()
//	{
//		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//	}
//	void clearLocationManager()
//	{
//		mLocationManager = null;
//	}
	void clearGPS()
	{
	    if (locationClient != null) {
	    	locationClient.disconnect();
	    }
		
//		if (mLocationManager != null) {
//	        mLocationManager.removeUpdates(this);
//	    }
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
	}
	void stopLog()
	{
	    if(mTimer != null){
	        mTimer.cancel();
	        mTimer = null;
	        timerTask = null;
	    }
	}
	@Override
	public void onLocationChanged(Location location) {
		Log.v("GPS","onLocationChanged");
		// NOTICE: 受診毎に切断
		// TODO:つなぐタイミング制御
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
//		Log.v("----------", "----------");
//        Log.v("Latitude", String.valueOf(location.getLatitude()));
//        Log.v("Longitude", String.valueOf(location.getLongitude()));
//        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
//        Log.v("Altitude", String.valueOf(location.getAltitude()));
//        Log.v("Time", String.valueOf(location.getTime()));
//        Log.v("Speed", String.valueOf(location.getSpeed()));
//        Log.v("Bearing", String.valueOf(location.getBearing()));
		
        // Send intent to activity
        Intent activityNotifyIntent = new Intent();
        activityNotifyIntent.putExtra(MainActivity.LOCATION_DATA, location);
        activityNotifyIntent.setAction(
        		MainActivity.LOCATION_CHANGE_NOTIFY);
        getBaseContext().sendBroadcast(activityNotifyIntent);
        
        lastGetLocationTime = location.getTime();
        
	}

//	@Override
//	public void onProviderDisabled(String provider) {
//		Log.v("gps","onProviderDisabled");
//		// NOTICE: GPSが切れたとき。ここに来るかどうか要確認＆来たら、メッセージ表示、ワークアウト終了も考慮
//		// たぶん、必要ない？		
//	}

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
	
//	@Override
//	public void onProviderEnabled(String provider) {
//		Log.v("gps","onProviderEnabled");
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// どうやら、宛てにならないようなので、廃止する		
//		Log.v("gps","onStatusChanged");
//	}
}
