package database;


import reportelements.AbstractReport;

import java.util.List;

/**
 * Interface for adding, getting, and removing reports from a database
 * @author as2388
 */
public interface IDBReportManager {

    /**
     * Adds a report to the database, and creates a new DBUser and/or DBTick if necessary
     * @param crsId         Id of user to add report to
     * @param tickId        Id of tick of user's tick to add report to
     * @param commitId      Id of git commit of report
     * @param report        Report to add
     */
    public void addReport(String crsId, String tickId, String commitId, AbstractReport report);

    /**
     * Gets the last report added to a given user's tick
     * @param crsId                 Id of user to get report from
     * @param tickId                Id of tick to get report from
     * @return                      Last added report
     * @throws UserNotInDBException Thrown if user with id crsId was not found in the database
     * @throws TickNotInDBException Thrown if tick with id tickId was not found for the user with id crsId
     */
    public AbstractReport getLastReport(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException;

    /**
     * Gets all reports in a given user's tick
     * @param crsId                 Id of user to get reports from
     * @param tickId                Id of tick to get reports from
     * @return                      List of all reports in user's tick
     * @throws UserNotInDBException Thrown if user with id crsId was not found in the database
     * @throws TickNotInDBException Thrown if tick with id tickId was not found for the user with id crsId
     */
    public List<AbstractReport> getAllReports(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException;

    public void removeUser(String crsId);

    public void removeTick(String crsId, String tickId);

    public void removeCommit(String crsId, String tickId, String commitId);
}
