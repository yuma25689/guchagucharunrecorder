package app.guchagucharr.guchagucharunrecorder;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class GGRRPreferenceActivity extends PreferenceActivity {
	public static final String DISTANCE_UNIT_KEY = "distance_unit_key";
	public static final String DEFAULT_ACTIVITY_TYPE_KEY = "default_activity_type_key";

	// TODO: 必要ならば、値の変更を監視する
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 多分、ここでsetSharedPreferencesNameしなければ、デフォルトの設定として作成されるはず
	       // version3.0 より前
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
		addPreferencesFromResource(R.xml.settings);
	       // version3.0 以降
//		} else {
//			getFragmentManager().beginTransaction().replace(android.R.id.content, new prefFragment()).commit();
//		}
		
	}

//	public static class prefFragment extends PreferenceFragment {
//		
//		@Override
//		public void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			
//		    addPreferencesFromResource(R.xml.settings);
//		}
//	}
}
