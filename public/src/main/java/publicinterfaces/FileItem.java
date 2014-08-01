package publicinterfaces;

/**
 * Stores the details of where to find a problem in a particular java file
 * @author as2388
 * @author kls82
 */
public class FileItem {
	// line where the problem has been found
    private Integer lineNumber; //Integer not int because we need the option for it to be null
    // String descibing the problem found in more detail, specific to this place
    private String detail;
    // java file where the problem has been found
    private String file;
    
    /**
     * Constructor used in code for FileItem
     * @param lineNumber  line where the problem was found
     * @param detail   detail specific to this particular instance of the problem
     * @param file   file where the problem was found
     */
    protected FileItem(int lineNumber, String detail, String file) {
        this.lineNumber = lineNumber;
        this.detail = detail;
        this.file = file;
    }
    
    // constuctors and getters/setters from here are for serializing json
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
