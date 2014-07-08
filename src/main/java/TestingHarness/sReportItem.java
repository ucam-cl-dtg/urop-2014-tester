package TestingHarness;

public class sReportItem {
	private String severity;
	private String fileName;
	private int lineNumber;
	private String message;
	

	public sReportItem(String severity, String fileName, int line, String message) {
		this.severity = severity;
		this.fileName = fileName;
		this.lineNumber = line;
		this.message = message;
	}


	public String getSeverity() {
		return this.severity;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getMessage() {
		return this.message;
	}
}
