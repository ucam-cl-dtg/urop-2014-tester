package TestingHarness;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/testerAPI")
@Produces("application/json")
public interface TestServiceInterface {
	
	/**
	 * Starts a new test
	 * @param repoAddress			The address of the git repository to examine for .java files to 
	 * 								analyse
	 * @return						The ID of the test just started, to be used by the caller of this
	 * 								function to access the status and result of the the test at a
	 * 								later time
	 */
	@GET
	@Path("/runNewTest")
	public abstract String runNewTest(@QueryParam("repoAddress") String repoAddress);
	
	/**
	 * Returns the status of the test with ID testID if a test with testID exists, otherwise returns an error code
	 * @param testID	ID of the test to access
	 * @return			If the test was found: HTTP status code 200, and a string containing the status,
	 * 										     either TODO e.g. waiting, running, completed, error
	 * 					Else: HTTP status code 410 (Gone)
	 */
	@GET
	@Path("/pollStatus")
	public abstract Response pollStatus(@QueryParam("testID") String testID);
	
	/**
	 * Gets the report associated with the testID.
	 * @param testID	ID of the test to access
	 * @return			A report object in JSON format if item found, otherwise HTTP code 410 (Gone)
	 */
	@GET
	@Path("/getReport")
	public abstract Report getReport(@QueryParam("testID") String testID);
	
	//TODO: remove
	@GET
	@Path("/test")
	public abstract Report test(@QueryParam("testID") String testID);
}
