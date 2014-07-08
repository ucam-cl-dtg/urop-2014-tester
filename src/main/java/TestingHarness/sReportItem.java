package TestingHarness;

import java.util.LinkedList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class sReportItem implements Comparable<sReportItem>{
	private String severity;
	private String fileName;
	private List<Integer> lineNumberList = new LinkedList<Integer>();
	private String problem;
	private String detail="";
	

	public sReportItem(String severity, String fileName, int lineNo, String message) {
		this.severity = severity;
		this.fileName = fileName;
		this.lineNumberList.add(lineNo);
		
		message=message.replaceAll("\'", "");
		//split the message into the two parts: more general problem type and more specific details (if the details exist)
		String[] messageParts = message.split(" ~ ");
		this.problem = messageParts[0];
		if (messageParts.length > 1) this.detail  = messageParts[1];
	}

	public String getSeverity() {
		return this.severity;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public List<Integer> getLineNumbers() {
		return this.lineNumberList;
	}
	
	public String getProblem() {
		return problem;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public String getMessage() {
		if (detail.equals(""))
		{
			return problem;
		}
		else
		{
			return problem + " (" + detail + ")";
		}
	}
	
	@Override
	public int compareTo(sReportItem reportItem2) {
		Collections.sort(this.getLineNumbers());
		Collections.sort(reportItem2.getLineNumbers());
		if (this.getFileName().compareTo(reportItem2.getFileName()) == 0) {
			return this.getLineNumbers().get(0) - reportItem2.getLineNumbers().get(0);
		}
		else {
			return this.getFileName().compareTo(reportItem2.getFileName());
		}
	}

	public void addErrorAtLine(int lineNo) {
		this.lineNumberList.add(lineNo);
	}
}
