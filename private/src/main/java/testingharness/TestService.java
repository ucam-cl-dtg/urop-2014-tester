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
	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forGit = rc.target(configuration.ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);
    }

    /** {@inheritDoc} */
    @Override
    public String runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId,
                           @PathParam("repoName") String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException, NoCommitsToRepoException {
    	
    	Map<ITestSetting, LinkedList<String>> tests = new HashMap<>();
        final String commitId = gitProxy.resolveCommit(repoName, "HEAD");
    	
        if (commitId == null) {
        	throw new NoCommitsToRepoException();
        }
        
        LinkedList<String> filesToTest = new LinkedList<>();
        
        //collect files to test from git
        log.info(crsId + " " + tickId + " " + commitId 
                + ": runNewTest: Connecting to git API to obtain list of files in repo");
        List<String> fileListFromGit = gitProxy.listFiles(repoName , commitId);
        log.info(crsId + " " + tickId + " " + commitId + ": request successful");

        for (String file : fileListFromGit) {
           if (FilenameUtils.getExtension(file).equals("java"))
            {
                log.info("adding java file to test : " + file);
                filesToTest.add(file);
            }
        }
        //obtain static tests to run on files according to what tick it is
        List<StaticOptions> staticTests = dbXMLTests.getTestSettings(tickId);
        
        log.info(crsId + " " + tickId + " " + commitId + " runNewTest: creating Tester object");
    	
        for (StaticOptions test : staticTests) {
        	if (test.getCheckedIndex() != 0) {
        		tests.put(test, filesToTest);
        		log.info("test added: " + test );
        	}
        }

        // create a new Tester object
        final Tester tester = new Tester(tests, repoName, commitId);

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
        return  dbReport.getLastReport(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException {
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
    	log.info("adding tests for " + tickId);
    	try {
    		TestService.dbXMLTests.addNewTest(tickId, checkstyleOpts);
    		log.info("new test created with id " + tickId);
    		return Response.ok().build();
    	}
    	catch (TestIDAlreadyExistsException e1) {
    		try {
	    		TestService.dbXMLTests.update(tickId, checkstyleOpts);
	    		log.info(tickId + "has been updated");
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
	    log.info("get test files request received");
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
			ReportResult tickerResult, String tickerComments, String commitId)
			throws UserNotInDBException, TickNotInDBException, ReportNotFoundException {
		log.info("setting ticker result");
		TestService.dbReport.editReportTickerResult(crsid,tickId,tickerResult,tickerComments, commitId);
		log.info("result set");
	}

	@Override
	public Response getTestFiles(String tickId) throws TestIDNotFoundException {
		log.info("get test files request received for " + tickId);
		List<StaticOptions> toReturn = TestService.dbXMLTests.getTestSettings(tickId);
		log.info("no of files found = " + toReturn.size());
		return Response.status(200).entity(toReturn).build(); 
	}
}
