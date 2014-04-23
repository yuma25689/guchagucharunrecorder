package app.guchagucharr.guchagucharunrecorder.util;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class MyTimePicker extends FrameLayout {
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnSecondChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(MyTimePicker view, int second);
    }

	private Context mContext;
	private TimePicker mTimePickerOrg; 
	
    private TextView mDivider = null;
    private NumberPicker mMinuteSpinner = null;
    private NumberPicker mSecondSpinner = null;
    private EditText mSecondSpinnerInput = null;
    private TextView mDividerSecond = null;
    // callbacks 
    private OnSecondChangedListener mOnTimeChangedListener;
    public MyTimePicker(Context context) {
        this(context, null);
    }	
    public MyTimePicker(Context context, AttributeSet attrs) {
    	this(context, attrs, 0);//Resources.getSystem().getIdentifier("timePickerStyle", "attr", "android"));
    }
	public MyTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mTimePickerOrg = new TimePicker(mContext);//, attrs, defStyle);
		
		this.addView(mTimePickerOrg);
        // ネットで参照した
        // platform_frameworks_base / core / res / res / layout / time_picker_holo.xml
        // では、これでNumberPickerの親のLinearLayoutが取れるはず
//		int idTopView = Resources.getSystem().getIdentifier("timePickerLayout","id","android");
//        LinearLayout topLayout = 
//        		(LinearLayout) mTimePickerOrg.findViewById(idTopView);
        
        LinearLayout parentLayout = (LinearLayout) mTimePickerOrg.getChildAt(0);//(LinearLayout) topLayout.getChildAt(0);
		
        int iSpinnerCount = 0;
        for( int i=0; i<parentLayout.getChildCount();i++ )
        {
        	if( parentLayout.getChildAt(i) instanceof ViewGroup )
        	{
        		ViewGroup vg = (ViewGroup) parentLayout.getChildAt(i);
        		for( int j=0; j< vg.getChildCount(); j++)
        		{
                	if( vg.getChildAt(i) instanceof NumberPicker )
                	{
                		if(iSpinnerCount != 0 )
                		{
                			if( mMinuteSpinner == null )
                				mMinuteSpinner = (NumberPicker) parentLayout.getChildAt(i);
                		}
                		else
                		{
                			iSpinnerCount++;        			
                		}
                	}
        			
        		}
        	}
        	
        	if( parentLayout.getChildAt(i) instanceof NumberPicker )
        	{
        		if(iSpinnerCount != 0 )
        		{
        			if( mMinuteSpinner == null )
        				mMinuteSpinner = (NumberPicker) parentLayout.getChildAt(i);
        		}
        		else
        		{
        			iSpinnerCount++;        			
        		}
        	}
        	else if( parentLayout.getChildAt(i) instanceof TextView )
        	{
        		mDivider = (TextView) parentLayout.getChildAt(i);
        	}
        	if( mMinuteSpinner != null && mDivider != null )
        	{
        		break;
        	}
        }
        
        // IDが取れない・・・(TT)
//		int idDiv = Resources.getSystem().getIdentifier(
//        		"divider","id","android");
//        mDivider = (TextView) mTimePickerOrg.findViewById(idDiv);//R.id.divider);
        // minute
//        int idMinute = Resources.getSystem().getIdentifier("minute", "id", "android");
//        mMinuteSpinner = (NumberPicker) mTimePickerOrg.findViewById(
//        		idMinute );
        // Second (My original)
        // divider (only for the new widget style)
        mDividerSecond = new TextView( mContext );// (TextView) findViewById(Resources.getSystem().getIdentifier("id","divider",null));
        mDividerSecond.setLayoutParams(mDivider.getLayoutParams());
        //mDividerSecond.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
//        if (mDividerSecond != null) {
//        	mDividerSecond.setText(Resources.getSystem().getIdentifier("time_picker_separator","string","android"));//R.string.time_picker_separator);
//        }
        parentLayout.addView(mDividerSecond);

        mSecondSpinner = new NumberPicker( mContext );
        mSecondSpinner.setMinValue(0);
        mSecondSpinner.setMaxValue(59);
        mSecondSpinner.setOnLongPressUpdateInterval(100);
        mSecondSpinner.setFormatter(sTwoDigitFormatter);//NumberPicker.getTwoDigitFormatter());
        mSecondSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mSecondSpinner.getMinValue();
                int maxValue = mSecondSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newMinute = mMinuteSpinner.getValue() + 1;
                    mMinuteSpinner.setValue(newMinute);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mMinuteSpinner.getValue() - 1;
                    mMinuteSpinner.setValue(newHour);
                }
                onTimeChanged();
            }
        });
        parentLayout.addView(mSecondSpinner);
        
        
        mSecondSpinnerInput = (EditText) mSecondSpinner.findViewById(
        		Resources.getSystem().getIdentifier("numberpicker_input", "id", null )
        		);
        		//R.id.numberpicker_input);
        mSecondSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
               
	}
    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        //InputMethodManager.peekInstance();
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mSecondSpinnerInput)) {
                mSecondSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
	
    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    private static class TwoDigitFormatter implements NumberPicker.Formatter {
        final StringBuilder mBuilder = new StringBuilder();

        Locale mLastLocale = null;
        //char mZeroDigit;
        java.util.Formatter mFmt;

        final Object[] mArgs = new Object[1];

        TwoDigitFormatter() {
            final Locale locale = Locale.getDefault();
            init(locale);
        }

        private void init(Locale locale) {
            mFmt = createFormatter(locale);
            mLastLocale = locale;
            //mZeroDigit = getZeroDigit(locale);
        }

        public String format(int value) {
            final Locale currentLocale = Locale.getDefault();
            //if (mZeroDigit != getZeroDigit(currentLocale)) {
            if (mLastLocale == currentLocale) {
                init(currentLocale);
            }
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }

//        private static char getZeroDigit(Locale locale) {
//            //return LocaleData.get(locale).zeroDigit;
//        }

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(mBuilder, locale);
        }
    }

    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();
    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentSecond());
        }
    }
    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
    	return mTimePickerOrg.getCurrentHour();
    }    
    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentMinute() {
    	return mTimePickerOrg.getCurrentMinute();
    }    
    /**
     * @return The current second.
     */
    public Integer getCurrentSecond() {
        return mSecondSpinner.getValue();
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == getCurrentSecond()) {
            return;
        }
        mSecondSpinner.setValue(currentSecond);
        onTimeChanged();
    }
    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnSecondChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }
    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnSecondChangedListener NO_OP_CHANGE_LISTENER = new OnSecondChangedListener() {
        public void onTimeChanged(MyTimePicker view, int second) {
        }
    };

}
