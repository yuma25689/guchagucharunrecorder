package app.guchagucharr.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

/**
 * location�f�[�^����AGPX�t�@�C���𐶐�����
 * @author 25689
 *
 */
public class GPXGenerator {
	// public static final String EXPORT_FILE_DIR = "/sdcard/patiman/export";
	public static final String EXPORT_FILE_NAME = ".gpx";
	private static final String[][] ESC_CHRS = {
		{"&","&amp;"},
		{"'","&apos;"},
		{"<","&lt;"},
		{">","&gt;"},
		{"\"","&quot;"}
	};
	
	private Exporter _exporter = null;

	/*
trk><name>仙人ヶ岳</name>
<number>1</number>
<trkseg>
<trkpt lat="36.415416275733136" lon="139.4245576551685"><ele>253</ele><time>2010-09-18T23:08:00Z</time></trkpt>
<trkpt lat="36.415147339915855" lon="139.42455766633571"><ele>253</ele><time>2010-09-18T23:08:32Z</time></trkpt>
・・・
・・・
・・・
</trkseg></trk> 
	 */
	
	public static final String XML_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String GPX_START_TAG 
	= "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">";
	public static final String GPX_END_TAG 
	= "</gpx>";
	
	public static final String TAG_LEFT_BLANCKET = "<";
	public static final String TAG_LEFT_BLANCKET_OF_CLOSE = "<";
	public static final String TAG_RIGHT_BLANCKET = ">";
	
	public static final String TAG_NAME_TRACK = "trk";
	public static final String TAG_NAME_NAME = "name";
	public static final String TAG_NAME_NUMBER = "number";
	public static final String TAG_NAME_TRK_SEGMENT = "trkseg";
	public static final String TAG_NAME_TRK_POINT = "trkpt";
	public static final String TAG_NAME_ELEVATION = "ele";
	public static final String TAG_NAME_TIME = "time";
	public static final String TAG_NAME_SPEED = "speed";
	
	public static final String ATTR_NAME_LATITUDE = "lat";
	public static final String ATTR_NAME_LONGITUDE = "lon";
	
	
	public static final int NG_ERROR_UNKNOWN = -1;
	public static final int RETURN_OK = 0;
	public static final int NG_DIR_CREATE = 1;
	
	Vector<Location> vData = null;
	SparseArray<LapData> lapData = null;
	Handler handler = null;

	public GPXGenerator(Vector<Location> vData_, SparseArray<LapData> lapData_, Handler hdr_)
	{
		// かなりデカいデータだが、シャローコピーになってくれるだろうか・・・
		vData = vData_;
		lapData = lapData_;
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
		
			// プログレスバー用の処理
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
	        for( String tableName:tblNames )
	        {
		        exportTable( tableName );
			}			
	        _exporter.endDbExport();
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
			String stg = XML_FORMAT + GPX_START_TAG;
			_bos.write( stg.getBytes() );
		}

		public void endExport() throws IOException
		{
			_bos.write( GPX_END_TAG.getBytes() );
		}
		/**
		 * ↓形式
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
		private void exportTable( String tableName ) throws IOException
		{
	        _exporter.startTable(tableName);

			// get everything from the table
			String sql = "select * from " + tableName;
			Cursor cur = _db.rawQuery( sql, new String[0] );
			int numcols = cur.getColumnCount();
			
			cur.moveToFirst();

			// move through the table, creating rows
			// and adding each column with name and value
			// to the row
			while( cur.getPosition() < cur.getCount() )
			{
				_exporter.startRow();
				String name;
				String val;

				for( int idx = 0; idx < numcols; idx++ )
				{
					name = cur.getColumnName(idx);
					val = cur.getString( idx );
					
					_exporter.addColumn( name, val );
				}

				_exporter.endRow();
				cur.moveToNext();
				if( _hdr != null )
				{
					_msg = new Message();
					_msg.what = PatiLogger.PROGRESS_VAL_INCL_MSG_ID;
					_hdr.sendMessage( _msg );
					//iProgressCnt++;
				}
			}

			cur.close();

			_exporter.endTable();
		}

		public void startTable( String tableName ) throws IOException
		{
			String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
			_bos.write( stg.getBytes() );
		}

		public void endTable() throws IOException
		{
			_bos.write( END_TABLE.getBytes() );
		}

		public void startRow() throws IOException
		{
			_bos.write( START_ROW.getBytes() );
		}

		public void endRow() throws IOException
		{
			_bos.write( END_ROW.getBytes() );
		}

		public void addColumn( String name, String val ) throws IOException
		{
			if( val != null )
			{
				//エスケープ文字のチェックを行う。あれば置換する。
				for( int i=0; i < ESC_CHRS.length; i++ )
				{
					if( val.indexOf( ESC_CHRS[i][0] ) != -1 )
					{
						val = val.replace( ESC_CHRS[i][0], ESC_CHRS[i][1] );
					}
				}
			}
			String stg = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
			_bos.write( stg.getBytes() );
		}
	}
	

}
