package publicinterfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

import java.util.Collections;

/**
 * Maintains all details about a report, and provides methods to update and retrieve report data
 * @author as2388
 * @author kls82
 */
public class Report extends AbstractReport{
    private List<Problem> problemsTestedFor = new LinkedList<>(); //list storing all problems looked for (also see what each Problem holds)

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void addDetail(String category, Severity severity, String filename, Integer lineNumber, String details)  {
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

    /*public ReportResult getReportResult() {
        return reportResult;
    }*/

    @JsonIgnore
    public List<Problem> getProblems() {
        return problemsTestedFor;
    }

    public List<Problem> getProblemsTestedFor() {
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
