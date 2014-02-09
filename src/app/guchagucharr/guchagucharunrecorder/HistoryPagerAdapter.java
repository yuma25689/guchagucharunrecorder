package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import app.guchagucharr.interfaces.IPageViewController;


public class HistoryPagerAdapter extends PagerAdapter {
	 
    private static final int TOTAL_PAGE = 1;
    private LayoutInflater mInflter;
    private IPageViewController mParent;
 
    public HistoryPagerAdapter(final Activity activity, IPageViewController parent ) {
        this.mInflter = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mParent = parent;
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
    	int iLayoutId = R.layout.page_vscrollable;
        if( false == mParent.getDispInfo().isPortrait() )
        {
        	iLayoutId = R.layout.page_vscrollable;
        }
        ViewGroup layout = (ViewGroup) this.mInflter.inflate(
        		iLayoutId, null);
        RelativeLayout rl = (RelativeLayout)layout.findViewById( R.id.page_content );
 
        mParent.initControls(position, rl);
        
        container.addView(layout);
        return layout;
    }
     
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager)container;
        viewPager.removeView((View)object);
        Log.i("<<<<destroyItem", "POS: " + position);
    }
    @Override
    public int getCount() {
        return TOTAL_PAGE;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
     
}
