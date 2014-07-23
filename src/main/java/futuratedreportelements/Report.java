package futuratedreportelements;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains all details about a report, and provides methods to update and retrieve report data
 */
public class Report {
    private ReportResult reportResult = ReportResult.PASS; //default to PASS. addDetail() and addProblem() will change to FAIL if an item
                                         //with severity ERROR is added
    private Map<String, ProblemCategory> problems = new HashMap<>(); //map problem category to ProblemCategory objects

    /**
     * Create a new problem object to add to this report
     * @param category  General problem description e.g. unused import, bad indentation
     * @param severity  Either error or warning
     */
    public void addProblem(String category, Severity severity) {
        problems.put(category, new ProblemCategory(severity));
    }

    /**
     * Add an existing problem object to this report
     * @param category                          General problem description e.g. unused import, bad indentation
     * @param problem                           Existing problem object to add
     * @throws CategoryAlreadyExistsException   Thrown if the problem object to add already exists in the report
     */
    public void addProblem(String category, ProblemCategory problem) throws CategoryAlreadyExistsException {
        if (!(problems.containsKey(category))) {
            problems.put(category, problem);

            //check if this problem should set the status to fail
            if (problem.causesFail()) {
                reportResult = ReportResult.FAIL;
            }
        }
        else {
            throw new CategoryAlreadyExistsException(category);
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
    public void addDetail(String category, String filename, Integer lineNumber, String details)
            throws CategoryNotInReportException {
        if (problems.containsKey(category))
        {
            problems.get(category).addDetail(filename, lineNumber, details);

            if (problems.get(category).getSeverity() == Severity.ERROR) {
                reportResult = ReportResult.FAIL;
            }
        }
        else {
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
    public void addDetail(String category, Severity severity, String filename, Integer lineNumber, String details)  {
        if (!(problems.containsKey(category))) {
            this.addProblem(category, severity);
        }

        try {
            this.addDetail(category, filename, lineNumber, details);
        }
        catch (CategoryNotInReportException e) {
            //Because this function has just added the category to the report, this exception will not be raised here.
        }
    }

    /**
     * Adds a new category which has no problems
     * @param category     General problem description e.g. unused import, bad indentation
     */
    public void addDetail(String category) {
        addDetail(category, null, null, null, null);
    }

    public ReportResult getReportResult() {
        return reportResult;
    }

    public Map<String, ProblemCategory> getProblems() {
        return problems;
    }
}
