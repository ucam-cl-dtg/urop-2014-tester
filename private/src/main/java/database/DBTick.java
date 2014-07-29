package database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

import publicinterfaces.Report;

/**
 * Used to maintain all reports in a tick instance
 * @author as2388
 */
class DBTick {
    private LinkedList<Report> reports = new LinkedList<>();

    public DBTick() {}

    /**
     * Adds a new report to this tick
     * @param newReport Report to add
     */
    public void addReport(Report newReport) {
        reports.add(newReport);
    }

    /**
     * @return  Last report added to this tick.
     */
    @JsonIgnore
    public Report getLast() {
        return reports.getLast();
    }

    /**
     * @return  All reports in this tick
     */
    @JsonIgnore
    public List<Report> getAll() {
        return reports;
    }

    //JSON serialisation stuff:
    public LinkedList<Report> getReports() {
        return reports;
    }

    public void setReports(LinkedList<Report> reports) {
        this.reports = reports;
    }
}
