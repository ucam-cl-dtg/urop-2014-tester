package database;

import com.mongodb.DB;
import exceptions.TickNotInDBException;
import exceptions.UserNotInDBException;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import privateinterfaces.IDBReportManager;
import reportelements.AbstractReport;

import java.util.List;

/**
 * Implementation of IDBReportManager using MongoDB
 * @author as2388
 */
public class MongoDBReportManager implements IDBReportManager {
    // initialise slf4j logger
    private static Logger log = LoggerFactory.getLogger(MongoDBReportManager.class);
    private final JacksonDBCollection<DBUser, String> DBUserColl;

    public MongoDBReportManager(DB database) {
        //TODO: remove hard coded string
        DBUserColl = JacksonDBCollection.wrap(database.getCollection("users"), DBUser.class, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public void addReport(String crsId, String tickId, String commitId, AbstractReport report) {
        //look up user by crsId
        DBUser user = DBUserColl.findOneById(crsId);

        //if user wasn't found, create them.
        if (user == null) {
            user = new DBUser(crsId);
            DBUserColl.insert(user);
        }

        //add report to the new user
        user.addReport(tickId, report);

        //save the user into the database
        DBUserColl.save(user);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractReport getLastReport(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        return getValidUser(crsId).getLastReport(tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<AbstractReport> getAllReports(String crsId, String tickId) throws UserNotInDBException, TickNotInDBException {
        return getValidUser(crsId).getAllReports(tickId);
    }

    @Override
    public void removeUser(String crsId) {

    }

    @Override
    public void removeTick(String crsId, String tickId) {

    }

    @Override
    public void removeCommit(String crsId, String tickId, String commitId) {

    }

    /**
     * Loads a valid user object from MongoDB
     * @param crsId                 Id of user to load
     * @return                      Loaded DBUser object from database
     * @throws UserNotInDBException If the user with id crsId does not exist in the database
     */
    private DBUser getValidUser(String crsId) throws UserNotInDBException {
        //look up user by crsId
        DBUser user = DBUserColl.findOneById(crsId);

        if (user == null) {
            log.error("User not found");
            throw new UserNotInDBException(crsId);
        }

        return user;
    }
}
