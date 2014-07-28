package publicinterfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

/**
 * Outline of all parameters/functions which a report MUST have/implement.
 * The exact method of organising this data is left to class extenders
 * @author as2388
 */
@JsonDeserialize(as=SimpleReport.class)
public abstract class AbstractReport {
    protected ReportResult reportResult = ReportResult.PASS; //default to pass
    protected Date creationDate = new Date();
    protected int noOfTests;
    //TODO: protected String commitId or commitDate;

    /**
     * Create a new problem object to add to this report
     * @param category  General problem description e.g. unused import, bad indentation
     * @param severity  Either error or warning
     */
    public abstract void addProblem(String category, Severity severity);

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
    public abstract void addDetail(String category, String filename, int lineNumber, String details)
            throws CategoryNotInReportException;

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
    public abstract void addDetail(String category, Severity severity, String filename, Integer lineNumber, String details);

    public AbstractReport() {}

    public Date getCreationDate() {
        return creationDate;
    }

    public ReportResult getReportResult() {
        return reportResult;
    }

    public int getNoOfTests() {
        return noOfTests;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setReportResult(ReportResult reportResult) {
        this.reportResult = reportResult;
    }

    public void setNoOfTests(int noOfTests) {
        this.noOfTests = noOfTests;
    }
}
