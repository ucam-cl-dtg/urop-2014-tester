package reportelements;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Stores all information about a report i.e. status, lists of errors, and the overall result
 * <p>
 * Users of this class should poll status to check when the report is complete
 * 
 * @author kls82
 *
 */
public class Report {
    private List<StaticReportItem> sReport;	//list of static analysis report items
    private List<DynamicReportItem> dReport;	//list of dynamic analysis report items
    private String result;				//Overall PASS/FAIL/UNDETERMINED

    /**
     * Constructor for a successfully completed report
     * @param sReport	list of static report items to be inserted into the report
     * @param dReport	list of dynamic report items to be inserted into the report
     */
    public Report(List<StaticReportItem> sReport, List<DynamicReportItem> dReport) {
        this.sReport = sReport;
        this.dReport = dReport;

        //Handle static reports
        Collections.sort(sReport);
        //If the severity of any problem found by CheckStyle is an error, then fail the student
        for (StaticReportItem s : sReport) {
            if (s.getSeverity().equals("error")) {
                this.result = "FAIL";
            }
        }

        //TODO: Handle dynamic reports

        if (this.result == null) {
            this.result = "PASS";
        }
    }

    public Report(){};

    //GETTERS AND SETTERS
    public List<StaticReportItem> getsReport() {
        return sReport;
    }

    public void setsReport(List<StaticReportItem> sReport) {
        this.sReport = sReport;
    }

    public List<DynamicReportItem> getdReport() {
        return dReport;
    }

    public void setdReport(List<DynamicReportItem> dReport) {
        this.dReport = dReport;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
