package futuratedreportelements;


import sun.util.logging.resources.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains all details about a report, and provides methods to get and retrieve report data
 */
public class Report {
    private String result; //Either "PASS" or "FAIL"
    private Map<String, ProblemCategory> problems = new HashMap<>(); //map problem category to ProblemCategory objects

    public void addProblem(String category, String severity) {
        problems.put(category, new ProblemCategory(severity));
    }

    public void addProblem(String category, ProblemCategory problemCategory) throws CategoryAlreadyExistsException {
        if (!(problems.containsKey(category))) {
            problems.put(category, problemCategory);
        }
        else {
            throw new CategoryAlreadyExistsException(category);
        }
    }

    public void addDetail(String category, String filename, int lineNumber, String details) throws CategoryNotInReportException {
        if (problems.containsKey(category))
        {
            problems.get(category).addDetail(filename, lineNumber, details);
        }
        else {
            throw new CategoryNotInReportException(category);
        }
    }

    public void addDetail(String category, String severity, String filename, int lineNumber, String details)  {
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
}
