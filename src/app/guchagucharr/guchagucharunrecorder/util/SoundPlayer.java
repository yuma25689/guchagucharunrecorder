package app.guchagucharr.guchagucharunrecorder.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class SoundPlayer {
	private static SoundPool soundPool = null;
	public static int sound(Context ctx,int soundResourceId)
	{
		releaseSound();
		soundPool = new SoundPool(1,AudioManager.STREAM_NOTIFICATION,0);
		soundPool.setOnLoadCompleteListener(
				new OnLoadCompleteListener()
				{
					@Override
					public void onLoadComplete(SoundPool s,int Id, int sts)
					{
						if( sts == 0 )
							s.play(Id, 1.0f, 1.0f, 1, 0, 1.0f);
					}
				}
		);
		return soundPool.load(ctx, soundResourceId, 1);
	}
	public static void releaseSound()
	{
		if( soundPool != null )
		{
			soundPool.release();
		}
	}
}
