package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
// import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayBlock extends LinearLayout {

	public static eSizeType getProperSizeTypeFromCount(int iCount)
	{
		if( iCount == 1 )
		{
			return eSizeType.MODE_ONE;
		}
		else if( iCount <= 4)
		{
			return eSizeType.MODE_QUARTER;
		}
		return eSizeType.MODE_ONE_EIGHTH;
	}
	// private ResourceAccessor res;
	public enum eSizeType {
		MODE_ONE,
		MODE_QUARTER,
		MODE_ONE_EIGHTH
		// MODE_SAVE_OR_CLEAR
	};
	static final float MIN_TITLE_FONT_SIZE = 20f; 
	static final float MIN_ITEM_FONT_SIZE = 16f; 
	
	DisplayInfo dispInfo = null;
	static final int ITEM_LEFT_MARGIN = 7;
	static final int BLOCK_MARGIN = 20;
	int width = 0;
	int height = 0;
	int magnify = 1;
	int magnifyWidth = 1;
	
//	Long time = null;
//	Double distance = null;
//	Double speed = null;
	String title = null;
	public String getTitle()
	{
		return title;
	}
	String text[] = null;
	eSizeType sizeType = eSizeType.MODE_ONE_EIGHTH;
	Activity mActivity = null;
	long recordId = -1;
	public long getRecordId()
	{
		return recordId;
	}
	
	public DisplayBlock(Activity activity,
			long recordId_,
			DisplayInfo dispInfo_, 
			String title_, String[] text_, 
			eSizeType sizeType_ ) {
		super(activity);
		recordId = recordId_;
		mActivity = activity;
		dispInfo = dispInfo_;
		title = title_;
		text = text_;
		sizeType = sizeType_;
		init();
	}
	
	/**
	 * その時の日時を挿入
	 */
	private void addTitle()
	{
		TextView txtTitle = new TextView(this.getContext());
		txtTitle.setTextColor(Color.argb(0xFF, 255, 255, 255));
		txtTitle.setTextSize(MIN_TITLE_FONT_SIZE * magnify);
		LinearLayout.LayoutParams lp
		= new LinearLayout.LayoutParams( 
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT );
		lp.weight = 2;
		txtTitle.setLayoutParams(lp);
		//txtTitle.setGravity(Gravity.CENTER_VERTICAL);
		txtTitle.setText(title);
		txtTitle.setSingleLine(false);
		this.addView( txtTitle );
	}
	
	
	private void init()
	{
		// 倍率の調整
		if( sizeType == eSizeType.MODE_ONE_EIGHTH )
		{
			// 倍率調整なし
		}
		else if( sizeType == eSizeType.MODE_QUARTER )
		{
			magnify = 2;
		}
		else if( sizeType == eSizeType.MODE_ONE )
		{
			magnify = 4;
			magnifyWidth = 2;
		}
		// このビューは、RelativeLayoutに置くものとする
		RelativeLayout.LayoutParams lpThis 
		= dispInfo.createLayoutParamForNoPosOnBk(
				(int)( ControlDefs.APP_BASE_WIDTH * ((double)magnifyWidth / 2) ) - BLOCK_MARGIN * 2, 
				(int)( ControlDefs.APP_BASE_HEIGHT * ((double)magnify) / 4 ) - BLOCK_MARGIN * 2, 
				false );
		lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN);
		setLayoutParams(lpThis);
		setOrientation(LinearLayout.VERTICAL);
		setClickable(true);
		mActivity.registerForContextMenu(this);
		
		addTitle();
		
		int iChildrenCount = 0;
		if( text == null )
		{
			return;
		}
		else
		{
			iChildrenCount = text.length;
		}
		for( int i=0; i<iChildrenCount; ++i )
		{
			TextView txt = new TextView(this.getContext());
			txt.setTextSize(MIN_ITEM_FONT_SIZE * magnify);
			txt.setTextColor(Color.argb(0xFF, 255, 255, 255));
			LinearLayout.LayoutParams lpTmp
			= new LinearLayout.LayoutParams( 
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT );
			lpTmp.leftMargin = ITEM_LEFT_MARGIN;
			lpTmp.weight = 2;
			txt.setGravity(Gravity.CENTER_VERTICAL);
			txt.setLayoutParams(lpTmp);
			txt.setText(text[i]);
			addView( txt );
			
		}
	}
	@SuppressWarnings("deprecation")
	public void setBackgroundColorAsStateList( int alpha, int red, int green, int blue)
	{
		Drawable focus = new ColorDrawable( Color.argb(alpha,red+60,green+30,blue) );
		Drawable tap   = new ColorDrawable( Color.argb(alpha,green + 30,red + 60,blue) );
		Drawable normal= new ColorDrawable( Color.argb(alpha,red,green,blue) );

		StateListDrawable d = new StateListDrawable();
		d.addState( new int[]{ android.R.attr.state_pressed }, tap );
		d.addState( new int[]{ android.R.attr.state_focused }, focus );
		d.addState( StateSet.WILD_CARD, normal );
		
		setBackgroundDrawable( d );
	}
	
	
}
