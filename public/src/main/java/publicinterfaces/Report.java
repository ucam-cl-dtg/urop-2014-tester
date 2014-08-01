package publicinterfaces;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 * Maintains all details about a report, and provides methods to update and retrieve report data
 * NOTE:
	 * students should only be allowed on signups if testResult = PASS 
	 * students have only passed the tick when reportResult = PASS 
 * @author as2388
 * @author kls82
 */
public class Report{
	// result from the computer tests
	private ReportResult testResult = ReportResult.UNDEFINED;
	// result given by the ticker
	private ReportResult tickerResult = ReportResult.UNDEFINED;
	// overall result defined by the results of the tests and the ticker
	private ReportResult reportResult = ReportResult.UNDEFINED;
	// any comments the ticker wished to add to the report
	private String tickerComments = null;
	// list of the problems the tests have checked for
    private List<Problem> problemsTestedFor = new LinkedList<>();
    // creation date of the report
    private Date creationDate = new Date();
    // number of tests carried out to generate the test result
    private int noOfTests;
    // commitId defining when the java files tested for this report were committed
    private String commitId;
    
    /**
     * Initializes a new problem category that is being tested for and adds it to problemsTestedFor. This
     * should always be called before addDetail is ever called for the same problem category.
     * @param category		human-readable version of the description defining what this new problem is
     * @param severity		severity of the problem for in case it is discovered in a file
     */	
    public void addProblem(String category, Severity severity) {
        problemsTestedFor.add(new Problem(category, severity));
    }

    /**
     * Called when an instance of a problem has been found in the code being tested and the details need to be
     * added to the corresponding Problem being stored in problemsTestedFor
     * @param category		human-readable version of the description defining what the problem is
     * @param filename		name of the file where the problem has been found
     * @param lineNumber	line number defining where the problem has been found in the file
     * @param details		further description of the problem found, specific to the file and line number
     */	
    public void addDetail(String category, String filename, int lineNumber, String details)
            throws CategoryNotInReportException {
    	boolean found = false;
    	for (Problem t : problemsTestedFor) {
        	if (t.getProblemDescription().equals(category)) {
        		found = true;
        		t.addDetail(filename, lineNumber, details);
        		
        		/*
        		 * An instance of the problem has been found in a file now, so if the problem's severity
        		 * was error, this means the student has failed their tick
        		 */
                if (t.getSeverity() == Severity.ERROR) {
                    setTestResult(ReportResult.FAIL);
                    setReportResult(ReportResult.FAIL);
                }
        	}
        }
    	//this should not happen if addProblem is always called first
	    if (!found) {
	    	throw new CategoryNotInReportException(category);
	    }
    }

    /**
     * Called once all tests have run to determine which tests defining the possible problems have 
     * detected error/warning/pass. Also, if no errors have been found then testResult will still 
     * be UNDEFINED and so this is changed to PASS. If not then testResult remains as FAIL. 
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

    /**
     * Allows the ticker to set their own PASS/FAIL grade and then uses this to calculate the overall
     * reportResult
     * @param tickerResult		PASS/FAIL of type ReportResult
     */	
	public void setTickerResult(ReportResult tickerResult) {
		this.tickerResult = tickerResult;
		if (this.tickerResult == ReportResult.PASS & this.testResult == ReportResult.PASS) {
			this.reportResult = ReportResult.PASS;
		}
		else {
			this.reportResult = ReportResult.FAIL;
		}
	}

	//Constructor and getters/setters for json serialisation
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
