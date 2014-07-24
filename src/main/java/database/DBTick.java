package database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import reportelements.AbstractReport;

import java.util.LinkedList;
import java.util.List;

public class DBTick {
    private LinkedList<AbstractReport> reports = new LinkedList<>();

    public DBTick() {}

    /**
     * @return  last report added to this tick.
     */
    @JsonIgnore
    public AbstractReport getLast() {
        return reports.getLast();
    }

    @JsonIgnore
    public List<AbstractReport> getAll() {
        return reports;
    }

    public LinkedList<AbstractReport> getReports() {
        return reports;
    }

    public void setReports(LinkedList<AbstractReport> reports) {
        this.reports = reports;
    }

    public void addReport(AbstractReport newReport) {
        reports.add(newReport);
    }
}
