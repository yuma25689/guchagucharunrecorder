package app.guchagucharr.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.app.Activity;
import android.location.Location;
import android.util.Log;
import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;
import app.guchagucharr.guchagucharunrecorder.util.TrackIconUtils;

/**
 * @author 25689
 *
 */
public class GPXGeneratorSync {
	// public static final String EXPORT_FILE_DIR = "/sdcard/patiman/export";
	public static final String EXPORT_FILE_EXT = ".gpx";
	public static final String LAP_TEXT = "Lap";
	public static final String GPX_TEMP_FILE_NAME = "gpx.tmp";
	
//	private static final String[][] ESC_CHRS = {
//		{"&","&amp;"},
//		{"'","&apos;"},
//		{"<","&lt;"},
//		{">","&gt;"},
//		{"\"","&quot;"}
//	};
	
	private Exporter _exporter = null;

	/*
	 * <?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/0" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<trk><name>莉吩ｺｺ繝ｶ蟯ｳ</name>
<number>1</number>
<trkseg>
<trkpt lat="36.415416275733136" lon="139.4245576551685"><ele>253</ele><time>2010-09-18T23:08:00Z</time></trkpt>
<trkpt lat="36.415147339915855" lon="139.42455766633571"><ele>253</ele><time>2010-09-18T23:08:32Z</time></trkpt>
繝ｻ繝ｻ繝ｻ
繝ｻ繝ｻ繝ｻ
繝ｻ繝ｻ繝ｻ
</trkseg></trk></gpx> 
	 */
	
	public static final String LINE_SEP = System.getProperty("line.separator");
	public static final String XML_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + LINE_SEP;
	public static final String GPX_START_TAG 
	= "<gpx version=\"1.1\"" + LINE_SEP
	+ "creator=\"GuchaGuchaRunRecorder\"" + LINE_SEP
	+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + LINE_SEP
	+ "xmlns=\"http://www.topografix.com/GPX/1/0\"" + LINE_SEP
	+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">"
	+ LINE_SEP;
	public static final String GPX_END_TAG 
	= "</gpx>" + LINE_SEP;
	
	public static final String TAG_ACTIVITY_TYPE_FOR_MY_TRACKS = "<type><![CDATA[";
	public static final String TAG_ACTIVITY_TYPE_FOR_MY_TRACKS_CLOSE = "]]></type>";	
	public static final String TAG_LEFT_BLANCKET = "<";
	public static final String TAG_LEFT_BLANCKET_OF_CLOSE = "</";
	public static final String TAG_RIGHT_BLANCKET_OF_CLOSE = "/>" + LINE_SEP;
	public static final String TAG_RIGHT_BLANCKET = ">";
	
	public static final String TAG_NAME_TRACK = "trk";
	public static final String TAG_NAME_NAME = "name";
	public static final String TAG_NAME_NUMBER = "number";
	public static final String TAG_NAME_TRK_SEGMENT = "trkseg";
	public static final String TAG_NAME_TRK_POINT = "trkpt";
	public static final String TAG_NAME_ELEVATION = "ele";
	public static final String TAG_NAME_TIME = "time";
	public static final String TAG_NAME_SPEED = "speed";
	
//	public static final String ATTR_NAME_LATITUDE_EQUAL = "lat=";
//	public static final String ATTR_NAME_LONGITUDE_EQUAL = "lon=";

	public static final String ATTR_LAT_AND_LONGITUDE = " lat=\"%.15f\" lon=\"%.15f\"";
	
	
	public static final int NG_ERROR_UNKNOWN = -1;
	public static final int RETURN_OK = 0;
	
	// SparseArray<LapData> lapData = null;

	File gpxFile = null;
    FileOutputStream fOut =  null;
    //BufferedOutputStream bos = null;
	int iCurrentOutputLap = -1;

    public GPXGeneratorSync()
    //Vector<Location> vData_, Handler hdr_ )//SparseArray<LapData> lapData_, Handler hdr_)
	{
    	clearCurrentBuf();	// 無意味だが、一応
		// lapData = lapData_;
	}

    public void clearCurrentBuf()
    {
    	gpxFile = null;
        fOut =  null;
    }

