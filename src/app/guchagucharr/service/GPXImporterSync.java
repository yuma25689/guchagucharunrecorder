package app.guchagucharr.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.location.Location;
import android.util.Log;

/**
 * @author 25689
 *
 */
public class GPXImporterSync {
	public static final String LOCATION_PROVIDER = "GuchaGuchaRunRecorder";
	
//	private static final String[][] ESC_CHRS = {
//		{"&","&amp;"},
//		{"'","&apos;"},
//		{"<","&lt;"},
//		{">","&gt;"},
//		{"\"","&quot;"}
//	};
	// private Importer _importer = null;
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

	public static final String TAG_NAME_TRACK = "trk";
	public static final String TAG_NAME_NAME = "name";
	public static final String TAG_NAME_NUMBER = "number";
	public static final String TAG_NAME_TRK_SEGMENT = "trkseg";
	public static final String TAG_NAME_TRK_POINT = "trkpt";
	public static final String TAG_NAME_ELEVATION = "ele";
	public static final String TAG_NAME_TIME = "time";
	public static final String TAG_NAME_SPEED = "speed";

	public static final int ID_TAG_TRACK = 1;
	public static final int ID_TAG_NAME = 2;
	public static final int ID_TAG_NUMBER = 3;
	public static final int ID_TAG_SEGMENT = 4;
	public static final int ID_TAG_TRK_POINT = 5;
	public static final int ID_TAG_ELEVATION = 6;
	public static final int ID_TAG_TIME = 7;
	public static final int ID_TAG_SPEED = 8;
	
	public static final String ATTR_NAME_LATITUDE = "lat";
	public static final String ATTR_NAME_LONGITUDE = "lon";

	public static final String ATTR_LAT_AND_LONGITUDE = " lat=\"%.15f\" lon=\"%.15f\"";
	
	
	public static final int NG_ERROR_UNKNOWN = -1;
	public static final int RETURN_OK = 0;
	
    //BufferedOutputStream bos = null;
	int iCurrentOutputLap = 1;
	int iLastOutputLap = 1;
	private String _path;
	private RunningLogStocker logStocker;
	
	// SAX API
	public GPXImporterSync( String strFilePath, RunningLogStocker stocker )
	{
		_path = strFilePath;
		logStocker = stocker;
	}
	
	public Boolean importData()
	{
		Boolean bRet = true;
		try {
			// ファイルからURIを生成
			// TODO:確認
			URI uri = new File( _path ).toURI();
			// new File( _path ).toURL().toExternalForm();
			SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser(); 				
            XMLReader xmlReader = sp.getXMLReader();
            xmlReader.setContentHandler( new ImportSaxHandler() );
			xmlReader.parse( uri.toURL().toExternalForm() );
		} catch (SAXException e) {
			Log.w("sax_error =>" + _path, e.getMessage());
			// strErr = e.getMessage();
			bRet = false;
		} catch (IOException e) {
			Log.w("sax_parse_error =>" + _path, e.getMessage());
			//strErr = e.getMessage();
			bRet = false;
		} catch (ParserConfigurationException e) {
			Log.w("sax_parser_creation_error", e.getMessage());
			//strErr = e.getMessage();
			bRet = false;
		}
		return bRet;
	}	

	private class ImportSaxHandler extends DefaultHandler {
		
		//String strCurrentTbl;
		// それで本当に整合性が取れるか分からないが、とりあえずエレメントの名前はスタックしない
		// (=開始と終了を合わせるチェックをしない)
		// Stack<String> stackName;

		//LapData lapData;
		Location currentData;
		Location prevData;
		StringBuffer sbCurrentVal;
		//String strCurrentName;
		//String strErr;
		int lastElmTagId = ID_NONE;
		// int lastElmNameId = ID_TAG_UNKNOWN;		
		// private static final int ID_TAG_UNKNOWN = -1;
		private static final int ID_NONE = 0;
		ImportSaxHandler()
		{}

