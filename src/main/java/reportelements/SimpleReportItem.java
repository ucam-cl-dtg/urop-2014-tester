package reportelements;

public class SimpleReportItem {
    private String category;
    private Severity severity;
    private String filename;
    private Integer lineNumber;
    private String details;

    public SimpleReportItem(String category, Severity severity, String filename, Integer lineNumber, String details) {
        this.category = category;
        this.severity = severity;
        this.filename = filename;
        this.lineNumber = lineNumber;
        this.details = details;
    }

    //JSON serialisation stuff:
    public SimpleReportItem() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
