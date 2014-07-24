package privateinterfaces;

import exceptions.TestIDAlreadyExistsException;
import exceptions.TestIDNotFoundException;
import testingharness.XMLTestSettings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

/**
 * @author kls82
 */
@Path("/accessTests")
public interface IDBXMLTestsManager {
	/**
     * creates a new test and stores its settings for access later
     *
     * @param tickId	            unique Id of the tick being created
     * @param staticTestSettings    settings to insert into database
     */
	@GET
    @Path("addNewTest/{tickId}")
    public void addNewTest(@PathParam("tickId") String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDAlreadyExistsException;

	/**
     * edits an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	                    unique Id of the tick being edited
     * @param staticTestSettings            settings to insert into database
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    @GET
    @Path("editExistingTest/{tickId}")
    public void update(@PathParam("tickId") String tickId, List<XMLTestSettings> staticTestSettings) throws TestIDNotFoundException;

    /**
     * deletes an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	unique Id of the tick being deleted
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    @GET
    @Path("deleteTest/{tickId}")
    public void deleteTest (@PathParam("tickId") String tickId) throws TestIDNotFoundException;
    
    /**
     * returns the settings for an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	unique Id of the tick being requested
     * @return settings		List containing the settings for each static test to be run for this test
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    @GET
    @Path("getTestSettings/{tickId}")
    public List<XMLTestSettings> getTestSettings (@PathParam("tickId") String tickId) throws TestIDNotFoundException;
    
    
}
