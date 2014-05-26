package app.guchagucharr.guchagucharunrecorder.util;

public class TextAndIcon {
	public TextAndIcon( int iconId_, String text_ )
	{
		text = text_;
		iconId = iconId_;
	}
	
	String text;
	int iconId;
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
	 * @return the iconId
	 */
	public int getIconId() {
		return iconId;
	}
	/**
	 * @param iconId the iconId to set
	 */
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	
}
