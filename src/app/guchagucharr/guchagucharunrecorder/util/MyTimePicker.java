package app.guchagucharr.guchagucharunrecorder.util;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.guchagucharr.guchagucharunrecorder.R;


//import com.android.internal.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode. The
 * hour, each minute digit, and AM/PM (if applicable) can be conrolled by
 * vertical spinners. The hour can be entered by keyboard input. Entering in two
 * digit hours can be accomplished by hitting two digits within a timeout of
 * about a second (e.g. '1' then '2' to select 12). The minutes can be entered
 * by entering single digits. Under AM/PM mode, the user can hit 'a', 'A", 'p'
 * or 'P' to pick. For a dialog using this view, see
 * {@link android.app.MyTimePickerDialog}.
 *<p>
 * See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.
 * </p>
 */
public class MyTimePicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;

    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(MyTimePicker view, int hourOfDay, int minute, int second) {
        }
    };

    private Context mContext;
    
    // state
    private boolean mIs24HourView;
    private boolean mIsTimeInputView = false;

    private boolean mIsAm;

    // ui components
    private final NumberPicker mHourSpinner;

    private final NumberPicker mMinuteSpinner;

    private final NumberPicker mSecondSpinner;

    //private final NumberPicker mAmPmSpinner;

    private final EditText mHourSpinnerInput;

    private final EditText mMinuteSpinnerInput;

    private final EditText mSecondSpinnerInput;

    //private final EditText mAmPmSpinnerInput;

//    private final TextView mDivider;
//    private final TextView mDividerSecond;

    // Note that the legacy implementation of the TimePicker is
    // using a button for toggling between AM/PM while the new
    // version uses a NumberPicker spinner. Therefore the code
    // accommodates these two cases to be backwards compatible.
    private final Button mAmPmButton;

    // private final String[] mAmPmStrings;
    private final int[] mAmPmBackgroundImage = {
        	R.drawable.selector_am_button_image,
        	R.drawable.selector_pm_button_image
    };

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(MyTimePicker view, int hourOfDay, int minute, int second);
    }

    public MyTimePicker(Context context) {
        this(context, null);
    }

    public MyTimePicker(Context context, AttributeSet attrs) {
    	this(context, attrs, R.attr.timePickerStyle);
    }

    public MyTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;
        
        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        // process style attributes
        int[] attrTimePicker = R.styleable.TimePicker;
        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs,
                attrTimePicker,
                //R.styleable.TimePicker
                defStyle, 0);
        int layoutResourceId = attributesArray.getResourceId(
        		R.styleable.TimePicker_internalLayout, 
        		R.layout.time_picker );
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(layoutResourceId, this, true);
        ViewGroup topLayout = (ViewGroup)v; 
        
        //LinearLayout parentLayout = (LinearLayout) ((LinearLayout) topLayout.getChildAt(0));
//        if( parentLayout.getChildAt(0) != null
//        && parentLayout.getChildAt(0) instanceof LinearLayout )
//        {
//        	parentLayout = (LinearLayout) parentLayout.getChildAt(0);
//        }
        // hour
        mHourSpinner = (NumberPicker) topLayout.findViewById(R.id.hour);//Resources.getSystem().getIdentifier("hour","id",null));//R.id.hour);
        // 親を再度設定し直す
        //parentLayout = (LinearLayout) mHourSpinner.getParent();
        mHourSpinner.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                if (isAmPmNeed()) {//!is24HourView()) {
                    if ((oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY)
                            || (oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1)) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                }
                onTimeChanged();
            }
        });
        mHourSpinnerInput = (EditText) mHourSpinner.findViewById(R.id.numberpicker_input);
        //Resources.getSystem().getIdentifier("numberpicker_input","id",null));//
        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
//        mDivider = (TextView) findViewById(R.id.divider);//Resources.getSystem().getIdentifier("divider","id","android"));//
//        if (mDivider != null) {
//            mDivider.setText(R.string.time_picker_separator);//Resources.getSystem().getIdentifier("time_picker_separator","string","android"));//
//        }

        // minute
        mMinuteSpinner = (NumberPicker) topLayout.findViewById(
        		//Resources.getSystem().getIdentifier("minute", "id", null) );
        		R.id.minute);
        mMinuteSpinner.setRange(0,59);
