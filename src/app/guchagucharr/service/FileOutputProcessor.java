package app.guchagucharr.service;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import app.guchagucharr.guchagucharunrecorder.R;

public class FileOutputProcessor implements Runnable {
	public static final int NG_DIR_CREATE = 1;
	
	private static ProgressDialog progressDialog = null;
	FileOutputThread thread = null;
	Activity mActivity;
	String dateTime;
	RunningLogStocker stocker;
	String gpxFilePath = null;
	public String getGpxFilePath()
	{
		return gpxFilePath;
	}

	public void outputGPX(Activity activity,
			// Vector<Location> vData,
			RunningLogStocker runStocker,
			String dateTime_,
			// SparseArray<LapData> lapData,			
			String dir, String fileName)
	{
		File objDir = new File( dir );
		if( false == objDir.exists() )
		{
			if( false == objDir.mkdirs() )
			{
				RunningLogStocker.setOutputGPXSaveResult( RunningLogStocker.SAVE_NG, stocker );
				return;
			}
		}
		// create a file on the SDcard to export the
		// database contents to
		gpxFilePath = dir + "/" + fileName;
				
		mActivity = activity;
		dateTime = dateTime_;
		stocker = runStocker;
        progressDialog = new ProgressDialog(activity);
		progressDialog.setTitle(
				activity.getString(R.string.DLG_TITLE_EXPORT_PROGRESS));
        progressDialog.setMessage(
        		activity.getString(R.string.MSG_EXPORT_PROGRESS));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        thread = new FileOutputThread(
        	activity,
			//vData,
        	runStocker,
			//lapData,
        	handler,
        	this,
        	FileOutputThread.PROC_TYPE_EXPORT_GPX,
        	gpxFilePath
        );
        thread.start();
	}
	
	public static final int END_MSG_ID = 0;
	public static final int ERROR_MSG_ID = 1;
	public static final int PROGRESS_MAX_MSG_ID = 2;
	public static final int PROGRESS_VAL_MSG_ID = 3;
	public static final int PROGRESS_VAL_INCL_MSG_ID = 4;
	public static final String PROGRESS_VAL_KEY = "gpxgen_progress_val";

	// 別スレッド実行中のイベントを処理するハンドラ
	private final Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {

			switch( msg.what )
			{
//			case ERROR_MSG_ID:
//				OKDialog(
//					this,
//					msg.getData().getStringArray(ERROR_MSG_KEY)[0],
//					msg.getData().getStringArray(ERROR_MSG_KEY)[1]
//				);	
//				return;
			case PROGRESS_MAX_MSG_ID:
				progressDialog.setMax(
					msg.getData()
						.getInt(PROGRESS_VAL_KEY) );
				progressDialog.setProgress( 0 );
				return;
			case PROGRESS_VAL_INCL_MSG_ID:
				progressDialog.incrementProgressBy( 1 );
				return;
			default:
				break;
			}			
			super.handleMessage(msg);
		}
	};
		
	@Override
	public void run() {
		// エクスポートダイアログ終了時のイベント
		if( progressDialog.isShowing() )
		{
			progressDialog.dismiss();
		}
		switch( thread.getProcType() )
		{
			case FileOutputThread.PROC_TYPE_EXPORT_GPX:
				if( true == thread.getResult() )
				{
					RunningLogStocker.setOutputGPXSaveResult(RunningLogStocker.SAVE_OK, stocker);
					// database�ւ̕ۑ�
					int iInsCount = stocker.insertRunHistoryLog(mActivity, dateTime, getGpxFilePath(), stocker );
					if( iInsCount < 0)
					{
						RunningLogStocker.setRunHistorySaveResult( RunningLogStocker.SAVE_NG, stocker );
					}
					else
					{
						RunningLogStocker.setRunHistorySaveResult( RunningLogStocker.SAVE_OK, stocker );
					}
					
				}
				else
				{
					RunningLogStocker.setOutputGPXSaveResult(RunningLogStocker.SAVE_NG, stocker);					
				}
				
				//mActivity.finish();
		    	break;
		}
	}

}
