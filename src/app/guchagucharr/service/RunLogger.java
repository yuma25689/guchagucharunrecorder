package app.guchagucharr.service;

import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;


public class RunLogger 
{

    public static IRunLoggerService sService = null;
    public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }
    private static HashMap<Context, ServiceBinder> sConnectionMap 
    = new HashMap<Context, ServiceBinder>();
    
    public static boolean hasServiceConnection(Context ctx)
    {
    	return sConnectionMap.containsKey(ctx);
    }
    public static int getServiceConnectionCount()
    {
    	return sConnectionMap.size();
    }    
    
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
            sService = null;
        }
    }    
    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }
    public static ServiceToken bindToService(
    		Activity context, ServiceConnection callback) {
        Activity realActivity = context;
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, RunLoggerService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, RunLoggerService.class), sb, 0)) {
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
        cw.unbindService(sb);
        token = null;
        if (sConnectionMap.isEmpty()) {
            sService = null;
        }
    }

}
