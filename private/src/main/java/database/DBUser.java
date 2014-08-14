package database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.Report;
import publicinterfaces.ReportNotFoundException;
import publicinterfaces.Status;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TickNotInDBException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains all reports added to all a user's ticks.
 * @author as2388
 */
class DBUser {
    //Initialize slf4j logger
    private static final Logger log = LoggerFactory.getLogger(DBUser.class);
    private String crsId;
    private Map<String, DBTick> ticks = new HashMap<>(); //maps tick ids to a DBTick object

    @JsonCreator
    public DBUser(@JsonProperty("_id") String crsId) {
        this.crsId = crsId;
    }

    /**
     * Add a new report to a given tick
     * @param tickId    Unique identifier of tick to add report to
     * @param newReport Report to add
     */
    public void addReport(String tickId, Report newReport) {
        log.debug("crsId: " + crsId + " tickId: " + tickId + " Adding new report");
        //if there is no tick object with the id of tickId, create one
        if (!(ticks.containsKey(tickId))) {
            log.debug("crsId: " + crsId + " tickId: " + tickId + ": DBTick object does not yet exist for this user/tick combination, creating one..." );
            ticks.put(tickId, new DBTick());
        }

        //add the new report
        ticks.get(tickId).addReport(newReport);
        log.debug("crsId: " + crsId + " tickId: " + tickId + ": Report added: " + ticks.containsKey(tickId));
    }

    /**
     * Returns the last report added to a given tick.
     * @param tickId                Unique identifier of tick to look up last report from
     * @return                      Last report added
     * @throws TickNotInDBException Thrown if the tick wasn't found in the database
     */
    @JsonIgnore
    public Report getLastReport(String tickId) throws TickNotInDBException {
        return getValidTick(tickId).getLast();
    }

    /**
     * Returns a list containing all reports in a given tick
     * @param tickId    Unique identifier of tick to look up
     * @return          List of all reports user has for a given tick
     */
    @JsonIgnore
    public List<Report> getAllReports(String tickId) throws TickNotInDBException {
        return getValidTick(tickId).getAll();
    }

    /**
     * Gets the status of the last report saved for this user's given tick
     * @param tickId    Unique identifier of tick to get status of
     * @return          Status of last report saved
     */
    @JsonIgnore
    public Status getStatus(String tickId) throws TickNotInDBException {
        Report report = getValidTick(tickId).getLast();
        return new Status(report.getNoOfTests() + 1, report.getTestResult());
    }

    /**
     * Gets the tick collection associated with tickId
     * @param tickId                Unique identifier of tick to look up
     * @return                      DBTick
     * @throws TickNotInDBException Thrown if the tick wasn't found
     */
    private DBTick getValidTick(String tickId) throws TickNotInDBException {
        log.debug("crsId: " + crsId + " tickId: " + tickId + ": Validating tickId");
        if (!(ticks.containsKey(tickId)))
        {
            log.warn("Request for non-existent tick made. crsId: " + crsId + " tickId: " + tickId);
            throw new TickNotInDBException(tickId);
        }

        return ticks.get(tickId);
    }

    /**
     * Deletes all the user's reports in a tick
     * @param tickId                    Id of report to remove
     * @throws TestIDNotFoundException  Thrown if the tick wasn't found
     */
    public void removeTick(String tickId) throws TickNotInDBException {
        log.debug("crsId: " + crsId + " tickId: " + tickId + ": removing tick");
        if (!(ticks.containsKey(tickId))) {
            log.warn("crsId: " + crsId + " tickId: " + tickId + ": Tick not found, throwing TickNotInDBException");
            throw new TickNotInDBException(tickId);
        }
        ticks.remove(tickId);
    }

    @JsonProperty("Ticks")
    public Map<String, DBTick> getTicks() {
        return ticks;
    }

    @JsonProperty("_id") //override mongoDB's auto-generated id with the user's crsId
    public String getCrsId() {
        return crsId;
    }

    @JsonProperty("_id")
    public void setCrsId(String crsId) {
        this.crsId = crsId;
    }

    @JsonProperty("Ticks")
    public void setTicks(Map<String, DBTick> ticks) {
        this.ticks = ticks;
    }

	public Report getReport(String tickId, String commitId, Date date) throws ReportNotFoundException {
		List<Report> reports = this.ticks.get(tickId).getReports();
		for (Report report : reports) {
			if (report.getCommitId().equals(commitId) && report.getCreationDate().equals(date)) {
				return report;
			}
		}
		throw new ReportNotFoundException("report with commit Id " + commitId + " for " + tickId + " was not found");
	}
}
