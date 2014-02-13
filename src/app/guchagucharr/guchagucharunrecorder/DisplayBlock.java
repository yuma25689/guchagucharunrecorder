package app.guchagucharr.guchagucharunrecorder;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import android.widget.RelativeLayout;
// import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayBlock extends RelativeLayout {

	Object data = null;
	public void setData(Object data_)
	{
		data = data_;
	}
	public Object  getData()
	{
		return data;
	}
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
		return eSizeType.MODE_ONE_SIXTH;
	}
	// private ResourceAccessor res;
	public enum eSizeType {
		MODE_ONE,
		MODE_QUARTER,
		MODE_ONE_SIXTH
		// MODE_SAVE_OR_CLEAR
	};
	public enum eShapeType {
		SHAPE_BLOCK,
		SHAPE_HORIZONTAL//,
		//SHAPE_HORIZONTAL
		// MODE_SAVE_OR_CLEAR
	};
	static final float MIN_TITLE_FONT_SIZE = 28f; 
	static final float MIN_TITLE_FONT_SIZE_HORZ = 24f;
	static final float MIN_ITEM_FONT_SIZE = 20f;
	static final float MIN_ITEM_FONT_SIZE_HORZ = 12f;
	
	DisplayInfo dispInfo = null;
	static final int ITEM_LEFT_MARGIN = 7;
	static final int BLOCK_MARGIN = 10;
	static final int CORRECT_VALUE = 0;	// 調整用の値 あまりよくないのだが・・・
	static final int BLOCK_MARGIN_HORZ = 10;
	int width = 0;
	int height = 0;
	double magnify = 1;
	double fontMagnify = 1;
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
	eSizeType sizeType = eSizeType.MODE_ONE_SIXTH;
	eShapeType shapeType = eShapeType.SHAPE_BLOCK;
	int parentWidth = 0;
	int parentHeight = 0;	
	Activity mActivity = null;
	long recordId = -1;
	public long getRecordId()
	{
		return recordId;
	}
	
	public DisplayBlock(Activity activity,
			int _parentWidth,
			int _parentHeight,
			long recordId_,
			DisplayInfo dispInfo_, 
			String title_, String[] text_, 
			eSizeType sizeType_,
			eShapeType shapeType_ ) {
		super(activity);
		recordId = recordId_;
		mActivity = activity;
		parentWidth = _parentWidth;
		parentHeight = _parentHeight;
		dispInfo = dispInfo_;
		title = title_;
		text = text_;
		sizeType = sizeType_;
		shapeType = shapeType_;
		init();
	}
	
	/**
	 * その時の日時を挿入
	 */
	private void addTitle(float fontSize)
	{
		TextView txtTitle = new TextView(this.getContext());
		txtTitle.setTextColor(Color.argb(0xAA, 0, 255, 0));
		txtTitle.setTextSize((int)(fontSize * magnify));
		RelativeLayout.LayoutParams lp
		= new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT );
		//lp.weight = 2;
		lp.addRule(ALIGN_RIGHT);
		lp.addRule(ALIGN_BOTTOM);		
		txtTitle.setLayoutParams(lp);
		//txtTitle.setGravity(Gravity.CENTER_VERTICAL);
		txtTitle.setText(title);
		txtTitle.setSingleLine(false);
		this.addView( txtTitle );
	}
	private void init()
	{
		// このビューは、RelativeLayoutに置くものとする
		RelativeLayout.LayoutParams lpThis = null; 
		if( shapeType == eShapeType.SHAPE_HORIZONTAL )
		{	
			addTitle(MIN_TITLE_FONT_SIZE_HORZ);
			// 倍率の調整
			if( sizeType == eSizeType.MODE_ONE_SIXTH )
			{
				// 倍率調整なし
			}
			else if( sizeType == eSizeType.MODE_QUARTER )
			{
				magnify = 1.5;
				fontMagnify = 1.5;
			}
			else if( sizeType == eSizeType.MODE_ONE )
			{
				magnify = 3;
				fontMagnify = 2.2;
				magnifyWidth = 2;
			}
			
			int height = (int)( ( (double)parentHeight - dispInfo.getStatusBarHeight() )
					* (magnify / 12 ) - BLOCK_MARGIN_HORZ * 2 );
			
			lpThis = dispInfo.createLayoutParamForNoPosOnBk(
					parentWidth - BLOCK_MARGIN * 2,
					height,
					false );
			lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN_HORZ, BLOCK_MARGIN, BLOCK_MARGIN_HORZ);			
		}
		else
		{
			addTitle(MIN_TITLE_FONT_SIZE);
			
			// 倍率の調整
			if( sizeType == eSizeType.MODE_ONE_SIXTH )
			{
				// 倍率調整なし
			}
			else if( sizeType == eSizeType.MODE_QUARTER )
			{
				magnify = 2;
				fontMagnify = 1.5;				
			}
			else if( sizeType == eSizeType.MODE_ONE )
			{
				magnify = 3;
				fontMagnify = 2.2;				
				magnifyWidth = 2;
			}
			// SHAPE_BLOCKとみなす
			// タイル状にレイアウトするイメージ
			int width = (int)( ( ( parentWidth - CORRECT_VALUE )* ((double)magnifyWidth / 2) )
					- BLOCK_MARGIN * 2 );
			int height = (int)(( parentHeight - dispInfo.getStatusBarHeight() )
					* (magnify / 3 ) - BLOCK_MARGIN * 2);
			lpThis = dispInfo.createLayoutParamForNoPosOnBk(
					width, 
					height, 
					false );
			lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN);
		}
		setLayoutParams(lpThis);
		// setOrientation(LinearLayout.VERTICAL);
		setClickable(true);
		mActivity.registerForContextMenu(this);
		
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
			if( text[i] == null )
			{
				continue;
			}
			TextView txt = new TextView(this.getContext());
			txt.setId(i+1);
			txt.setTextColor(Color.argb(0xAA, 255, 255, 255));
			RelativeLayout.LayoutParams lpTmp = null;
			if( shapeType == eShapeType.SHAPE_HORIZONTAL )
			{
				lpTmp = new RelativeLayout.LayoutParams( 
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
				txt.setTextSize((int)(MIN_ITEM_FONT_SIZE_HORZ * fontMagnify ));
				txt.setSingleLine(false);
				lpTmp.leftMargin = BLOCK_MARGIN;
				if( i == 0 )
				{
					lpTmp.addRule(ALIGN_LEFT);
					lpTmp.addRule(ALIGN_TOP);
				}
				else
				{
					if( (i+1) % 3 == 0 )
					{
						lpTmp.addRule(BELOW,i-1);
					}
					else
					{
						lpTmp.addRule(RIGHT_OF,i);
					}
				}
			}
			else
			{
				lpTmp = new RelativeLayout.LayoutParams( 
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
				txt.setTextSize((int)(MIN_ITEM_FONT_SIZE * fontMagnify));
				if( i== 0)
				{
					lpTmp.addRule(ALIGN_LEFT);
					lpTmp.addRule(ALIGN_TOP);
				}
				else
				{
					lpTmp.addRule(BELOW,i);
				}
			}
			// lpTmp.leftMargin = ITEM_LEFT_MARGIN;
			lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
			//txt.setGravity(Gravity.CENTER_VERTICAL);
			if( shapeType == eShapeType.SHAPE_HORIZONTAL )
			{
				txt.setSingleLine(false);
			}
			txt.setLayoutParams(lpTmp);
			txt.setText(text[i]);
			addView( txt );
			
		}
	}
	@SuppressWarnings("deprecation")
	public void setBackgroundColorAsStateList( int alpha, int red, int green, int blue)
	{
		Drawable focus = new ColorDrawable( Color.argb(alpha,red+60,green+60,blue) );
		Drawable tap   = new ColorDrawable( Color.argb(alpha,red,green+100,0) );
		Drawable normal= new ColorDrawable( Color.argb(alpha,red,green,blue) );

		StateListDrawable d = new StateListDrawable();
		d.addState( new int[]{ android.R.attr.state_pressed }, tap );
		d.addState( new int[]{ android.R.attr.state_focused }, focus );
		d.addState( StateSet.WILD_CARD, normal );
		
		setBackgroundDrawable( d );
	}
	
	
}
