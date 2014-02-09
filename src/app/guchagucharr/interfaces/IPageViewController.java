package app.guchagucharr.interfaces;

import android.widget.RelativeLayout;
import app.guchagucharr.guchagucharunrecorder.DisplayInfo;

public interface IPageViewController {
	public int initPager();
	public int initControls(int position,RelativeLayout rl);
	public DisplayInfo getDispInfo();

}
