package database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

import futuratedreportelements.AbstractReport;
import futuratedreportelements.SimpleReport;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        DBUser user = null;
        user = DBUserColl.findOneById(crsId);

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
    public AbstractReport getLastReport(String crsId, String tickId) {
        DBUser user = getValidUser(crsId);

        //TODO: what should happen if the tickId wasn't found?

        //get and the report most recently added to the user's tick at tickId
        return user.getLastReport(tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<AbstractReport> getAllReports(String crsId, String tickId) {
        DBUser user = getValidUser(crsId);

        //TODO: what should happen if tickId wasn't found?

        return user.getAllReports(tickId);
    }

    private DBUser getValidUser(String crsId) {
        //look up user by crsId
        DBUser user = DBUserColl.findOneById(crsId);

        if (user == null) {
            //TODO: throw new UserNotFoundException();
            log.error("User not found");
        }

        return user;
    }
}
