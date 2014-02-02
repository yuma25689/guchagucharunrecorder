package app.guchagucharr.service;

import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import app.guchagucharr.guchagucharunrecorder.R;

public class FileOutputProcessor implements Runnable {
	
	FileOutputThread thread = null;

	public void outputGPX(Activity activity,
			Vector<Location> vData,
			SparseArray<LapData> lapData,			
			String dir, String fileName)
	{
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
			vData,
			lapData,
        	handler,
        	this,
        	FileOutputThread.PROC_TYPE_EXPORT_GPX,
        	dir,
        	fileName
        );
        thread.start();
        
		
	}
	
	public static final int END_MSG_ID = 0;
	public static final int ERROR_MSG_ID = 1;
	public static final int PROGRESS_MAX_MSG_ID = 2;
	public static final int PROGRESS_VAL_MSG_ID = 3;
	public static final int PROGRESS_VAL_INCL_MSG_ID = 4;
	public static final String PROGRESS_VAL_KEY = "gpxgen_progress_val";

	private ProgressDialog progressDialog = null;
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
		progressDialog.dismiss();
		if( true == thread.getResult() )
		{
			switch( thread.getProcType() )
			{
				case FileOutputThread.PROC_TYPE_EXPORT_GPX:				
			    	break;
			}
		}
	}

}
