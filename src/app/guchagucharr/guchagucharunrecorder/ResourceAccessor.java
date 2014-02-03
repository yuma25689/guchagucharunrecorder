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
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import app.guchagucharr.service.RunningLogStocker;

/**
 * ���\�[�X�ɃA�N�Z�X���邽�߂̃N���X
 * dalvik�̐���(�����炭�A24MB�`48MB���炢�̃������m�ۂŗ�����)
 * ��������邽�߂ɁA�Ȃ�ׂ�Drawable��20MB�ȏ�ɂȂ�Ȃ��悤�ɂ���
 * (1��ʂŎg��Drawable�̃T�C�Y������𒴂��Ă��܂��A�Ȃǂ̂ǂ����悤���Ȃ��ꍇ�͏���)
 * @author 25689
 *
 */
public final class ResourceAccessor {
	
	public static String SELECTOR_PREFIX ="selector_";
	
	public static String IND_M = null;
	public static String IND_KM = null;
	public static String IND_MPERS = null;
	public static String IND_KMPERHOUR = null;
	public static String IND_SEC = null;
	public static String IND_MINUTE = null;
	public static String IND_HOUR = null;
		
	public static final long TIME_MINUTE = 60;
	public static final long TIME_HOUR = 60 * 60;
	public static final String DISPINFO_KEY = "DISP_INFO";
	
	public static final int SOUND_MAX_COUNT = 9;
	public static final int SOUND_RES_IDS[] =
		{
//			R.raw.sound1,
//			R.raw.sound2,
//			R.raw.sound3,
//			R.raw.sound4,
//			R.raw.sound5,
//			R.raw.sound6,
//			R.raw.sound7,
//			R.raw.sound8,
//			R.raw.sound9
		};
	private int soundIds[];
	private int iSoundLoadCnt = 0;
	private SoundPool soundPool;
	
	private SparseArray<Bitmap> bmpArray = new SparseArray<Bitmap>();
	private RunningLogStocker runLogStocker = null;
	
	public void createLogStocker(long time)
	{
		runLogStocker = new RunningLogStocker(time);
	}
	public RunningLogStocker getLogStocker()
	{
		return runLogStocker;
	}
	public void putLocationLog( Location data )
	{
		runLogStocker.putLocationLog(data);
	}
	public boolean isEmptyLogStocker()
	{
		if( runLogStocker == null )
			return true;
		
		return false;
	}
	public void clearRunLogStocker()
	{
		runLogStocker = null;
	}
	
	// ���\�[�X���擾���邽�߂̃A�N�e�B�r�e�B��ݒ�
	// TODO: �������A�����ɕێ����Ă����ƁA
	// �ċN����ȂǂɃA�N�e�B�r�e�B���L��ǂ������ׂȂ��Ă����̂��낤���H
	Activity activity;
	
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
	public void initSound()
	{
		// �����o�͐ݒ�̏�����s��
		soundPool = new SoundPool(SOUND_MAX_COUNT,AudioManager.STREAM_MUSIC,100);
		soundPool.setOnLoadCompleteListener(
				new OnLoadCompleteListener()
				{
					@Override
					public void onLoadComplete(SoundPool s,int Id, int sts)
					{
						if( sts == 0 ) iSoundLoadCnt++;
					}
				}
		);
		soundIds = new int[SOUND_RES_IDS.length];
		int j=0;
		for( int i : SOUND_RES_IDS ) {
			soundIds[j] = soundPool.load(this.activity, i, 1);
			j++;
		}
	}
	public void playSound( int idIndex )
	{
		if( iSoundLoadCnt != SOUND_RES_IDS.length 
		|| idIndex < 0 
		|| SOUND_RES_IDS.length <= idIndex)
		{
			// ������̏ꍇ�A�������́Aindex������̏ꍇ�A�Đ����Ȃ�
			return;
		}
		// id, leftVol, rightVol, priority, loop, speedrate
		soundPool.play(soundIds[idIndex], 2.0f, 2.0f, 1, 0, 1.0f);
		//soundPool.stop(soundIds[idIndex]);
	}
	public void releaseSound()
	{
		iSoundLoadCnt = 0;
		if( soundPool != null )
		{
			soundPool.release();
		}
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
		
		// selector�̏ꍇ�A���[�h�ł��Ȃ�
		// �l�[�~���O�K��ɂ���ē��̃v���t�B�b�N�X�͌Œ�Ƃ���
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
//				Log.i("test", activity.getResources().getResourcePackageName(id));
//				Log.i("test", activity.getResources().getResourceName(id));
//				Log.i("test", activity.getResources().getResourceEntryName(id));
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
		// �X�P�[���̐ݒ�
		//Matrix matrix = new Matrix();
		//matrix.postScale(1f, 1f); // 2�{�Ɋg��
		// �w��̃X�P�[����bitmap�č쐬
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
	 * �w�肳�ꂽ�b���̎��Ԃ��A�`���ԂƂ����\���ɕς���
	 * TODO:���̃A�v���P�[�V�����ł́A������ł̎��ԕ\���͍s��Ȃ��A���Ȃ킿�A�b��łł���A�����s�v�ɂȂ�̂ŁA�폜����
	 * ���������A���̃N���X�ɂ����̂͂�������
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
	  //SD�J�[�h�����邩�`�F�b�N
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
