package testingharness;

import Security.SecurityManager;
import configuration.ConfigurationLoader;
import database.Mongo;
import database.MongoDBReportManager;

import database.MongoDBTestsManager;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.ConfigurationLoader;
import privateinterfaces.IDBReportManager;
import privateinterfaces.IDBTestsManager;
import publicinterfaces.ITestService;
import publicinterfaces.ITestSetting;
import publicinterfaces.NoCommitsToRepoException;
import publicinterfaces.NoSuchTestException;
import publicinterfaces.Report;
import publicinterfaces.ReportNotFoundException;
import publicinterfaces.ReportResult;
import publicinterfaces.StaticOptions;
import publicinterfaces.Status;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TestStillRunningException;
import publicinterfaces.TickNotInDBException;
import publicinterfaces.TickSettings;
import publicinterfaces.UserNotInDBException;

import privateinterfaces.IDBReportManager;
import privateinterfaces.IDBTestsManager;
import publicinterfaces.*;

import threadcontroller.MyExecutor;
import threadcontroller.TesterThread;
import uk.ac.cam.cl.dtg.teaching.containers.api.TestsApi;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInfo;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import javax.ws.rs.InternalServerErrorException;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
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
    private static final ExecutorService threadExecutor = MyExecutor.newFixedThreadPool(ConfigurationLoader.getConfig().getThreadNumber());
    // private static final ExecutorService threadExecutor = Executors.newFixedThreadPool(ConfigurationLoader.getConfig().getThreadNumber());
    private static final IDBReportManager dbReport = new MongoDBReportManager(Mongo.getDb());
    private static final IDBTestsManager dbTicks = new MongoDBTestsManager(Mongo.getDb());
    private WebInterface gitProxy;
    private TestsApi testerProxyTest;
    private static final ConcurrentMap<String, TesterThread> ticksInProgress = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, TesterThread> getTicksInProgress() {
		return ticksInProgress;
	}

    public TestService() {
        log.info("Initialising TestService (including proxy to git service)");

	    ResteasyClient rc = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forGit = rc.target(ConfigurationLoader.getConfig().getGitAPIPath());
	    gitProxy = forGit.proxy(WebInterface.class);

	    ResteasyClient rc2 = new ResteasyClientBuilder().build();
	    
	    ResteasyWebTarget forTester = rc2.target(ConfigurationLoader.getConfig().getTesterPath());
	    testerProxyTest = forTester.proxy(TestsApi.class);	    
        log.debug("TestService initialised");
    }

    /** {@inheritDoc} */
    @Override
    public String runNewTest(String securityToken, final String crsId, final String tickId, String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException,
            NoCommitsToRepoException {
        SecurityManager.validateSecurityToken(securityToken);

        log.debug(crsId + " " + tickId + ": Preparing test suite");

        log.debug(crsId + " " + tickId + ": Querying git API for SHA of head of repository: " + repoName);
        final String commitId = gitProxy.resolveCommit(SecurityManager.getSecurityToken(), repoName, "HEAD");
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
        List<String> fileListFromGit = gitProxy.listFiles(SecurityManager.getSecurityToken(), repoName , commitId);
        
        log.debug(crsId + " " + tickId + " " + commitId + ": file list obtained from git API");

        for (String file : fileListFromGit) {
        	//TODO: add check for other types of file eg .c etc
           if (FilenameUtils.getExtension(file).equals("java"))
            {
                log.debug(crsId + " " + tickId + " " + commitId + ": Adding java file to test: " + file);
                filesToTest.add(file);
            }
        }
        
        log.debug(crsId + " " + tickId + " " + commitId + ": all files loaded");
        //obtain static tests to run on files according to what tick it is
        log.debug(crsId + " " + tickId + " " + commitId + ": Loading test settings for tickId");
        List<StaticOptions> staticTests = dbTicks.getTestSettings(tickId);
    	
        for (StaticOptions test : staticTests) {
        	if (test.getCheckedIndex() != 0) {
        		tests.put(test, filesToTest);
        		log.debug(crsId + " " + tickId + " " + commitId + ": Added stylistic check: " + test.getText());
        	}
        }
        log.debug(crsId + " " + tickId + " " + commitId + ": Tests loaded");
        
        // create a new Tester object
        final Tester tester = new Tester(tests, repoName, commitId);

        TesterThread thread = new TesterThread(tester, crsId, tickId, commitId, gitProxy, testerProxyTest);
        
        // add the object to the list of in-progress tests atomically
        //this key should be unique as they shouldn't be able to run the same tests more than once
        //at the same time
        if (ticksInProgress.putIfAbsent(crsId + tickId, thread) != null) {
        	log.warn(crsId + " " + tickId + " " + commitId + ": Test is already running; throwing TestStillRunningException");
        	throw new TestStillRunningException("You can't submit this tick as you already have the same one running");
        }
        
        // start the test in an asynchronous thread
        TestService.threadExecutor.execute(thread);
        
        if(MyExecutor.getWaitingQueue().contains(thread)) {
        	log.debug(crsId + " " + tickId + " " + commitId + ": Is waiting in queue");
        	Status status = new Status(getQueuePosition(crsId,tickId));
        	thread.setStatus(status);
        }
        
        return "Test Started";
    }

    /** {@inheritDoc} */
    @Override
    public Status pollStatus(String securityToken, String crsId, String tickId)
            throws NoSuchTestException {
        SecurityManager.validateSecurityToken(securityToken);

        log.debug(crsId+ " " + tickId + ": poll status request received");
        //if the test is currently running then return its status from memory
        if (ticksInProgress.containsKey(crsId + tickId) && !ticksInProgress.get(crsId + tickId).getStatus().getInfo().equals("Complete")) {
        	//if test is in queue then recalc position then return status
        	log.debug(crsId+ " " + tickId + ": active thread found");
        	TesterThread thread = ticksInProgress.get(crsId + tickId);
        	if(MyExecutor.getWaitingQueue().contains(thread)) {
        		log.debug(crsId+ " " + tickId + ": thread still in wait queue");
        		thread.getStatus().updateQueueStatus(getQueuePosition(crsId,tickId));
        	}
            log.debug(crsId+ " " + tickId + ": returning status");
            return ticksInProgress.get(crsId + tickId).getStatus();
        }
        else {
            //the test is not currently in memory, so try to get it from the DB
        	log.debug(crsId+ " " + tickId + ": thread no longer active, attempting to access database");
            try {
                log.debug(crsId+ " " + tickId + ": returning status from database");
                return dbReport.getLastStatus(crsId, tickId);
            } catch (UserNotInDBException | TickNotInDBException e) {
                log.warn(crsId+ " " + tickId + ": Report not in map of ticks in progress or db, throwing NoSuchTestException");
                throw new NoSuchTestException();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Report getLastReport(String securityToken, String crsId, String tickId)
            throws UserNotInDBException, TickNotInDBException {
        SecurityManager.validateSecurityToken(securityToken);

        log.debug(crsId+ " " + tickId + ": get last report request received...");
        return  dbReport.getLastReport(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Report> getAllReports(String securityToken, String crsId, String tickId)
            throws UserNotInDBException, TickNotInDBException {
        SecurityManager.validateSecurityToken(securityToken);

        log.debug(crsId+ " " + tickId + ": get all reports request received...");
        return dbReport.getAllReports(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentReportData(String securityToken, String crsId)
            throws UserNotInDBException {
        SecurityManager.validateSecurityToken(securityToken);

    	log.debug(crsId + ": delete request received");
        dbReport.removeUserReports(crsId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStudentTick(String securityToken, String crsId, String tickId)
            throws TickNotInDBException, UserNotInDBException {
        SecurityManager.validateSecurityToken(securityToken);

    	log.debug(crsId+ " " + tickId + ": delete request received");
        dbReport.removeUserTickReports(crsId, tickId);
    }

    /** {@inheritDoc} */
    @Override
	public Response createNewTest(String securityToken, String tickId, List<StaticOptions> checkstyleOpts, String containerId, String testId) {
        SecurityManager.validateSecurityToken(securityToken);

    	log.debug("creating new test with tickId: " + tickId);

    	try {
    		TestService.dbTicks.addNewTest(tickId, checkstyleOpts , containerId , testId);
    		log.info("Settings created for tickId: " + tickId);
    		return Response.ok().build();
    	}
    	catch (TestIDAlreadyExistsException e1) {
    		try {
	    		TestService.dbTicks.update(tickId, checkstyleOpts , containerId , testId);
	    		log.info("Updated settings for tickId: " + tickId);
	    		return Response.ok().build();
    		}
    		catch (TestIDNotFoundException e2) {
    			System.out.println("Error");
    			return Response.status(500).entity(e2).build();
    			//should never happen!
    		}
    	}
	}

    public static IDBReportManager getDatabase() {
    	return TestService.dbReport;
    }
    
    public static IDBTestsManager getTestsDatabase() {
    	return TestService.dbTicks;
    }

    /** {@inheritDoc} */
	@Override
	public TickSettings getTestFiles(String securityToken) {
        SecurityManager.validateSecurityToken(securityToken);

	    log.debug("request received to get default java style settings");
	    TickSettings settingsToReturn  = new TickSettings();
		List<StaticOptions> toReturn = new LinkedList<>();
		try {
			URI dir = (TestService.class.getClassLoader().getResource("checkstyleFiles")).toURI();
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
					log.error("IO failed, unable to read from " + file);
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
			settingsToReturn.setCheckstylesFiles(toReturn);
		} catch (URISyntaxException e) {
			// this should never happen
			e.printStackTrace();
		}
		finally {
			log.debug("returning default tests");
			return settingsToReturn;
		} 
	}

	/** {@inheritDoc} */
	@Override
	public void setTickerResult(String securityToken, String crsid, String tickId, ReportResult tickerResult,
                                String tickerComments, String commitId, long date)
			throws UserNotInDBException, TickNotInDBException, ReportNotFoundException {
		log.debug("setting ticker result...");
		Date d = new Date(date);
		log.debug("transformed recieved millisecond date to: " + d);
		TestService.dbReport.editReportTickerResult(crsid,tickId,tickerResult,tickerComments, commitId, d);
		log.debug("result set");
	}

	/** {@inheritDoc} */
	@Override
	public TickSettings getTestFiles(String securityToken , String tickId) throws TestIDNotFoundException {
		SecurityManager.validateSecurityToken(securityToken);
		
		log.info("get test files request received for " + tickId);
		List<StaticOptions> toReturn = TestService.dbTicks.getTestSettings(tickId);
		if (toReturn != null) {
			log.info("no of checkstyle settings found = " + toReturn.size());
		}
		else {
			log.info("no checkstyle settings found for " + tickId);
		}
		String testId = TestService.dbTicks.getTestId(tickId);
		log.info("testId found for " + tickId + ", creating object...");
		TickSettings settings = new TickSettings(toReturn , testId);
		log.info("returning object...");
		return settings; 
	}
	
	public int getQueuePosition(String crsId, String tickId) {
		Iterator<Runnable> iterator = MyExecutor.getWaitingQueue().iterator();
		int i = 1;
	       while (iterator.hasNext()) {
	           TesterThread t = (TesterThread) iterator.next();
	           if (t.getCrsId().equals(crsId) && t.getTickId().equals(tickId)) {
	        	   return i;
	           }
	           i++;
	        }
	    return 0;
	}

	@Override
	public Response getAvailableDynamicTests(String securityToken) {
		SecurityManager.validateSecurityToken(securityToken);
		
		log.info("request for dynamic tests recieved");
		List<TestInfo> tests =  null;
		try {
			log.info("accessing dynamic API");
			try {
				tests = testerProxyTest.listTests();
			} catch (InternalServerErrorException e) {
				System.err.println(e.getResponse().getEntity());
			}
			if(tests != null) {
				log.info(tests.size() + " tests found, returning");
				return Response.status(200).entity(tests).build();
			}
			else{
				log.info("no available dynamic tests found");
				return Response.status(200).entity(tests).build();
			}
		} catch (TestNotFoundException e) {
			// TODO how to handle??? - error should probably be thrown
			return Response.status(500).entity(e).build();
		}	
	}
}
