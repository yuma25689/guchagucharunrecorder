package app.guchagucharr.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.R;

// �����j���O���̃f�[�^�𒙂߂�̃N���X
public class RunningLogStocker {

	//public static String KEY_LAP_INDEX = "KEY_LAP_INDEX";
	private final int MAX_LOCATION_LOG_CNT = 72000;	
	// ��ԑ����Ă�0.1�b���ł������O���擾�ł��Ȃ��͂��Ȃ̂ŁA�ō���2.4���ԕ�
	// �������A���[�^�[�̕��1m�ȏ�͂Ȃ�Ȃ��ƌv������Ȃ����䂪����̂ŁA
	// 0.1�b����������蕨�ɏ���Ă��Ȃ����薳�����Ǝv��
	static long removeMilli( long val )
	{
		return val * 1000;
	}
	// 1970�N����̎���(ms)
	long totalStartTime = 0;
	long totalStopTime = 0;
	double firstCorrectDistance = 0;	// GPS�Ōv������O�̌덷�ƂȂ鋗��
	public double getTotalDistance()
	{
		double ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getDistance();
		}
		return ret;
	}
	public long getTotalTime()
	{
		long ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getTotalTime();
		}
		return ret;
	}
	/**
	 * 
	 * @return m/s
	 */
	public double getTotalSpeed()
	{
		double ret = 0;
		for( int i=0; i<getLapCount(); i++ )
		{
			ret += lapData.get(iLap).getSpeedAccurateAsPossible();
		}
		ret /= getLapCount();
		return ret;
	}
	public LapData getLapData(int index)
	{
		return lapData.get(index);
	}
	public int getLapCount()
	{
		return iLap+1;
	}
	
	int iLap = 0;	// lap(from0)
	LapData currentLapData = new LapData();
	
	SparseArray<LapData> lapData = new SparseArray<LapData>();
	Vector<Location> vLocation = new Vector<Location>();
	public Vector<Location> getLocationData()
	{
		return vLocation;
	}
	Location prevLocation = null;
	public LapData getCurrentLapData()
	{
		return currentLapData;
	}

	public void clear()
	{
		lapData.clear();
		currentLapData.clear();
		iLap = 0;		
	}
	public RunningLogStocker(long time)
	{
		clear();
		totalStartTime = time;
		currentLapData.setStartTime(time);
	}
	public void putLocationLog( Location location )
	{
		if( vLocation.isEmpty() )
		{
			// ��̎�=����A����lap1(lap2�ȍ~)
			// ���̎���Speed�ƁA���Ԃŋ������v�Z���AGPS�ł܂��v������Ă��Ȃ��͈͂̋����Ƃ���
			// TODO: ���Ԃ�A���܂萳�m�ł͂Ȃ��̂ŁA�`�����X����������ʂ̂�����l��
			// ->�Ƃ肠�����A�������Ȃ�
			//long diffTime = location.getTime() - currentLapData.getStartTime();
			//firstCorrectDistance = location.getSpeed() * diffTime * removeMilli(diffTime);
			//currentLapData.increaseTime(diffTime);
			//currentLapData.increaseDistance(firstCorrectDistance);
		}
		else
		{
			// �����ŁA���̃��b�v�̊e�l�ɂ��Čv�Z���邪�A�p�t�H�[�}���X���l�����āA
			// �Ȃ�ׂ��ߋ��̒l�͌��Ȃ��Ă������悤�ɂ���
			//float[] result = new float[1];
			//float result = prevLocation.distanceTo(location);
//			Location.distanceBetween(
//					prevLocation.getLatitude(),//vLocation.lastElement().getLatitude(),
//					prevLocation.getLongitude(),//vLocation.lastElement().getLongitude(),
//					location.getLatitude(),
//					location.getLongitude(), result);
			//distance = result[0];
			currentLapData.increaseDistance(prevLocation.distanceTo(location));
			// long time = location.getTime() - vLocation.lastElement().getTime();
			// currentLapData.increaseTime(location.getTime() - prevLocation.getTime());//vLocation.lastElement().getTime());
			currentLapData.addSpeedData(location.getSpeed());
		}
        // Log.v("Speed", String.valueOf(location.getSpeed()));
		if( MAX_LOCATION_LOG_CNT < vLocation.size() )
		{
			// TODO: 精度の低いものを消す？
			// �}�b�N�X�l�𒴂�����A�^�񒆂�ւ񂩂甲���Ă���
			vLocation.remove(MAX_LOCATION_LOG_CNT/2);
		}
		// NOTICE:
		// Lapを各ロケーションに格納したかったが、Bundleはメモリを食いそうなので、
		// 未使用のbearingに無理矢理lapを突っ込む
//		Bundle b = new Bundle();
//		b.putInt(KEY_LAP_INDEX, iLap);
//		location.setExtras(b);
		location.setBearing(iLap);
		vLocation.add(location);
		prevLocation = new Location(location);
	}
	public void nextLap(Long time)
	{
		lapData.put(iLap, currentLapData);
		iLap++;
		currentLapData.clear();
		prevLocation = new Location( vLocation.lastElement() );
		// ���Ԃ�������������
		prevLocation.setTime(time);
	}
	public void stop( long time )
	{		
		totalStopTime = time;
		currentLapData.setStopTime(time);
		lapData.put(iLap, currentLapData);		
	}
	
	public ContentValues createContentValues(int tableID, 
			long insertTime, String[] strExtra, long lngExtra, int iExtra )
	{
		ContentValues ret = null;
		if( tableID == RunHistoryTableContract.HISTORY_TABLE_ID)
		{
			ret = new ContentValues();
			ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime);
			ret.put(RunHistoryTableContract.NAME, strExtra[0]);
			ret.put(RunHistoryTableContract.LAP_COUNT, getLapCount() );
            ret.put( RunHistoryTableContract.GPX_FILE_PATH, strExtra[1] );
			// TODO: place id under construction
			ret.put(RunHistoryTableContract.PLACE_ID, -1);

		}
		else
		{
			ret = new ContentValues();
    		ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime );
    		ret.put( RunHistoryTableContract.PARENT_ID, lngExtra );
    		ret.put( RunHistoryTableContract.LAP_INDEX, iExtra );
            ret.put( RunHistoryTableContract.LAP_DISTANCE, lapData.get(iExtra).getDistance() );
            ret.put( RunHistoryTableContract.LAP_TIME, lapData.get(iExtra).getTotalTime() );
            ret.put( RunHistoryTableContract.LAP_SPEED, lapData.get(iExtra).getSpeed() );
            // TODO: ������
            ret.put( RunHistoryTableContract.LAP_FIXED_DISTANCE, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_TIME, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_SPEED, 0 );
			
		}
		
		return ret;
		
	}
	

	public int insertRunHistoryLog(
			Activity activity,
			String logname,
			String gpxFilePath,
			RunningLogStocker log)
	{
		int insertCount = -1;
		activity.getContentResolver().insert(
				Uri.parse("content://" 
				+ RunHistoryTableContract.AUTHORITY + "/" 
				+ RunHistoryTableContract.HISTORY_TRANSACTION )
				,null);
        //SQLiteDatabase db = databaseHelper.getReadableDatabase();
        //db.beginTransaction();
        try {
        	// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        	ContentValues values = null;
        	Date date = new Date();
        	long time = date.getTime();
        	String saveText[] = { logname, gpxFilePath };
        	values = log.createContentValues(
        			RunHistoryTableContract.HISTORY_TABLE_ID
        			, time, saveText, 0, 0);
        	if( values == null )
        	{
        		Toast.makeText(activity, "failed to save the runnning data.", 
        				Toast.LENGTH_LONG).show();
                return -1;
        	}
        	Uri uriRet = activity.getContentResolver().insert(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/" 
        					+ RunHistoryTableContract.HISTORY_TABLE_NAME ), values);
            long id = Integer.parseInt(uriRet.getPathSegments().get(1)); 
        	if( -1 == id )
        	{
        		Toast.makeText(activity, "failed to save the runnning data.", 
        				Toast.LENGTH_LONG).show();
                return -1;        		
        	}
        	else
        	{
        		insertCount = 1;
        		for( int iLap=0; iLap < log.getLapCount(); iLap++ )
        		{
	            	values = log.createContentValues(RunHistoryTableContract.HISTORY_LAP_TABLE_ID, 
	            			time, 
	            			null,
	            			id,	// �e��id
	            			iLap);	// lap index
	            	if( values == null )
	            	{
	            		Toast.makeText(activity, "failed to save the runnning data.", 
	            				Toast.LENGTH_LONG).show();
	                    return -1;
	            	}
	            	// long lapId = 
	            	//db.insert(RunHistoryTableContract.HISTORY_LAP_TABLE_NAME, null, values);
	            	Uri uriRetLap = 
	            	activity.getContentResolver().insert(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/" 
        					+ RunHistoryTableContract.HISTORY_LAP_TABLE_NAME ), values);
	            	
	            	if( uriRetLap != null)
	            	{
	            		Log.v("insert",uriRetLap.toString());
	            	}
	            	else
	            	{
	            		Log.v("insert failed?","null");
	            	}
	            	insertCount++;
        		}
                //db.setTransactionSuccessful();
        		activity.getContentResolver().insert(
        				Uri.parse("content://" 
        				+ RunHistoryTableContract.AUTHORITY + "/" 
        				+ RunHistoryTableContract.HISTORY_COMMIT )
        				,null);
        		
        	}
        	
        	
        } finally {
        	//db.endTransaction();
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
    				,null);
        	
        }
        return insertCount;
	}
	
	static Activity mActivityWhenSave = null;
	/**
	 * �擾���ꂽ���O�f�[�^�̕ۑ�
	 * @return
	 */
	public void save(Activity activity, boolean bSaveGPX )
	{
		mActivityWhenSave = activity;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    	Date date = new Date();
    	String strDateTime = sdf.format( date );
    	// it's retry process too
		// gpx　out
		if( bSaveGPX
		&& ( outputGPXSaveResult == SAVE_NOT_TRY 
		||  outputGPXSaveResult != SAVE_OK )
		)
		{
			outputGPXSaveResult = SAVING;
			runHistorySaveResult = SAVING;			
	    	// TODO: SDカードにつなげない時の処理
	    	String dir = Environment.getExternalStorageDirectory() + "/" + activity.getPackageName();   
	    	FileOutputProcessor outFileProc = new FileOutputProcessor();
			outFileProc.outputGPX(activity, this, strDateTime, dir, 
					strDateTime + GPXGenerator.EXPORT_FILE_EXT );
		}
		else if( runHistorySaveResult == SAVE_NOT_TRY 
		||  runHistorySaveResult != SAVE_OK )
		{
			
			// GPXを保存する場合、スレッド終了後に行うのでここではやらない
			outputGPXSaveResult = SAVE_OK;
			runHistorySaveResult = SAVING;
			// database�ւ̕ۑ�
			int iInsCount = insertRunHistoryLog(activity, strDateTime, null, this );
			if( iInsCount < 0)
			{
				setRunHistorySaveResult( SAVE_NG, this );
			}
			else
			{
				setRunHistorySaveResult( SAVE_OK, this );
			}
		}
	}
	
	public static int SAVE_NOT_TRY = 2;
	public static int SAVING = 1;
	public static int SAVE_OK = 0;
	public static int SAVE_NG = -1;
	
	static int runHistorySaveResult = SAVE_NOT_TRY;
	static int outputGPXSaveResult = SAVE_NOT_TRY;
	
	public static void setRunHistorySaveResult( int code,RunningLogStocker stocker )
	{
		runHistorySaveResult = code;
		trySaveFinish(stocker);
	}
	
	public static void setOutputGPXSaveResult( int code,RunningLogStocker stocker )
	{
		outputGPXSaveResult = code;
		trySaveFinish(stocker);
	}
	
	public static void trySaveFinish(RunningLogStocker stocker)
	{
		if( outputGPXSaveResult == SAVE_NOT_TRY
		|| runHistorySaveResult == SAVE_NOT_TRY )
		{
			return;
		}
		
		if( outputGPXSaveResult != SAVING
		&& runHistorySaveResult != SAVING )
		{
			// 保存処理終了
			if( outputGPXSaveResult == SAVE_NG )
			{
				Toast.makeText(mActivityWhenSave, R.string.SaveErrorGPX, Toast.LENGTH_LONG).show();
				return;
			}
			else if( runHistorySaveResult == SAVE_NG )
			{
				// failed save database only 
				Toast.makeText(mActivityWhenSave, R.string.SaveError, Toast.LENGTH_LONG).show();				
			}
			Toast.makeText(mActivityWhenSave, R.string.SaveOK, Toast.LENGTH_LONG).show();
			
			stocker.clear();
			mActivityWhenSave.finish();
		}
	}
}
