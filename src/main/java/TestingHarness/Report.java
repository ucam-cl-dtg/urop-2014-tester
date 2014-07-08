package TestingHarness;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

//front end will ALWAYS need to check the status first
public class Report {

	private List<sReportItem> sReport;
	private List<dReportItem> dReport;
	private String reportStatus;
	private String result;
	
	//constructor if all tests finished normally
	public Report(List<sReportItem> sReport, List<dReportItem> dReport) {
		this.sReport = sReport;
		Collections.sort(sReport);
		for (sReportItem s : sReport) {
			if (s.getSeverity().equals("error")) {
				this.result = "FAIL";
			}
		}
		this.dReport = dReport;
		this.reportStatus = "complete";
		if(this.result == null) {
			this.result = "PASS";
		}
	}

	//constructor if tests were force exited due to an error
	public Report(String error) {
		this.reportStatus = error;
		this.result = "undetermined";
	}
	
	public List<sReportItem> getStaticResults() {
		return this.sReport;
	}
	
	public List<dReportItem> getDynamicResults() {
		return this.dReport;
	}
	
	public String getReportStatus() {
		return this.reportStatus;
	}
	
	public String getResult() {
		return this.result;
	}
}
