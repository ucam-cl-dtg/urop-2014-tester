package database;

import com.mongodb.DB;
import com.mongodb.DBObject;

import configuration.ConfigurationLoader;

import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import privateinterfaces.IDBReportManager;
import publicinterfaces.Report;
import publicinterfaces.ReportNotFoundException;
import publicinterfaces.ReportResult;
import publicinterfaces.Status;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TickNotInDBException;
import publicinterfaces.UserNotInDBException;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of IDBReportManager using MongoDB
 * @author as2388
 */
public class MongoDBReportManager implements IDBReportManager {
    // initialise slf4j logger
    private static final Logger log = LoggerFactory.getLogger(MongoDBReportManager.class);
    private final JacksonDBCollection<DBUser, String> DBUserColl;

    public MongoDBReportManager(DB database) {
        DBUserColl = JacksonDBCollection.wrap(
                database.getCollection(ConfigurationLoader.getConfig().getMongoUsersCollectionName()),
                DBUser.class, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public void addReport(String crsId, String tickId, Report report) {
        log.debug("Adding report for user " + crsId + " tickId " + tickId);
        //look up user by crsId
        DBUser user = DBUserColl.findOneById(crsId);

        //if user wasn't found, create them.
        if (user == null) {
            log.debug("User " + crsId + " not found, creating");
            user = new DBUser(crsId);
            DBUserColl.insert(user);
        }
        //add report to the new user
        user.addReport(tickId, report);

        //save the user into the database
        DBUserColl.save(user);

        log.debug("Saved user to database; restarting...");
    }

    /** {@inheritDoc} */
    @Override
    public Report getLastReport(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        return getValidUser(crsId).getLastReport(tickId);
    }

    /** {@inheritDoc} */
    @Override
    public Status getLastStatus(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        return getValidUser(crsId).getStatus(tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> getAllReports(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        return getValidUser(crsId).getAllReports(tickId);
    }

    /** {@inheritDoc} */
    @Override
    public void removeUserReports(String crsId) throws UserNotInDBException {
        log.debug("Removing all reports for user " + crsId);

        getValidUser(crsId);

        DBUserColl.removeById(crsId);

        log.debug("Reports removed, returning...");
    }

    /** {@inheritDoc} */
    @Override
    public void removeUserTickReports(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        DBUser user = getValidUser(crsId);
        user.removeTick(tickId);
    }

    /**
     * Loads a valid user object from MongoDB
     * @param crsId                 Id of user to load
     * @return                      Loaded DBUser object from database
     * @throws UserNotInDBException If the user with id crsId does not exist in the database
     */
    private DBUser getValidUser(String crsId) throws UserNotInDBException {
        log.debug("Checking if user" + crsId + " is in database...");
        //look up user by crsId
        DBUser user = DBUserColl.findOneById(crsId);

        if (user == null) {
            log.warn(crsId + " not found in database");
            throw new UserNotInDBException(crsId);
        }

        log.debug("user " + crsId + " found, returning...");
        return user;
    }

    //@author kls82, which is why there is no JavaDoc
	@Override
	public void editReportTickerResult(String crsid, String tickId,
			ReportResult tickerResult, String tickerComments, String commitId, Date date) 
					throws UserNotInDBException, TickNotInDBException, ReportNotFoundException {
		DBUser user = getValidUser(crsid);
		Report report = user.getReport(tickId,commitId,date);
		report.setTickerResult(tickerResult);
		report.setTickerComments(tickerComments);
		DBUserColl.save(user);
	}
}
