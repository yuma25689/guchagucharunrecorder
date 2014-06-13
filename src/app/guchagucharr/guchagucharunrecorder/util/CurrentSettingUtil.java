package app.guchagucharr.guchagucharunrecorder.util;

import android.content.Context;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import app.guchagucharr.guchagucharunrecorder.GGRRPreferenceActivity;

public class CurrentSettingUtil {

	public static double getCurrentDefaultUnitDistanceFromMeter( Context ctx, int currentUnit, double meter )
	{
		double ret = meter;
		
		// int currentUnit = UnitConversions.DISTANCE_UNIT_KILOMETER;
		
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
