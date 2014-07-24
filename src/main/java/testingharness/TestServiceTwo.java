package testingharness;

import database.Mongo;
import database.MongoDBReportManager;
import database.MongoDBXMLTestsManager;
import exceptions.*;
import gitapidependencies.RepositoryNotFoundException;
import gitapidependencies.WebInterface;
import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import privateinterfaces.IDBReportManager;
import privateinterfaces.IDBXMLTestsManager;
import publicinterfaces.ITestService;
import reportelements.AbstractReport;
import reportelements.Severity;
import reportelements.Status;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestServiceTwo implements ITestService {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestServiceTwo.class);
    private static final IDBReportManager db = new MongoDBReportManager(Mongo.getDb());
    private static IDBXMLTestsManager testDb;
    private static WebInterface gitProxy;
    private static Map<String, Tester> ticksInProgress = new HashMap<>();
    
    //TODO: will this work?
    public TestServiceTwo() { 
    	System.out.println("setting up proxys");
	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forGit = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);
	   
	    System.out.println("git proxy set up");
	    
	    testDb = new MongoDBXMLTestsManager(Mongo.getDb());
    }
    
    @Override
    public void runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId,
                           @PathParam("repoName") String repoName, @QueryParam("commitId") final String commitId)
            throws IOException, RepositoryNotFoundException, WrongFileTypeException,
            TestStillRunningException, TestIDNotFoundException {
    	Map<XMLTestSettings, LinkedList<String>> tests = new HashMap<>();

        // add corresponding git file to tests
        /* Response response = gitProxy.listFiles(repoName); */
        
        LinkedList<String> filesToTest;
        
        //collect files to test from git
        log.info(crsId + " " + tickId
                + ": runNewTest: Connecting to git API to obtain list of files in repo");
        filesToTest = gitProxy.listFiles(repoName);
        log.info(crsId + " " + tickId + ": request successful");

        for (String file : filesToTest) {
            switch (FilenameUtils.getExtension(file))
            {
                case "java":
                    log.info("added java file to test : " + file);
                    break;
                default:
                    //TODO: should we really  be throwing an exception? Isn't it conceivable that a student's repo might contain
                    //TODO: a non .java file? (1A Java tick 1 needs a .txt file, for example)
                    throw new WrongFileTypeException();
            }
        }
         //obtain static tests to run on files according to what tick it is
        List<XMLTestSettings> staticTests = testDb.getTestSettings(tickId);
        
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
                asyncTestRunner(tester, crsId, tickId, commitId);
            }
        }).start();

        log.info(crsId+ " " + tickId + ": runNewTest: Test started");
        
        /*
    	AbstractReport reportToAdd = new SimpleReport();
        reportToAdd.addDetail("bad indentation", Severity.WARNING, "eg.java", 7, "Expected 12 spaces, found 10");
        db.addReport(crsId, tickId, commitId, reportToAdd);
        */
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
        ticksInProgress.remove(crsId + tickId);
    }

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
                return db.getLastStatus(crsId, tickId);
            } catch (UserNotInDBException | TickNotInDBException e) {
                throw new NoSuchTestException();
            }
        }
    }

    @Override
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        return db.getLastReport(crsId, tickId);
    }

    @Override
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        return db.getAllReports(crsId, tickId);
    }

    @Override
    public void deleteStudentReportData(@PathParam("crsId") String crsId) throws UserNotInDBException {
        db.removeUserReports(crsId);
    }

    @Override
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws TestIDNotFoundException, UserNotInDBException {
        db.removeUserTickReports(crsId, tickId);
    }
    
    @Override
	public void createNewTest(@PathParam("tickId") String tickId /* List<XMLTestSettings> checkstyleOpts */)
            throws TestIDAlreadyExistsException {
    	log.info("adding tests for " + tickId);
		List<XMLTestSettings> checkstyleOptsTemp = new LinkedList<>();
		checkstyleOptsTemp.add(new XMLTestSettings("emptyBlocks",Severity.ERROR,"empty blocks between braces"));
		checkstyleOptsTemp.add(new XMLTestSettings("longVariableDeclaration",Severity.WARNING,"use of 'L' to declare long"));
		checkstyleOptsTemp.add(new XMLTestSettings("unusedImports",Severity.WARNING,"unused imports"));
		checkstyleOptsTemp.add(new XMLTestSettings("TODOorFIXME",Severity.ERROR,"TODO or FIXME still in code"));
		log.info("all tests added for " + tickId);
		
		//add to database
	    testDb.addNewTest(tickId, checkstyleOptsTemp);
	    log.info("added test to database");
	}
	
    public static IDBReportManager getDatabase() {
    	return TestServiceTwo.db;
    }
    
	//TODO: getAvailableCheckstyleTests()
}
