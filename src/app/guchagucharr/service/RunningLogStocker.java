package app.guchagucharr.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import app.guchagucharr.guchagucharunrecorder.R;
import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;
import app.guchagucharr.guchagucharunrecorder.RunNotificationSoundPlayer;
import app.guchagucharr.guchagucharunrecorder.util.CurrentSettingUtil;
import app.guchagucharr.guchagucharunrecorder.util.FileUtil;
import app.guchagucharr.guchagucharunrecorder.util.TrackIconUtils;
// import app.guchagucharr.service.RunHistoryLoader.ActivityLapData;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;

public class RunningLogStocker {

	GPXGeneratorSync gpxGen = null;
	
	//public static String KEY_LAP_INDEX = "KEY_LAP_INDEX";
	//private final int MAX_LOCATION_LOG_CNT = 72000;	
	static long removeMilli( long val )
	{
		return val * 1000;
	}
	File workOutDir = null;
	Vector<String> vImageUrisNotPutGpx = new Vector<String>();
	HashMap<Long,String> mapImageUris = new HashMap<Long,String>();
	public void addImageUri(String uri)
	{
		// TODO: これをGPXに書こうと思ったけど、そんな例はないらしい・・・
		// とりあえず、ジオタグだけつけてなんとかする・・・？
		vImageUrisNotPutGpx.add( uri );
	}
	
	
	long totalStartTime = 0;
	long totalStopTime = 0;
	double firstCorrectDistance = 0;
	public double getTotalDistance()
	{
		double ret = 0;
		for( int i=0; i<getStockedLapCount(); i++ )
		{
			ret += lapData.get(i).getDistance();
		}
		
		//ret += getCurrentLapData().getDistance();
		return ret;
	}
	public long getTotalTime()
	{
		long ret = 0;
		for( int i=0; i<getStockedLapCount(); i++ )
		{
			ret += lapData.get(i).getTotalTime();
		}
		return ret;
	}
	/**
	 * 
	 * @return m/s
	 */
//	public double getTotalSpeed()
//	{
//		double ret = 0;
//		for( int i=0; i<getStockedLapCount(); i++ )
//		{
//			ret += lapData.get(iLap).getSpeedAccurateAsPossible();
//		}
//		ret /= getStockedLapCount();
//		return ret;
//	}
	public LapData getLapData(int index)
	{
		return lapData.get(index);
	}
	public LapData getLastLapData()
	{
		return lapData.get(m_iLap);
	}
	public int getStockedLapCount()
	{
		return lapData.size();//+1;
	}
	
	int m_iLap = 0;	// lap(from0)
	LapData currentLapData = new LapData();
	
	int iLocationDataCount = 0;
	SparseArray<LapData> lapData = new SparseArray<LapData>();
	Location currentLocation = null;
	//Vector<Location> vLocation = new Vector<Location>();
	public int getLocationDataCount()
	{
		return iLocationDataCount;
	}
	public Location getCurrentLocation()
	{
		return currentLocation;
	}
//	public Vector<Location> getLocationData()
//	{
//		return vLocation;
//	}
	Location prevLocation = null;
	public LapData getCurrentLapData()
	{
		return currentLapData;
	}

