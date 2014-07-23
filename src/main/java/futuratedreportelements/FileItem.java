package futuratedreportelements;


public class FileItem {
    private final Integer lineNumber; //Integer not int because we need the option for it to be null
    private final String detail;

    protected FileItem(int lineNumber, String detail) {
        this.lineNumber = lineNumber;
        this.detail = detail;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDetail() {
        return detail;
    }
}
