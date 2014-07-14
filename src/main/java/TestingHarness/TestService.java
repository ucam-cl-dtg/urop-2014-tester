package TestingHarness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Provides all API functions. 
 * THE EXACT FUNCTION PARAMETERS AND RETURN VALUES ARE SUBJECT TO CHANGE
 * @author as2388
 */
@Path("/testerAPI") //full path to here is /tester/API/
public class TestService
{
	static Logger log = Logger.getLogger(TestService.class.getName());	//initialise log4j logger
	
	/* Maps the ID of a test to in-progress tests. 
	 * TestService is responsible for generating unique IDs
	 * Class users are responsible for remembering the ID so that they can poll its status and get its report when done */
	private static Map<String, Tester> ticksInProgress;	//TODO: should we be keeping these in a DB instead?
	private int notFoundCode = 410;						//TODO: investigate whether this is the best status code to be returning
	
	public TestService()
	{
		if (ticksInProgress == null)
		{
			log.info("ticksInProgress Initialised");
			ticksInProgress = new HashMap<String, Tester>();
		}
	}
	
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
	public Response runNewTest(@QueryParam("repoAddress") String repoAddress)
	{
		log.info("New test request received");
		//TODO: use git team's API to get files to test
		Map<String, LinkedList<String>> tests = new HashMap<String, LinkedList<String>>();
		
		//TODO: add corresponding git files to tests
		
		//create a new Tester object
		final Tester tester = new Tester(tests);
		
		//generate a UUID for the tester
		String id=UUID.randomUUID().toString();
		while (TestService.ticksInProgress.containsKey(id)) {
			id=UUID.randomUUID().toString();
		}
		
		//add the object to the list of in-progress tests
		ticksInProgress.put(id, tester);
		
		//start the test in an asynchronous thread
		new Thread(new Runnable() {
	           public void run() {
	               tester.runTests();			
		}
		}).start();
		
		log.info("New test started; assigned id: " + id);
		
		//return status ok and the id of the tester object
		return Response.status(200).entity(id).build();
	}	
	
	/**
	 * Returns the status of the test with ID testID if a test with testID exists, otherwise returns an error code
	 * @param testID	ID of the test to access
	 * @return			If the test was found: HTTP status code 200, and a string containing the status,
	 * 										     either TODO e.g. waiting, running, completed, error
	 * 					Else: HTTP status code 410 (Gone)
	 */
	@GET
	@Path("/pollStatus")
	@Produces("text/plain")
	public Response pollStatus(@QueryParam("testID") String testID)
	{
		log.info("Poll request received for id: " + testID);
		if (ticksInProgress.containsKey(testID))
		{
			log.info("Poll request for id " + testID + " returned: " + ticksInProgress.get(testID).getStatus());
			return Response.status(200).entity(ticksInProgress.get(testID).getStatus()).build();
		}
		else
		{
			log.error("ID " + testID + " of poll request could not be found");
			return Response.status(notFoundCode).build();
		}
	}
	
	/**
	 * Gets the report associated with the testID.
	 * @param testID	ID of the test to access
	 * @return			A report object in JSON format if item found, otherwise HTTP code 410 (Gone)
	 */
	@GET
	@Path("/getReport")
	@Produces("application/json")
	public Response getReport(@QueryParam("testID") String testID)
	{
		log.info("Report get request received for id: " + testID);
		if (ticksInProgress.containsKey(testID))
		{
			Report toReturn = ticksInProgress.get(testID).getReport();
			//Assuming we're not responsible for storing tests, we should remove the test at this point. So I am.
			ticksInProgress.remove(testID);
			log.info("Report message found for id: " + testID);
			return Response.status(200).entity(toReturn).build();
		}
		else
		{
			log.error("Report message not found for id: "+ testID);
			return Response.status(notFoundCode).build();
		}	
	}
}
