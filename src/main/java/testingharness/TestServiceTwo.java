package testingharness;

import database.IDBReportManager;
import database.Mongo;
import database.MongoDBReportManager;
import database.TickNotInDBException;
import database.UserNotInDBException;
import exceptions.TestIDNotFoundException;
import exceptions.TestStillRunningException;
import exceptions.WrongFileTypeException;
import gitapidependencies.RepositoryNotFoundException;
import gitapidependencies.WebInterface;
import reportelements.AbstractReport;
import reportelements.Severity;
import reportelements.SimpleReport;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import privateinterfaces.MongoTestsInterface;
import publicinterfaces.ITestService;
import reportelements.Status;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import java.io.IOException;
import java.util.*;

public class TestServiceTwo implements ITestService {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestServiceTwo.class);
    private static final IDBReportManager db = new MongoDBReportManager(Mongo.getDb());
    private static final MongoTestsInterface testProxy;
    private static final WebInterface gitProxy;
    private static Map<String,Tester> ticksInProgress = new HashMap<String,Tester>();
    
    //TODO: will this work?
    static { 
	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forTests = rc.target(configuration.ConfigurationLoader.getConfig().getDatabaseTestsPath());
	    testProxy = forTests.proxy(MongoTestsInterface.class);
	    
	    ResteasyWebTarget forGit = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);
    }
    
    @Override
    public void runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId, @PathParam("repoName") String repoName, @QueryParam("commitId") final String commitId) throws TestStillRunningException, IOException, RepositoryNotFoundException, WrongFileTypeException, TestIDNotFoundException {
    	Map<XMLTestSettings, LinkedList<String>> tests = new HashMap<>();

        // add corresponding git file to tests
        /* Response response = gitProxy.listFiles(repoName); */
        
        LinkedList<String> filesToTest = new LinkedList<>();
        
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
                    throw new WrongFileTypeException();
            }
        }
        
        //obtain static tests to run on files according to what tick it is
        List<XMLTestSettings> staticTests = testProxy.getTestSettings(tickId);
        
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
        if (!ticksInProgress.containsKey(crsId+tickId)) {
        	ticksInProgress.put(crsId+tickId, tester);
        }
        else {
        	throw new TestStillRunningException("You can't submit this tick as you already have the same one running");
        }
        
        // start the test in an asynchronous thread
        new Thread(new Runnable() {
            public void run() {
                tester.runTests(crsId,tickId,commitId);
            }
        }).start();

        log.info(crsId+ " " + tickId + ": runNewTest: Test started");
        
        /*
    	AbstractReport reportToAdd = new SimpleReport();
        reportToAdd.addDetail("bad indentation", Severity.WARNING, "eg.java", 7, "Expected 12 spaces, found 10");
        db.addReport(crsId, tickId, commitId, reportToAdd);
        */
    }

    @Override
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {
        return null;
    }

    @Override
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) throws UserNotInDBException, TickNotInDBException {
        return db.getLastReport(crsId, tickId);
    }

    @Override
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) throws UserNotInDBException, TickNotInDBException {
        return db.getAllReports(crsId, tickId);
    }

    @Override
    public void deleteStudentReportData(@PathParam("crsId") String crsId) {

    }

    @Override
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {

    }

    @Override
    public void deleteStudentTickCommit(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {

    }
    
    @Override
	public void createNewTest(@PathParam("tickId") String tickId, List<XMLTestSettings> checkstyleOpts) {
		String testName = "exampleTest";
		List<XMLTestSettings> checkstyleOptsTemp = new LinkedList<>();
		checkstyleOptsTemp.add(new XMLTestSettings("emptyBlocks",Severity.ERROR,"empty blocks between braces"));
		checkstyleOptsTemp.add(new XMLTestSettings("longVariableDeclaration",Severity.WARNING,"use of 'L' to declare long"));
		checkstyleOptsTemp.add(new XMLTestSettings("unusedImports",Severity.WARNING,"unused imports"));
		checkstyleOptsTemp.add(new XMLTestSettings("TODOorFIXME",Severity.ERROR,"TODO or FIXME still in code"));
		log.info("all tests added for " + testName);
		
		//add to database
	    testProxy.addNewTest(tickId, checkstyleOptsTemp);
	    log.info("added test to database");
	}
	
    public static IDBReportManager getDatabase() {
    	return TestServiceTwo.db;
    }
    
	//TODO: getAvailableCheckstyleTests()
}
