package TestingHarness;

import java.util.LinkedList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Stores information about one type of problem generated by the static analysis
 * @author as2388
 * @author kls82
 */
public class StaticReportItem implements Comparable<StaticReportItem>{
    private String severity;
    private String fileName;
    private List<Integer> lineNumberList = new LinkedList<Integer>();	//list of lines on which the problem was found
    private String problem;		//Problem are broken up into two parts. problem is a more general description
    private String detail;		//										detail gives more specific information/a short piece of advice

    /**
     * Constructor for static report item
     * @param severity	either "error" or "warning"
     * @param fileName	name of file in which the problem was found
     * @param lineNo	first line at which problem was found
     * @param problem	more general problem description
     * @param detail	detail giving advice and/or more specific detail about the problem 
     */
    public StaticReportItem(String severity, String fileName, int lineNo, String problem, String detail) {
        this.severity = severity;
        this.fileName = fileName;
        this.lineNumberList.add(lineNo);
        this.problem = problem;
        this.detail  = detail;
    }	

    /**
     * Enables sorting of a collection of sReportItem
     * <p>
     * Order of sorting (most significant first):
     * <ol>
     * <li>File</li>
     * <li>Severity</li>
     * <li>line number of first occurrence</li>\
     * </ol>
     * The function also sorts all the lines at which the problem was found into ascending order
     */
    public int compareTo(StaticReportItem reportItem2) {
        Collections.sort(this.getLineNumbers());
        Collections.sort(reportItem2.getLineNumbers());
        if (this.getFileName().compareTo(reportItem2.getFileName()) == 0) {
            if (this.getSeverity().equals("error") && reportItem2.getSeverity().equals("warning")) {
                return -1;
            }
            else if (this.getSeverity().equals("warning") && reportItem2.getSeverity().equals("error")) {
                return 1;
            }
            else {
                if (this.getLineNumbers().get(0) - reportItem2.getLineNumbers().get(0) == 0) {
                    return this.getProblem().compareTo(reportItem2.getProblem());
                }
                else {
                    return this.getLineNumbers().get(0) - reportItem2.getLineNumbers().get(0);
                }
            }
        }
        else {
            return this.getFileName().compareTo(reportItem2.getFileName());
        }
    }

    /**
     * Add the line of another occurrence of this error to this object
     * @param lineNo	the line at which the new instance was found
     */
    public void addErrorAtLine(int lineNo) {
        this.lineNumberList.add(lineNo);
    }

    public String getSeverity() {
        return this.severity;
    }

    public String getFileName() {
        return this.fileName;
    }

    public List<Integer> getLineNumbers() {
        return this.lineNumberList;
    }

    public String getProblem() {
        return problem;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * Gets the problem message as a nicely formatted string
     * @return message in format "{problem} ({detail})"
     */
    public String getMessage() {
        if (detail.equals(""))
        {
            return problem;
        }
        else
        {
            return problem + " (" + detail + ")";
        }
    }
}
