package publicinterfaces;

/*
 * stores details of where a problem was found in a file and describes in more detail the problem found
 */

public class FileItem {
    private Integer lineNumber; //Integer not int because we need the option for it to be null
    private String detail;

    protected FileItem(int lineNumber, String detail) {
        this.lineNumber = lineNumber;
        this.detail = detail;
    }

    public FileItem() {}

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDetail() {
        return detail;
    }
}
