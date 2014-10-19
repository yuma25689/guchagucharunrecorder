package app.guchagucharr.guchagucharunrecorder.edit;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.RelativeLayout;
// import android.widget.RelativeLayout;
import android.widget.TextView;
import app.guchagucharr.guchagucharunrecorder.DisplayInfo;
import app.guchagucharr.guchagucharunrecorder.util.ColumnData;

/**
 * @author 25689
 *
 */
public class EditBlock extends RelativeLayout {

	EditText edit = null;
	public ColumnData getColumnData()
	{
		// editだけは、更新されている可能性があるので、取得直前に上書きして返す
		ColumnData dataRet = columnData;
		dataRet.setText( String.valueOf(edit.getText() ) );
		
		return dataRet;
	}
	
	DisplayInfo dispInfo = null;
	Paint paintForMeasureText = null;
	static final int ITEM_PADDING = 3;
	static final int ITEM_LEFT_MARGIN = 3;
	static final int ITEM_RIGHT_MARGIN = 3;
	static final int ITEM_BOTTOM_MARGIN = 3;
	static final int BLOCK_MARGIN = 10;
	static final int CORRECT_VALUE = 0;	// 調整用の値 あまりよくないのだが・・・
	static final int BLOCK_MARGIN_HORZ = 10;
	static final int TITLE_MAX_LINE_CNT = 4;
	static final int MAX_CHILDREN_COUNT = 8;
	static final int PROPER_LABEL_TEXT_LENGTH = 6;

	static final int LABEL_ID_1 = 666;
	static final int LABEL_ID_2 = 667;
	static final int EDIT_ID = 668;

	
	int width = 0;
	int height = 0;
	int parentWidth = 0;
	int parentHeight = 0;	
	Activity mActivity = null;
	String columnName;
	ColumnData columnData;
	int iEstimatedBlockCount = 0;
	//String columnId;
//	long recordId = -1;
//	public long getRecordId()
//	{
//		return recordId;
//	}
	
	public EditBlock(Activity activity,
			int _parentWidth,
			int _parentHeight,
			DisplayInfo dispInfo_,
			ColumnData columnData_,
			int iEstimatedBlockCount_
	) 
	{
		super(activity);
		columnData = columnData_;
		mActivity = activity;
		parentWidth = _parentWidth;
		parentHeight = _parentHeight;
		dispInfo = dispInfo_;
		iEstimatedBlockCount = iEstimatedBlockCount_;
 
		init();
	}
	
	/**
	 * あるサイズ調整
	 */
	private float getProperTextSize(int width,int height,int lineCnt,String text)
	{
		//int lineCnt = 1;
		int viewWidth = width;
		int viewHeight = height;
		
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
			if( i==lineCnt-1 )
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
		return paintForMeasureText.measureText(text);
	}
	
