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
		R.raw.sound15kilo,
		R.raw.sound16kilo,
		R.raw.sound17kilo,
		R.raw.sound18kilo,
		R.raw.sound19kilo,
		R.raw.sound20kilo,
		R.raw.sound21kilo,
		R.raw.sound22kilo,
		R.raw.sound23kilo,
		R.raw.sound24kilo,
		R.raw.sound25kilo,
		R.raw.sound26kilo,
		R.raw.sound27kilo,
		R.raw.sound28kilo,
		R.raw.sound29kilo,
		R.raw.sound30kilo,
		R.raw.sound31kilo,
		R.raw.sound33kilo,
		R.raw.sound34kilo,
		R.raw.sound35kilo,
		R.raw.sound36kilo,
		R.raw.sound37kilo,
		R.raw.sound38kilo,
		R.raw.sound39kilo,
		R.raw.sound40kilo,
		R.raw.sound42kilo,
		R.raw.sound43kilo,
		R.raw.sound44kilo,
		R.raw.sound45kilo,
		R.raw.sound46kilo,
		R.raw.sound47kilo,
		R.raw.sound48kilo,
		R.raw.sound49kilo,
		R.raw.sound50kilo
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
	// 5分ごとに通知する
	// TODO: 設定に移す
	public static final int MINUTE_NOTIFY_RANGE = 5;

	public static final int MINUTE_SOUND_RES_IDS[] =
	{
		R.raw.sound5minute,
		R.raw.sound10minute,
		R.raw.sound15minute,
		R.raw.sound20minute,
		R.raw.sound25minute,
		R.raw.sound30minute,
		R.raw.sound35minute,
		R.raw.sound40minute,
		R.raw.sound45minute,
		R.raw.sound50minute,
		R.raw.sound55minute,
		R.raw.sound1hour,
		R.raw.sound1hour5,
		R.raw.sound1hour10,
		R.raw.sound1hour15,
		R.raw.sound1hour20,
		R.raw.sound1hour25,
		R.raw.sound1hour30,
		R.raw.sound1hour35,
		R.raw.sound1hour40,
		R.raw.sound1hour50,
		R.raw.sound1hour55,
		R.raw.sound2hour,
		R.raw.sound2hour5,
		R.raw.sound2hour10,
		R.raw.sound2hour15,
		R.raw.sound2hour20,
		R.raw.sound2hour25,
		R.raw.sound2hour30,
		R.raw.sound2hour35,
		R.raw.sound2hour40,
		R.raw.sound2hour45,
		R.raw.sound2hour50,
		R.raw.sound2hour55,
		R.raw.sound3hour,
		R.raw.sound3hour5,
		R.raw.sound3hour10,
		R.raw.sound3hour15,
		R.raw.sound3hour20,
		R.raw.sound3hour25,
		R.raw.sound3hour30,
		R.raw.sound3hour35,
		R.raw.sound3hour40,
		R.raw.sound3hour45,
		R.raw.sound3hour50,
		R.raw.sound3hour55,
		R.raw.sound4hour,
		R.raw.sound4hour5,
		R.raw.sound4hour10,
		R.raw.sound4hour15,
		R.raw.sound4hour20,
		R.raw.sound4hour25,
		R.raw.sound4hour30,
		R.raw.sound4hour35,
		R.raw.sound4hour40,
		R.raw.sound4hour45,
		R.raw.sound4hour50,
		R.raw.sound4hour55,
		R.raw.sound5hour,
		R.raw.sound5hour5,
		R.raw.sound5hour10,
		R.raw.sound5hour15,
		R.raw.sound5hour20,
		R.raw.sound5hour25,
		R.raw.sound5hour30,
		R.raw.sound5hour35,
		R.raw.sound5hour40,
		R.raw.sound5hour45,
		R.raw.sound5hour50,
		R.raw.sound5hour55,
		R.raw.sound6hour,
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
	public static void soundTimeNotify( Context ctx, int arrivalTime )//, int unitType )
	{
		if( arrivalTime < 1)
		{
			// 到達した距離が1より小さい場合、まだ通知できないものとする
			return;
		}
		// km
		if( arrivalTime - 1 < MINUTE_SOUND_RES_IDS.length )
		{
			// 通知
			SoundPlayer.sound(ctx, MINUTE_SOUND_RES_IDS[arrivalTime]);
		}
	}
	
	public static void soundArrivalNotify( Context ctx, int arrivalDistance, int unitType )
	{
		if( arrivalDistance < 0)
		{
			// 到達した距離が0より小さい場合、まだ通知できないものとする
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
