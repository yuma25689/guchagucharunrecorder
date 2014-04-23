package app.guchagucharr.guchagucharunrecorder.util;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class RouteButton extends ImageButton {

	String gpxFilePath = null;
	Context ctx = null;
	
	public RouteButton(Context context, String gpxFilePath_) {
		super(context);
		ctx = context;
		setOnClickListener(listener);
		gpxFilePath = gpxFilePath_;
	}

	OnClickListener listener = new OnClickListener()
	{

		@Override
		public void onClick(View v) {
			// GPXの共有を行う
			// アストロと同じになっていると思う
	        Intent intent = new Intent();
	        intent.setAction(android.content.Intent.ACTION_VIEW);//SEND);//VIEW);
	        intent.addCategory(Intent.CATEGORY_DEFAULT);
	        intent.setDataAndType(Uri.fromFile(new File(gpxFilePath)), "application/gpx+xml");
	        try {
		        ctx.startActivity(intent);
	        } catch (ActivityNotFoundException e) {
	        	// TODO エラー処理
	        	Log.e("ActivityNotFound", e.getMessage());
	        }
		}
		
	};
}
