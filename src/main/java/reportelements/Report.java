package reportelements;

import java.util.LinkedList;
import java.util.List;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Maintains all details about a report, and provides methods to update and retrieve report data
 */
public class Report {
    private ReportResult reportResult = ReportResult.PASS; //default to PASS. addDetail() and addProblem() will change to FAIL if an item
                                         //with severity ERROR is added
    private List<Problem> problemsTestedFor = new LinkedList<>(); //list storing all problems looked for (also see what each Problem holds)

    /**
     * Create a new problem object to add to this report
     * @param category  General problem description e.g. unused import, bad indentation
     * @param severity  Either error or warning
     */
    public void addProblem(String category, Severity severity) {
        problemsTestedFor.add(new Problem(category, new ProblemDetails(severity)));
    }

    /**
     * Add an existing problem object to this report
     * @param category                          General problem description e.g. unused import, bad indentation
     * @param problem                           Existing problem object to add
     * @throws CategoryAlreadyExistsException   Thrown if the problem object to add already exists in the report
     */
    public void addProblem(String category, ProblemDetails problem) throws CategoryAlreadyExistsException {
        for (Problem t : problemsTestedFor) {
        	if (t.getCategory().equals(category)) {
        		throw new CategoryAlreadyExistsException(category);
        	}
        }
        problemsTestedFor.add(new Problem(category, problem));

        //check if this problem should set the status to fail
        if (problem.causesFail()) {
            reportResult = ReportResult.FAIL;
        }
    }

    /**
     * Adds a new problem detail to an existing report
     * @param category                      General problem description e.g. unused import, bad indentation
     * @param filename                      Name of the file in which the problem was found
     * @param lineNumber                    Line number within the file where the problem was found
     * @param details                       More specific problem description e.g. java.io.StreamReader, expected
     *                                      12 space, found 16
     * @throws CategoryNotInReportException Thrown if the category into which the new details should go does not exist
     *                                      in this report
     */
    public void addDetail(String category, String filename, int lineNumber, String details)
            throws CategoryNotInReportException {
    	boolean found = false;
    	for (Problem t : problemsTestedFor) {
        	if (t.getCategory().equals(category)) {
        		found = true;
        		t.getProblems().addDetail(filename, lineNumber, details);

                if (t.getProblems().getSeverity() == Severity.ERROR) {
                    reportResult = ReportResult.FAIL;
                }
        	}
        }
	    if (!found) {
	    	throw new CategoryNotInReportException(category);
	    }
    }

    /**
     * Adds a new problem detail to an existing report if the problem already exists, otherwise creates a new problem
     * object and adds a new problem to that.
     * @param category      General problem description e.g. unused import, bad indentation
     * @param severity      Either error or warning
     * @param filename      Name of the file in which the problem was found
     * @param lineNumber    Line number within the file where the problem was found
     * @param details       More specific problem description e.g. java.io.StreamReader, expected
     *                      12 spaces, found 16
     */
    public void addDetail(String category, Severity severity, String filename, int lineNumber, String details)  {
    	boolean found = false;
    	for (Problem t : problemsTestedFor) {
	       	if (t.getCategory().equals(category)) {
	       		found = true;
	       	}
    	}
	    if (!found) {
	    	problemsTestedFor.add(new Problem(category,severity));
	    }
        try {
            this.addDetail(category, filename, lineNumber, details);
        }
        catch (CategoryNotInReportException e) {
            //Because this function has just added the category to the report, this exception will not be raised here.
        }
    }

    public ReportResult getReportResult() {
        return reportResult;
    }

    public List<Problem> getProblems() {
        return problemsTestedFor;
    }
    
    /*
     * calculates what tests parts/problems were passed, failed or got a warning
     */
    public void calculateProblemStatuses() {
		for (Problem e : problemsTestedFor)
		{
		    if (e.getProblems().getFileDetails().size() > 0) {
		    	if (e.getProblems().getSeverity() == Severity.ERROR) {
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
    }
}
