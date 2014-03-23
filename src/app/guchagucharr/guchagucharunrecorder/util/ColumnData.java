package app.guchagucharr.guchagucharunrecorder.util;

import app.guchagucharr.service.SQLiteContract;

public class ColumnData {
	String columnName;
	String labelBefore;
	String labelAfter;
	String dataType = SQLiteContract.NONE;
	String text;
	String hint;
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
	
}
