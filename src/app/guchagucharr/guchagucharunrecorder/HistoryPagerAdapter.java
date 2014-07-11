package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import app.guchagucharr.guchagucharunrecorder.util.LogWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import app.guchagucharr.interfaces.IPageViewController;


public class HistoryPagerAdapter extends PagerAdapter {
	 
    private int mPageCount = 1;
    public void setCount(int pageCount)
    {
    	if( mPageCount != pageCount )
    	{
    		mPageCount = pageCount;
			// TODO: これで更新は行われるのか・・・要確認
			notifyDataSetChanged();    		
    	}
    }
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
        if( position == 1 )
        {
        	iLayoutId = R.layout.page_history_sub_vscrollable;
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
        LogWrapper.i("<<<<destroyItem", "POS: " + position);
    }
    @Override
    public int getCount() {
        return mPageCount;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
    @Override
    public int getItemPosition(Object object) {
    	// 強制更新用
    	return POSITION_NONE;
    }     
}
