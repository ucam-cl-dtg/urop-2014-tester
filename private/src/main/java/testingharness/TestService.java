package testingharness;

import database.Mongo;
import database.MongoDBReportManager;
import database.MongoDBXMLTestsManager;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import privateinterfaces.IDBReportManager;
import privateinterfaces.IDBXMLTestsManager;
import publicinterfaces.ITestService;
import publicinterfaces.NoSuchTestException;
import publicinterfaces.Report;
import publicinterfaces.Severity;
import publicinterfaces.Status;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TestStillRunningException;
import publicinterfaces.TickNotInDBException;
import publicinterfaces.UserNotInDBException;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import javax.ws.rs.PathParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestService implements ITestService {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestService.class);
    private static final IDBReportManager dbReport = new MongoDBReportManager(Mongo.getDb());
    private static final IDBXMLTestsManager dbXMLTests = new MongoDBXMLTestsManager(Mongo.getDb());
    public static WebInterface gitProxy;
    private static Map<String, Tester> ticksInProgress = new HashMap<>();

    public TestService() {
    	System.out.println("setting up proxies");
	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forGit = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);
	   
	    System.out.println("git proxy set up");
    }

    /** {@inheritDoc} */
    @Override
    public String runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId,
                           @PathParam("repoName") String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException {
    	Map<XMLTestSettings, LinkedList<String>> tests = new HashMap<>();
        
        LinkedList<String> filesToTest = new LinkedList<>();
        
        //collect files to test from git
        log.info(crsId + " " + tickId
                + ": runNewTest: Connecting to git API to obtain list of files in repo");
        List<String> fileListFromGit = gitProxy.listFiles(repoName);
        log.info(crsId + " " + tickId + ": request successful");

        for (String file : fileListFromGit) {
           if (FilenameUtils.getExtension(file).equals("java"))
            {
                log.info("adding java file to test : " + file);
                filesToTest.add(file);
            }
        }
        //obtain static tests to run on files according to what tick it is
        List<XMLTestSettings> staticTests = dbXMLTests.getTestSettings(tickId);
        
        log.info(crsId + " " + tickId + ": runNewTest: creating Tester object");
    	
        for (XMLTestSettings test : staticTests) {
            tests.put(test, filesToTest);
            log.info("test added: " + test );
        }

        // create a new Tester object
        final Tester tester = new Tester(tests, repoName);

        // add the object to the list of in-progress tests
        //this key should be unique as they shouldn't be able to run the same tests more than once
        //at the same time
        if (!ticksInProgress.containsKey(crsId + tickId)) {
        	ticksInProgress.put(crsId + tickId, tester);
        }
        else {
        	throw new TestStillRunningException("You can't submit this tick as you already have the same one running");
        }
        
        // start the test in an asynchronous thread
        new Thread(new Runnable() {
            public void run() {
                asyncTestRunner(tester, crsId, tickId, "//TODO: set commit Id");
            }
        }).start();

        log.info(crsId+ " " + tickId + ": runNewTest: Test started");

        return "Test Started";
    }

    /**
     * Runs a test, then deletes the tester object from the map of in progress tests
     * Note: named async because this function is expected to be run in a separate thread
     * @param tester    The tester object configured to be run
     * @param crsId     As required by tester.runTests()
     * @param tickId    As required by tester.runTests()
     * @param commitId  As required by tester.runTests()
     */
    private void asyncTestRunner(final Tester tester, final String crsId, final String tickId, final String commitId) {
        tester.runTests(crsId, tickId, commitId);

        //once tests have run, remove the tester from the map of in-progress tests
        assert ticksInProgress.containsKey(crsId + tickId);
        System.out.println("removing...");
        ticksInProgress.remove(crsId + tickId);
    }

    /** {@inheritDoc} */
    @Override
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws NoSuchTestException {
        //if the test is currently running then return its status from memory
        if (ticksInProgress.containsKey(crsId + tickId)) {
            return ticksInProgress.get(crsId + tickId).getStatus();
        }
        else {
            //the test is not currently in memory, so try to get it from the DB
            try {
                return dbReport.getLastStatus(crsId, tickId);
            } catch (UserNotInDBException | TickNotInDBException e) {
                throw new NoSuchTestException();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Report getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        return dbReport.getLastReport(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        return dbReport.getAllReports(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentReportData(@PathParam("crsId") String crsId) throws UserNotInDBException {
        dbReport.removeUserReports(crsId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws TestIDNotFoundException, UserNotInDBException {
        dbReport.removeUserTickReports(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
	public void createNewTest(@PathParam("tickId") String tickId /* List<XMLTestSettings> checkstyleOpts */)
            throws TestIDAlreadyExistsException {
    	log.info("adding tests for " + tickId);
		List<XMLTestSettings> checkstyleOptsTemp = new LinkedList<>();
		checkstyleOptsTemp.add(new XMLTestSettings("emptyBlocks",Severity.ERROR,"Empty blocks"));
		checkstyleOptsTemp.add(new XMLTestSettings("unusedImports",Severity.WARNING,"Unused Imports"));
		checkstyleOptsTemp.add(new XMLTestSettings("TODOorFIXME",Severity.ERROR,"TODOs and FIXMEs"));
        checkstyleOptsTemp.add(new XMLTestSettings("illegalCatch",Severity.ERROR,"Illegal catches"));
        checkstyleOptsTemp.add(new XMLTestSettings("illegalThrow",Severity.ERROR,"Illegal throws"));
        checkstyleOptsTemp.add(new XMLTestSettings("indentation",Severity.WARNING,"Indentation"));
        checkstyleOptsTemp.add(new XMLTestSettings("noProblem", Severity.WARNING, "No problem"));
        checkstyleOptsTemp.add(new XMLTestSettings("longVariableDeclaration",Severity.WARNING,"Lower case long assignment"));
		log.info("all tests added for " + tickId);
		
		//add to database
	    dbXMLTests.addNewTest(tickId, checkstyleOptsTemp);
	    log.info("added test to database");
	}
	
    public static IDBReportManager getDatabase() {
    	return TestService.dbReport;
    }
    
	//TODO: getAvailableCheckstyleTests()
}
