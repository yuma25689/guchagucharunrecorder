package app.guchagucharr.guchagucharunrecorder.util;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import app.guchagucharr.guchagucharunrecorder.MessageDef;

public class CameraView extends SurfaceView implements Callback, PictureCallback {
	private Camera camera = null;
	private Handler handler = null;
	public void setHandler( Handler hdl )
	{
		handler = hdl;
	}

	public CameraView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// SURFACE_TYPE_NORMAL
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters p = camera.getParameters();
		p.setPreviewSize(width,height);
		camera.setParameters(p);
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// カメラを取得
		camera = Camera.open();
		
		if( camera != null ){
		    try {
		        camera.setPreviewDisplay(holder);
		    } catch (IOException e) {
		        e.printStackTrace();
		        Log.e("setPreviewDisplay",e.getMessage());
		    }
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        // プレビュー停止
        if( camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if( camera != null )
		{
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, null);
			String uri = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bmp, "", null);
			// handler
			Message msg = Message.obtain();
			msg.what = MessageDef.MSG_CAMERA_END;
			msg.obj = uri;
			// msg.arg1 = DisplayInfo.MSG_INIT_END;
			handler.sendMessage( msg );
			
			//camera.startPreview();
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if(me.getAction()==MotionEvent.ACTION_DOWN) {
			camera.takePicture(null,null,this);
		}
		return true;
	}
}
