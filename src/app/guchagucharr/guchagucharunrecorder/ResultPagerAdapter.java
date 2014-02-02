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


public class ResultPagerAdapter extends PagerAdapter {
	 
    private static final int TOTAL_PAGE = 2;
    private LayoutInflater mInflter;    // レイアウトを作るやつ
    private IPageViewController mParent;   // アクティビティ
 
    /**
     * コンストラクタ
     * @param context コンテキスト
     */
    public ResultPagerAdapter(final Activity activity, IPageViewController parent ) {
        this.mInflter = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mParent = parent;
    }
 
    /**
     * ページを作るときの呼ばれる
     * @param container この中にビューを作る(レイアウトからひっぱてきたり)
     * @param position インスタンスを作る位置
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
         
        RelativeLayout layout = (RelativeLayout)this.mInflter.inflate(R.layout.result_page, null);
 
        mParent.initControls(position, layout);
        
        container.addView(layout);
        return layout;
    }
     
    /**
     * 削除されるタイミングで呼ばれる。
     * 常にメモリにレイアウトをキープせずにそのつど作り出す設計らしい。
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager)container;
        viewPager.removeView((View)object);
        Log.i("<<<<destroyItem", "POS: " + position);
    }
 
    /**
     * ページの枚数を返す
     */
    @Override
    public int getCount() {
        // TODO 自動生成されたメソッド・スタブ
        return TOTAL_PAGE;
    }
 
    /**
     * ビュー同士の比較
     * 多分内部で呼び出されるんじゃないかな？
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        // TODO 自動生成されたメソッド・スタブ
        return view.equals(object);
    }
     
}
