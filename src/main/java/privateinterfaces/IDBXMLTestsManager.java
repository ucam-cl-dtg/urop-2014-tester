package privateinterfaces;

import exceptions.TestIDAlreadyExistsException;
import exceptions.TestIDNotFoundException;
import testingharness.XMLTestSettings;

import java.util.List;

/**
 * @author kls82
 */
public interface IDBXMLTestsManager {
	/**
     * creates a new test and stores its settings for access later
     *
     * @param tickId	            unique Id of the tick being created
     * @param staticTestSettings    settings to insert into database
     */
    public void addNewTest(String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDAlreadyExistsException;

	/**
     * edits an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	                    unique Id of the tick being edited
     * @param staticTestSettings            settings to insert into database
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    public void update(String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDNotFoundException;

    /**
     * deletes an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	unique Id of the tick being deleted
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    public void deleteTest (String tickId) throws TestIDNotFoundException;
    
    /**
     * returns the settings for an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	unique Id of the tick being requested
     * @return settings		List containing the settings for each static test to be run for this test
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    public List<XMLTestSettings> getTestSettings (String tickId) throws TestIDNotFoundException;
    
    
}
