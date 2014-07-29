package publicinterfaces;

/*
 * stores details of where a problem was found in a file and describes in more detail the problem found
 */

public class FileItem {
    private Integer lineNumber; //Integer not int because we need the option for it to be null
    private String detail;
    private String file;
    
    protected FileItem(int lineNumber, String detail, String file) {
        this.lineNumber = lineNumber;
        this.detail = detail;
        this.file = file;
    }

    //for json
    public FileItem() {}

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDetail() {
        return detail;
    }

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	public void setLineNumber(int i) {
		this.lineNumber = i;
	}
	
	public void setDetail(String d) {
		this.detail = d;
	}
}
