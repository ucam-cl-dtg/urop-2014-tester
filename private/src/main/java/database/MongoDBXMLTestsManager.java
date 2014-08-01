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
        //if XMLTestSettings for the given tickId are already in the database, throw an exception
        if (XMLTestSettingsColl.findOneById(tickId) != null) {
            throw new TestIDAlreadyExistsException(tickId);
        }

        XMLTestSettingsColl.save(new ListWrapper(tickId, staticTestSettings));
	}

    /** {@inheritDoc} */
    @Override
    public void update(@PathParam("tickId") String tickId, List<StaticOptions> staticTestSettings) throws TestIDNotFoundException {
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        //hopefully this overwrites automatically
        XMLTestSettingsColl.save(new ListWrapper(tickId,staticTestSettings));
        
        /* deleteTest(tickId);
        try {
            addNewTest(tickId, staticTestSettings);
        }
        catch (TestIDAlreadyExistsException e) {
            //because addNewTest() is preceded by deleteTest(), this should not happen
        } */
    }

    /** {@inheritDoc} */
	@Override
	public void deleteTest(String tickId) throws TestIDNotFoundException {
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        XMLTestSettingsColl.removeById(tickId);
	}

    /** (@inheritDoc} */
	@Override
	public List<StaticOptions> getTestSettings(String tickId) throws TestIDNotFoundException {
        if (XMLTestSettingsColl.findOneById(tickId) == null) {
            throw new TestIDNotFoundException(tickId);
        }

        ListWrapper listWrapper = XMLTestSettingsColl.findOneById(tickId);
        return listWrapper.getElements();
	}
}
