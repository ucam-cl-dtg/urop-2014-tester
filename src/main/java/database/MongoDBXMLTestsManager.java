package database;

import com.mongodb.DB;
import configuration.ConfigurationLoader;
import exceptions.TestIDAlreadyExistsException;
import exceptions.TestIDNotFoundException;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import privateinterfaces.IDBXMLTestsManager;
import testingharness.XMLTestSettings;

import javax.ws.rs.PathParam;
import java.util.List;

/**
 * Implementation of TestsInterface using MongoDB
 * @author as2388
 */
public class MongoDBXMLTestsManager implements IDBXMLTestsManager {
    // initialise slf4j logger
    private static Logger log = LoggerFactory.getLogger(MongoDBXMLTestsManager.class);
    private final JacksonDBCollection<ListWrapper, String> XMLTetsSettingsColl;

    public MongoDBXMLTestsManager(DB database) {
        XMLTetsSettingsColl = JacksonDBCollection.wrap(
                database.getCollection(ConfigurationLoader.getConfig().getMongoXMLSettingsCollectionName()),
                ListWrapper.class, String.class);
    }
    
    public MongoDBXMLTestsManager() {
        XMLTetsSettingsColl = null;
    }
    
    /** {@inheritDoc} */
	@Override
	public void addNewTest(String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDAlreadyExistsException {
        //if XMLTestSettings for the given tickId are already in the database, throw an exception
        if (XMLTetsSettingsColl.findOneById(tickId) != null) {
            throw new TestIDAlreadyExistsException(tickId);
        }

        XMLTetsSettingsColl.save(new ListWrapper(tickId, staticTestSettings));
	}

    /** {@inheritDoc} */
    @Override
    public void update(@PathParam("tickId") String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDNotFoundException {
        if (XMLTetsSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        deleteTest(tickId);
        try {
            addNewTest(tickId, staticTestSettings);
        }
        catch (TestIDAlreadyExistsException e) {
            //because addNewTest() is preceded by deleteTest(), this should not happen
        }
    }

    /** {@inheritDoc} */
	@Override
	public void deleteTest(String tickId) throws TestIDNotFoundException {
        if (XMLTetsSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        XMLTetsSettingsColl.removeById(tickId);
	}

    /** (@inheritDoc} */
	@Override
	public List<XMLTestSettings> getTestSettings(String tickId) throws TestIDNotFoundException {
        if (XMLTetsSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        ListWrapper listWrapper = XMLTetsSettingsColl.findOneById(tickId);
        return listWrapper.getElements();
	}
}
