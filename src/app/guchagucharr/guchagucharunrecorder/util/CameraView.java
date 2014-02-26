package app.guchagucharr.guchagucharunrecorder.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import app.guchagucharr.guchagucharunrecorder.MessageDef;
import app.guchagucharr.guchagucharunrecorder.R;
import app.guchagucharr.service.RunLogger;
import app.guchagucharr.service.RunLoggerService;

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
		List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
		if (supportedSizes != null && supportedSizes.size() > 0 )//&& RunLogger.sService != null)
		{
			//p.setPreviewFormat(format);
			// getSupportedPictureSizesは必ず１つしかサイズを返さないとドキュメントには書いてある
			p.setPreviewSize(supportedSizes.get(0).width,supportedSizes.get(0).height);
			Log.v("getSupportedPictureSizes", "width=" 
			+ supportedSizes.get(0).width  + " height=" + supportedSizes.get(0).height);

			// ジオタグの設定
//			Location location = RunLoggerService.getLogStocker().getCurrentLocation();
//			if( location != null )
//			{
//				p.removeGpsData();
//				// TODO: アプリケーション名を設定するのは微妙かも
//				p.setGpsProcessingMethod(this.getContext().getString(R.string.app_name));
//				p.setGpsAltitude(location.getAltitude());
//				p.setGpsLatitude(location.getLatitude());
//				p.setGpsLongitude(location.getLongitude());
//				p.setGpsTimestamp(location.getTime());
//				p.setPictureFormat(ImageFormat.JPEG);
//				Log.v("geotag","geotag set");
//			}
			try{
				camera.setParameters(p);
			} catch( Exception e ) {
				Log.e("setParameters",e.getMessage());
				return;
			}
			camera.startPreview();
		}
		// TODO: サポートされていなかったら、それをユーザへ通知
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
	        //Uri uriTarget = this.getContext().getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
			File pathExternalPublicDir = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DCIM);
			// DCIMフォルダーのパス
			String dir = pathExternalPublicDir.getPath() + "/GuchaGuchaRR";
			File dirFile = new File( dir);
			if( dirFile.exists() == false )
			{
				dirFile.mkdirs();
			}
	        // NOTICE:Bitmapとして保存しては行けない
			
            try {
				SimpleDateFormat sdfDateTime = new SimpleDateFormat(
						this.getContext().getString(R.string.time_for_id_format));			
		        String path = dir + "/" + sdfDateTime.format( RunLogger.sService.getTimeInMillis() ) + ".jpg";
		        FileOutputStream imageFileOS;
//		        File file = new File(path);
//		        file.createNewFile();
		        
                imageFileOS = new FileOutputStream(new File(path));//uriTarget);
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();

//              Toast.makeText(AndroidCamera.this, 
//                      "Image saved: " + uriTarget.toString(), 
//                      Toast.LENGTH_LONG).show();
//              test.getLatitude();
//              test.getLongitude();
//              String Text = "Lat = " + test.getLatitude() + "|Long = " + test.getLongitude();
//              Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			//Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, null);
                String uri;
			// try {
				uri = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), path, "", null);
				// handler
				Message msg = Message.obtain();
				msg.what = MessageDef.MSG_CAMERA_END;
				msg.obj = uri;
				// msg.arg1 = DisplayInfo.MSG_INIT_END;
				handler.sendMessage( msg );
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
            
			
			//camera.startPreview();
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if(me.getAction()==MotionEvent.ACTION_DOWN) {
			Camera.Parameters p = camera.getParameters();
			List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
			if (supportedSizes != null && supportedSizes.size() > 0 )//&& RunLogger.sService != null)
			{
				//p.setPreviewFormat(format);
				// getSupportedPictureSizesは必ず１つしかサイズを返さないとドキュメントには書いてある
				p.setPreviewSize(supportedSizes.get(0).width,supportedSizes.get(0).height);
				Log.v("getSupportedPictureSizes", "width=" 
				+ supportedSizes.get(0).width  + " height=" + supportedSizes.get(0).height);

				// ジオタグの設定
				Location location = RunLoggerService.getLogStocker().getCurrentLocation();
				if( location != null )
				{
					p.removeGpsData();
					// TODO: アプリケーション名を設定するのは微妙かも
					p.setGpsProcessingMethod(this.getContext().getString(R.string.app_name));
					p.setGpsAltitude(location.getAltitude());
					p.setGpsLatitude(location.getLatitude());
					p.setGpsLongitude(location.getLongitude());
					p.setGpsTimestamp(location.getTime());
					//p.setPictureFormat(ImageFormat.JPEG);
					Log.v("geotag","geotag set");
				}
				try{
					camera.setParameters(p);
				} catch( Exception e ) {
					Log.e("setParameters",e.getMessage());
				}
				camera.takePicture(null,null,this);
			}
			
		}
		return true;
	}
}
