package app.guchagucharr.guchagucharunrecorder;

import android.content.Context;
import app.guchagucharr.guchagucharunrecorder.util.SoundPlayer;
import app.guchagucharr.guchagucharunrecorder.util.UnitConversions;

public class RunNotificationSoundPlayer {
	public static final int KILO_SOUND_RES_IDS[] =
	{
		R.raw.sound1kilo,
		R.raw.sound2kilo,
		R.raw.sound3kilo,
		R.raw.sound4kilo,
		R.raw.sound5kilo,
		R.raw.sound6kilo,
		R.raw.sound7kilo,
		R.raw.sound8kilo,
		R.raw.sound9kilo,
		R.raw.sound10kilo,
		R.raw.sound11kilo,
		R.raw.sound12kilo,
		R.raw.sound13kilo,
		R.raw.sound14kilo,
		R.raw.sound15kilo
	};
	public static final int MILE_SOUND_RES_IDS[] =
	{
		R.raw.sound1mile,
		R.raw.sound2mile,
		R.raw.sound3mile,
		R.raw.sound4mile,
		R.raw.sound5mile,
		R.raw.sound6mile,
		R.raw.sound7mile,
		R.raw.sound8mile,
		R.raw.sound9mile,
		R.raw.sound10mile,
		R.raw.sound11mile,
		R.raw.sound12mile,
		R.raw.sound13mile,
		R.raw.sound14mile,
		R.raw.sound15mile
	};
	public static void soundCantGetLocationLongTime( Context ctx )
	{
		// しばらくGPSが取得できていないのを鳴らす
		SoundPlayer.sound(ctx, R.raw.cant_get_location );	
	}
	public static void soundActivityStart( Context ctx )
	{
		// 計測開始
		SoundPlayer.sound(ctx, R.raw.start_activity );	
	}
	public static void soundActivityFinish( Context ctx )
	{
		// 計測終了
		SoundPlayer.sound(ctx, R.raw.finished );	
	}
	public static void soundActivitySaved( Context ctx )
	{
		// 計測終了
		SoundPlayer.sound(ctx, R.raw.saved );	
	}
	
	public static void soundArrivalNotify( Context ctx, int arrivalDistance, int unitType )
	{
		if( arrivalDistance < 1)
		{
			// 到達した距離が1より小さい場合、まだ通知できないものとする
			return;
		}
		switch( unitType )
		{
		case UnitConversions.DISTANCE_UNIT_METER:
			// メートル単位は未対応とする
			break;
		case UnitConversions.DISTANCE_UNIT_KILOMETER:
			// km
			if( arrivalDistance - 1 < KILO_SOUND_RES_IDS.length )
			{
				// 通知
				SoundPlayer.sound(ctx, KILO_SOUND_RES_IDS[arrivalDistance]);
			}
			break;
		case UnitConversions.DISTANCE_UNIT_MILE:
			// mile
			if( arrivalDistance - 1 < MILE_SOUND_RES_IDS.length )
			{
				// 通知
				SoundPlayer.sound(ctx, MILE_SOUND_RES_IDS[arrivalDistance]);
			}
			break;
			
		}
	}
	
}
