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
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
//import android.os.Vibrator;

import java.lang.ref.WeakReference;

public class RunLoggerService extends Service {
    //private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    //private boolean mServiceInUse = false;

    public RunLoggerService() {
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

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
//        mWakeLock.setReferenceCounted(false);

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
        mServiceStartId = startId;
        return START_STICKY;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        //mServiceInUse = false;

        stopSelf(mServiceStartId);
        return true;
    }
    public int initGPS()
    {
//    	Looper looper = Looper.getMainLooper();
//    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 0, listener, looper);
    	
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
		public int initGPS() throws RemoteException {
			// TODO Auto-generated method stub
			return mService.get().initGPS();
		}
    }

	private final IBinder mBinder = new ServiceStub(this);
}
