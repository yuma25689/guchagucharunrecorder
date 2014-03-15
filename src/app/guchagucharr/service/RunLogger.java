package app.guchagucharr.service;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;


public class RunLogger {

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
    public static ServiceToken bindToService(
    		Activity context, ServiceConnection callback) {
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
        serviceName = cw.startService(new Intent(cw, app.guchagucharr.service.RunLoggerService.class));
        Log.v("componentName"," " + serviceName);
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
//        if (sConnectionMap.isEmpty()) {
//            sService = null;
//        }
    }
	public static void stopService(Context ctx) {
		for( Entry<Context,ServiceBinder> entry : sConnectionMap.entrySet() )
		{
			entry.getKey().unbindService(entry.getValue());
			Log.v("unbindService","come" + entry.getKey().getClass());   
		}
		sConnectionMap.clear();
		
		ctx.stopService(new Intent(ctx, RunLoggerService.class));
		serviceName = null;
        Log.v("stopService","come");		
		sService = null;
	}

}
