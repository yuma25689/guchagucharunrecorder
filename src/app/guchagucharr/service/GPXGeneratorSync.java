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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.content.Context;
//import android.app.Activity;
import android.location.Location;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;
import app.guchagucharr.guchagucharunrecorder.util.XmlUtil;
import app.guchagucharr.guchagucharunrecorder.R;
import app.guchagucharr.guchagucharunrecorder.ResourceAccessor;
import app.guchagucharr.guchagucharunrecorder.util.TrackIconUtils;

/**
 * @author 25689
 *
 */
public class GPXGeneratorSync {
	// ロック用オブジェクト
	public static Object mFileWriteLock= new Object();
	// public static final String EXPORT_FILE_DIR = "/sdcard/patiman/export";
	// ファイルの拡張子
	public static final String EXPORT_FILE_EXT = ".gpx";
	// 一時ファイル名
	public static final String GPX_TEMP_FILE_NAME = "gpx.tmp";

	// エクスポーター
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
	
	// ===== ファイルの内容
	// ラップ
	public static final String LAP_TEXT = "Lap";
	// 改行コード
	public static final String LINE_SEP = System.getProperty("line.separator");
	// ヘッダ
	// xml宣言
	public static final String XML_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + LINE_SEP;
	// gpxタグ
	public static final String GPX_START_TAG 
	= "<gpx version=\"1.1\"" + LINE_SEP
	+ "creator=\"GuchaGuchaRunRecorder\"" + LINE_SEP
	+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + LINE_SEP
	+ "xmlns=\"http://www.topografix.com/GPX/1/0\"" + LINE_SEP
	+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">"
	+ LINE_SEP;
	// gpx終了タグ
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
	
	// エラーコード
	public static final int NG_ERROR_UNKNOWN = -1;
	public static final int RETURN_OK = 0;

	// SparseArray<LapData> lapData = null;
	File gpxFile = null;
    FileOutputStream fOut =  null;
    //BufferedOutputStream bos = null;
	int iCurrentOutputLap = -1;

    public GPXGeneratorSync()
	{
    	// コンストラクタ
    	// 無意味だが、一応クリア
    	clearCurrentBuf();
		// lapData = lapData_;
	}

    public void clearCurrentBuf()
    {
    	// Fileクラスを解放
    	gpxFile = null;
    	// 出力のストリームを解放？
        fOut =  null;
    }

    /**
     * GPXファイルのストリームを開く
     * @param file
     * @param append
     * @return
     */
    public BufferedOutputStream openGPXFileStream(File file,boolean append)
    {
    	BufferedOutputStream bos = null;
        //FileOutputStream fOut;
		try {
			// 引数で指定されたファイルを、引数で指定されたappendフラグで開く
			FileOutputStream fOut = new FileOutputStream(file,append);
	        bos = new BufferedOutputStream( fOut );    	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 開いたストリームを返す
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

            createGPXFileExporter( bos );
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
			LogWrapper.e("RecoveryFileInput failed","");
			return -1;
		}        	
		catch ( Exception e)
		{
			e.printStackTrace();
			LogWrapper.e("RecoveryFileInput failed","");
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
	 * start recovering GPX file
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
			// startCreateGPXFileと違って、続きから
            BufferedOutputStream bos = openGPXFileStream(gpxFile,true);

            // 少なくとも最初のラップ開始の表記はファイルに書かれているものとするので、
            // 最初のラップの表記が書かれた直後から書けるように、iCurrentOutputLapを-1から0に更新しておく
            iCurrentOutputLap = 0;
            createGPXFileExporter( bos );
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
			LogWrapper.e("addLocationToCurrentGPXFile",e.getMessage());
		}
    }
    public void createGPXFileExporter(BufferedOutputStream bos)
    {
		_exporter = new Exporter(bos);    	
    }

    public void endCreateGPXFile()
    {
        try {
			_exporter.endExport();
			_exporter.close();
		} catch (IOException e) {
			e.printStackTrace();
			LogWrapper.e("addLocationToCurrentGPXFile",e.getMessage());
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
			synchronized( mFileWriteLock )
			{
				_bos.write( stg.getBytes() );
			}
		}

		public void endExport() throws IOException
		{
			// </trkseg></trk>
			String out = TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
					+ LINE_SEP					
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET
					+ LINE_SEP
					;					
			
			synchronized( mFileWriteLock )
			{
				_bos.write( out.getBytes() );
			}
			// </gpx>
			String stg = GPX_END_TAG;
			
			synchronized( mFileWriteLock )
			{
				_bos.write( stg.getBytes() );
			}
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
			// NOTICE: Bearingを無理やりラップ数に利用中
			int iLapOfLocation = (int)loc.getBearing();
			if( iCurrentOutputLap != iLapOfLocation )
			{
				_exporter.startLap(iLapOfLocation);
				iCurrentOutputLap = iLapOfLocation;
			}
			_exporter.startLoc(loc);
			_exporter.endLoc();
		}

		public void startLap( int iLap ) throws IOException
		{
			Date date = new Date();
			// TODO: リソースに移す
			SimpleDateFormat dateFormatForName = new SimpleDateFormat(
					ResourceAccessor.getInstance().getActivity().getString(
							R.string.datetime_display_format_full));
			String strName = dateFormatForName.format(date);
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
					+ strName
					//+ LAP_TEXT + String.valueOf( iLap + 1 )
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
					
			synchronized( mFileWriteLock )
			{
				_bos.write( stg.getBytes() );
			}
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
			
			synchronized( mFileWriteLock )
			{
				_bos.write( builder.toString().getBytes() );
			}
		}

		public void endLoc() throws IOException
		{
			synchronized( mFileWriteLock )
			{
				//_bos.write( END_ROW.getBytes() );
				// NOTICE:いちいちflushする？大丈夫かな？
				_bos.flush();
			}
		}
	}
	
	public static boolean updateActivityType(Context ctx, String targetGpx, int activityTypeCode)
	{
		synchronized( mFileWriteLock )
		{		
			boolean bRet = true;
			File fileObject = new File(targetGpx);
			DocumentBuilder docBuilder;
			try {
				docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = docBuilder.parse(fileObject);
				document.normalize();
	
				Element doc = document.getDocumentElement();
				// ノード名がtype
				NodeList docChildren = doc.getElementsByTagName("type");
				for( int i=0; i<docChildren.getLength();i++ )
				{
					Node n = docChildren.item(i);
					
					if( n.getFirstChild() != null 
					&&( n.getFirstChild().getNodeType() == Node.TEXT_NODE
					|| n.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE ) )
					{
						// 子のテキストOR CDATAのノードがあれば
						// typeを文字列に変換
						String sActivityTypeName = 
								TrackIconUtils.getActivityTypeNameFromCode(ctx,activityTypeCode);
						// そのCDATAセクションノードを作成
						Node newNode = document.createCDATASection(sActivityTypeName);
						// 現在のノードと入れ替え
						n.replaceChild(newNode, n.getFirstChild());
					}
				}
				
				// 上書き
				XmlUtil.writeXML(fileObject,document);								
				
			} catch (ParserConfigurationException e) {
				LogWrapper.e("ParserConfigurationException",e.getMessage());
				bRet = false;
			} catch (SAXException e) {
				LogWrapper.e("SAXException",e.getMessage());
				bRet = false;
			} catch (IOException e) {
				LogWrapper.e("IOException",e.getMessage());
				bRet = false;
			}
			return bRet;
		}
	}
	

}
