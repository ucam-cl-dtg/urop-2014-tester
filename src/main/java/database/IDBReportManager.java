package database;


import futuratedreportelements.AbstractReport;

import java.util.List;

public interface IDBReportManager {

    /**
     * Adds a report to the database, and creates a new DBUser and/or DBTick if necessary
     * @param crsId         Id of user to add report to
     * @param tickId        Id of tick of user's tick to add report to
     * @param commitId      Id of git commit of report
     * @param report        Report to add
     */
    public void addReport(String crsId, String tickId, String commitId, AbstractReport report);

    public AbstractReport getLastReport(String crsId, String tickId);

    public List<AbstractReport> getAllReports(String crsId, String tickId);

}
