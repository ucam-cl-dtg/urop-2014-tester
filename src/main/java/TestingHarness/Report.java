package TestingHarness;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

//front end will ALWAYS need to check the status first
public class Report {

	private List<sReportItem> sReport;
	private List<dReportItem> dReport;
	private String reportStatus;
	// TODO: private String result;
	
	//constructor if all tests finished normally
	public Report(List<sReportItem> sReport, List<dReportItem> dReport) {
		this.sReport = sReport;
		Collections.sort(sReport);
		this.dReport = dReport;
		this.reportStatus = "complete";
	}

	//constructor if tests were force exited due to an error
	public Report(String error) {
		this.reportStatus = error;
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
}
