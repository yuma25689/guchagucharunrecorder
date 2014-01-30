package app.guchagucharr.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class RunHistoryContentProvider extends ContentProvider {

	public static String DB_NAME = "GuchaGuchaRunRecorder.db";
	public static String HISTORY_TABLE_NAME = "tbl_history";
	
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + HISTORY_TABLE_NAME 
            		+ " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," 
            		+ "col2 TEXT,"
                    + "col3 TEXT"
            		+ ");");
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
            onCreate(db);
        }
    }
 
    DatabaseHelper databaseHelper;
    
    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.delete(HISTORY_TABLE_NAME, selection, null);
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.insert(HISTORY_TABLE_NAME, null, values);
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(HISTORY_TABLE_NAME);
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                            null, null);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
