package app.guchagucharr.guchagucharunrecorder.util;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import app.guchagucharr.guchagucharunrecorder.GGRRPreferenceActivity;
import app.guchagucharr.guchagucharunrecorder.R;
import app.guchagucharr.service.LapData;
import app.guchagucharr.service.SQLiteContract;

public class ColumnData {
	Context ctx = null;
	boolean hidden = false;
	boolean editable = true;
	String columnName = null;
	String labelBefore = null;
	String labelAfter = null;
	String dataType = SQLiteContract.NONE;
	String text = null;
	String hint = null;
	int itemValueControlID = -1;
	public static final int EDIT_METHDO_TEXT = 0;
	public static final int EDIT_METHDO_REAL = 1;
	public static final int EDIT_METHDO_INTEGER = 2;
	public static final int EDIT_METHDO_DATETIME = 3;
	public static final int EDIT_METHDO_TIME = 4;
	public static final int EDIT_METHDO_DISTANCE = 5;
	public static final int EDIT_METHDO_SPEED = 6;
	public static final int EDIT_METHDO_ACTIVITY_TYPE = 7;
	int editMethod = EDIT_METHDO_TEXT;
	public int getEditMethod()
	{
		return editMethod;
	}
	public void setEditMethod(int editMethod_)
	{
		editMethod = editMethod_;
	}
	public ColumnData(
			Context context, 
			boolean hidden, 
			boolean editable,
			String columnName,
			Integer labelBefore,
			Integer labelAfter, 
			String dataType, 
			String text, 
			Integer hint) {
		super();
		this.ctx = context;
		this.hidden = hidden;
		this.editable = editable;
		this.columnName = columnName;
		if( labelBefore != null )
			this.labelBefore = ctx.getString(labelBefore);
		if( labelAfter != null )
			this.labelAfter = ctx.getString(labelAfter);
		this.dataType = dataType;
		this.text = text;
		if( hint != null )
			this.hint = ctx.getString(hint);
	}
	public ColumnData(Context context, boolean editable,
			String columnName,
			Integer labelBefore,
			Integer labelAfter, 
			String dataType, 
			String text, 
			Integer hint
		) {

		super();
		this.ctx = context;		
		this.hidden = false;
		this.editable = editable;
		this.columnName = columnName;
		if( labelBefore != null )
			this.labelBefore = ctx.getString(labelBefore);
		if( labelAfter != null )
			this.labelAfter = ctx.getString(labelAfter);
		this.dataType = dataType;
		this.text = text;
		if( hint != null )
			this.hint = ctx.getString(hint);
	}
	
	public ColumnData(Context context, boolean editable,
			String columnName,
			Integer labelBefore,
			Integer labelAfter, 
			String dataType, 
			String text, 
			Integer hint,
			int iEditMethod) {

		super();
		this.ctx = context;		
		this.hidden = false;
		this.editable = editable;
		this.columnName = columnName;
		if( labelBefore != null )
			this.labelBefore = ctx.getString(labelBefore);
		if( labelAfter != null )
			this.labelAfter = ctx.getString(labelAfter);
		this.dataType = dataType;
		this.text = text;
		if( hint != null )
			this.hint = ctx.getString(hint);
		this.editMethod = iEditMethod;
	}
	public ColumnData(Context context,
			String columnName,
			Integer labelBefore,
			Integer labelAfter, 
			String dataType, 
			String text, 
			Integer hint) {

		super();
		this.ctx = context;		
		this.hidden = false;
		this.editable = true;
		this.columnName = columnName;		
		if( labelBefore != null )
			this.labelBefore = ctx.getString(labelBefore);
		if( labelAfter != null )
			this.labelAfter = ctx.getString(labelAfter);
		this.dataType = dataType;
		this.text = text;
		if( hint != null )
			this.hint = ctx.getString(hint);
	}
	
	public ColumnData(Context context,
			String columnName,
			Integer labelBefore,
			Integer labelAfter, 
			String dataType, 
			String text, 
			Integer hint,
			int iEditMethod) {

		super();
		this.ctx = context;		
		this.hidden = false;
		this.editable = true;
		this.columnName = columnName;
		if( labelBefore != null )
			this.labelBefore = ctx.getString(labelBefore);
		if( labelAfter != null )
			this.labelAfter = ctx.getString(labelAfter);
		this.dataType = dataType;
		this.text = text;
		if( hint != null )
			this.hint = ctx.getString(hint);
		this.editMethod = iEditMethod;
	}
	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	/**
	 * @return the labelBefore
	 */
	public String getLabelBefore() {
		return labelBefore;
	}
	/**
	 * @param labelBefore the labelBefore to set
	 */
	public void setLabelBefore(String labelBefore) {
		this.labelBefore = labelBefore;
	}
	/**
	 * @return the labelAfter
	 */
	public String getLabelAfter() {
		return labelAfter;
	}
	/**
	 * @param labelAfter the labelAfter to set
	 */
	public void setLabelAfter(String labelAfter) {
		this.labelAfter = labelAfter;
	}
	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}
	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}
	/**
	 * @param hint the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}
	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}
	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	/**
	 * @return the itemValueControlID
	 */
	public int getItemValueControlID() {
		return itemValueControlID;
	}
	/**
	 * @param itemValueControlID the itemValueControlID to set
	 */
	public void setItemValueControlID(int itemValueControlID) {
		this.itemValueControlID = itemValueControlID;
	}
	
	public static String getFormatText( Context ctx,int iEditMethod, String text )
	{
		if( iEditMethod == ColumnData.EDIT_METHDO_DATETIME )
		{
			SimpleDateFormat sdfDateTime = new SimpleDateFormat(
					ctx.getString(R.string.datetime_display_format_full));
			return sdfDateTime.format(Long.parseLong(text) );
		}
		else if( iEditMethod == ColumnData.EDIT_METHDO_TIME )
		{
//			SimpleDateFormat sdfDateTime = new SimpleDateFormat(
//					ctx.getString(R.string.time_display_format));
			return UnitConversions.getWorkoutTimeString(ctx,Long.parseLong(text));
		}
		else if( iEditMethod == ColumnData.EDIT_METHDO_DISTANCE )
		{
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//			int currentUnit = pref.getInt(GGRRPreferenceActivity.DISTANCE_UNIT_KEY,
//					UnitConversions.DISTANCE_UNIT_KILOMETER);
			int currentUnit = Integer.valueOf(pref.getString(GGRRPreferenceActivity.DISTANCE_UNIT_KEY,
					String.valueOf( UnitConversions.DISTANCE_UNIT_KILOMETER ) ) );
			
			return LapData.createDistanceFormatText( currentUnit, Double.parseDouble(text) );
		}
		else if( iEditMethod == ColumnData.EDIT_METHDO_SPEED )
		{
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
			int currentUnit = Integer.valueOf(pref.getString(GGRRPreferenceActivity.DISTANCE_UNIT_KEY,
					String.valueOf( UnitConversions.DISTANCE_UNIT_KILOMETER ) ) );

			return LapData.createSpeedFormatTextKmPerH( currentUnit, Double.parseDouble(text) );
		}
		return text;
	}
}
