package app.guchagucharr.service;


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
        
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
        	
            db.execSQL("CREATE TABLE " + RunHistoryTableContract.HISTORY_TABLE_NAME 
            		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
            		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
            		+ RunHistoryTableContract.NAME + " TEXT,"
            		+ RunHistoryTableContract.LAP_COUNT + " INTEGER,"
            		+ RunHistoryTableContract.PLACE_ID + " INTEGER,"
                    + RunHistoryTableContract.GPX_FILE_PATH + " TEXT"
                    + ");");
            db.execSQL("CREATE TABLE " + RunHistoryTableContract.HISTORY_LAP_TABLE_NAME 
            		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            		+ RunHistoryTableContract.INSERT_DATETIME + " INTEGER,"
            		+ RunHistoryTableContract.PARENT_ID + " INTEGER,"
            		+ RunHistoryTableContract.LAP_INDEX + " INTEGER,"
                    + RunHistoryTableContract.LAP_DISTANCE + " REAL,"
                    + RunHistoryTableContract.LAP_TIME + " INTEGER,"
                    + RunHistoryTableContract.LAP_SPEED + " REAL,"
                    + RunHistoryTableContract.LAP_FIXED_DISTANCE + " REAL,"
                    + RunHistoryTableContract.LAP_FIXED_TIME + " INTEGER,"
                    + RunHistoryTableContract.LAP_FIXED_SPEED + " REAL"
            		+ ");");
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	// TODO: �f�[�^�̈ڍs
            db.execSQL("DROP TABLE IF EXISTS " + RunHistoryTableContract.HISTORY_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + RunHistoryTableContract.HISTORY_LAP_TABLE_NAME);
            onCreate(db);
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
