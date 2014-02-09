package app.guchagucharr.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class RunHistoryContentProvider extends ContentProvider {

	public static String DB_NAME = "GuchaGuchaRunRecorder.db";
	//public static String CONTENT_PROVIDER_URI = "content://app.guchagucharr.service.RunHistoryContentProvider";
    //public static final String PATH = "persons";
	
    // UriMatcher�̒�`
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
    	private static int DB_VERSION = 2;
    	
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }
 
        public String createTableCreateSQL( String tblName )
        {
        	if( RunHistoryTableContract.HISTORY_TABLE_NAME.equals(tblName))
        	{
        		return
        		"CREATE TABLE IF NOT EXISTS " + RunHistoryTableContract.HISTORY_TABLE_NAME 
        		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
        		+ RunHistoryTableContract.NAME + " TEXT,"
        		+ RunHistoryTableContract.LAP_COUNT + " INTEGER,"
        		+ RunHistoryTableContract.PLACE_ID + " INTEGER,"
                + RunHistoryTableContract.GPX_FILE_PATH + " TEXT"
                + ");"
                ;
        	}
        	else if( RunHistoryTableContract.HISTORY_LAP_TABLE_NAME.equals(tblName))
        	{
                return
                		"CREATE TABLE IF NOT EXISTS " + RunHistoryTableContract.HISTORY_LAP_TABLE_NAME 
                		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
                		+ RunHistoryTableContract.PARENT_ID + " INTEGER,"
                		+ RunHistoryTableContract.NAME + " TEXT,"
                		+ RunHistoryTableContract.LAP_INDEX + " INTEGER,"
                        + RunHistoryTableContract.LAP_DISTANCE + " REAL,"
                        + RunHistoryTableContract.LAP_TIME + " INTEGER,"
                        + RunHistoryTableContract.LAP_SPEED + " REAL,"
                        + RunHistoryTableContract.LAP_FIXED_DISTANCE + " REAL,"
                        + RunHistoryTableContract.LAP_FIXED_TIME + " INTEGER,"
                        + RunHistoryTableContract.LAP_FIXED_SPEED + " REAL"
                		+ ");"
                   ;
        		
        	}
        	return null;
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL( createTableCreateSQL(RunHistoryTableContract.HISTORY_TABLE_NAME) );
            db.execSQL( createTableCreateSQL(RunHistoryTableContract.HISTORY_LAP_TABLE_NAME ) );
        }
 
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        	// TODO: �f�[�^�̈ڍs
//            db.execSQL("DROP TABLE IF EXISTS " + RunHistoryTableContract.HISTORY_TABLE_NAME);
//            db.execSQL("DROP TABLE IF EXISTS " + RunHistoryTableContract.HISTORY_LAP_TABLE_NAME);
//            onCreate(db);
//        }
        
        public static String TMP_PREFIX = "_TEMP";
        public static String NEW_PREFIX = "NEW_";
    	/**
    	 * テーブルのヴァージョンが上がったときに実行される
    	 * 旧から新へデータの引き継ぎを行う
    	 *
    	 * @param db
    	 * @param oldVersion
    	 * @param newVersion
    	 * @return なし
    	 */	
    	@Override
    	public void onUpgrade(SQLiteDatabase db,
    			int oldVersion, 
    			int newVersion) {
    		
    		db.beginTransaction();
    		try {
    			String tblInf[] = {
    					RunHistoryTableContract.HISTORY_TABLE_NAME,
    					RunHistoryTableContract.HISTORY_LAP_TABLE_NAME 
    			};
    			for( int i=0; i < tblInf.length; i++ )
    			{
    				/*
    				// 元のテーブルをリネームして退避
    				db.execSQL("ALTER TABLE " + tblInf[i].getTblName() + " RENAME TO " + TMP_PREFIX + tblInf[i].getTblName());
    				// 元のテーブルのカラムを取得
    				List<String> lstOldColumnList = getColumns( db, TMP_PREFIX + tblInf[i].getTblName() );
    				// 新しいテーブルを作成
    				createTable( db, tblInf[i].getTblName(), tblInf[i].getColumnsCreateText() );
    				// 新しいテーブルのカラムを取得
    				List<String> lstNewColumnList = getColumns(db, tblInf[i].getTblName());
    		*/
    				// 元のテーブルのカラムを取得
    				List<String> lstOldColumnList = getColumns( db, tblInf[i] );
    				if( lstOldColumnList == null || lstOldColumnList.size() == 0 )
    				{
    					// 新規テーブルと思われる
    				}
    				else
    				{
    					// 元のテーブルをリネームして退避
    					db.execSQL("ALTER TABLE " + tblInf[i] + " RENAME TO " + TMP_PREFIX + tblInf[i]);
    				}
    								
    				// 新しいテーブルを作成
    				createTable( db, createTableCreateSQL( NEW_PREFIX + tblInf[i] ) );

    				// 新しいテーブルのカラムを取得
    				List<String> lstNewColumnList = getColumns(db, NEW_PREFIX + tblInf[i]);
    				
    				db.execSQL("ALTER TABLE " + NEW_PREFIX + tblInf[i] + " RENAME TO " + tblInf[i]);
    				
    				if( lstOldColumnList == null || lstOldColumnList.size() == 0 )
    					// 新規テーブルと思われる
    					continue;
    				
    				// 新旧で名称の変化しないカラムのみ抽出
    				lstOldColumnList.retainAll(lstNewColumnList);
    				
    				// 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)
    				String cols = join(lstOldColumnList, ",");
    				String cols2 = cols;
    				
    				db.execSQL(
    					String.format(
    						"INSERT INTO %s (%s) SELECT %s from " + TMP_PREFIX + "%s", 
    						tblInf[i],	cols, cols2, tblInf[i]
    					)
    				);
    				// 退避した旧テーブルを削除 
    				dropTable( db, TMP_PREFIX + tblInf[i] );
    			}
    			// コミット
    			db.setTransactionSuccessful();
    		} catch(Exception ex ){
    			ex.printStackTrace();
    		}finally {
    			// トランザクション終了
    			db.endTransaction();
    		}
    	}
    	private void dropTable(SQLiteDatabase db, String strTblName_ ) {
			db.execSQL(
					"drop table if exists " + strTblName_ 
					);
    	}
    	private void createTable(SQLiteDatabase db,
    			String strColumnsCreateText_
    			) {
    		if ( !strColumnsCreateText_.equals("")) {
    			try {
    				db.execSQL(
    						strColumnsCreateText_ //"(id text primary key,info text)");
    						);
    			} catch ( Exception e ) {
    				e.printStackTrace();				
    			}
    		}
    	}
    	
    	/**
    	 * 文字列を任意の区切り文字で連結する。
    	 *
    	 * @param list
    	 * 文字列のリスト
    	 * @param delim
    	 * 区切り文字
    	 * @return 連結後の文字列
    	 */
    	private static String join(List<String> list, String delim) {
    		final StringBuilder buf = new StringBuilder();
    		final int num = list.size();
    		for (int i = 0; i < num; i++) {
    			if (i != 0)
    				buf.append(delim);
    			buf.append((String) list.get(i));
    		}
    		return buf.toString();
    	}
    	
    	/**
    	 * 指定したテーブルのカラム名リストを取得する。
    	 *
    	 * @param db
    	 * @param tableName
    	 * @return カラム名のリスト
    	 */
    	private List<String> getColumns(
    			SQLiteDatabase db,
    			String tableName) {
    		List<String> ar = null;
    		Cursor c = null;
    		try {
    			c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
    			if (c != null) {
    				ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
    			}
    		} catch(Exception ex) {
    			if (c != null)
    				c.close();			
    		} finally {
    			if (c != null)
    				c.close();
    		}
    		return ar;
    	}

    }
 
    DatabaseHelper databaseHelper;
    
    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        mUriMatcher.addURI(RunHistoryTableContract.AUTHORITY, 
        		RunHistoryTableContract.HISTORY_TABLE_NAME, 
        		RunHistoryTableContract.HISTORY_TABLE_ID);
        mUriMatcher.addURI(RunHistoryTableContract.AUTHORITY, 
        		RunHistoryTableContract.HISTORY_LAP_TABLE_NAME, 
        		RunHistoryTableContract.HISTORY_LAP_TABLE_ID);        
        mUriMatcher.addURI(RunHistoryTableContract.AUTHORITY, 
        		RunHistoryTableContract.HISTORY_TRANSACTION, 
        		RunHistoryTableContract.HISTORY_TRANSACTION_ID);        
        mUriMatcher.addURI(RunHistoryTableContract.AUTHORITY, 
        		RunHistoryTableContract.HISTORY_COMMIT, 
        		RunHistoryTableContract.HISTORY_COMMIT_ID);        
        mUriMatcher.addURI(RunHistoryTableContract.AUTHORITY, 
        		RunHistoryTableContract.HISTORY_ENDTRANSACTION, 
        		RunHistoryTableContract.HISTORY_ROLLBACK_ID);        
        return true;
    }
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if( mUriMatcher.match(uri) == UriMatcher.NO_MATCH )
		{
			return -1;
		}
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
		if( mUriMatcher.match(uri) == RunHistoryTableContract.HISTORY_TABLE_ID )
		{
	        return db.delete(RunHistoryTableContract.HISTORY_TABLE_NAME, selection, null);
		}
		else if( mUriMatcher.match(uri) == RunHistoryTableContract.HISTORY_LAP_TABLE_ID )
		{
			return db.delete(RunHistoryTableContract.HISTORY_LAP_TABLE_NAME, selection, null);
		}
		return -1;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match =  mUriMatcher.match(uri);
		if( match == UriMatcher.NO_MATCH )
		{
			throw new SQLException("Failed to insert row into " + uri);
			//return null;
		}
		Uri returnUri = null;
		long lngRet = 0;
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch( match )
		{
			case RunHistoryTableContract.HISTORY_TABLE_ID: 
				lngRet = db.insert(RunHistoryTableContract.HISTORY_TABLE_NAME, null, values);
				returnUri = ContentUris.withAppendedId(uri,
						                    lngRet);				
				break;
			case RunHistoryTableContract.HISTORY_LAP_TABLE_ID:
				lngRet = db.insert(RunHistoryTableContract.HISTORY_LAP_TABLE_NAME, null, values);
				returnUri = ContentUris.withAppendedId(uri,
	                    lngRet);				
				break;
			case RunHistoryTableContract.HISTORY_TRANSACTION_ID:
				db.beginTransaction();
				break;
			case RunHistoryTableContract.HISTORY_COMMIT_ID:
				db.setTransactionSuccessful();
				// db.endTransaction();
				break;
			case RunHistoryTableContract.HISTORY_ROLLBACK_ID:
				db.endTransaction();
				break;
			default:
				throw new SQLException("Failed to insert row into " + uri);				
		}
		return returnUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if( mUriMatcher.match(uri) == UriMatcher.NO_MATCH )
		{
			return null;
		}
		String tbl_name = null;
		if( mUriMatcher.match(uri) == RunHistoryTableContract.HISTORY_TABLE_ID )
		{
			tbl_name = RunHistoryTableContract.HISTORY_TABLE_NAME;
		}
		else if( mUriMatcher.match(uri) == RunHistoryTableContract.HISTORY_LAP_TABLE_ID )
		{
			tbl_name = RunHistoryTableContract.HISTORY_LAP_TABLE_NAME;
		}
		
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(tbl_name);
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                            null, sortOrder);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
