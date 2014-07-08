package TestingHarness;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class Report {

	private List<sReportItem> sReport;
	private List<dReportItem> dReport;
	
	public Report(List<sReportItem> sReport, List<dReportItem> dReport) {
		this.sReport = sReport;
		Collections.sort(sReport);
		this.dReport = dReport;
	}
	
	public List<sReportItem> getStaticResults() {
		return this.sReport;
	}
	
	public List<dReportItem> getDynamicResults() {
		return this.dReport;
	}
}
