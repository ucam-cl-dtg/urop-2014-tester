package publicinterfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

public class SimpleReport extends AbstractReport {
    private LinkedList<SimpleReportItem> items = new LinkedList<>();

    public SimpleReport() {}

    /** {@inheritDoc} */
    @Override
    public void addProblem(String category, Severity severity) {
        items.add(new SimpleReportItem(category, severity, null, null, null));
    }

    /** {@inheritDoc} */
    @Override
    public void addDetail(String category, String filename, int lineNumber, String details) throws CategoryNotInReportException {
        //items.add(new SimpleReportItem(category,  null, filename, lineNumber, details));
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).getCategory().equals(category) && items.get(i).getFilename() == null) {
                addDetail(category, items.get(i).getSeverity(), filename, lineNumber, details);
                items.remove(i);
                return;
            }
    }

    /** {@inheritDoc} */
    @Override
    @JsonIgnore
    public void addDetail(String category, Severity severity, String filename, Integer lineNumber, String details) {
        items.add(new SimpleReportItem(category, severity, filename, lineNumber, details));
        if (severity == Severity.ERROR && lineNumber != null) {
            this.reportResult = ReportResult.FAIL;
        }
    }

    //for JSON serialisation
    public LinkedList<SimpleReportItem> getItems() {
        return items;
    }

    public void setItems(LinkedList<SimpleReportItem> items) {
        this.items = items;
    }
}
