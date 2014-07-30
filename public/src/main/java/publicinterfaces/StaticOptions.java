package publicinterfaces;

/**
 * Used for transmitting static option data in a format that is easy to use with Polymer
 * @author as2388
 */
public class StaticOptions {
    private String text;      //problem category description
    private int checkedIndex; //for ignore, warning, or error

    public StaticOptions(String text, int checkedIndex) {
        this.text = text;
        this.checkedIndex = checkedIndex;
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
}
