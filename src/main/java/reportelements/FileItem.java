package reportelements;

/*
 * stores details of where a problem was found in a file and describes in more detail the problem found
 */

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
