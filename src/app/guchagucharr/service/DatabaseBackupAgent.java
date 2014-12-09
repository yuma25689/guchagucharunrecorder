package app.guchagucharr.service;

import java.io.File;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

/**
 * @author 25689
 *
 */
public class DatabaseBackupAgent extends BackupAgentHelper {
	static final String DATABASE_FILENAME = RunHistoryContentProvider.DB_NAME;
	static final String DATABASE_BACKUP_KEY = "key_database_backup";
	static final Object LOCK = new Object();
	static final String TAG = "DatabaseBackupAgent";
	@Override
	public void onCreate() {
		FileBackupHelper helper = new FileBackupHelper(this, DATABASE_FILENAME);
		addHelper(DATABASE_BACKUP_KEY, helper);
	}
	@Override
	public File getFilesDir() {
		File path = getDatabasePath(DATABASE_FILENAME);
		return path.getParentFile();
	}	
}