//        mMinuteSpinner.setMinValue(0);
//        mMinuteSpinner.setMaxValue(59);
        //mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(TWO_DIGIT_FORMATTER);//NumberPicker.getTwoDigitFormatter());
        mMinuteSpinner.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mMinuteSpinner.getMinValue();
                int maxValue = mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourSpinner.getCurrent() + 1;
                    if (isAmPmNeed() && newHour == HOURS_IN_HALF_DAY) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setCurrent(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourSpinner.getCurrent() - 1;
                    if (isAmPmNeed() && newHour == HOURS_IN_HALF_DAY - 1) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setCurrent(newHour);
                }
                onTimeChanged();
            }
        });
        mMinuteSpinnerInput = (EditText) mMinuteSpinner.findViewById(
        		//Resources.getSystem().getIdentifier("numberpicker_input", "id", null )
        		//);
        		R.id.numberpicker_input);
        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // Second (My original)
        // divider (only for the new widget style)
//        mDividerSecond = new TextView( mContext );// (TextView) findViewById(Resources.getSystem().getIdentifier("id","divider",null));
//        mDividerSecond.setLayoutParams(mDivider.getLayoutParams());
//        mDividerSecond.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
//        if (mDividerSecond != null) {
//        	mDividerSecond.setText(//Resources.getSystem().getIdentifier("time_picker_separator","string",null));
//        			R.string.time_picker_separator);
//        }
//        parentLayout.addView(mDividerSecond);

        mSecondSpinner = (NumberPicker) topLayout.findViewById(
        		//Resources.getSystem().getIdentifier("minute", "id", null) );
        		R.id.second); 
        		//new NumberPicker( mContext );
        mSecondSpinner.setRange(0,59);
        //mSecondSpinner.setLayoutParams( mMinuteSpinner.getLayoutParams() );
//        mSecondSpinner.setMinValue(0);
//        mSecondSpinner.setMaxValue(59);
//        mSecondSpinner.setOnLongPressUpdateInterval(100);
        mSecondSpinner.setFormatter(TWO_DIGIT_FORMATTER);//NumberPicker.getTwoDigitFormatter());
        mSecondSpinner.setOnChangeListener(new NumberPicker.OnChangedListener() {
            public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mSecondSpinner.getMinValue();
                int maxValue = mSecondSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newMinute = mMinuteSpinner.getCurrent() + 1;
                    mMinuteSpinner.setCurrent(newMinute);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mMinuteSpinner.getCurrent() - 1;
                    mMinuteSpinner.setCurrent(newHour);
                }
                onTimeChanged();
            }
        });
        //topLayout.addView(mSecondSpinner);
        
        mSecondSpinnerInput = (EditText) mSecondSpinner.findViewById(
        		//Resources.getSystem().getIdentifier("numberpicker_input", "id", null )
        		//);
        		R.id.numberpicker_input);
        mSecondSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        
        
        /* Get the localized am/pm strings and use them in the spinner */
        //mAmPmStrings = new DateFormatSymbols().getAmPmStrings();

        //mAmPmBackgroundImage = 
        
        // am/pm
        View amPmView = findViewById(
//        		Resources.getSystem().getIdentifier("amPm", "id", null)
//        		);
        		R.id.amPm);
//        if (amPmView instanceof Button) {
//            mAmPmSpinner = null;
//            mAmPmSpinnerInput = null;
        mAmPmButton = (Button) amPmView;
        mAmPmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View button) {
                button.requestFocus();
                mIsAm = !mIsAm;
                updateAmPmControl();
                onTimeChanged();
            }
        });
