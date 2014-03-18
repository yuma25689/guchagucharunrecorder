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
	 
    private static final int TOTAL_PAGE = 2;
    private LayoutInflater mInflter;    // ���C�A�E�g�������
    private IPageViewController mParent;   // �A�N�e�B�r�e�B
 
    /**
     * �R���X�g���N�^
     * @param context �R���e�L�X�g
     */
    public ResultPagerAdapter(final Activity activity, IPageViewController parent ) {
        this.mInflter = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mParent = parent;
    }
 
    /**
     * �y�[�W�����Ƃ��̌Ă΂��
     * @param container ���̒��Ƀr���[�����(���C�A�E�g����Ђ��ςĂ�����)
     * @param position �C���X�^���X�����ʒu
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
     * �폜�����^�C�~���O�ŌĂ΂��B
     * ��Ƀ������Ƀ��C�A�E�g���L�[�v�����ɂ��̂Ǎ��o���݌v�炵���B
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager)container;
        viewPager.removeView((View)object);
        Log.i("<<<<destroyItem", "POS: " + position);
    }
 
    /**
     * �y�[�W�̖�����Ԃ�
     */
    @Override
    public int getCount() {
        // TODO �����������ꂽ���\�b�h�E�X�^�u
        return TOTAL_PAGE;
    }
 
    /**
     * �r���[���m�̔�r
     * ���������ŌĂяo�����񂶂�Ȃ����ȁH
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        // TODO �����������ꂽ���\�b�h�E�X�^�u
        return view.equals(object);
    }
     
}
