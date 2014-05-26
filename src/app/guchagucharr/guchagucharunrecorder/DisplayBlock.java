package app.guchagucharr.guchagucharunrecorder;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
// import android.widget.RelativeLayout;
import android.widget.TextView;
import app.guchagucharr.guchagucharunrecorder.util.RouteButton;
import app.guchagucharr.guchagucharunrecorder.util.TextAndIcon;

/**
 * TODO: 無理矢理このクラスに入れ過ぎなので、分けた方がいいかもしれない
 * @author 25689
 *
 */
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
	public static int getBlockCountOnOnePage(eSizeType sizeType)
	{
		if( sizeType == eSizeType.MODE_ONE )
		{
			return 1;
		}
		else if( sizeType == eSizeType.MODE_QUARTER )
		{
			return 4;
		}
		else if( sizeType == eSizeType.MODE_ONE_SIXTH )
		{
			return 6;
		}
		return 0;
		
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
	static final int ITEM_PADDING = 5;
	static final int ITEM_LEFT_MARGIN = 7;
	static final int ITEM_RIGHT_MARGIN = 7;
	static final int ITEM_BOTTOM_MARGIN = 7;
	static final int BLOCK_MARGIN = 10;
	static final int CORRECT_VALUE = 0;	// 調整用の値 あまりよくないのだが・・・
	static final int BLOCK_MARGIN_HORZ = 10;
	static final int TITLE_MAX_LINE_CNT = 4;

	static final int TITLE_ID_1 = 1111;
	static final int TITLE_ID_2 = 2222;
	
	int width = 0;
	int height = 0;
	double maxRowCnt = 3;
	double maxColCnt = 2;
	double magnifyHeight = 1;
	double fontMagnify = 1;
	int magnifyWidth = 1;
	Paint paintForMeasureText = null;
	
//	Long time = null;
//	Double distance = null;
//	Double speed = null;
	String[] title = null;
	public String[] getTitle()
	{
		return title;
	}
	String text[] = null;
	ArrayList<TextAndIcon> textAndIcon = null; 
	eSizeType sizeType = eSizeType.MODE_ONE_SIXTH;
	eShapeType shapeType = eShapeType.SHAPE_BLOCK;
	int parentWidth = 0;
	int parentHeight = 0;	
	String gpxFilePath = null;
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
			String[] title_, String[] text_,
			String strGpxFilePath_,
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
		gpxFilePath = strGpxFilePath_;
		sizeType = sizeType_;
		shapeType = shapeType_;
		init();
	}
	
	public DisplayBlock(Activity activity,
			int _parentWidth,
			int _parentHeight,
			long recordId_,
			DisplayInfo dispInfo_, 
			String[] title_, ArrayList<TextAndIcon> textAndIcon_,
			String strGpxFilePath_,
			eSizeType sizeType_,
			eShapeType shapeType_ ) {
		super(activity);
		recordId = recordId_;
		mActivity = activity;
		parentWidth = _parentWidth;
		parentHeight = _parentHeight;
		dispInfo = dispInfo_;
		title = title_;
		text = null;	// NOTICE: textと、textAndIconはトレードオフ？
		textAndIcon = textAndIcon_;
		gpxFilePath = strGpxFilePath_;
		sizeType = sizeType_;
		shapeType = shapeType_;
		init();
	}
	
	/**
	 * あるサイズ調整
	 */
	private float getProperTextSize(int width,int height,int lineCnt,String text)
	{
		int viewWidth = width;
		int viewHeight = height;
		
		if( 1 < lineCnt )
		{
			viewHeight /= lineCnt;
		}
		/** 最小のテキストサイズ */
		final float MIN_TEXT_SIZE = 10f;
		final float MAX_TEXT_SIZE = 50f;
		
		float textSizeTmp = MAX_TEXT_SIZE;		

		// それほど良いロジックではないかもしれないが、
		// 行数が指定されている場合は、テキストを行数で等分し、
		// 全ての行の中で一番大きいサイズが収まるまでループする
		float[] textSize = new float[lineCnt];
		String[] textSplited = new String[lineCnt];
		int iStart = 0;
		for( int i=0; i<lineCnt;++i )
		{
			textSize[i] = textSizeTmp;
			
			if( text == null )
			{
				textSplited[i] = null;
			}
			else if( i==lineCnt-1 )
			{
				textSplited[i] = text.substring(iStart);
			}
			else
			{
				int iPlus = text.length() / lineCnt;
				textSplited[i] = text.substring( iStart, iStart + iPlus );
				iStart += iPlus;
			}
			// Paintにテキストサイズ設定
			paintForMeasureText.setTextSize(textSize[i]);
			
			// テキストの縦幅取得
			FontMetrics fm = paintForMeasureText.getFontMetrics();
			float textHeight = (float) (Math.abs(fm.top)) + (Math.abs(fm.descent));

			// テキストの横幅取得
			float textWidth = getMeasureTextWidth(textSizeTmp,textSplited[i]);
			// 縦幅と、横幅が収まるまでループ
			while (viewHeight < textHeight || viewWidth < textWidth)
			{
				// 調整しているテキストサイズが、定義している最小サイズ以下か。
				if (MIN_TEXT_SIZE >= textSize[i])
				{
					// 最小サイズ以下になる場合は最小サイズ
					textSize[i] = MIN_TEXT_SIZE;
					break;
				}
	
				// テキストサイズをデクリメント
				textSize[i]--;
	
				// Paintにテキストサイズ設定
				paintForMeasureText.setTextSize(textSize[i]);
	
				// テキストの縦幅を再取得
				fm = paintForMeasureText.getFontMetrics();
				textHeight = (float) (Math.abs(fm.top)) + (Math.abs(fm.descent));
	
				// テキストの横幅を再取得
				textWidth = getMeasureTextWidth(textSize[i],textSplited[i]);
			}
		}
		
		float textSizeRet = MAX_TEXT_SIZE;
		boolean bSetFromLine = false;
		for( int i=0;i<lineCnt;++i )
		{
			if( textSize[i] <= textSizeRet )
			{
				textSizeRet = textSize[i]; 
				bSetFromLine = true;
			}
		}
		if( bSetFromLine == false )
		{
			textSizeRet = MIN_TEXT_SIZE;
		}
		// テキストサイズ設定
		//setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		return textSizeRet;
	}	
	float getMeasureTextWidth( float textSize, String text )
	{
		paintForMeasureText.setTextSize(textSize);
		if( text == null )
			return 0;
		return paintForMeasureText.measureText(text);
	}
	
	/**
	 * その時の日時を挿入
	 */
	private void addTitle(int id, int height, int lineCnt, String text,int iAlign)//float fontSize)
	{
		TextView txtTitle = new TextView(this.getContext());
		txtTitle.setId(id);
		txtTitle.setTextColor(Color.argb(0xAA, 0, 255, 0));
		float maxTextSize = getProperTextSize(
				width-ITEM_LEFT_MARGIN-ITEM_PADDING,
				height,
				lineCnt,
				text);
		txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, maxTextSize);
		RelativeLayout.LayoutParams lp
		= new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT );
