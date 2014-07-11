package TestingHarness;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Stores all information about a report i.e. status, lists of errors, and the overall result
 * <p>
 * Users of this class should poll status to check when the report is complete
 * 
 * @author kls2510
 *
 */
public class Report {
	private List<sReportItem> sReport;	//list of static analysis report items
	private List<dReportItem> dReport;	//list of dynamic analysis report items
	private String reportStatus;		//TODO: should this be initialised to something?
	private String result;				//Overall PASS/FAIL/ERROR
	
	/**
	 * Constructor for a successfully completed report
	 * @param sReport	list of static report items to be inserted into the report
	 * @param dReport	list of dynamic report items to be inserted into the report
	 */
	public Report(List<sReportItem> sReport, List<dReportItem> dReport) {
		this.sReport = sReport;
		this.dReport = dReport;
		
		//Handle static reports
		Collections.sort(sReport);
		//If the severity of any problem found by CheckStyle is an error, then fail the student
		for (sReportItem s : sReport) {
			if (s.getSeverity().equals("error")) {
				this.result = "FAIL";
			}
		}
		
		//TODO: Handle dynamic reports
		

		if(this.result == null) {
			this.result = "PASS";	//TODO: is it ok to default to PASS?
		}
		
		//Change the status to complete (so any object polling status knows the report is ready 
		this.reportStatus = "complete";
	}

	/**
	 * Constructor for a report which could not complete
	 * <p>
	 * This is most likely due to an error with the static analysis config file or due to a problem with the fail system
	 * @param error
	 */
	public Report(String error) {
		this.reportStatus = "error: " + error;
		this.result = "UNDETERMINED";
	}
	
	//TODO: should we check if the report is ready instead of blindly returning any of these things?	
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
