package futuratedreportelements;


public class FileItem {
    private final int lineNumber;
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
