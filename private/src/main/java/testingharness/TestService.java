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

import com.puppycrawl.tools.checkstyle.Main;

import configuration.ConfigurationLoader;
import privateinterfaces.IDBReportManager;
import privateinterfaces.IDBXMLTestsManager;
import publicinterfaces.FailedToMakeTestException;
import publicinterfaces.ITestService;
import publicinterfaces.ITestSetting;
import publicinterfaces.NoCommitsToRepoException;
import publicinterfaces.NoSuchTestException;
import publicinterfaces.Report;
import publicinterfaces.ReportNotFoundException;
import publicinterfaces.ReportResult;
import publicinterfaces.Severity;
import publicinterfaces.StaticOptions;
import publicinterfaces.Status;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TestStillRunningException;
import publicinterfaces.TickNotInDBException;
import publicinterfaces.UserNotInDBException;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the interface ITestService
 * 
 * @author as2388
 * @author kls82
 *
 */

public class TestService implements ITestService {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestService.class);
    private static final IDBReportManager dbReport = new MongoDBReportManager(Mongo.getDb());
    private static final IDBXMLTestsManager dbXMLTests = new MongoDBXMLTestsManager(Mongo.getDb());
    public static WebInterface gitProxy;
    private static Map<String, Tester> ticksInProgress = new HashMap<>();

    public TestService() {
        log.debug("Initialising TestService (including proxy to git service)");

	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forGit = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);

        log.debug("TestService initialised");
    }

    /** {@inheritDoc} */
    @Override
    public String runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId,
                           @PathParam("repoName") String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException, NoCommitsToRepoException {

        log.debug(crsId + " " + tickId + ": Preparing test suite");

        log.debug(crsId + " " + tickId + ": Querying git API for SHA of head of repository: " + repoName);
        final String commitId = gitProxy.resolveCommit(repoName, "HEAD");
        log.debug(crsId + " " + tickId + ": SHA for repository: " + repoName + " is " + commitId);
    	
        if (commitId == null) {
            log.warn(crsId + " " + tickId + ": commitId is null; throwing NoCommitsToRepoException");
        	throw new NoCommitsToRepoException();
        }

        Map<ITestSetting, LinkedList<String>> tests = new HashMap<>();
        LinkedList<String> filesToTest = new LinkedList<>();

        //collect files to test from git
        log.debug(crsId + " " + tickId + " " + commitId
                + ": Connecting to git API to obtain list of files in repo");
        List<String> fileListFromGit = gitProxy.listFiles(repoName , commitId);
        log.debug(crsId + " " + tickId + " " + commitId + ": file list obtained from git API");

        for (String file : fileListFromGit) {
           if (FilenameUtils.getExtension(file).equals("java"))
            {
                log.debug(crsId + " " + tickId + ": Adding java file to test: " + file);
                filesToTest.add(file);
            }
        }
        //obtain static tests to run on files according to what tick it is
        log.debug(crsId + " " + tickId + ": Loading test settings for tickId");
        List<StaticOptions> staticTests = dbXMLTests.getTestSettings(tickId);
    	
        for (StaticOptions test : staticTests) {
        	if (test.getCheckedIndex() != 0) {
        		tests.put(test, filesToTest);
        		log.debug(crsId + " " + tickId + ": Added stylistic check: " + test.getText());
        	}
        }

        // create a new Tester object
        final Tester tester = new Tester(tests, repoName, commitId);

        // add the object to the list of in-progress tests
        //this key should be unique as they shouldn't be able to run the same tests more than once
        //at the same time
        //TODO: move this to top of function?
        if (!ticksInProgress.containsKey(crsId + tickId)) {
        	ticksInProgress.put(crsId + tickId, tester);
            log.debug(crsId + " " + tickId + ": Added to map of ticks in progress");
        }
        else {
            log.warn(crsId + " " + tickId + ": Test is already running; throwing TestStillRunningException");
        	throw new TestStillRunningException("You can't submit this tick as you already have the same one running");
        }
        
        // start the test in an asynchronous thread
        log.debug(crsId + " " + tickId + ": Creating async thread in which test will run");
        new Thread(new Runnable() {
            public void run() {
                asyncTestRunner(tester, crsId, tickId, commitId);
            }
        }).start();

        log.debug(crsId+ " " + tickId + ": Created async thread; returning");
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
        log.debug(crsId+ " " + tickId + ": Removed test from map");
    }

    /** {@inheritDoc} */
    @Override
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws NoSuchTestException {
        log.debug(crsId+ " " + tickId + ": poll status request received");
        //if the test is currently running then return its status from memory
        if (ticksInProgress.containsKey(crsId + tickId)) {
            log.debug(crsId+ " " + tickId + ": Found in map of ticks in progress, returning...");
            return ticksInProgress.get(crsId + tickId).getStatus();
        }
        else {
            //the test is not currently in memory, so try to get it from the DB
            try {
                log.debug(crsId+ " " + tickId + ": If report exists in db, will return status from that");
                return dbReport.getLastStatus(crsId, tickId);
            } catch (UserNotInDBException | TickNotInDBException e) {
                log.warn(crsId+ " " + tickId + ": Report not in map of ticks in progress or db, throwing NoSuchTestException");
                throw new NoSuchTestException();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Report getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        log.debug(crsId+ " " + tickId + ": get last report request received...");
        return  dbReport.getLastReport(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
        log.debug(crsId+ " " + tickId + ": get all reports request received...");
        return dbReport.getAllReports(crsId, tickId);
        //return null;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentReportData(@PathParam("crsId") String crsId) throws UserNotInDBException {
        dbReport.removeUserReports(crsId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws TickNotInDBException, UserNotInDBException {
        dbReport.removeUserTickReports(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
	public Response createNewTest(@PathParam("tickId") String tickId, List<StaticOptions> checkstyleOpts) {
    	log.debug("creating new test with tickId: " + tickId);
    	try {
    		TestService.dbXMLTests.addNewTest(tickId, checkstyleOpts);
    		log.debug("XMLSettings created for tickId: " + tickId);
    		return Response.ok().build();
    	}
    	catch (TestIDAlreadyExistsException e1) {
    		try {
	    		TestService.dbXMLTests.update(tickId, checkstyleOpts);
	    		log.debug("Updated XMLSettings for tickId: " + tickId);
	    		return Response.ok().build();
    		}
    		catch (TestIDNotFoundException e2) {
    			return Response.status(418).build();
    			//should never happen!
    		}
    	}
	}

    public static IDBReportManager getDatabase() {
    	return TestService.dbReport;
    }
    
    public static IDBXMLTestsManager getTestsDatabase() {
    	return TestService.dbXMLTests;
    }

    /** {@inheritDoc} */
    @Override
    public void test() throws NoSuchTestException {
        ResteasyClient rc = new ResteasyClientBuilder().build();

        ResteasyWebTarget t = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
        ITestService proxy = t.proxy(ITestService.class);

        proxy.pollStatus("as2388", "tick-test");
        System.out.println("done");
    }

    /** {@inheritDoc} */
	@Override
	public Response getTestFiles() {
	    log.debug("request received to get default java style settings");
		List<StaticOptions> toReturn = new LinkedList<>();
		try {
			URI dir = (TestService.class.getClassLoader().getResource("checkstyleFiles")).toURI();
			System.out.println(dir);
			File path = new File(dir);
			String[] files = path.list();
			File[] filesToRead = path.listFiles();
			log.info("no of files found = " + files.length);
			int i = 0;
			for (String file : files) {		
				BufferedReader b = new BufferedReader(new FileReader(filesToRead[i]));
				String line;
				String fileContents = "";
				try {
					while ((line = b.readLine()) != null) {
						fileContents += line + System.getProperty("line.separator");
					}
				} 
				catch (IOException e) {
					//TODO
					b.close();
					e.printStackTrace();
				} 
				b.close();
				String name = file.substring(0,file.length()-4);
	            toReturn.add(new StaticOptions
	              		(ConfigurationLoader.getConfig().getDescription(name), 
	               				ConfigurationLoader.getConfig().getSeverity(name),
	               						fileContents));
	            i++;
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return Response.status(200).entity(toReturn).build();
		} 
	}

	/** {@inheritDoc} */
	@Override
	public void setTickerResult(String crsid, String tickId,
			ReportResult tickerResult, String tickerComments, String commitId, long date)
			throws UserNotInDBException, TickNotInDBException, ReportNotFoundException {
		log.debug("setting ticker result...");
		Date d = new Date(date);
		log.debug("transformed date to: " + d);
		TestService.dbReport.editReportTickerResult(crsid,tickId,tickerResult,tickerComments, commitId, d);
		log.debug("result set; returning...");
	}

	@Override
	public Response getTestFiles(String tickId) throws TestIDNotFoundException {
		log.debug("get test files request received for " + tickId);
		List<StaticOptions> toReturn = TestService.dbXMLTests.getTestSettings(tickId);
		log.debug("no of files found = " + toReturn.size());
		return Response.status(200).entity(toReturn).build(); 
	}
}
