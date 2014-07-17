package TestingHarness;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * @author as2388
 * @author kls82
 */
// full path to here is /tester/API/
public class TestService implements TestServiceInterface {
    // initialise log4j logger
    static Logger log = Logger.getLogger(TestService.class.getName());

    /*
     * Maps the ID of a test to in-progress tests. TestService is responsible
     * for generating unique IDs Class users are responsible for remembering the
     * ID so that they can poll its status and get its report when done TODO:
     * should we be keeping these in a DB instead?
     */
    private static Map<String, Tester> ticksInProgress;
    private WebInterface gitProxy;
    private TesterFactory testerFactory = new TesterFactory();

    public TestService() {
        buildGitProxy();
        if (ticksInProgress == null) {
            log.info("ticksInProgress Initialised");
            ticksInProgress = new HashMap<String, Tester>();
        }
    }

    /** Expected constructor */
    public TestService(WebInterface gitProxy, TesterFactory testerFactory) {
        this.gitProxy = gitProxy;
        this.testerFactory = testerFactory;
        if (ticksInProgress == null) { 
            ticksInProgress = new HashMap<String, Tester>();
            log.info("ticksInProgress Initialised");
        }
    }

    /** Constructor used for testing */
    private void buildGitProxy() {
        log.info("Creating new client for accessing git API");
        ResteasyClient c = new ResteasyClientBuilder().build();
        ResteasyWebTarget t = c.target("http://localhost:8080/TestingSystem/");
        // to be provided as dependency by git team
        gitProxy = t.proxy(WebInterface.class);
    }

    /** Constructor used for testing */
    public TestService(Map<String, Tester> ticksInProgress) {
        TestService.ticksInProgress = ticksInProgress;
    }

    /** {@inheritDoc} */
    @Override
    public String runNewTest(@QueryParam("repoAddress") String repoAddress) throws IOException, WrongFileTypeException {
        log.info("New test request received");
        // generate a UUID for the tester
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (ticksInProgress.containsKey(id));
        log.info(id + ": runNewTest: test creation started");

        Map<String, LinkedList<String>> tests = new HashMap<String, LinkedList<String>>();

        // add corresponding git file to tests
        log.info(id
                + ": runNewTest: Connecting to git API to obtain list of files in repo");
        LinkedList<String> filesInRepo = gitProxy.listFiles(repoAddress);
        log.info(id + ": runNewTest: List of files obtained");
        LinkedList<String> filesToTest = new LinkedList<String>();
        LinkedList<String> staticTests = new LinkedList<String>();
        for (String file : filesInRepo) {
            String ext = file.substring(file.lastIndexOf('.') + 1,
                    file.length());
            // TODO: note that dynamic tests will also be java files! - what to
            // do? as2388: the dynamic tests should be in a different repository
            if (ext.equals("java")) {
                filesToTest.add(file);
                log.info("added java file to test : " + file);
            } else if (ext.equals("xml")) {
                staticTests.add(file);
                log.info("added test file: " + file);
            } else {
                throw new WrongFileTypeException();
            }
        }

        log.info(id + ": runNewTest: creating Tester object");

        for (String test : staticTests) {
            tests.put(test, filesToTest);
            log.info("test added");
        }

        // create a new Tester object
        final Tester tester = testerFactory.createNewTester(tests, repoAddress);

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
    @Override
    public String pollStatus(@QueryParam("testID") String testID)
            throws TestIDNotFoundException {
        log.info(testID + ": pollStatus: request received");
        if (ticksInProgress.containsKey(testID)) {
            String status = ticksInProgress.get(testID).getStatus();
            log.info(testID + ": pollStatus: returning " + status);
            return status;
        } else {
            log.error(testID + ": pollStatus: testID not found");
            throw new TestIDNotFoundException(testID);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Report getReport(@QueryParam("testID") String testID)
            throws TestIDNotFoundException, CheckstyleException,
            WrongFileTypeException, IOException, TestStillRunningException {
        log.info(testID + ": getReport: request received");
        if (ticksInProgress.containsKey(testID)) {
            //if the test finished
            if (ticksInProgress.get(testID).getStatus().equals("complete"))
            {
                Tester tester = ticksInProgress.get(testID);
                // Assuming we're not responsible for storing tests, we should
                // remove the test at this point. So I am.
                ticksInProgress.remove(testID);
                if (tester.getFailCause() == null) {
                    // test completed normally; return the report
                    log.info(testID + ": getReport: report found; returning it");
                    return tester.getReport();
                } else {
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
                    } else if (failCause instanceof WrongFileTypeException) {
                        throw (WrongFileTypeException) failCause;
                    } else {
                        throw (IOException) failCause;
                    }
                }
            }
            else
            {
                throw new TestStillRunningException();
            }

        } else {
            log.error(testID + ": getReport: testID not found");
            throw new TestIDNotFoundException(testID);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getException() {
        log.info("getException: accessing get exception method on git API");
        // TODO: query git API for an exception
        //gitProxy.__something__();
        
        log.error("getException: Didn't get an exception back :(");
        return "Exception not generated by git team";
    }

    /**
     * Used for manually testing that the three main API functions work
     * 
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
