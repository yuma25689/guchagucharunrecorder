/**
 * 
 */
package app.guchagucharr.guchagucharunrecorder;

//import java.util.HashMap;
//import java.util.Map;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import app.guchagucharr.service.RunHistoryLoader.ActivityData;
import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;

public final class ResourceAccessor {
	
	public static String SELECTOR_PREFIX ="selector_";
	
	public String IND_M = null;
	public String IND_KM = null;
	public String IND_MPERS = null;
	public String IND_KMPERHOUR = null;
	public String IND_SEC = null;
	public String IND_MINUTE = null;
	public String IND_HOUR = null;
		
	public static final long TIME_MINUTE = 60;
	public static final long TIME_HOUR = 60 * 60;
	public static final String DISPINFO_KEY = "DISP_INFO";
		
	private SparseArray<Bitmap> bmpArray = new SparseArray<Bitmap>();
	
	Activity activity;
	// 2014/04/10 データ編集に利用するテンポラリデータを、ここに設定するものとする
	ActivityData workOutDataTmp;
	public void setWorkOutDataTmp(ActivityData data)
	{
		workOutDataTmp = data;
	}
	public ActivityData getWorkOutDataTmp()
	{
		return workOutDataTmp;
	}
	ActivityLapData lapDataTmp;
	public void setLapDataTmp(ActivityLapData data)
	{
		lapDataTmp = data;
	}
	public ActivityLapData getLapDataTmp()
	{
		return lapDataTmp;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	public Activity getActivity() {
		return this.activity;
	}
	// Singleton
	private static ResourceAccessor instance = null;
	private ResourceAccessor(Activity activity) 
	{
		this.activity = activity;
		IND_M = activity.getString(R.string.meter);
		IND_KM = activity.getString(R.string.killometer);
		IND_MPERS = activity.getString(R.string.meterpersecond);
		IND_KMPERHOUR = activity.getString(R.string.killoperhour);
		IND_SEC = getString(R.string.second);
		IND_MINUTE = getString(R.string.minute);
		IND_HOUR = getString(R.string.hour);
		
	}
	public static void CreateInstance( Activity activity )
	{
		if( instance == null ) 
		{
			instance = new ResourceAccessor( activity );
		}
		else
		{
			instance.setActivity( activity );
		}
	}
	public static ResourceAccessor getInstance()
	{
		return instance;
	}
	public void clearAllBitmap()
	{
		bmpArray.clear();
	}
	public Bitmap createBitmapFromDrawableId( int id )
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_4444;
		
		Bitmap ret = null;
		
		String idString = activity.getResources().getResourceEntryName(id);
		if(idString.startsWith(SELECTOR_PREFIX))
		{
			return ret;
		}
		
		if( 0 < bmpArray.indexOfKey( id ) && bmpArray.get(id) != null )
		{
			ret = bmpArray.get(id);
		}
		else
		{
			boolean bMemErr = false;
			try {
				ret = BitmapFactory.decodeResource(activity.getResources(), id, options);
			} catch( OutOfMemoryError ex ) {
				System.gc();
				Log.e("Out of memory occur","bitmap create");
				bMemErr = true;
				ret = null;
			}
			if( ret == null )
			{
				String log = String.format("%X", id);
				Log.e("decodeError",log);
				if( bMemErr == true )
					ret = BitmapFactory.decodeResource(activity.getResources(), id, options);
			}
			if( ret != null )
			{
				bmpArray.put( id, ret );
			}
		}
		return ret;
	}	
	public Drawable getResourceDrawable( int id )
	{
		Bitmap bitmap = createBitmapFromDrawableId(id);
		//Matrix matrix = new Matrix();
		//matrix.postScale(1f, 1f);
		//Bitmap bitmapScale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);		
		BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(),bitmap);
		return bitmapDrawable;
		//return activity.getResources().getDrawable(id);
	}
	
	public int getIntPref( String name, int def) {
        SharedPreferences prefs =
            activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getInt(name, def);
    }
    
	public void setIntPref(String name, int value) {
        SharedPreferences prefs =
        	activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putInt(name, value);
        ed.commit();
    }
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
	
	/**
	 * @param context
	 * @param secs
	 * @return
	 */
	private static final Object[] sTimeArgs = new Object[5];

	public static String makeTimeString(Context context, long secs) {
		String durationformat = context.getString(
	                secs < 3600 ? R.string.durationformatshort : R.string.durationformatlong);
		/* Provide multiple arguments so the format can be changed easily
		 * by modifying the xml.
		 */
	    sFormatBuilder.setLength(0);

	    final Object[] timeArgs = sTimeArgs;
	    timeArgs[0] = secs / 3600;
	    timeArgs[1] = secs / 60;
	    timeArgs[2] = (secs / 60) % 60;
	    timeArgs[3] = secs;
	    timeArgs[4] = secs % 60;

	    return sFormatter.format(durationformat, timeArgs).toString();
	}
	public String getString( int id )
	{
		return activity.getResources().getString(id);
	}
	public int getColor( int id )
	{
		return activity.getResources().getColor(id);
	}
	
	public String getQuantityString( int id, int num, Object[] args )
	{
		return activity.getResources().getQuantityString(id, num, args);
	}
	
	public boolean bReadSDcardSuccess = false;
	public boolean isReadSDCardSuccess()
	{
		return bReadSDcardSuccess;
	}
	public void setReadSDCardSuccess(boolean b)
	{
		bReadSDcardSuccess = b;
	}
	
	public boolean isSdCanRead() {
	  String status = Environment.getExternalStorageState();
	  if (!status.equals(Environment.MEDIA_MOUNTED)) {
	    return false;
	  }
	
	  File file = Environment.getExternalStorageDirectory();
	  if (file.canRead()){
	    return true;
	  }
	  return false;
	}
	public BitmapFactory.Options getBitmapSizeFromMineType(int id)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();  
		options.inJustDecodeBounds = true;  
		
		//activity.getResources().getRe
		BitmapFactory.decodeResource(activity.getResources(), id, options);  
		  
//		int imageHeight = options.outHeight;  
//		int imageWidth = options.outWidth; 
//		String imageType = options.outMimeType;
		return options;
	}
	
}