//				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
//				android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
		//lp.weight = 2;
//		lp.addRule(ALIGN_PARENT_LEFT);
//		lp.addRule(ALIGN_PARENT_BOTTOM);
		if( iAlign == 0 )
		{
			lp.addRule(ALIGN_PARENT_LEFT);
			lp.addRule(ALIGN_PARENT_TOP);
			txtTitle.setGravity(Gravity.TOP);
		}
		else
		{
			lp.addRule(ALIGN_PARENT_RIGHT);
			lp.addRule(ALIGN_PARENT_BOTTOM);
			txtTitle.setGravity(Gravity.RIGHT|Gravity.BOTTOM);			
			//txtTitle.setGravity();			
		}
//		lp.addRule(ALIGN_PARENT_LEFT);
//		lp.addRule(ALIGN_PARENT_TOP);
//		txtTitle.setGravity(Gravity.TOP);
//	}
//	else
//	{
//		lp.addRule(ALIGN_PARENT_RIGHT);
//		lp.addRule(ALIGN_PARENT_BOTTOM);
		txtTitle.setLayoutParams(lp);
		//txtTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
		//txtTitle.setGravity(Gravity.CENTER_VERTICAL);
		txtTitle.setText(text);
		txtTitle.setSingleLine(false);
		this.addView( txtTitle );
	}
	private void init()
	{
		// 子要素の数
		int iChildrenCount = 0;
		if( text != null )
		{
			iChildrenCount = text.length;
		}
		else if( textAndIcon != null )
		{
			iChildrenCount = textAndIcon.size();
		}
		else
		{
			// テキストが設定されていなければ、何も作らない
			return;
		}			
		int iShowTextCount = 0;
		if( text != null )
		{
			for( int j=0; j<iChildrenCount; ++j )
			{
				if( text[j] == null )
				{
					continue;
				}
				iShowTextCount++;
			}
		}
		else
		{
			for( int j=0; j<iChildrenCount; ++j )
			{
				if( textAndIcon.get(j).getText() == null )
				{
					continue;
				}
				iShowTextCount++;
			}			
		}
		paintForMeasureText = new Paint();
		
		int iTitleLineCnt = 2;
		if( 0 < title.length && title[0].length() <= 8 )
		{
			// 8文字以下のタイトルは、１行に入れる
			iTitleLineCnt = 1;
		}
		int iTitleHeightBunbo = iShowTextCount+title.length-iTitleLineCnt;
		if( iTitleHeightBunbo == 0 )
		{
			iTitleHeightBunbo = iShowTextCount+title.length;			
		}
		// このビューは、RelativeLayoutに置くものとする
		RelativeLayout.LayoutParams lpThis = null;
		if( shapeType == eShapeType.SHAPE_HORIZONTAL )
		{
			// 1行に1要素の設定
			// ラップの表示に使っているようだが、多分あまりよくない
			// TODO: 廃止でいいかもしれない
			width = parentWidth - BLOCK_MARGIN * 2;
			height = 0;
			if( dispInfo.isPortrait() )
			{
				// Log.w("portrait","dispblock-horizontal");				
				// 倍率の調整
				if( sizeType == eSizeType.MODE_ONE_SIXTH )
				{
					// 倍率調整なし
				}
				else if( sizeType == eSizeType.MODE_QUARTER )
				{
					magnifyHeight = 1.5;
					fontMagnify = 1.5;
				}
				else if( sizeType == eSizeType.MODE_ONE )
				{
					magnifyHeight = 6;
					fontMagnify = 3;
					magnifyWidth = 2;
				}
				width =  (int) (parentWidth - BLOCK_MARGIN * 2);
				height = (int)( ( (double)parentHeight - dispInfo.getStatusBarHeight() )
						//* (magnify / 12 ) - BLOCK_MARGIN_HORZ * 2 );
						* (magnifyHeight / 6 ) - BLOCK_MARGIN_HORZ * 2 );				
			}
			else
			{
				// Log.w("horizontal","dispblock-horizontal");				
				// 倍率の調整
				if( sizeType == eSizeType.MODE_ONE_SIXTH )
				{
					// 倍率調整なし
				}
				else if( sizeType == eSizeType.MODE_QUARTER )
				{
					magnifyHeight = 1.5;
					fontMagnify = 1.5;
				}
				else if( sizeType == eSizeType.MODE_ONE )
				{
					magnifyHeight = 1;
					fontMagnify = 3;
					magnifyWidth = 2;
				}
				width = (int) (parentWidth * ((double)magnifyWidth/2) - BLOCK_MARGIN * 2);
				height = (int)( ( (double)parentHeight - dispInfo.getStatusBarHeight() )
						//* (magnifyHeight / 12 ) - BLOCK_MARGIN_HORZ * 2 );
						* (magnifyHeight / 3 ) - BLOCK_MARGIN_HORZ * 2 );
				
			}
			if( 0 < title.length )
			{
				// 分母の計算
				// 結構無理があると思われる。
				// 引いてる2が何の2か不明
				addTitle(TITLE_ID_1,height/iTitleHeightBunbo,2,title[0],0);//iShowTextCount);
			}
			if( 1 < title.length )
			{
				// TODO:ここの分母微妙
				addTitle(TITLE_ID_2,height/(iShowTextCount+title.length-1),2,title[1],1);
			}

			lpThis = dispInfo.createLayoutParamForNoPosOnBk(
					width,
					height,
					false );
			lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN_HORZ, BLOCK_MARGIN, BLOCK_MARGIN_HORZ);			
		}
		else
		{
			// ブロック要素
			// 4個の場合
			// 縦横どっちにしても、2*2
			// 6個の場合
			// 縦ならば、col*row=2*3になっている
			// 横ならば、col*row=3*2にする
			if(dispInfo.isPortrait())
			{
				maxRowCnt = 3;
				maxColCnt = 2;
			}
			else
			{
				maxRowCnt = 2;
				maxColCnt = 3;				
			}
			// 倍率の調整
			if( sizeType == eSizeType.MODE_ONE_SIXTH )
			{
				// 倍率調整なし
			}
			else if( sizeType == eSizeType.MODE_QUARTER )
			{
				magnifyHeight = 2;
				fontMagnify = 1.5;				
			}
			else if( sizeType == eSizeType.MODE_ONE )
			{
				if( dispInfo.isPortrait() )
				{
					// 縦
					magnifyHeight = 3;
					fontMagnify = 2.2;
					magnifyWidth = 2;
				}
				else
				{
					// 横
					magnifyHeight = 2;
					fontMagnify = 2.2;
					magnifyWidth = 3;					
				}
			}
			// SHAPE_BLOCKとみなす
			// タイル状にレイアウトするイメージ
			width = (int)( ( ( parentWidth - CORRECT_VALUE )* ((double)magnifyWidth / maxColCnt) )
					- BLOCK_MARGIN * 2 );
			height = (int)(( parentHeight - dispInfo.getStatusBarHeight() )
					* (magnifyHeight / maxRowCnt ) - BLOCK_MARGIN * 2);
			//addTitle(TITLE_MAX_LINE_CNT);
			if( 0 < title.length )
			{
				// 分母の計算
				// 結構無理があると思われる。
				addTitle(TITLE_ID_1,height/iTitleHeightBunbo,iTitleLineCnt,title[0],0);//iShowTextCount);
			}
			//addTitle(TITLE_ID_1,height/(iShowTextCount+title.length-2),2,title[0],0);//iShowTextCount);
			if( 1 < title.length )
			{
				// TODO: ここはまだ通っていないので、確認必要
				addTitle(TITLE_ID_2,height/(iShowTextCount+title.length-1),iTitleLineCnt,title[1],1);
			}
			lpThis = dispInfo.createLayoutParamForNoPosOnBk(
					width,
					height,
					false );
			lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN, BLOCK_MARGIN);
		}
		//paint.setTextSize(20);

		setLayoutParams(lpThis);
		// setOrientation(LinearLayout.VERTICAL);
		setClickable(true);
		mActivity.registerForContextMenu(this);
		
		if( text != null )
		{
			int i=0;
			for( i=0; i<text.length; ++i )
			{
				if( text[i] == null )
				{
					continue;
				}
				TextView txt = new TextView(this.getContext());
				txt.setId(i+1);
				txt.setTextColor(Color.argb(0xAA, 255, 255, 255));
				//txt.setPadding(ITEM_PADDING,0,ITEM_PADDING,0);
				RelativeLayout.LayoutParams lpTmp = null;
				if( shapeType == eShapeType.SHAPE_HORIZONTAL )
				{
					lpTmp = new RelativeLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
					float maxTextSize = getProperTextSize(width-ITEM_LEFT_MARGIN-ITEM_PADDING,
							height/(iShowTextCount+title.length),1,text[i]);
					//txt.setTextSize((int)(MIN_ITEM_FONT_SIZE_HORZ * fontMagnify ));
					txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
					txt.setSingleLine(false);
					lpTmp.leftMargin = ITEM_LEFT_MARGIN;
					if( i == 0 )
					{
						//BELOW,TITLE_ID_1);
						lpTmp.addRule(ALIGN_PARENT_LEFT);
						lpTmp.addRule(ALIGN_PARENT_TOP);
						lpTmp.topMargin = height/(iShowTextCount+title.length-(iTitleLineCnt-1));
						//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
					else
					{
						//if( (i+1) % 3 == 0 )
						//{
							lpTmp.addRule(BELOW,i);
							lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
						//}
						//else
						//{
							//lpTmp.addRule(RIGHT_OF,i);
						//}
					}
				}
				else
				{
					lpTmp = new RelativeLayout.LayoutParams( 
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
					float maxTextSize = getProperTextSize(width-ITEM_LEFT_MARGIN-ITEM_PADDING,
							height/(iShowTextCount+title.length),1,text[i]);
					txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
					//txt.setTextSize((int)(MIN_ITEM_FONT_SIZE * fontMagnify));
					if( i== 0)
					{
						//lpTmp.addRule(BELOW,TITLE_ID_1);					
						lpTmp.addRule(ALIGN_PARENT_RIGHT);
						lpTmp.addRule(ALIGN_PARENT_TOP);
						lpTmp.rightMargin = ITEM_LEFT_MARGIN;
						lpTmp.topMargin = height/(iShowTextCount+title.length-(iTitleLineCnt-1));
						//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
					else
					{
						lpTmp.addRule(BELOW,i);
						lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
				}
				// lpTmp.leftMargin = ITEM_LEFT_MARGIN;
				//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
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
		else
		{			
			int i=0;
			for( i=0; i<textAndIcon.size(); ++i )
			{
				if( textAndIcon.get(i).getText() == null )
				{
					continue;
				}
				BitmapFactory.Options bmpoptions = ResourceAccessor.getInstance().getBitmapSizeFromMineType(
						textAndIcon.get(i).getIconId());
				int imageWidth = bmpoptions.outWidth;
				int imageHeight = bmpoptions.outHeight;
				int iProperHeight = height/(iShowTextCount+title.length);
				if( iProperHeight < imageHeight )
				{
					imageWidth = imageHeight = iProperHeight;
				}
				ImageView imgIcon = new ImageView(this.getContext());
				imgIcon.setImageResource(textAndIcon.get(i).getIconId());
				imgIcon.setLayoutParams(
						new RelativeLayout.LayoutParams( 
								imageWidth,
								imageHeight )
//								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
//								android.view.ViewGroup.LayoutParams.WRAP_CONTENT )
					);
				
				LinearLayout row = new LinearLayout(this.getContext());
				TextView txt = new TextView(this.getContext());
				row.setId(i+1);
				txt.setTextColor(Color.argb(0xAA, 255, 255, 255));
				//txt.setPadding(ITEM_PADDING,0,ITEM_PADDING,0);
				RelativeLayout.LayoutParams lpTmp = null;
				if( shapeType == eShapeType.SHAPE_HORIZONTAL )
				{
					lpTmp = new RelativeLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
					float maxTextSize = getProperTextSize(width-ITEM_LEFT_MARGIN-ITEM_PADDING,
							height/(iShowTextCount+title.length),1,textAndIcon.get(i).getText());
					//txt.setTextSize((int)(MIN_ITEM_FONT_SIZE_HORZ * fontMagnify ));
					txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
					txt.setSingleLine(false);
					lpTmp.leftMargin = ITEM_LEFT_MARGIN;
					if( i == 0 )
					{
								//BELOW,TITLE_ID_1);
						lpTmp.addRule(ALIGN_PARENT_LEFT);
						lpTmp.addRule(ALIGN_PARENT_TOP);
						lpTmp.topMargin = height/(iShowTextCount+title.length-1);
						//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
					else
					{
						lpTmp.addRule(BELOW,i);
						lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
				}
				else
				{
					lpTmp = new RelativeLayout.LayoutParams( 
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
					// ここの計算は、ImageViewを考慮したものにしなければならない
					float maxTextSize = getProperTextSize(//width-ITEM_LEFT_MARGIN-ITEM_PADDING,
							//height/(iShowTextCount+title.length),1,textAndIcon.get(i).getText());
							width-imageWidth-ITEM_LEFT_MARGIN-ITEM_PADDING,
							imageHeight,1,textAndIcon.get(i).getText());
					txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
					//txt.setTextSize((int)(MIN_ITEM_FONT_SIZE * fontMagnify));
					if( i == 0 )
					{
						//lpTmp.addRule(BELOW,TITLE_ID_1);
						lpTmp.addRule(ALIGN_PARENT_LEFT);
						lpTmp.addRule(ALIGN_PARENT_TOP);
						lpTmp.rightMargin = ITEM_LEFT_MARGIN;
						lpTmp.topMargin = height/(iShowTextCount+title.length);
						//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
					else
					{
						lpTmp.addRule(BELOW,i);
						lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
					}
				}
				// lpTmp.leftMargin = ITEM_LEFT_MARGIN;
				//lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
				//txt.setGravity(Gravity.CENTER_VERTICAL);
				if( shapeType == eShapeType.SHAPE_HORIZONTAL )
				{
					txt.setSingleLine(false);
				}
				RelativeLayout.LayoutParams lpTxt = new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT );
				lpTxt.addRule( RelativeLayout.CENTER_VERTICAL );
				txt.setLayoutParams(lpTxt);
				
				row.addView(imgIcon);
				row.setLayoutParams(lpTmp);
				txt.setText(textAndIcon.get(i).getText());
				row.addView(txt);
				addView( row );
				
			}
			
		}
		if( gpxFilePath != null )
		{
			RouteButton gpxBtn = new RouteButton(this.getContext(),gpxFilePath);
			RelativeLayout.LayoutParams lpTmp = null;
			gpxBtn.setBackgroundResource(R.drawable.selector_route_button_image);
			lpTmp = new RelativeLayout.LayoutParams(
					width / 5, height / 5 );
					//RelativeLayout.LayoutParams.WRAP_CONTENT,
					//RelativeLayout.LayoutParams.WRAP_CONTENT );
			lpTmp.addRule( RelativeLayout.CENTER_VERTICAL );
			lpTmp.addRule( RelativeLayout.ALIGN_PARENT_RIGHT );
			lpTmp.rightMargin = ITEM_LEFT_MARGIN;
			//lpTmp.bottomMargin = ITEM_BOTTOM_MARGIN;
			//lpTmp.addRule( RelativeLayout.ALIGN_PARENT_TOP);
			gpxBtn.setLayoutParams(lpTmp);
			
			addView( gpxBtn );
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
