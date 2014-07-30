package publicinterfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 * Maintains all details about a report, and provides methods to update and retrieve report data
 * @author as2388
 * @author kls82
 */
public class Report{
	/* students should only be allowed on signups if testResult = PASS */
	private ReportResult testResult = ReportResult.UNDEFINED;
	private ReportResult tickerResult = ReportResult.UNDEFINED;
	private ReportResult reportResult = ReportResult.UNDEFINED;
	private String tickerComments = null;
    private List<Problem> problemsTestedFor = new LinkedList<>(); //list storing all problems looked for (also see what each Problem holds)
    private Date creationDate = new Date();
    private int noOfTests;
    private String commitId;
    
    //for JSON
    public Report() {}
    
    public List<Problem> getItems() {
        return problemsTestedFor;
    }

    public void setProblemsTestedFor(List<Problem> items) {
        this.problemsTestedFor = items;
    }
    
    public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setNoOfTests(int noOfTests) {
		this.noOfTests = noOfTests;		
	}

	public int getNoOfTests() {
		return noOfTests;
	}

	public void setReportResult(ReportResult result) {
		this.reportResult = result;
	}
	
	public ReportResult getReportResult() {
		return reportResult;
	}
	
	
    public void addProblem(String category, Severity severity) {
        problemsTestedFor.add(new Problem(category, severity));
    }

    public void addDetail(String category, String filename, int lineNumber, String details)
            throws CategoryNotInReportException {
    	boolean found = false;
    	for (Problem t : problemsTestedFor) {
        	if (t.getProblemDescription().equals(category)) {
        		found = true;
        		t.addDetail(filename, lineNumber, details);

                if (t.getSeverity() == Severity.ERROR) {
                    setTestResult(ReportResult.FAIL);
                    setReportResult(ReportResult.FAIL);
                }
        	}
        }
	    if (!found) {
	    	throw new CategoryNotInReportException(category);
	    }
    }

    public void addDetail(String category, Severity severity, String filename, Integer lineNumber, String details) throws CategoryAlreadyExistsException  {
    	boolean found = false;
    	for (Problem t : problemsTestedFor) {
	       	if (t.getProblemDescription().equals(category)) {
	       		found = true;
	       	}
    	}
	    if (!found) {
	    	Problem newProblem = new Problem(category,severity);
	    	newProblem.addDetail(filename, lineNumber, details);
	    	problemsTestedFor.add(newProblem);
	    }
	    //NOTE:category should never be repeated 
	    //as each individual test only runs once, but add error just incase
	    else {
	    	throw new CategoryAlreadyExistsException("Tick has a repeated test");
	    }
    }

    /*
         * calculates what tests parts/problems were passed, failed or got a warning
         */
    public void calculateProblemStatuses() {
		for (Problem e : problemsTestedFor)
		{
		    if (e.getFileDetails().size() > 0) {
		    	if (e.getSeverity() == Severity.ERROR) {
	            	e.setOutcome(Outcome.ERROR);
	            }
	            else {
	            	e.setOutcome(Outcome.WARNING);
	            }
		    }
		    else {
		    	e.setOutcome(Outcome.PASS);
		    }
		}
		Collections.sort(problemsTestedFor);
		if (this.testResult != ReportResult.FAIL) {
			this.testResult = ReportResult.PASS;
		}
    }

	public void setTickerResult(ReportResult tickerResult) {
		this.tickerResult = tickerResult;
		if (this.tickerResult == ReportResult.PASS & this.testResult == ReportResult.PASS) {
			this.reportResult = ReportResult.PASS;
		}
		else {
			this.reportResult = ReportResult.FAIL;
		}
	}

	public ReportResult getTickerResult() {
		return this.tickerResult;
	}
	
	public ReportResult getTestResult() {
		return testResult;
	}

	public void setTestResult(ReportResult testResult) {
		this.testResult = testResult;
	}

	public String getTickerComments() {
		return tickerComments;
	}

	public void setTickerComments(String tickerComments) {
		this.tickerComments = tickerComments;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
}
