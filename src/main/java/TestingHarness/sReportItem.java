package TestingHarness;

import java.util.LinkedList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class sReportItem implements Comparable<sReportItem>{
	private String severity;
	private String fileName;
	private List<Integer> lineNumberList = new LinkedList<Integer>();
	private String problem;
	private String detail;
	

	public sReportItem(String severity, String fileName, int lineNo, String problem, String detail) {
		this.severity = severity;
		this.fileName = fileName;
		this.lineNumberList.add(lineNo);
		this.problem = problem;
		this.detail  = detail;
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
	
	public int compareTo(sReportItem reportItem2) {
		Collections.sort(this.getLineNumbers());
		Collections.sort(reportItem2.getLineNumbers());
		if (this.getFileName().compareTo(reportItem2.getFileName()) == 0) {
			if (this.getSeverity().equals("error") & reportItem2.getSeverity().equals("warning")) {
				return -1;
			}
			else if (this.getSeverity().equals("warning") & reportItem2.getSeverity().equals("error")) {
				return 1;
			}
			else {
				if (this.getLineNumbers().get(0) - reportItem2.getLineNumbers().get(0) == 0) {
					return this.getProblem().compareTo(reportItem2.getProblem());
				}
				else {
					return this.getLineNumbers().get(0) - reportItem2.getLineNumbers().get(0);
				}
			}
		}
		else {
			return this.getFileName().compareTo(reportItem2.getFileName());
		}
	}

	public void addErrorAtLine(int lineNo) {
		this.lineNumberList.add(lineNo);
	}
}
