package privateinterfaces;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import exceptions.TestIDNotFoundException;
import testingharness.XMLTestSettings;

@Path("/accessTests")
public interface MongoTestsInterface {
	/**
     * creates a new test and stores its settings for access later
     *
     * @param tickId	unique Id of the tick being created
     */
	@GET
    @Path("addNewTest/{tickId}")
    public void addNewTest(@PathParam("tickId") String tickId , List<XMLTestSettings> staticTestSettings);

	/**
     * edits an existing test if it is stored - if it is not found an exception is thrown
     *
     * @param tickId	unique Id of the tick being edited
     * @throws TestIDNotFoundException		if testId doesn't exist
     */
    @GET
    @Path("editExistingTest/{tickId}")
    public void editExistingTest (@PathParam("tickId") String tickId) throws TestIDNotFoundException;

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