//        } else {
//            mAmPmButton = null;
//            mAmPmSpinner = (NumberPicker) amPmView;
//            mAmPmSpinner.setRange(0,1);
////            mAmPmSpinner.setMinValue(0);
////            mAmPmSpinner.setMaxValue(1);
//            //mAmPmSpinner.setDisplayedValues(mAmPmStrings);
//            mAmPmSpinner.setOnChangeListener(new NumberPicker.OnChangedListener() {
//                public void onChanged(NumberPicker picker, int oldVal, int newVal) {
//                    updateInputState();
//                    picker.requestFocus();
//                    mIsAm = !mIsAm;
//                    updateAmPmControl();
//                    onTimeChanged();
//                }
//            });
//            mAmPmSpinnerInput = (EditText) mAmPmSpinner.findViewById(
////            		Resources.getSystem().getIdentifier("numberpicker_input", "id", null)
////            		);
//            		R.id.numberpicker_input);
//            mAmPmSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        }

        // update controls to initial state
        updateHourControl();
        updateAmPmControl();

        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));
        setCurrentSecond(mTempCalendar.get(Calendar.SECOND));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
//        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
//            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
//        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
//        if (mDivider != null) {
//            mDivider.setEnabled(enabled);
//        }
        mHourSpinner.setEnabled(enabled);
//        if (mAmPmSpinner != null) {
//            mAmPmSpinner.setEnabled(enabled);
//        } else {
        mAmPmButton.setEnabled(enabled);
        //}
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;

        private final int mMinute;

        private final int mSecond;
        
        private SavedState(Parcelable superState, int hour, int minute, int second) {
            super(superState);
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        public int getSecond() {
            return mSecond;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }

        @SuppressWarnings({"unused", "hiding"})
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        int currentHour = mHourSpinner.getCurrent();
        if (false == isAmPmNeed()) {
            return currentHour;
        } else if (mIsAm) {
            return currentHour % HOURS_IN_HALF_DAY;
        } else {
            return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        if (isAmPmNeed()) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourSpinner.setCurrent(currentHour);
        onTimeChanged();
    }
    public void setIsTimeInputView(Boolean isTimeInputView) {
        if (mIsTimeInputView == isTimeInputView) {
            return;
        }
    	mIsTimeInputView = isTimeInputView;
        // cache the current hour since spinner range changes
        int currentHour = getCurrentHour();
        updateHourControl();
        // set value after spinner range is updated
        setCurrentHour(currentHour);
        updateAmPmControl();
    	
    }
    public boolean isTimeInputView() {
    	return mIsTimeInputView;
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        // cache the current hour since spinner range changes
        int currentHour = getCurrentHour();
        updateHourControl();
        // set value after spinner range is updated
        setCurrentHour(currentHour);
        updateAmPmControl();
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }
    public boolean isAmPmNeed()
    {
    	return (false == is24HourView() && false == isTimeInputView()); 
    }
    
    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteSpinner.getCurrent();
    }
    /**
     * @return The current second.
     */
    public Integer getCurrentSecond() {
        return mSecondSpinner.getCurrent();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setCurrent(currentMinute);
        onTimeChanged();
    }
    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == getCurrentSecond()) {
            return;
        }
        mSecondSpinner.setCurrent(currentSecond);
        onTimeChanged();
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

//    @Override
//    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
//        onPopulateAccessibilityEvent(event);
//        return true;
//    }

//    @Override
//    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
//        super.onPopulateAccessibilityEvent(event);
//
//        int flags = DateUtils.FORMAT_SHOW_TIME;
//        if (mIs24HourView) {
//            flags |= DateUtils.FORMAT_24HOUR;
//        } else {
//            flags |= DateUtils.FORMAT_12HOUR;
//        }
//        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
//        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
//        mTempCalendar.set(Calendar.SECOND, getCurrentSecond());
//        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
//                mTempCalendar.getTimeInMillis(), flags);
//        event.getText().add(selectedDateUtterance);
//    }

