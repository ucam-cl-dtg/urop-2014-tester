package publicinterfaces;

/**
 * Used for transmitting static option data in a format that is easy to use with Polymer
 * @author as2388
 */
public class StaticOptions {
    private String text;      //problem category description
    private int checkedIndex; //for ignore, warning, or error
    private String code; //xml code

    public StaticOptions(String text, int checkedIndex, String code) {
        this.text = text;
        this.checkedIndex = checkedIndex;
        this.code = code;
    }

    //For JSON serialisation
    public StaticOptions(){}

    public int getCheckedIndex() {
        return checkedIndex;
    }

    public String getText() {
        return text;
    }

    public void setCheckedIndex(int checkedIndex) {
        this.checkedIndex = checkedIndex;
    }

    public void setText(String text) {
        this.text = text;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