		private int chkLocalName( String strLocal )
		{				
			if( strLocal.equals( TAG_NAME_TRACK ) )
			{
				return ID_TAG_TRACK;
			} 
			else if( strLocal.equals( TAG_NAME_TRK_POINT ) )
			{
				return ID_TAG_TRK_POINT;
			}
			else if( strLocal.equals( TAG_NAME_NAME ) )
			{
				return ID_TAG_NAME;
			}
			else if( strLocal.equals( TAG_NAME_NUMBER ) )
			{
				return ID_TAG_NUMBER;
			}
			else if( strLocal.equals( TAG_NAME_TRK_SEGMENT ) )
			{
				return ID_TAG_SEGMENT;
			}
			else if( strLocal.equals( TAG_NAME_ELEVATION ) )
			{
				return ID_TAG_ELEVATION;
			}
			else if( strLocal.equals( TAG_NAME_TIME ) )
			{
				return ID_TAG_TIME;
			}
			else if( strLocal.equals( TAG_NAME_SPEED ) )
			{
				return ID_TAG_SPEED;
			}
			return ID_NONE;
		}	
		
		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			sbCurrentVal = new StringBuffer();
			// stackName = new Stack<String>();
		}
		@Override
		public void startElement(
				String uri,
				String localName,
				String qName,
				Attributes attributes) throws SAXException
		{
			// 名前の判定
			lastElmTagId = chkLocalName( localName ); 
			switch( lastElmTagId )
			{
			case ID_TAG_TRK_POINT:
				// 現在位置データを初期化
				// TODO:プロバイダを、なるべく元と同じものに
				currentData = new Location(LOCATION_PROVIDER);
				break;
			}
			// strCurrentName = "";
			// 属性の判定
			for(int i=0; i < attributes.getLength(); i++){
				// 属性を持つ場合、それは緯度経度
				// 現在のロケーションに格納する
		        if( ATTR_NAME_LATITUDE == attributes.getLocalName(i) )
		        {
		        	currentData.setLatitude(Double.parseDouble(attributes.getValue(i)));
		        }
		        else if( ATTR_NAME_LONGITUDE == attributes.getLocalName(i) )
		        {
		        	currentData.setLongitude(Double.parseDouble(attributes.getValue(i)));			        	
		        }
		    }
			super.startElement(uri, localName, qName, attributes);
		}
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException
		{
			// ある項目の値
			sbCurrentVal.append( ch, start, length );
			super.characters(ch, start, length);
		}	
		@Override
		public void endElement(String uri,
				String localName,
				String qName
		) throws SAXException
		{
			// エレメントの終わり
			// int iRet = 0;
			switch( chkLocalName( localName ) )
			{
			case ID_TAG_TRK_POINT:
				// 1つのロケーションが終了
				// ロケーションを格納
				// 格納前に、ラップが変わっているかどうか判別し、
				// ラップが変わっていたら次のラップの設定に
				currentData.setBearing(iCurrentOutputLap - 1);
				// 中でやる
//				if( iLastOutputLap < iCurrentOutputLap)
//				{
//					logStocker.nextLapNoFileProcess(iCurrentOutputLap-1,prevData.getTime());
//				}
				logStocker.putLocationLogNotAddFile(currentData,_path);
				prevData = new Location( currentData );
				iLastOutputLap = iCurrentOutputLap;
				break;
			case ID_TAG_NUMBER:
				// ファイルに記述されたラップ番号の取得
				int iLap = Integer.parseInt(sbCurrentVal.toString().trim());
				if( iLap != 1 && iCurrentOutputLap < iLap )
				{
					iCurrentOutputLap = iLap;
				}
				break;
			case ID_TAG_ELEVATION:
				// ファイルに書かれた高度
				// ele
				currentData.setAltitude(Double.parseDouble(sbCurrentVal.toString()));
				break;
			case ID_TAG_SPEED:
				// speed
				currentData.setSpeed(Float.parseFloat(sbCurrentVal.toString()));
				break;
			case ID_TAG_TIME:
				// グリニッジ標準を元に戻す
				SimpleDateFormat dateFormatGmt 
				= //new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
				try {
					// TとZは変換できないようなので、変換前にTとZはスペースに戻す
					String strGmt = sbCurrentVal.toString();
					strGmt = strGmt.replace('T', ' ');
					strGmt = strGmt.replace('Z', ' ');
					strGmt = strGmt.trim();
					Date date = dateFormatGmt.parse(strGmt);
					currentData.setTime(date.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e("cant parse gmt time from xml","recovery");
				}				
				break;
			case ID_NONE:
				break;
			}
			sbCurrentVal.setLength(0);
			super.endElement(uri, localName, qName);
		}	
		@Override
		public void endDocument() throws SAXException
		{
			super.endDocument();
		}
	}		
}
