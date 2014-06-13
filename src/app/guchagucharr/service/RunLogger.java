package app.guchagucharr.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
//import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.MainActivity;
import app.guchagucharr.guchagucharunrecorder.RunNotificationSoundPlayer;
import app.guchagucharr.service.RunLoggerService.eMode;


public class RunLogger {
	//public static final String TEMP_INFO_FILE_NAME = "mode.tmp";

	public static ComponentName serviceName = null;
    public static IRunLoggerService sService = null;
    public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }
    private static HashMap<Context, ServiceBinder> sConnectionMap 
    = new HashMap<Context, ServiceBinder>();
//    
//    public static boolean hasServiceConnection(Context ctx)
//    {
//    	return sConnectionMap.containsKey(ctx);
//    }
//    public static int getServiceConnectionCount()
//    {
//    	return sConnectionMap.size();
//    }    
    
    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }
        
        @Override
		public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            sService = IRunLoggerService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }
        
        @Override
		public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
               mCallback.onServiceDisconnected(className);
            }
            Log.e("onServiceDisconnected","come");
            sService = null;
        }
    }
//    public static ServiceToken bindToService(Activity context) {
//        return bindToService(context, null);
//    }
    /**
     * サービスを開始し、サービスとActivityをつなぐ
     * @param context
     * @param callback
     * @return
     */
    public static ServiceToken bindToService(
    		Activity context, ServiceConnection callback) {
    	// 既に接続マップにあるかどうかチェックする
        Activity realActivity = context;
        for( Context ctmp : sConnectionMap.keySet() )
        {
        	ContextWrapper cwtmp = (ContextWrapper) ctmp;
        	if( cwtmp.getBaseContext().equals( context ) )
        	{
        		// 既にあるばあい、bindしない
        		return new ServiceToken(cwtmp);
        	}
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        // サービスがまだnullならば、サービス作成
        if( sService == null )
        {
	        serviceName = cw.startService(new Intent(cw, app.guchagucharr.service.RunLoggerService.class));
	        Log.v("componentName"," " + serviceName);
        }
        // サービスとActivityを接続
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, app.guchagucharr.service.RunLoggerService.class), sb, 0 )) { 
        		//Context.BIND_AUTO_CREATE)) {
            Log.v("bindService","come");
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        Log.e("RunLogger", "Failed to bind to service");
        return null;
    }
    public static void unbindFromService(ServiceToken token) 
    {
        if (token == null) {
            Log.e("RunLogger", "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            Log.e("RunLogger", "Trying to unbind for unknown Context");
            return;
        }
        Log.v("unbindService","come");                
        cw.unbindService(sb);
    }
	public static void stopService(Context ctx) {
		for( Entry<Context,ServiceBinder> entry : sConnectionMap.entrySet() )
		{
			try
			{
				entry.getKey().unbindService(entry.getValue());
			}
			catch ( IllegalArgumentException ex )
			{
				// 登録されていないサービスに対してUnbindの場合等にここにくるはず
				// その場合は、エラーにはせずにスルーする
				Log.e("unbindService - error", ex.getMessage() );
			}
			Log.v("unbindService","come" + entry.getKey().getClass());   
		}
		sConnectionMap.clear();
		Log.w("stopService","mapclear");   
		
		ctx.stopService(new Intent(ctx, RunLoggerService.class));
		serviceName = null;
        Log.v("stopService","come");		
		sService = null;
	}
	@SuppressWarnings("deprecation")
	public static int startLog(Activity activity,long time) throws RemoteException
	{
		int iRet = 0;
		
		// 位置情報取得開始
		if( false == RunLoggerService.getLogStocker().start(activity,time) )
		{
			// ログ取得開始に失敗したら、ログをクリアして戻る
			// RunLoggerService.clearRunLogStocker();
			// Toast.makeText(activity, R.string.cant_start_workout_because_error, Toast.LENGTH_LONG).show();
			return -1;
		}
		// Notificationの表示
		Notification notif = SetLoggingNotification(activity);
		RunLoggerService.setNotification(notif);
		// サービスの方でも、ログ取得開始？
		sService.startLog();
		
		// モードを計測中に設定
		sService.setMode( eMode.MODE_MEASURING.ordinal() );
		// モードをファイルに書き込み
		// writeModeToTmpFile(activity,eMode.MODE_MEASURING);
		return iRet;
	}
	public static int recovery(Context ctx, TempolaryDataLoader.TempolaryData data) throws RemoteException
	{
		int iRet = 0;
		// 位置情報取得開始
		if( false == RunLoggerService.getLogStocker().recovery(ctx, data, true) )
		{
			// ログ取得開始に失敗したら、ログをクリアして戻る
			return -1;
		}
		// サービスの方でも、ログ取得開始。ここは、復旧の時も同じで良いと思われる
		sService.startLog();
		// モードを計測中に設定
		sService.setMode( eMode.MODE_MEASURING.ordinal() );
		// モードをファイルに書き込み
		// writeModeToTmpFile(activity,eMode.MODE_MEASURING);
		// Notificationの表示
		SetLoggingNotification(ctx);
		return iRet;
	}
	
	static Notification SetLoggingNotification(Context ctx)
	{
//    	Notification.Builder builder = new Notification.Builder(activity);
//    	builder.setTicker("ticker");
//    	builder.setContentTitle("RunLoggerService");
//    	builder.setContentText("位置情報ログ取得中");
//    	builder.setSmallIcon(android.R.drawable.ic_dialog_info);
//    	builder.setWhen(System.currentTimeMillis());
//    	Notification notification = builder.build();		
		Notification notification = new Notification(android.R.drawable.ic_dialog_info,
				"RunLoggerService", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, MainActivity.class), 0);
		notification.setLatestEventInfo(ctx.getApplicationContext(),
				"RunLoggerService", "位置情報ログ取得中", contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
    	NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    	manager.notify(RunLoggerService.NOTIF_ID, notification);    	
		
    	return notification;
	}
	
	
	/**
	 * ファイルをストリームとして開く
	 * ==>共通化できるなら、共通化を行う
	 * @param file
	 * @return
	 */
    private static BufferedOutputStream openFileStream(File file)
    {
    	BufferedOutputStream bos = null;
		try {
			FileOutputStream fOut = new FileOutputStream(file);
	        bos = new BufferedOutputStream( fOut );    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bos;
    }
//	public static void writeModeToTmpFile(Activity activity, eMode mode)
//	{
//		// フォルダ取得
//		File tmpDir = activity.getFilesDir();
//		// 一時ファイル名作成
//		String gpxFilePath = tmpDir + "/" + TEMP_MODE_FILE_NAME;
//		// ファイルの書き込みを始める
//		try
//		{
//			File modeFile = new File( gpxFilePath );
//			if( modeFile.exists() == false )
//			{
//				modeFile.createNewFile();
//			}
//            BufferedOutputStream bos = openFileStream(modeFile);
//            String sMode;
//            sMode = String.valueOf(mode.ordinal());
//            bos.write( sMode.getBytes() );
//            bos.close();
//		}
//		catch ( Exception e)
//		{
//			e.printStackTrace();
//		}
//		return;
//	}
//	public static int getModeFromTmpFile(Activity activity)
//	{
//        String sMode = "";	
//		// フォルダ取得
//		File tmpDir = activity.getFilesDir();
//		// 一時ファイル名作成
//		String modefile = tmpDir + "/" + TEMP_MODE_FILE_NAME;
//		// ファイルの書き込みを始める
//		try
//		{
//			File modeFile = new File( modefile );
//			if( modeFile.exists() == false )
//			{
//				return -1;
//			}
//        	// 入力中の状態から復帰する
//			// onPauseの時、ファイルに保存されているはず。
//			FileReader fr = new FileReader(modefile);
//			//byte[] buffer = new char[fr.available()];
//			char[] buf = new char[30];
//			fr.read(buf);
//			fr.close();
//            if( buf[0] != 0)
//            {
//            	sMode = String.valueOf(buf[0]);
//            }
//		} catch( IOException ex ) {
//			ex.printStackTrace();
//			Log.e("ModeFileInput failed","");
//			return -1;
//		}        	
//		catch ( Exception e)
//		{
//			e.printStackTrace();
//			return -1;
//		}
//		if( sMode.isEmpty() == false )
//		{
//			return Integer.valueOf(sMode);
//		}
//		else
//		{
//			return -1;
//		}
//	}

}
