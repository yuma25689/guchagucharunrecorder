package app.guchagucharr.service;
import android.net.Uri;
import android.provider.BaseColumns;

public class RunHistoryTableContract implements BaseColumns {
	 
	public static final int HISTORY_TABLE_ID = 1;
	public static final int HISTORY_LAP_TABLE_ID = 2;
	public static final int HISTORY_TRANSACTION_ID = 3;
	public static final int HISTORY_COMMIT_ID = 4;
	public static final int HISTORY_ROLLBACK_ID = 5;
	public static final String HISTORY_TABLE_NAME = "tbl_history";
	public static final String HISTORY_LAP_TABLE_NAME = "tbl_history_lap";
	public static final String HISTORY_TRANSACTION = "Transaction";
	public static final String HISTORY_COMMIT = "Commit";
	public static final String HISTORY_ENDTRANSACTION = "EndTransaction";
    public static final String AUTHORITY = "guchagucharr.runhistoryprovider";
    public static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY );
 
//    private static final String PROVIDER_SPECIFIC_NAME = AUTHORITY;
//    private static final String PROVIDER_SPECIFIC_TYPE = PATH;
//    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd." + PROVIDER_SPECIFIC_NAME + "." + PROVIDER_SPECIFIC_TYPE;
//    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd." + PROVIDER_SPECIFIC_NAME + "." + PROVIDER_SPECIFIC_TYPE;
 
    public static final String START_DATETIME = "START_DATETIME";
    public static final String INSERT_DATETIME = "INSERT_DATETIME";
    public static final String NAME = "NAME";
    public static final String LAP_COUNT = "LAP_COUNT";
    public static final String PLACE_ID = "PLACE_ID";
    public static final String PARENT_ID = "PARENT_ID";
	public static final String LAP_INDEX = "LAP_INDEX";
    public static final String LAP_DISTANCE = "LAP_DISTANCE";
    public static final String LAP_TIME = "LAP_TIME";
    public static final String LAP_SPEED = "LAP_SPEED";
    public static final String LAP_FIXED_DISTANCE = "LAP_FIXED_DISTANCE";
    public static final String LAP_FIXED_TIME = "LAP_FIXED_TIME";
    public static final String LAP_FIXED_SPEED = "LAP_FIXED_SPEED";
    public static final String GPX_FILE_PATH = "GPX_FILE_PATH";
    
}