    public BufferedOutputStream openGPXFileStream(File file,boolean append)
    {
    	BufferedOutputStream bos = null;
        //FileOutputStream fOut;
		try {
			FileOutputStream fOut = new FileOutputStream(file,append);
	        bos = new BufferedOutputStream( fOut );    	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos;
    }
    
	/**
	 * start creating GPX file
	 * @param activity
	 * @param gpxFilePath
	 * @return
	 */
	public int startCreateGPXFile(
			//Activity activity,
			String gpxFilePath )
			//int iLap ) ラップはLocationのBearingに無理矢理セットしてあるので、そこから取れる
	{
		// iCurrentOutputLap = iLap;
		try
		{
			File gpxFile = new File( gpxFilePath );
			// 新規作成
            gpxFile.createNewFile();
            BufferedOutputStream bos = openGPXFileStream(gpxFile,false);

            openGPXFile( bos );
			_exporter.startExport();
		}
		catch ( Exception e)
		{
			return NG_ERROR_UNKNOWN;
		}					
		return RETURN_OK;
	}
	/**
	 * 指定されたGPXファイルが、コミット済のものかどうか
	 * @param gpxFilePath
	 * @return -1:エラー、ファイルが空、またはファイルが不正 0:コミット済 1:コミットされていない
	 */
	public static int checkCommitedGpxFile(String gpxFilePath)
	{
		final int CONFIRM_LINE_CNT = 3;		
		ArrayList<String> buf = new ArrayList<String>();
		// ファイルを開くが、ただ単に、ファイルの最後に書かれているのが、
		// このアプリで吐かれたGPXの終了部分と一致するかどうかを調べる
		try
		{
			File file = new File( gpxFilePath );
			if( file.exists() == false )
			{
				return -1;
			}
			// 入力中の状態から復帰する
			FileReader fr = new FileReader(file);
			BufferedReader inBuffer = new BufferedReader(fr);
			String line = null;
			while ((line = inBuffer.readLine()) != null) 
			{
				// 行ごとにバッファに読み込みを行う
				buf.add(line);
				if( CONFIRM_LINE_CNT < buf.size() )
				{
					buf.remove(0);
				}
		    }
			inBuffer.close();
			fr.close();
		} catch( IOException ex ) {
			ex.printStackTrace();
			Log.e("RecoveryFileInput failed","");
			return -1;
		}        	
		catch ( Exception e)
		{
			e.printStackTrace();
			Log.e("RecoveryFileInput failed","");
			return -1;
		}
		
		// 下記が、コミットされているGPXのラスト３行のはず？
		final String[] sAnswer = {
			TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
			,TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET
			,GPX_END_TAG
		};
		// 読み込んだバッファから、確認を行う
		boolean bBufOK = true;
		int i=0;
		for( String line : buf )
		{
			// コミットされているGPXのラスト３行のはずのテキストと、ファイルのラスト３行を一致確認する
			// TODO: 実際にコミットされているGPXに書かれている文字列をデバッグ確認すること！
			if( false == line.equals(sAnswer[i]) )
			{
				bBufOK = false;
				break;
			}
		}
		if( buf.isEmpty() || buf.size() < CONFIRM_LINE_CNT )
		{
			bBufOK = false;
		}
		if( bBufOK )
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
	/**
	 * start creating GPX file
	 * @param activity
	 * @param gpxFilePath
	 * @return
	 */
	public int recoveryGPXFile(
			//Activity activity,
			String gpxFilePath )
			//int iLap ) ラップはLocationのBearingに無理矢理セットしてあるので、そこから取れる
	{
		// iCurrentOutputLap = iLap;
		try
		{
			File gpxFile = new File( gpxFilePath );
			// 続きから
            BufferedOutputStream bos = openGPXFileStream(gpxFile,true);

            // 少なくとも最初のラップ開始の表記はファイルに書かれているものとするので、
            // 最初のラップの表記が書かれた直後から書けるように、iCurrentOutputLapを-1から0に更新しておく
            iCurrentOutputLap = 0;
            openGPXFile( bos );
		}
		catch ( Exception e)
		{
			return NG_ERROR_UNKNOWN;
		}					
		return RETURN_OK;
	}
    public void addLocationToCurrentGPXFile(Location loc)
    {
        try {
        	_exporter.exportLoc( loc );
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("addLocationToCurrentGPXFile",e.getMessage());
		}
    }
    public void openGPXFile(BufferedOutputStream bos)
    {
		_exporter = new Exporter(bos);    	
    }
//    public void openGPXStream()
//    {
//        try {
//			_exporter.endExport();
//			_exporter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			Log.e("addLocationToCurrentGPXFile",e.getMessage());
//		}
//    }
    public void endCreateGPXFile()
    {
        try {
			_exporter.endExport();
			_exporter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("addLocationToCurrentGPXFile",e.getMessage());
		}
    }
	
	class Exporter
	{
		private BufferedOutputStream _bos;

		public Exporter( BufferedOutputStream bos )
		{
			_bos = bos;
		}

		public void close() throws IOException
		{
			if ( _bos != null )
			{
				_bos.close();
			}
		}

		public void startExport() throws IOException
		{			
			String stg = XML_FORMAT + GPX_START_TAG; 
			_bos.write( stg.getBytes() );
			
		}

		public void endExport() throws IOException
		{
			// </trkseg></trk>
			String out = TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
					+ LINE_SEP					
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET
					+ LINE_SEP
					;					
			
			_bos.write( out.getBytes() );
			
			// </gpx>
			String stg = GPX_END_TAG;
			
			_bos.write( stg.getBytes() );
		}
		/**
		 * 竊灘ｽ｢蠑�
		 * <export-database name=''>
		 * <table name=''>
		 * <row>
		 * <col name=''>value</col>
		 * <col name=''>value</col>
		 * ...
		 * </row>
		 * </table>
		 * </export-database>
		 * @param tableName
		 * @throws IOException
		 */
		private void exportLoc( Location loc ) throws IOException
		{
			// NOTICE:
			int iLapOfLocation = (int)loc.getBearing();
			if( iCurrentOutputLap != iLapOfLocation )
			{
//				if( iCurrentOutputLap != -1 )
//				{
//					_exporter.endLap();
//				}
				_exporter.startLap(iLapOfLocation);
				iCurrentOutputLap = iLapOfLocation;
			}

			_exporter.startLoc(loc);

			_exporter.endLoc();

		}

		public void startLap( int iLap ) throws IOException
		{
			/*
			<trk>
			<name>莉吩ｺｺ繝ｶ蟯ｳ</name>
			<number>1</number>
			<trkseg>
			*/			
			String stg = 
					TAG_LEFT_BLANCKET + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET
					+ LINE_SEP
					+ TAG_LEFT_BLANCKET + TAG_NAME_NAME + TAG_RIGHT_BLANCKET 
					+ LAP_TEXT + String.valueOf( iLap + 1 )
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_NAME + TAG_RIGHT_BLANCKET
					+ LINE_SEP					
					+ TAG_LEFT_BLANCKET + TAG_NAME_NUMBER + TAG_RIGHT_BLANCKET 
					+ String.valueOf( iLap + 1 )
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_NUMBER + TAG_RIGHT_BLANCKET
					+ LINE_SEP
					+ TAG_ACTIVITY_TYPE_FOR_MY_TRACKS
					+ TrackIconUtils.getActivityTypeNameFromCode(
							ResourceAccessor.getInstance().getActivity(), RunLoggerService.getActivityTypeCode())
					+ TAG_ACTIVITY_TYPE_FOR_MY_TRACKS_CLOSE
					+ LINE_SEP
					+ TAG_LEFT_BLANCKET + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
					+ LINE_SEP
					;
					
			_bos.write( stg.getBytes() );
		}

//		public void endLap() throws IOException
//		{
//			// </trkseg></trk>
//			String out = TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
//					+ LINE_SEP					
//					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET
//					+ LINE_SEP
//					;					
//			
//			_bos.write( out.getBytes() );
//		}

		public void startLoc(Location loc) throws IOException
		{
			//<trkpt lat="36.415416275733136" lon="139.4245576551685"><ele>253</ele>
			// <time>2010-09-18T23:08:00Z</time></trkpt>
			StringBuilder builder = new StringBuilder();
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_TRK_POINT );
			builder.append( String.format( ATTR_LAT_AND_LONGITUDE,
					loc.getLatitude(), loc.getLongitude() ) );
			builder.append( TAG_RIGHT_BLANCKET );			
			builder.append( LINE_SEP );
			
			// elevation
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_ELEVATION );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( loc.getAltitude() );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_ELEVATION );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( LINE_SEP );			

			// speed
			// is not standard?
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_SPEED );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( loc.getSpeed() );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_SPEED );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( LINE_SEP );
			
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_TIME );
			builder.append( TAG_RIGHT_BLANCKET );
			Date date = new Date(loc.getTime());
			// TODO: リソースに移す
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			String strTmp = dateFormatGmt.format(date);
			builder.append( strTmp );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_TIME );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( LINE_SEP );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_TRK_POINT );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( LINE_SEP );
			
			_bos.write( builder.toString().getBytes() );
		}

		public void endLoc() throws IOException
		{			
			//_bos.write( END_ROW.getBytes() );
			// NOTICE:いちいちflushする？大丈夫かな？
			_bos.flush();
		}
	}
	

}
