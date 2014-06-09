package app.guchagucharr.guchagucharunrecorder.util;

public class CurrentSettingUtil {

	public static double getCurrentDefaultUnitDistanceFromMeter( double meter )
	{
		double ret = meter;
		
		// TODO: ここは、設定から取得する
		int currentUnit = UnitConversions.DISTANCE_UNIT_KILOMETER;
		
		switch( currentUnit )
		{
		case UnitConversions.DISTANCE_UNIT_KILOMETER:
			ret = meter * UnitConversions.M_TO_KM;
			break;
		case UnitConversions.DISTANCE_UNIT_MILE:
			ret = meter * UnitConversions.M_TO_MI;
			break;
		case UnitConversions.DISTANCE_UNIT_FEET:
			ret = meter * UnitConversions.M_TO_FT;
			break;
		}
		return ret;
	}
}
