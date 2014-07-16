package TestingHarness;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * For JavaDoc, see TestServiceInterface
 * @author as2388
 * @author kls2510
 */
// full path to here is /tester/API/
public class TestService implements TestServiceInterface {
	//initialise log4j logger
	static Logger log = Logger.getLogger(TestService.class.getName()); 

	/*
	 * Maps the ID of a test to in-progress tests. TestService is responsible
	 * for generating unique IDs Class users are responsible for remembering the
	 * ID so that they can poll its status and get its report when done
	 */
	private static Map<String, Tester> ticksInProgress; // TODO: should we be
														// keeping these in a DB
														// instead?

	public TestService() {
		if (ticksInProgress == null) {
			log.info("ticksInProgress Initialised");
			ticksInProgress = new HashMap<String, Tester>();
		}
	}

	public TestService(Map<String, Tester> ticksInProgress) {
		TestService.ticksInProgress = ticksInProgress;
	}

	@Override
	public String runNewTest(@QueryParam("repoAddress") String repoAddress) throws IOException{
		log.info("New test request received");

		Map<String, LinkedList<String>> tests = new HashMap<String, LinkedList<String>>();

		// add corresponding git file to tests
		log.info("Creating new client");
		ResteasyClient c = new ResteasyClientBuilder().build();
		ResteasyWebTarget t = c.target("http://localhost:8080/TestingSystem/");
		// to be provided as dependency by git team
		WebInterface proxy = t.proxy(WebInterface.class);

		log.info("Connecting to git API");
		LinkedList<String> filesInRepo = proxy
				.listFiles("removeThisExampleString" + repoAddress);
		log.info("Files obtained");
		LinkedList<String> filesToTest = new LinkedList<String>();
		LinkedList<String> staticTests = new LinkedList<String>();
		for (String file : filesInRepo) {
			String ext = file.substring(file.lastIndexOf('.') + 1,
					file.length());
			// TODO: note that dynamic tests will also be java files! - what to
			// do? as2388: the dynamic tests should be in a different repository
			if (ext.equals("java")) {
				filesToTest.add(file);
			} else if (ext.equals("xml")) {
				staticTests.add(file);
			} else {
				System.out.println("File not recognised");
			}
		}

		for (String test : staticTests) {
			tests.put(test, filesToTest);
		}

		c.close();

		// create a new Tester object
		final Tester tester = new Tester(tests);

		// generate a UUID for the tester
		String id;
		do {
			id = UUID.randomUUID().toString();
		} while (TestService.ticksInProgress.containsKey(id));

		// add the object to the list of in-progress tests
		ticksInProgress.put(id, tester);

		// start the test in an asynchronous thread
		new Thread(new Runnable() {
			public void run() {
				tester.runTests();
			}
		}).start();

		log.info("New test started; assigned id: " + id);

		return id;
	}

	@Override
	public String pollStatus(@QueryParam("testID") String testID)
			throws TestIDNotFoundException {
		log.info("Poll request received for id: " + testID);
		if (ticksInProgress.containsKey(testID)) {
			String status = ticksInProgress.get(testID).getStatus();
			log.info("Poll request for id " + testID + " returned: " + status);
			return status;
		} else {
			log.error("ID " + testID + " of poll request could not be found");
			throw new TestIDNotFoundException(testID);
		}
	}

	@Override
	public Report getReport(@QueryParam("testID") String testID)
			throws TestIDNotFoundException, CheckstyleException,
			WrongFileTypeException, TestHarnessException {
		log.info("Report get request received for id: " + testID);
		if (ticksInProgress.containsKey(testID)) {
			Tester tester = ticksInProgress.get(testID);
			// Assuming we're not responsible for storing tests, we should
			// remove the test at this point. So I am.
			ticksInProgress.remove(testID);
			log.info("Report message found for id: " + testID);
			if (!(tester.getStatus().equals("error"))) {
				// test completed normally; return the report
				return tester.getReport();
			}
			else
			{
				/*test failed, throw the exception causing the problem.
				Exceptions are thrown lazily because we need to run Tester instances in separate threads,
				so that we can return the ID of the test to the UI team when they call runNewTest.
				The UI team needs this ID to poll the status of this test.
				  To do this, exceptions are stored in a variable of type Exception, but to provide better 
				error information to the ticking team, the Exception is casted back to its original type
				before being lazily thrown, hence why this bit is so yucky*/
				Exception failCause = tester.getFailCause();
				if (failCause instanceof CheckstyleException) {
					throw (CheckstyleException) failCause;
				} else if (failCause instanceof WrongFileTypeException) {
					throw (WrongFileTypeException) failCause;
				} else {
					throw (TestHarnessException) failCause;
				}
			}
		} else {
			log.error("Report message not found for id: " + testID);
			throw new TestIDNotFoundException(testID);
		}
	}
	
	@Override
	public String getException()
	{
		//TODO: query git API for an exception
		return "Exception not generated by git team";
	}
	
	/**
	 * Used for manually testing that the three main API functions work
	 * @param testID
	 * @return
	 * @throws TestIDNotFoundException
	 */
	public String test(@QueryParam("testID") String testID)
			throws TestIDNotFoundException {
		ResteasyClient c = new ResteasyClientBuilder().build();
		ResteasyWebTarget t = c.target("http://localhost:8080/TestingSystem/");
		TestServiceInterface proxy = t.proxy(TestServiceInterface.class);
		return proxy.pollStatus(testID);
	}
}
