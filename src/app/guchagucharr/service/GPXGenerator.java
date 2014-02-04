package app.guchagucharr.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * location�ｽf�ｽ[�ｽ^�ｽ�ｽ�ｽ�ｽAGPX�ｽt�ｽ@�ｽC�ｽ�ｽ�ｽ�生撰ｿｽ�ｽ�ｽ�ｽ�ｽ
 * @author 25689
 *
 */
public class GPXGenerator {
	// public static final String EXPORT_FILE_DIR = "/sdcard/patiman/export";
	public static final String EXPORT_FILE_EXT = ".gpx";
	public static final String LAP_TEXT = "Lap";
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
	
	public static final String XML_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String GPX_START_TAG 
	= "<gpx version=\"1.1\" creator=\"GuchaGuchaRunRecorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">";
	public static final String GPX_END_TAG 
	= "</gpx>";
	
	public static final String TAG_LEFT_BLANCKET = "<";
	public static final String TAG_LEFT_BLANCKET_OF_CLOSE = "</";
	public static final String TAG_RIGHT_BLANCKET_OF_CLOSE = "/>";
	public static final String TAG_RIGHT_BLANCKET = ">";
	
	public static final String TAG_NAME_TRACK = "trk";
	public static final String TAG_NAME_NAME = "name";
	public static final String TAG_NAME_NUMBER = "number";
	public static final String TAG_NAME_TRK_SEGMENT = "trkseg";
	public static final String TAG_NAME_TRK_POINT = "trkpt";
	public static final String TAG_NAME_ELEVATION = "ele";
	public static final String TAG_NAME_TIME = "time";
	public static final String TAG_NAME_SPEED = "speed";
	
	public static final String ATTR_NAME_LATITUDE = "lat=";
	public static final String ATTR_NAME_LONGITUDE = "lon=";

	public static final String ATTR_LAT_AND_LONGITUDE = " lat=\"%15f\" lon=\"%15f\" ";
	
	
	public static final int NG_ERROR_UNKNOWN = -1;
	public static final int RETURN_OK = 0;
	public static final int NG_DIR_CREATE = 1;
	
	int iCurrentOutputLap = -1;
	Vector<Location> vData = null;
	// SparseArray<LapData> lapData = null;
	Handler handler = null;

	public GPXGenerator(Vector<Location> vData_, Handler hdr_ )//SparseArray<LapData> lapData_, Handler hdr_)
	{
		// 縺九↑繧翫ョ繧ｫ縺�ョ繝ｼ繧ｿ縺�縺後�√す繝｣繝ｭ繝ｼ繧ｳ繝斐�縺ｫ縺ｪ縺｣縺ｦ縺上ｌ繧九□繧阪≧縺九�繝ｻ繝ｻ
		vData = vData_;
		// lapData = lapData_;
		handler = hdr_;
	}
	
	
	private int getAllRecordCount()
	{		
		if( vData != null )
		{
			return vData.size();
		}
		return 0;
	}
	
	/**
	 * create GPX file
	 * @param vData
	 * @param lapData
	 * @param dir
	 * @param file
	 * @return
	 */
	public int createGPXFileFromLocations(
			Activity activity,
			String dir, String file)
	{
		
		try
		{
			File objDir = new File( dir );
			if( false == objDir.exists() )
			{
				if( false == objDir.mkdirs() )
				{
					return NG_DIR_CREATE;
				}
			}
			// create a file on the SDcard to export the
			// database contents to
			File gpxFile = new File( dir + "/" + file );
            gpxFile.createNewFile();

            FileOutputStream fOut =  new FileOutputStream(gpxFile);
            BufferedOutputStream bos = new BufferedOutputStream( fOut );

			_exporter = new Exporter( bos );
		
			// 繝励Ο繧ｰ繝ｬ繧ｹ繝舌�逕ｨ縺ｮ蜃ｦ逅�
			if( handler != null )
			{
				int iAllRecCnt = getAllRecordCount();
				Bundle bdl = new Bundle();
				bdl.putInt(
					FileOutputProcessor.PROGRESS_VAL_KEY,
					iAllRecCnt
				);
				Message _msg = new Message();
				_msg.setData( bdl );
				_msg.what = FileOutputProcessor.PROGRESS_MAX_MSG_ID;
				handler.sendMessage( _msg );
			}
			_exporter.startExport();
	        for( Location loc:vData )
	        {
	        	_exporter.exportLoc( loc );
				if( handler != null )
				{
					Message _msg = new Message();
					_msg.what = FileOutputProcessor.PROGRESS_VAL_INCL_MSG_ID;
					handler.sendMessage( _msg );
					//iProgressCnt++;
				}	        	
			}			
	        _exporter.endExport();
			_exporter.close();
		}
		catch ( Exception e)
		{
			return NG_ERROR_UNKNOWN;
		}					
		return RETURN_OK;
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
			// ...
			String stg = XML_FORMAT + GPX_START_TAG; 
			_bos.write( stg.getBytes() );
		}

		public void endExport() throws IOException
		{
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
			// NOTICE: 縺薙�繧｢繝励Μ縺ｧ縺ｯ縲∵悴菴ｿ逕ｨ縺ｮ鬆�岼Bearing繧鱈ap縺ｫ菴ｿ縺｣縺ｦ縺�ｋ
			int iLapOfLocation = (int)loc.getBearing();
			if( iCurrentOutputLap != iLapOfLocation )
			{
				if( iCurrentOutputLap != -1 )
				{
					_exporter.endLap();
				}
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
					+ TAG_LEFT_BLANCKET + TAG_NAME_NAME + TAG_RIGHT_BLANCKET 
					+ LAP_TEXT + String.valueOf( iLap + 1 )
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_NAME + TAG_RIGHT_BLANCKET
					+ TAG_LEFT_BLANCKET + TAG_NAME_NUMBER + TAG_RIGHT_BLANCKET 
					+ String.valueOf( iLap + 1 )
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_NUMBER + TAG_RIGHT_BLANCKET
					+ TAG_LEFT_BLANCKET + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET;
					
			_bos.write( stg.getBytes() );
		}

		public void endLap() throws IOException
		{
			// </trkseg></trk>
			String out = TAG_LEFT_BLANCKET + TAG_NAME_TRK_SEGMENT + TAG_RIGHT_BLANCKET
					+ TAG_LEFT_BLANCKET_OF_CLOSE + TAG_NAME_TRACK + TAG_RIGHT_BLANCKET;					
			
			_bos.write( out.getBytes() );
		}

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
			
			// elevation
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_ELEVATION );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( loc.getAltitude() );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_ELEVATION );
			builder.append( TAG_RIGHT_BLANCKET );

			// speed
			// is not standard?
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_SPEED );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( loc.getSpeed() );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_SPEED );
			builder.append( TAG_RIGHT_BLANCKET );
			
			builder.append( TAG_LEFT_BLANCKET );
			builder.append( TAG_NAME_TIME );
			builder.append( TAG_LEFT_BLANCKET );
			Date date = new Date(loc.getTime());
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			String strTmp = dateFormatGmt.format(date);
			builder.append( strTmp );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_TIME );
			builder.append( TAG_RIGHT_BLANCKET );
			builder.append( TAG_LEFT_BLANCKET_OF_CLOSE );
			builder.append( TAG_NAME_TRK_POINT );
			builder.append( TAG_RIGHT_BLANCKET );
			
			_bos.write( builder.toString().getBytes() );
		}

		public void endLoc() throws IOException
		{
			//_bos.write( END_ROW.getBytes() );
		}
	}
	

}
