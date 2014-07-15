package TestingHarness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;


/**
 * @see TestServiceInterface for JavaDoc
 * @author as2388
 */
 //full path to here is /tester/API/
public class TestService implements TestServiceInterface
{
	static Logger log = Logger.getLogger(TestService.class.getName());	//initialise log4j logger
	
	/* Maps the ID of a test to in-progress tests. 
	 * TestService is responsible for generating unique IDs
	 * Class users are responsible for remembering the ID so that they can poll its status and get its report when done */
	private static Map<String, Tester> ticksInProgress;	//TODO: should we be keeping these in a DB instead?
	
	public TestService()
	{
		if (ticksInProgress == null)
		{
			log.info("ticksInProgress Initialised");
			ticksInProgress = new HashMap<String, Tester>();
		}
	}

	@Override
	public String runNewTest(@QueryParam("repoAddress") String repoAddress)
	{
		log.info("New test request received");
		//TODO: use git team's API to get files to test
		Map<String, LinkedList<String>> tests = new HashMap<String, LinkedList<String>>();
		
		//TODO: add corresponding git files to tests
		
		//create a new Tester object
		final Tester tester = new Tester(tests);
		
		//generate a UUID for the tester
		String id;
		do
		{
			id=UUID.randomUUID().toString();
		} while (TestService.ticksInProgress.containsKey(id));
		
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
		//return Response.status(200).entity(id).build();
		
		return id;
	}	
	
	@Override
	public String pollStatus(@QueryParam("testID") String testID) throws TestIDNotFoundException
	{
		log.info("Poll request received for id: " + testID);
		if (ticksInProgress.containsKey(testID))
		{
			log.info("Poll request for id " + testID + " returned: " + ticksInProgress.get(testID).getStatus());
			return ticksInProgress.get(testID).getStatus();
		}
		else
		{
			log.error("ID " + testID + " of poll request could not be found");
			throw new TestIDNotFoundException(testID);
		}
	}
	
	@Override
	public Report getReport(@QueryParam("testID") String testID) throws TestIDNotFoundException,
																		CheckstyleException, 
																		WrongFileTypeException, 
																		TestHarnessException
	{
		log.info("Report get request received for id: " + testID);
		if (ticksInProgress.containsKey(testID))
		{
			Tester tester= ticksInProgress.get(testID);
			//Assuming we're not responsible for storing tests, we should remove the test at this point. So I am.
			ticksInProgress.remove(testID);
			log.info("Report message found for id: " + testID);
			if (!(tester.getStatus().equals("error")))
			{
				//test completed normally; return the report
				return tester.getReport();
			}
			else
			{
				//test failed, throw the exception causing the problem.
				Exception failCause = tester.getFailCause();
				if (failCause instanceof CheckstyleException)
				{
					throw (CheckstyleException) failCause;
				}
				else if (failCause instanceof WrongFileTypeException)
				{
					throw (WrongFileTypeException) failCause;
				}
				else
				{
					throw (TestHarnessException) failCause;
				}
			}
			
		}
		else
		{
			log.error("Report message not found for id: "+ testID);
			throw new TestIDNotFoundException(testID);
		}
	}
	
	@Override
	public String test(@QueryParam("testID") String testID) throws TestIDNotFoundException
	{
		ResteasyClient c = new ResteasyClientBuilder().build();
		ResteasyWebTarget t = c.target("http://localhost:8080/TestingSystem/");
		TestServiceInterface proxy = t.proxy(TestServiceInterface.class);	
		return proxy.pollStatus(testID);
	}
}
