package testingharness;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import configuration.ConfigurationLoader;
import exceptions.TestIDNotFoundException;
import exceptions.TestNameNotFoundException;
import exceptions.TestStillRunningException;
import exceptions.WrongFileTypeException;
import gitapidependencies.HereIsYourException;
import gitapidependencies.RepositoryNotFoundException;
import gitapidependencies.WebInterface;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.TestServiceInterface;
import reportelements.Report;
import reportelements.Severity;
import reportelements.Status;

import javax.ws.rs.QueryParam;

import java.io.IOException;
import java.util.*;

/* import uk.ac.cam.cl.git.public_interfaces.WebInterface; */

/**
 * Default implementation of TestServiceInterface
 * @author as2388
 * @author kls82
 */
// full path to here is /rest/tester/API/
public class TestService implements TestServiceInterface {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestService.class);
    //TODO: implement database - this stores the settings for each tick that has been set
    private static Map<String, List<XMLTestSettings>> availableTestSettings;
    /*
     * Maps the ID of a test to in-progress tests. TestService is responsible
     * for generating unique IDs Class users are responsible for remembering the
     * ID so that they can poll its status and get its report when done
     */
    private static Map<String, Tester> ticksInProgress;
    private WebInterface gitProxy;
    private TesterFactory testerFactory = new TesterFactory();

    /** Expected constructor */
    public TestService() {    
        buildGitProxy();
        if (ticksInProgress == null) {
            log.info("ticksInProgress Initialised");
            ticksInProgress = new HashMap<>();
            availableTestSettings = new HashMap<>();
        }
    }
    
    /** Constructor used for testing */
    public TestService(WebInterface gitProxy, TesterFactory testerFactory) {
        this.gitProxy = gitProxy;
        this.testerFactory = testerFactory;
        if (ticksInProgress == null) { 
            ticksInProgress = new HashMap<>();
            log.info("ticksInProgress Initialised");
        }
    }

    /** Initialises the proxy to the git API */
    private void buildGitProxy() {
        log.info("Creating new client for accessing git API");
        ResteasyClient c = new ResteasyClientBuilder().build();
        ResteasyWebTarget t = c.target(ConfigurationLoader.getConfig().getGitAPIPath());
        // to be provided as dependency by git team
        gitProxy = t.proxy(WebInterface.class);
    }

    /** Constructor used for testing */
    public TestService(Map<String, Tester> ticksInProgress) {
        TestService.ticksInProgress = ticksInProgress;
    }

    /** {@inheritDoc} */
    public String runNewTest(@QueryParam("repoName") String repoName, @QueryParam("testName") String testName) throws IOException, WrongFileTypeException,
            RepositoryNotFoundException, TestNameNotFoundException {
        log.info("New test request received");
        // generate a UUID for the tester
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (ticksInProgress.containsKey(id));
        log.info(id + ": runNewTest: test creation started");

        Map<XMLTestSettings, LinkedList<String>> tests = new HashMap<>();

        // add corresponding git file to tests
        /* Response response = gitProxy.listFiles(repoName); */
        
        LinkedList<String> filesToTest = new LinkedList<>();
        
        //collect files to test from git
        log.info(id
                + ": runNewTest: Connecting to git API to obtain list of files in repo");
        filesToTest = gitProxy.listFiles(repoName);
        log.info(id + ": request successful");

        for (String file : filesToTest) {
            switch (FilenameUtils.getExtension(file))
            {
                case "java":
                    log.info("added java file to test : " + file);
                    break;
                default:
                    throw new WrongFileTypeException();
            }
        }
        
        //obtain static tests to run on files according to what tick it is
        List<XMLTestSettings> staticTests = new LinkedList<>();
        
        if(TestService.availableTestSettings.containsKey(testName)) {
        	log.info("obtained test settings");
        	staticTests = TestService.availableTestSettings.get(testName);
        	System.out.println(staticTests.size());
        }
        else {
        	throw new TestNameNotFoundException("The test requested does not exist!");
        }
        
        log.info(id + ": runNewTest: creating Tester object");

        for (XMLTestSettings test : staticTests) {
            tests.put(test, filesToTest);
            log.info("test added: " + test );
        }

        // create a new Tester object
        final Tester tester = testerFactory.createNewTester(tests, repoName);

        // add the object to the list of in-progress tests
        ticksInProgress.put(id, tester);

        // start the test in an asynchronous thread
        new Thread(new Runnable() {
            public void run() {
                tester.runTests();
            }
        }).start();

        log.info(id + ": runNewTest: Test started");
        
        return id;
    }

    /** {@inheritDoc} */
    public Status pollStatus(@QueryParam("testID") String testID)
            throws TestIDNotFoundException {
        log.info(testID + ": pollStatus: request received");
        if (ticksInProgress.containsKey(testID)) {
        	Tester tester = ticksInProgress.get(testID);
            Status status = ticksInProgress.get(testID).getStatus();
            log.info(testID + ": pollStatus: returning " + status);
            System.out.println("in tester as tests are running report has " + tester.getReport().getProblems().size() + " entries");
            return status;
        } else {
            log.error(testID + ": pollStatus: testID not found");
            throw new TestIDNotFoundException(testID);
        }
    }

    /** {@inheritDoc} */
    public Report getReport(@QueryParam("testID") String testID)
            throws TestIDNotFoundException, CheckstyleException,
            RepositoryNotFoundException, IOException, TestStillRunningException {
        log.info(testID + ": getReport: request received");
        if (ticksInProgress.containsKey(testID)) {
            //check if the test if finished
            if (ticksInProgress.get(testID).getStatus().getInfo().equals("complete"))
            {
                Tester tester = ticksInProgress.get(testID);
                //API users are responsible for managing storage of reports, so delete the report from the
                //map of reports in progress
                ticksInProgress.remove(testID);
                if (tester.getFailCause() == null) {
                    // test completed normally; return the report
                    log.info(testID + ": getReport: report found; returning it");
                    return tester.getReport();
                }
                else {
                    /*
                     * test failed, throw the exception causing the problem.
                     * Exceptions are thrown lazily because we need to run Tester
                     * instances in separate threads, so that we can return the ID
                     * of the test to the UI team when they call runNewTest. The UI
                     * team needs this ID to poll the status of this test. To do
                     * this, exceptions are stored in a variable of type Exception,
                     * but to provide better error information to the ticking team,
                     * the Exception is casted back to its original type before
                     * being lazily thrown, hence why this bit is so yucky
                     */
                    log.error(testID
                            + ": getReport: Report didn't complete successfully, lazily throwing the exception it generated");
                    Exception failCause = tester.getFailCause();
                    if (failCause instanceof CheckstyleException) {
                        throw (CheckstyleException) failCause;
                    } else if (failCause instanceof RepositoryNotFoundException) {
                        throw (RepositoryNotFoundException) failCause;
                    } else {
                        throw (IOException) failCause;
                    }
                }
            }
            else
            {
                throw new TestStillRunningException(testID);
            }
        }
        else {
            log.error(testID + ": getReport: testID not found");
            throw new TestIDNotFoundException(testID);
        }
    }

    /** {@inheritDoc} */
    public String getException() throws HereIsYourException 
    {
        log.info("getException: accessing get exception method on git API");
        
        //this call should throw an exception!
        //Don't bother catching it, handling exceptions is the UI team's problem
        gitProxy.getMeAnException();    
        //if we get to here, gitProxy.getMeAnException didn't actually throw an exception
        log.error("getException: Didn't get an exception back :(");
        return "Exception not generated by git team";
    }

    /**
     * Used for manually testing that the three main API functions work.
     * Will eventually be removed
     * @param testID                    optional parameter, depending on manual test to be performed
     * @return                          depends on test
     * @throws TestIDNotFoundException
     * @throws HereIsYourException 
     */
    public String test(@QueryParam("testID") String testID)
            throws TestIDNotFoundException, HereIsYourException {
        ResteasyClient c = new ResteasyClientBuilder().build();
        ResteasyWebTarget t = c.target("http://localhost:8080/TestingSystem/");
        TestServiceInterface proxy = t.proxy(TestServiceInterface.class);
        
        //String result = proxy.getException();
        String result = "Hello World";
        System.out.println(result);
        return result;
        
        //return proxy.pollStatus(testID);
    }

	@Override
	public void createNewTest(/* String testName, List<XMLTestSettings> checkstyleOpts */) {
		String testName = "exampleTest";
		List<XMLTestSettings> checkstyleOpts = new LinkedList<>();
		checkstyleOpts.add(new XMLTestSettings("emptyBlocks",Severity.ERROR,"empty blocks between braces"));
		checkstyleOpts.add(new XMLTestSettings("longVariableDeclaration",Severity.WARNING,"use of 'L' to declare long"));
		checkstyleOpts.add(new XMLTestSettings("unusedImports",Severity.WARNING,"unused imports"));
		checkstyleOpts.add(new XMLTestSettings("TODOorFIXME",Severity.ERROR,"TODO or FIXME still in code"));
		log.info("all tests added for " + testName);
		TestService.availableTestSettings.put(testName, checkstyleOpts);
	}
	
	//TODO: getAvailableCheckstyleTests()
	//TODO: ? danger of repeats ? addACheckstyleTest(xml content, filename, description);
}
