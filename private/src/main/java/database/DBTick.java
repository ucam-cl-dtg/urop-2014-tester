package database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

import publicinterfaces.AbstractReport;

/**
 * Used to maintain all reports in a tick instance
 * @author as2388
 */
class DBTick {
    private LinkedList<AbstractReport> reports = new LinkedList<>();

    public DBTick() {}

    /**
     * Adds a new report to this tick
     * @param newReport Report to add
     */
    public void addReport(AbstractReport newReport) {
        reports.add(newReport);
    }

    /**
     * @return  Last report added to this tick.
     */
    @JsonIgnore
    public AbstractReport getLast() {
        return reports.getLast();
    }

    /**
     * @return  All reports in this tick
     */
    @JsonIgnore
    public LinkedList<AbstractReport> getAll() {
        return reports;
    }

    //JSON serialisation stuff:
    public LinkedList<AbstractReport> getReports() {
        return reports;
    }

    public void setReports(LinkedList<AbstractReport> reports) {
        this.reports = reports;
    }
}