	private void init()
	{
		paintForMeasureText = new Paint();
		int iPageMaxCount = 0;
		if( MAX_CHILDREN_COUNT < iEstimatedBlockCount )
		{
			iPageMaxCount = MAX_CHILDREN_COUNT;
		}
		else
		{
			iPageMaxCount = iEstimatedBlockCount;
		}
		
		// このビューは、RelativeLayoutに置くものとする
		RelativeLayout.LayoutParams lpThis = null; 
		//width = parentWidth - BLOCK_MARGIN * 2;
		//height = 0;
		if( dispInfo.isPortrait() )
		{
			// １行に１つで上から順に並べる
			width =  (int) (parentWidth - BLOCK_MARGIN * 2);
			height = (int)( ( (double)parentHeight - dispInfo.getStatusBarHeight() )
					/ iPageMaxCount ) - BLOCK_MARGIN_HORZ * 2;				
		}
		else
		{
			// １行に2つで上から順に並べる
			width =  (int) (parentWidth - BLOCK_MARGIN * 2) / 2;
			height = (int)( ( (double)parentHeight - dispInfo.getStatusBarHeight() )
					/ ( iPageMaxCount / 2 ) ) - BLOCK_MARGIN_HORZ * 2;			
		}
			
		lpThis = dispInfo.createLayoutParamForNoPosOnBk(
				width,
				height,
				false );
		lpThis.setMargins(BLOCK_MARGIN, BLOCK_MARGIN_HORZ, BLOCK_MARGIN, BLOCK_MARGIN_HORZ);			

		setLayoutParams(lpThis);
		// setClickable(true);
		// mActivity.registerForContextMenu(this);
		
		// --------------------
		// 前のラベルを作成
		// --------------------
		TextView txtBeforeLabel = new TextView(this.getContext());
		txtBeforeLabel.setId(LABEL_ID_1);
		txtBeforeLabel.setTextColor(Color.argb(0xAA, 255, 255, 255));
		RelativeLayout.LayoutParams lpTmp = null;
		lpTmp = new RelativeLayout.LayoutParams(
				width / 5, //android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT );
		// テキストサイズの調整
		int iLabelLineCnt=1;
		if( PROPER_LABEL_TEXT_LENGTH < columnData.getLabelBefore().length() )
		{
			iLabelLineCnt = 2;
			txtBeforeLabel.setSingleLine(false);
		}
		else
		{
			txtBeforeLabel.setSingleLine(true);
		}
		float maxTextSize = getProperTextSize(width/5-ITEM_PADDING*2,
				height,iLabelLineCnt,columnData.getLabelBefore());
		txtBeforeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
		lpTmp.addRule(ALIGN_PARENT_LEFT);
		lpTmp.addRule(ALIGN_PARENT_TOP);
		lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
		txtBeforeLabel.setLayoutParams(lpTmp);
		txtBeforeLabel.setText( columnData.getLabelBefore() );
		addView( txtBeforeLabel );

		// --------------------
		// 後ろのラベルがあれば、それを作成
		// --------------------
		TextView txtAfterLabel = null;
		if( columnData.getLabelAfter() != null 
		&& 0 < columnData.getLabelBefore().length()	)
		{
			txtAfterLabel = new TextView(this.getContext());
			
			txtAfterLabel.setId(LABEL_ID_2);
			txtAfterLabel.setTextColor(Color.argb(0xAA, 255, 255, 255));
			lpTmp = new RelativeLayout.LayoutParams(
					width / 6, //android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT );
			// テキストサイズの調整
			iLabelLineCnt=1;
			if( PROPER_LABEL_TEXT_LENGTH < columnData.getLabelAfter().length() )
			{
				iLabelLineCnt = 2;
				txtAfterLabel.setSingleLine(false);
			}
			else
			{
				txtAfterLabel.setSingleLine(true);
			}
			maxTextSize = getProperTextSize(width/6-ITEM_PADDING*2,
					height,iLabelLineCnt,columnData.getLabelAfter());
			txtAfterLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,maxTextSize);
			lpTmp.addRule(ALIGN_PARENT_RIGHT);
			lpTmp.addRule(ALIGN_PARENT_TOP);
			lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
			txtAfterLabel.setLayoutParams(lpTmp);
			txtAfterLabel.setText( columnData.getLabelAfter() );
			addView( txtAfterLabel );
		}
		// --------------------
		// Editを作成
		// --------------------
		edit = new EditText(this.getContext());
		edit.setId(EDIT_ID);
		//edit.setTextColor(Color.argb(0xAA, 255, 255, 255));
		//RelativeLayout.LayoutParams lpTmp = null;
		lpTmp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT );
		edit.setSingleLine(true);
		lpTmp.addRule(RIGHT_OF,LABEL_ID_1);
		if( txtAfterLabel != null )
		{
			lpTmp.addRule(LEFT_OF,LABEL_ID_2);
		}
		//lpTmp.addRule(CENTER_VERTICAL);
		lpTmp.setMargins(ITEM_LEFT_MARGIN, 0, 0, 0);
		edit.setLayoutParams(lpTmp);
		edit.setText( columnData.getText() );
		edit.setHint( columnData.getHint() );
		addView( edit );
	}	
}