//    @Override
//    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
//        super.onInitializeAccessibilityEvent(event);
//        event.setClassName(MyTimePicker.class.getName());
//    }
//
//    @Override
//    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
//        super.onInitializeAccessibilityNodeInfo(info);
//        info.setClassName(MyTimePicker.class.getName());
//    }

    private void updateHourControl() {
    	if( isTimeInputView() )
    	{
            mHourSpinner.setRange(0,9999);
            //mHourSpinner.setFormatter(TWO_DIGIT_FORMATTER);
    	}
    	else if (is24HourView()) {
            mHourSpinner.setRange(0,23);
//            mHourSpinner.setMinValue(0);
//            mHourSpinner.setMaxValue(23);
            mHourSpinner.setFormatter(TWO_DIGIT_FORMATTER);//NumberPicker.getTwoDigitFormatter());
        } else {
            mHourSpinner.setRange(1,12);
//            mHourSpinner.setMinValue(1);
//            mHourSpinner.setMaxValue(12);
            mHourSpinner.setFormatter(null);
        }
    }

    private void updateAmPmControl() {
        if (false == isAmPmNeed()){//is24HourView()||isTimeInputView() ) {
//            if (mAmPmSpinner != null) {
//                mAmPmSpinner.setVisibility(View.GONE);
//            } else {
              mAmPmButton.setVisibility(View.GONE);
//            }
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
//            if (mAmPmSpinner != null) {
//                mAmPmSpinner.setCurrent(index);
//                mAmPmSpinner.setVisibility(View.VISIBLE);
//            } else {
            mAmPmButton.setBackgroundResource(mAmPmBackgroundImage[index]);
            mAmPmButton.setVisibility(View.VISIBLE);
//            }
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
        }
    }

    private void setContentDescriptions() {
        // Minute
        trySetContentDescription(mMinuteSpinner,
        		//Resources.getSystem().getIdentifier("increment", "id", null ),
        		R.id.increment,
        		//Resources.getSystem().getIdentifier("time_picker_increment_minute_button", "string", null ));
                R.string.time_picker_increment_minute_button);
        trySetContentDescription(mMinuteSpinner,
        		//Resources.getSystem().getIdentifier("decrement", "id", null ),
        		R.id.decrement,
        		//Resources.getSystem().getIdentifier("time_picker_decrement_minute_button", "string", null ) );
                R.string.time_picker_decrement_minute_button);
        // Hour
        trySetContentDescription(mHourSpinner,
        		//Resources.getSystem().getIdentifier("increment", "id", null ),
        		R.id.increment,
        		//Resources.getSystem().getIdentifier("time_picker_increment_hour_button", "string", null ));
                R.string.time_picker_increment_hour_button);
        trySetContentDescription(mHourSpinner, 
        		//Resources.getSystem().getIdentifier("decrement", "id", null ),        		
        		R.id.decrement,
        		//Resources.getSystem().getIdentifier("time_picker_decrement_minute_button", "string", null ) );        		
                R.string.time_picker_decrement_hour_button);
        // AM/PM
//        if (mAmPmSpinner != null) {
//            trySetContentDescription(mAmPmSpinner,
//            		//Resources.getSystem().getIdentifier("increment", "id", null ),
//            		R.id.increment,
//            		//Resources.getSystem().getIdentifier("time_picker_increment_set_pm_button", "string", null ) );            		
//                    R.string.time_picker_increment_set_pm_button);
//            trySetContentDescription(mAmPmSpinner,
//            		//Resources.getSystem().getIdentifier("decrement", "id", null ),            		
//            		R.id.decrement,
//            		//Resources.getSystem().getIdentifier("time_picker_decrement_set_am_button", "string", null ) );            		
//                    R.string.time_picker_decrement_set_am_button);
//        }
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(mContext.getString(contDescResId));
        }
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        InputMethodManager inputMethodManager 
        = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        //InputMethodManager.peekInstance();
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourSpinnerInput)) {
                mHourSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mSecondSpinnerInput)) {
            	mSecondSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } 
//            else if (inputMethodManager.isActive(mAmPmSpinnerInput)) {
//                mAmPmSpinnerInput.clearFocus();
//                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
//            }
        }
    }
    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    /*
     * 数値2桁のためのFormetterインタフェース実装クラスを提供します
     */
    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
            new NumberPicker.Formatter() {
                final StringBuilder mBuilder = new StringBuilder();
                final java.util.Formatter mFmt = new java.util.Formatter(mBuilder);
                final Object[] mArgs = new Object[1];
                public String toString(int value) {
                    mArgs[0] = value;
                    mBuilder.delete(0, mBuilder.length());
                    mFmt.format("%02d", mArgs);
                    return mFmt.toString();
                }
        };

    //private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();


}