	public void clear()
	{
		lapData.clear();
		currentLapData.clear();
		m_iLap = 0;		
	}
	public RunningLogStocker()//long time)
	{
	}
	public boolean start(Activity activity, long time)
	{
		clear();
		totalStartTime = time;
		currentLapData.setStartTime(time);
		// そのワークアウトのフォルダを作成
		SimpleDateFormat sdfDateTime = new SimpleDateFormat(
				activity.getString(R.string.time_for_id_format));
		String strDateTime = sdfDateTime.format(time);		
    	// TODO: SDカードにつなげない時の処理
    	String dir = Environment.getExternalStorageDirectory() 
    			+ "/" + activity.getPackageName()
    			+ "/" + strDateTime;
    	workOutDir = new File( dir );
    	if( false == workOutDir.exists() )
		{
			if( false == workOutDir.mkdirs() )
			{
				Log.e("workOutDir create error", dir);
				return false;
			}
		}
    	Log.v("workOutDir created", dir);
    	if( -1 == insertLogMetaInfo(activity,RunLoggerService.eMode.MODE_MEASURING.ordinal(),
    			time,dir,this) )
    	{
    		Log.e("cant start because log start info insert failed","");
    		return false;
    	}
		// GPX出力開始
		gpxGen = new GPXGeneratorSync();
		// ファイル作成
		resetTmpGpxFile(activity);
		
		return true;
	}
	public int recoveryLogToMemoryFromGpx(String strGpxFolder,String strTmpGpxFilePath)
	{
		int iRet = 0;
		// GPXのフォルダを検索し、そこにあるGPXを全てメモリ展開する
		ArrayList<String> files = FileUtil.searchFiles(strGpxFolder,GPXGeneratorSync.EXPORT_FILE_EXT);
		// TODO:ソートで、ちゃんと昇順になっているかどうか調べること
		Collections.sort(files);
		m_iLap = 0;
		GPXImporterSync importer = null;
		for( String file : files )
		{
			// 頭を１周目として読み込む
			importer = new GPXImporterSync(file,this);
			if( false == importer.importData() )
			{
				Log.e("GpxImportError","perhaps recovery");
			}
		}
		
		// 一時ファイルとして格納されているGPXがあれば、それをカレントとしてメモリ展開？
		// ==>GPXフォルダの最新のものをカレントに展開しないといけないこともあるのだろうか？
		// また、一時フォルダにあるGPXは、不完全な可能性がある。
		if( strTmpGpxFilePath != null )
		{
			File fileTmpGpx = new File(strTmpGpxFilePath);
			if( fileTmpGpx.exists() )
			{
				importer = new GPXImporterSync(strTmpGpxFilePath,this);
				if( false == importer.importData() )
				{
					Log.e("GpxImportError","perhaps recovery");
					return -1;
				}
			}
		}
		return iRet;
	}
	public boolean recovery(Context ctx, TempolaryDataLoader.TempolaryData data, boolean bWorkOutStart)
	{
		// 一度全て消して、外部記憶のデータから設定し直す
		clear();
		// totalStartTime = data.getStartDateTime();

		// 一時フォルダのGPXファイルがあれば、それをカレントとしてリカバリ
		String gpxTmp = createRecoveryTmpGpxFile(ctx);

		// lapData
		// ==>現在のラップデータをメモリに設定し直す
		recoveryLogToMemoryFromGpx(data.getGpxDir(),gpxTmp);

		if( 0 < lapData.size() )
		{
			totalStartTime = lapData.get(0).getStartTime();
		}
		else
		{
			totalStartTime = data.getStartDateTime();
		}
		//currentLapData.setStartTime(totalStartTime);

		// currentLapData.setStartTime(time);
		// そのワークアウトのフォルダを作成
    	workOutDir = new File( data.getGpxDir() );
    	if( false == workOutDir.exists() )
		{
    		// ここにくるのは、フォルダができていないということだが、できていなければおかしい
			if( false == workOutDir.mkdirs() )
			{
				Log.e("workOutDir create error", data.getGpxDir());
				return false;
			}
		}
    	Log.v("workOutDir created or exists", data.getGpxDir());

    	if( bWorkOutStart )
    	{
	    	File tmpGpx = new File( getTmpGpxFilePath(ctx) );
			// GPX出力開始
			gpxGen = new GPXGeneratorSync();
	    	if( tmpGpx.exists() == true )
	    	{
	    		// 一時フォルダのGPXファイルが既にあれば、それをリカバリ（そこから書き込み続行）する
	    		gpxGen.recoveryGPXFile(getTmpGpxFilePath(ctx));
			}
	    	else
	    	{
				// ファイル作成
				resetTmpGpxFile(ctx);
	    	}
    	}
		return true;
	}
	/**
	 * 
	 * @param activity
	 * @param startTime
	 * @return null:失敗 ファイルパス:成功
	 */
	private String commitTmpGpxFile(Activity activity,long startTime)//String filePath)
	{
		String ret = null;
		if( gpxGen != null )
		{
			gpxGen.endCreateGPXFile();
			// ファイルをコピーする
			// コピー先ファイルの作成
			SimpleDateFormat sdfDateTime = new SimpleDateFormat(
					activity.getString(R.string.time_for_id_format));
			String strDateTime = sdfDateTime.format(startTime) + "_" + (m_iLap+1);
			String outputFileName = strDateTime + GPXGeneratorSync.EXPORT_FILE_EXT;
			String outputFilePath = workOutDir.getPath() + "/" + outputFileName;
			
			// コピー元ファイルの取得
			File tmpDir = activity.getFilesDir();			
			String gpxTmpFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;			
			File gpxTmpFile = new File( gpxTmpFilePath );
			if( gpxTmpFile.exists() )
			{
				File oFile = new File(outputFilePath);
				try {
					FileChannel iChannel = new FileInputStream(gpxTmpFile).getChannel();
					FileChannel oChannel = new FileOutputStream(oFile).getChannel();
					iChannel.transferTo(0, iChannel.size(), oChannel);
					iChannel.close();
					oChannel.close();
					ret = outputFilePath;
					clearTmpGpxFile(activity);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.e("FileCopy",e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("FileCopy",e.getMessage());
				}
			}
		}
		return ret;
	}
	private void clearTmpGpxFile(Context ctx)
	{
		// フォルダ取得
		File tmpDir = ctx.getFilesDir();
		// 一時ファイル名作成
		String gpxFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;
		// ファイルがあったら消す
		File gpxFile = new File( gpxFilePath );
		if( gpxFile.exists() )
		{
			gpxFile.delete();
		}		
	}
	private void clearRecoveryTmpGpxFile(Context ctx)
	{
		// フォルダ取得
		File tmpDir = ctx.getFilesDir();
		// 一時ファイル名作成
		String gpxFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME + "2";
		// ファイルがあったら消す
		File gpxFile = new File( gpxFilePath );
		if( gpxFile.exists() )
		{
			gpxFile.delete();
		}		
	}
	public static String getTmpGpxFilePath(Context ctx)
	{
		// フォルダ取得
		File tmpDir = ctx.getFilesDir();
		// 一時ファイル名作成
		String gpxFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;
		
		return gpxFilePath;
	}
	/**
	 * カレントのGPXファイルを、リカバリ用にタグを閉じて完成させたものをコピーして作成する 
	 * @return null:失敗 ファイルパス:成功
	 */
	private String createRecoveryTmpGpxFile(Context ctx)
	{
		String ret = null;
		GPXGeneratorSync gpxGenTmp = new GPXGeneratorSync();
		{
			// コピー元ファイルの取得
			File tmpDir = ctx.getFilesDir();
			String gpxTmpFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;			
			File gpxTmpFile = new File( gpxTmpFilePath );
			if( gpxTmpFile.exists() )
			{
				// ファイルをコピーする
				// コピー先ファイルの作成
				String outputFilePath = tmpDir.getPath() 
						+ "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME + "2";
				File oFile = new File(outputFilePath);
				try {
					FileChannel iChannel = new FileInputStream(gpxTmpFile).getChannel();
					FileChannel oChannel = new FileOutputStream(oFile).getChannel();
					iChannel.transferTo(0, iChannel.size(), oChannel);
					iChannel.close();
					oChannel.close();
					ret = outputFilePath;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.e("FileCopy createRecoveryTmpGpxFile",e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("FileCopy createRecoveryTmpGpxFile",e.getMessage());
				}

				// コピー後、さらにコピー先ファイルをXMLとして完成させる処理を行う
				
				int iRet = gpxGenTmp.checkCommitedGpxFile(outputFilePath);
				if( iRet == 1 )
				{
					// まだ閉じられていないと思われる場合
					// NOTICE: この方法では、確実に復旧できる訳ではないが、
					// 大体の場合は復旧できるはず
					gpxGenTmp.recoveryGPXFile(outputFilePath);
					gpxGenTmp.endCreateGPXFile();
					ret = outputFilePath;
				}
				else if( iRet == 0 )
				{
					ret = outputFilePath;
				}
			}
		}
		return ret;
	}
	
	private void resetTmpGpxFile(Context ctx)
	{
//		// フォルダ取得
//		File tmpDir = activity.getFilesDir();
//		// 一時ファイル名作成
//		String gpxFilePath = tmpDir + "/" + GPXGeneratorSync.GPX_TEMP_FILE_NAME;
		// ファイルの書き込みを始める
		gpxGen.startCreateGPXFile( getTmpGpxFilePath(ctx));
	}
	public void putLocationLog( Context ctx, Location location )
	{
		if( 0 == iLocationDataCount ) // vLocation.isEmpty() )
		{
		}
		else
		{
		
			int iPrev =	(int)Math.floor(
							CurrentSettingUtil.getCurrentDefaultUnitDistanceFromMeter(
									currentLapData.getDistance()));
			
			currentLapData.increaseDistance(prevLocation.distanceTo(location));
			currentLapData.addSpeedData(location.getSpeed());

			int iCurrent =	(int)Math.floor(
					CurrentSettingUtil.getCurrentDefaultUnitDistanceFromMeter(
							currentLapData.getDistance()));
			if( iPrev < iCurrent )
			{
		        // 音声でユーザに到達距離を通知
		        RunNotificationSoundPlayer.soundArrivalNotify(
		        		ctx,
		        		iCurrent,
		        		UnitConversions.DISTANCE_UNIT_KILOMETER
		        		);
			}
		}
		location.setBearing(m_iLap);
		
		// ファイル出力
		if( gpxGen != null )
		{
			gpxGen.addLocationToCurrentGPXFile(location);
			currentLocation = location;
			iLocationDataCount++;
			prevLocation = new Location(location);
		}
	}
	public void putLocationLogNotAddFile( Location location, String gpxpath )
	{
		int iLap = (int)location.getBearing();
		if( m_iLap < iLap )
		{
			nextLapNoFileProcess(m_iLap, location.getTime(), gpxpath);
			m_iLap = iLap;
		}
		// location.setBearing(m_iLap);

		if( 0 == iLocationDataCount ) // vLocation.isEmpty() )
		{
			currentLapData.setStartTime(location.getTime());
			currentLapData.setGpxFilePath(gpxpath);
		}
		else
		{
			currentLapData.increaseDistance(prevLocation.distanceTo(location));
			currentLapData.addSpeedData(location.getSpeed());
			currentLapData.setStopTime(location.getTime());
		}
		currentLocation = location;		
		iLocationDataCount++;
		prevLocation = new Location(location);
	}
	public void nextLap(Activity activity, Long time)
	{
		// TODO: コピーすべき？
		currentLapData.setStopTime(time);
		// 作成中のGPXを閉じて、保存場所にコピー後、データとしてそのパスを保存する
		String strGpxFile = commitTmpGpxFile(activity,currentLapData.getStartTime());
		currentLapData.setGpxFilePath(strGpxFile);
		resetTmpGpxFile(activity);

		LapData saveLapData = new LapData(currentLapData);
		lapData.put(m_iLap, saveLapData);
		m_iLap++;
		currentLapData.clear();
		currentLapData.setStartTime(time);
		
		if( 0 < iLocationDataCount )//vLocation.size() )
		{
			prevLocation = new Location( currentLocation );//vLocation.lastElement() );
			prevLocation.setTime(time);
		}
		else
		{
			prevLocation = null;
		}
	}
	public void nextLapNoFileProcess(int iLap,Long time,String path)
	{
		currentLapData.setStopTime(time);

		LapData saveLapData = new LapData(currentLapData);
		lapData.put(iLap, saveLapData);
		currentLapData.clear();
		currentLapData.setStartTime(time);
		currentLapData.setGpxFilePath(path);
		
		if( 0 < iLocationDataCount )//vLocation.size() )
		{
			prevLocation = new Location( currentLocation );//vLocation.lastElement() );
			prevLocation.setTime(time);
		}
		else
		{
			prevLocation = null;
		}
	}
	public void stop( Activity activity, long time, boolean bRecoveryMode )
	{		
		totalStopTime = time;
		currentLapData.setStopTime(time);
		if( false == bRecoveryMode )
		{
			// 作成中のGPXを閉じて、保存場所にコピー後、データとしてそのパスを保存する
			String strGpxFile = commitTmpGpxFile(activity,currentLapData.getStartTime());
			currentLapData.setGpxFilePath(strGpxFile);
		}
		lapData.put(m_iLap, currentLapData);
		deleteLogMetaInfo(activity);
	}
	
	public ContentValues createContentValues(Activity activity, int tableID, 
			long insertTime, String[] strExtra, long lngExtra, int iExtra )
	{
		ContentValues ret = null;
		if( tableID == RunHistoryTableContract.HISTORY_TABLE_ID)
		{
			ret = new ContentValues();
			ret.put(RunHistoryTableContract.START_DATETIME, totalStartTime);
			ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime);
			ret.put(RunHistoryTableContract.NAME, strExtra[0]);
			ret.put(RunHistoryTableContract.LAP_COUNT, getStockedLapCount() );
			// 2014/02/17 lapに移動
            //ret.put( RunHistoryTableContract.GPX_FILE_PATH, strExtra[1] );
			// TODO: place id under construction
			ret.put(RunHistoryTableContract.PLACE_ID, -1);
			ret.put(RunHistoryTableContract.ACTIVITY_TYPE, 
					RunLoggerService.getActivityTypeCode() );
					//RunLogger.sService.getActivityTypeCode());

		}
		else if( tableID == RunHistoryTableContract.HISTORY_LAP_TABLE_ID)
		{
			ret = new ContentValues();
			ret.put(RunHistoryTableContract.START_DATETIME, lapData.get(iExtra).getStartTime());
    		ret.put(RunHistoryTableContract.INSERT_DATETIME, insertTime );
    		ret.put( RunHistoryTableContract.PARENT_ID, lngExtra );
    		ret.put( RunHistoryTableContract.LAP_INDEX, iExtra );
            ret.put( RunHistoryTableContract.LAP_DISTANCE, lapData.get(iExtra).getDistance() );
            ret.put( RunHistoryTableContract.LAP_TIME, lapData.get(iExtra).getTotalTime() );
            ret.put( RunHistoryTableContract.LAP_SPEED, lapData.get(iExtra).getSpeed() );
            ret.put( RunHistoryTableContract.LAP_FIXED_DISTANCE, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_TIME, 0 );
            ret.put( RunHistoryTableContract.LAP_FIXED_SPEED, 0 );
            if( strExtra != null)
            {
            	ret.put( RunHistoryTableContract.NAME, strExtra[0]);//activity.getString(R.string.LAP_LABEL) + ( iExtra + 1 ) );
            }
            else
            {
            	ret.put( RunHistoryTableContract.NAME, "");            	
            }
            ret.put( RunHistoryTableContract.GPX_FILE_PATH, lapData.get(iExtra).getGpxFilePath() );//strExtra[0] );
			
		}
		else if( tableID == RunHistoryTableContract.TEMPOLARY_INFO_TABLE_ID)
		{
			ret = new ContentValues();
			//try {
				ret.put(RunHistoryTableContract.CURRENT_MODE, iExtra );//RunLogger.sService.getMode() );
				ret.put(RunHistoryTableContract.START_DATETIME, insertTime );//lapData.get(0).getStartTime());
	            if( strExtra != null)
	            {
	            	ret.put( RunHistoryTableContract.GPX_FILE_DIR, strExtra[0] );
	            }
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.e("Create Tempolary table value failed",e.getMessage());
//			}
			
		}
		
		return ret;
		
	}
	
	public int insertLogMetaInfo(
			Activity activity,
			int iMode,
			Long startTime,
			String gpxFileDir,
			RunningLogStocker log)
	{
		int insertCount = -1;
		activity.getContentResolver().insert(
				Uri.parse("content://" 
				+ RunHistoryTableContract.AUTHORITY + "/" 
				+ RunHistoryTableContract.HISTORY_TRANSACTION )
				,null);
        try {
        	ContentValues values = null;
        	String[] data = {gpxFileDir};
        	values = log.createContentValues(
        			activity,
        			RunHistoryTableContract.TEMPOLARY_INFO_TABLE_ID
        			, startTime, data, 0, iMode);
        	if( values == null )
        	{
        		Toast.makeText(activity, "failed to save the running start data.", 
        				Toast.LENGTH_LONG).show();
                return -1;
        	}
        	Uri uriRet = activity.getContentResolver().insert(
        					Uri.parse("content://" 
        					+ RunHistoryTableContract.AUTHORITY + "/" 
        					+ RunHistoryTableContract.TEMPOLARY_INFO_TABLE_NAME ), values);
            long id = Long.parseLong(uriRet.getPathSegments().get(1)); 
        	if( -1 == id )
        	{
        		// TODO:Test用
        		Toast.makeText(activity, "failed to save the runnning start data.", 
        				Toast.LENGTH_LONG).show();
        		
            	int iRet = activity.getContentResolver().delete(
    					Uri.parse("content://" 
    					+ RunHistoryTableContract.AUTHORITY + "/" 
    					+ RunHistoryTableContract.TEMPOLARY_INFO_TABLE_NAME  ), null, null );
            	
    					//RunHistoryTableContract.PARENT_ID + "=" + item.getItemId(),null);
            	if( iRet <= 0 )
            	{
            		// TODO:テスト用
            		Toast.makeText(activity, "failed to delete the runnning start data.", 
            				Toast.LENGTH_LONG).show();
            		return -1;
            	}
        		
            	// deleteをコミットさせる
                // return -1;        		
        	}
        	else
        	{
        		insertCount++;
        	}
            //db.setTransactionSuccessful();
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_COMMIT )
    				,null);
        		
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
	public boolean deleteLogMetaInfo(Activity activity)
	{
    	// トランザクションの考慮
		activity.getContentResolver().insert(
				Uri.parse("content://"
				+ RunHistoryTableContract.AUTHORITY + "/"
				+ RunHistoryTableContract.HISTORY_TRANSACTION )
				,null);
        try {
        	
        	// where句を指定しない
        	// 試してみたら、削除はされた模様
        	int iRet = activity.getContentResolver().delete(
					Uri.parse("content://" 
					+ RunHistoryTableContract.AUTHORITY + "/" 
					+ RunHistoryTableContract.TEMPOLARY_INFO_TABLE_NAME  ), null, null );
					//RunHistoryTableContract.PARENT_ID + "=" + item.getItemId(),null);
        	if( iRet <= 0 )
        	{
        		return false;
        	}
    		activity.getContentResolver().insert(
    				Uri.parse("content://" 
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_COMMIT )
    				,null);
        } catch(Exception e){
        	e.printStackTrace();
        	Log.e("tempolary table delete failed!",e.getMessage());
        	return false;
        } finally {
        	//db.endTransaction();
    		activity.getContentResolver().insert(
    				Uri.parse("content://"
    				+ RunHistoryTableContract.AUTHORITY + "/" 
    				+ RunHistoryTableContract.HISTORY_ENDTRANSACTION )
    				,null);
        	
        }
        return true;
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
        	String saveText[] = { logname };//, gpxFilePath };
        	values = log.createContentValues(
        			activity,
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
        		for( int iLap=0; iLap < log.getStockedLapCount(); iLap++ )
        		{
        			String[] texts = null;
        			if( 1 < log.getStockedLapCount() )
        			{
        				String name = String.valueOf( iLap + 1 );
            			texts = new String[1];
            			texts[0] = name;
        			}
                	//String saveText2[] = { gpxFilePath };
	            	values = log.createContentValues(
	            			activity,
	            			RunHistoryTableContract.HISTORY_LAP_TABLE_ID, 
	            			time, 
	            			texts,//saveText2,
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
	public void save(Activity activity, String name, boolean bSaveGPX )
	{
		mActivityWhenSave = activity;
    	//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    	// Date date = new Date();
//    	String strDateTime = null;
//		try {
//			strDateTime = sdf.format( RunLogger.sService.getTimeInMillis() );
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// NOTICE: ここでこのフラグを立てることで、次のif文の上のフローには入らない
		// なぜなら、ここではGPXを保存せず、常に保存し続ける処理に変更になった。
		outputGPXSaveResult = SAVE_OK;
    	// it's retry process too
		// gpx　out
//		if( bSaveGPX
//		&& ( outputGPXSaveResult == SAVE_NOT_TRY 
//		||  outputGPXSaveResult != SAVE_OK )
//		)
//		{
//			outputGPXSaveResult = SAVING;
//			runHistorySaveResult = SAVING;			
//	    	// TODO: SDカードにつなげない時の処理
//	    	String dir = Environment.getExternalStorageDirectory() 
//	    			+ "/" + activity.getPackageName()
//	    			+ "/" + strDateTime;
//	    	FileOutputProcessor outFileProc = new FileOutputProcessor();
//			outFileProc.outputGPX(activity, this, name, dir, //strDateTime, dir, 
//					strDateTime + GPXGenerator.EXPORT_FILE_EXT );
//		}
		//else 
		if( runHistorySaveResult == SAVE_NOT_TRY 
		||  runHistorySaveResult != SAVE_OK )
		{
			// GPXを保存する場合、スレッド終了後に行うのでここではやらない
			outputGPXSaveResult = SAVE_OK;
			runHistorySaveResult = SAVING;
			// database
			int iInsCount = insertRunHistoryLog(activity, name, null, this );//strDateTime, null, this );
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
			
	        // 音声でユーザに保存完了を通知
	        RunNotificationSoundPlayer.soundActivitySaved(ResourceAccessor.getInstance().getActivity());
			
			stocker.clear();
			mActivityWhenSave.finish();
		}
	}
}
