package TestingHarness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


public class TestService
{
	/* Maps the ID of a test to in-progress tests. 
	 * TestService is responsible for generating unique ID's
	 * Class user's are responsible for remembering the ID so that they can poll it's status and get it's report when done */
	private static Map<String, Tester> ticksInProgress;	//TODO: should we be keeping these in a DB instead?
	private int notFoundCode = 404;
	
	private TestService()
	{
		if (ticksInProgress == null)
		{
			ticksInProgress = new HashMap<String, Tester>();
		}
	}
	
	@POST
	@Path("/runNewTest")
	public Response runNewTest(@QueryParam("testData") String serializedTestData)
	{
		//TODO: deserialise the parameter to a Map.
		//for now:
		Map<String, LinkedList<String>> tests = new HashMap<String, LinkedList<String>>();
		
		//create a new Tester object
		Tester tester = new Tester(tests);
		
		//TODO: generate unique ID. For now, all tests have ID 0
		String id="0";
		
		//add the object to the list of in-progress tests
		ticksInProgress.put(id, tester);
		
		//start the test
		tester.runTests();
		
		//return ok		
		return Response.status(200).entity(id).build();
	}
	
	
	/**
	 * Returns the status of the test with ID testID if a test with testID exists, otherwise returns an error code
	 * @param testID
	 * @return			
	 */
	@POST
	@Path("/pollStatus")
	@Produces("text/plain")
	public Response pollStatus(@QueryParam("testID") String testID)
	{
		if (ticksInProgress.containsKey(testID))
		{
			return Response.status(200).entity(ticksInProgress.get(testID)).build();
		}
		else
		{
			//TODO: investigate whether this is the best status code to be returning
			return Response.status(notFoundCode).build();
		}
	}
	
	/**
	 * 
	 * @param testID
	 * @return
	 */
	@POST
	@Path("/getReport")
	@Produces("application/json")
	public Response getReport(@QueryParam("testID") String testID)
	{
		if (ticksInProgress.containsKey(testID))
		{
			Report toReturn = ticksInProgress.get(testID).getReport();
			//Assuming we're not responsible for storing tests, we should remove the test at this point
			ticksInProgress.remove(testID);
			return Response.status(200).entity(toReturn).build();
		}
		else
		{
			return Response.status(notFoundCode).build();
		}	
	}
}
