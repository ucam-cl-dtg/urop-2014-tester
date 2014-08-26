package database;

import com.mongodb.DB;

import configuration.ConfigurationLoader;

import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import privateinterfaces.IDBTestsManager;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.StaticOptions;

import javax.ws.rs.PathParam;

import java.util.List;

/**
 * Implementation of TestsInterface using MongoDB
 * @author as2388
 */
public class MongoDBTestsManager implements IDBTestsManager {
    // initialise slf4j logger
    private static Logger log = LoggerFactory.getLogger(MongoDBTestsManager.class);
    private final JacksonDBCollection<ListWrapper, String> testSettingsColl;

    public MongoDBTestsManager(DB database) {
        testSettingsColl = JacksonDBCollection.wrap(
                database.getCollection(ConfigurationLoader.getConfig().getMongoTickSettingsCollectionName()),
                ListWrapper.class, String.class);
    }
    
    public MongoDBTestsManager() {
        testSettingsColl = null;
    }
    
    /** {@inheritDoc} */
	@Override
	public void addNewTest(String tickId, List<StaticOptions> staticTestSettings, String containerId, String testId) throws TestIDAlreadyExistsException {
        log.info("Saving new stylistic settings for tickId " + tickId);

        //if XMLTestSettings for the given tickId are already in the database, throw an exception
        if (testSettingsColl.findOneById(tickId) != null) {
            log.info("Stylistic settings for tickId " + tickId + " already exist in the database. Throwing TestIDAlreadyExistsException");
            throw new TestIDAlreadyExistsException(tickId);
        }

        testSettingsColl.save(new ListWrapper(tickId, staticTestSettings, containerId, testId));
        log.info("Stylistic settings saved for tickId " + tickId);
	}

    /** {@inheritDoc} */
    @Override
    public void update(@PathParam("tickId") String tickId, List<StaticOptions> staticTestSettings, String containerId, String testId) throws TestIDNotFoundException {
        log.debug("Updating stylistic settings in db for tickId " + tickId);
        if (testSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in the database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        testSettingsColl.save(new ListWrapper(tickId, staticTestSettings, containerId, testId));

        log.debug("Stylistic settings for tickId " + tickId + " updated, returning...");
    }

    /** {@inheritDoc} */
	@Override
	public void deleteTest(String tickId) throws TestIDNotFoundException {
        log.debug("Deleting stylistic settings from db for tickId " + tickId);
        if (testSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        testSettingsColl.removeById(tickId);
        log.debug("Removed stylistic settings for tickId " + tickId + "; returning...");
	}

    /** (@inheritDoc} */
	@Override
	public List<StaticOptions> getTestSettings(String tickId) throws TestIDNotFoundException {
        log.debug("Obtaining stylistic settings from db for tickId " + tickId);
        if (testSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        ListWrapper listWrapper = testSettingsColl.findOneById(tickId);

        log.debug("Stylistic settings found for tickId " + tickId + "; returning...");
        return listWrapper.getElements();
	}

	@Override
	public String getContainerId(String tickId) throws TestIDNotFoundException {
		 if (testSettingsColl.findOneById(tickId) == null) {
	            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
	            throw new TestIDNotFoundException(tickId);
	        }

	        ListWrapper listWrapper = testSettingsColl.findOneById(tickId);

	        log.debug("Stylistic settings found for tickId " + tickId + "; returning...");
	        return listWrapper.getDynamicContainerId();
	}

	@Override
	public String getTestId(String tickId) throws TestIDNotFoundException {
		if (testSettingsColl.findOneById(tickId) == null) {
            log.warn("tickId " + tickId + " not found in database; throwing TestIDNotFoundException");
            throw new TestIDNotFoundException(tickId);
        }

        ListWrapper listWrapper = testSettingsColl.findOneById(tickId);

        log.debug("Stylistic settings found for tickId " + tickId + "; returning...");
        return listWrapper.getDynamicTestId();
	}
}
