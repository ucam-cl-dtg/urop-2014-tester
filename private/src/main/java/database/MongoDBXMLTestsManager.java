package database;

import com.mongodb.DB;

import configuration.ConfigurationLoader;

import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import privateinterfaces.IDBXMLTestsManager;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;

import publicinterfaces.StaticOptions;

import javax.ws.rs.PathParam;

import java.util.List;

/**
 * Implementation of TestsInterface using MongoDB
 * @author as2388
 */
public class MongoDBXMLTestsManager implements IDBXMLTestsManager {
    // initialise slf4j logger
    private static Logger log = LoggerFactory.getLogger(MongoDBXMLTestsManager.class);
    private final JacksonDBCollection<ListWrapper, String> XMLTestSettingsColl;

    public MongoDBXMLTestsManager(DB database) {
        XMLTestSettingsColl = JacksonDBCollection.wrap(
                database.getCollection(ConfigurationLoader.getConfig().getMongoXMLSettingsCollectionName()),
                ListWrapper.class, String.class);
    }
    
    public MongoDBXMLTestsManager() {
        XMLTestSettingsColl = null;
    }
    
    /** {@inheritDoc} */
	@Override
	public void addNewTest(String tickId, List<StaticOptions> staticTestSettings) throws TestIDAlreadyExistsException {
        log.debug("Saving new stylistic settings for tickId " + tickId);

        //if XMLTestSettings for the given tickId are already in the database, throw an exception
        if (XMLTestSettingsColl.findOneById(tickId) != null) {
            log.warn("Stylistic settings for tickId " + tickId + " already exist in the database. Throwing TestIDAlreadyExistsException");
            throw new TestIDAlreadyExistsException(tickId);
        }

        XMLTestSettingsColl.save(new ListWrapper(tickId, staticTestSettings));
        log.debug("Stylistic settings saved for tickId " + tickId);
	}

    /** {@inheritDoc} */
    @Override
    public void update(@PathParam("tickId") String tickId, List<StaticOptions> staticTestSettings) throws TestIDNotFoundException {
        log.debug("Updating stylistic settings in db for tickId " + tickId);
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in the database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        XMLTestSettingsColl.save(new ListWrapper(tickId, staticTestSettings));

        log.debug("Stylistic settings for tickId " + tickId + " updated, returning...");
    }

    /** {@inheritDoc} */
	@Override
	public void deleteTest(String tickId) throws TestIDNotFoundException {
        log.debug("Deleting stylistic settings from db for tickId " + tickId);
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        XMLTestSettingsColl.removeById(tickId);
        log.debug("Removed stylistic settings for tickId " + tickId + "; returning...");
	}

    /** (@inheritDoc} */
	@Override
	public List<StaticOptions> getTestSettings(String tickId) throws TestIDNotFoundException {
        log.debug("Obtaining stylistic settings from db for tickId " + tickId);
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        ListWrapper listWrapper = XMLTestSettingsColl.findOneById(tickId);

        log.debug("Stylistic settings found for tickId " + tickId + "; returning...");
        return listWrapper.getElements();
	}
}
