package testingharness;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import configuration.ConfigurationLoader;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.CategoryNotInReportException;
import publicinterfaces.ITestSetting;
import publicinterfaces.Report;
import publicinterfaces.ReportResult;
import publicinterfaces.Severity;
import publicinterfaces.StaticOptions;
import publicinterfaces.Status;
import uk.ac.cam.cl.dtg.teaching.containers.api.TestsApi;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.GitRepositoryCloneException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.InvalidNameException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestInstanceNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInstance;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestStep;
import uk.ac.cam.cl.dtg.teaching.exceptions.SerializableException;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Runs all the static and dynamic analysis tests for a given tick, and produces a report,
 * stored in memory until the object is destroyed
 * 
 * @author as2388
 * @author kls82
 *
 */
public class Tester {
    static Logger log = LoggerFactory.getLogger(Tester.class); //initialise log4j logger

    private Report report; //Report object into which all the report items will ultimately go
    private Exception failCause = null; //if the report fails, save it here so that it can be thrown when the report is requested
    private String repoName;
    //Maps the path of a test (either static or dynamic) to a list of paths to files on which that test should be run
    private Map<ITestSetting, LinkedList<String>> testingQueue;
    private boolean dynamicPass;

    /**
     * Creates a new Tester
     */
    public Tester(Map<ITestSetting, LinkedList<String>> testingQueue, String repoName, String commitId)  {
        this.testingQueue = testingQueue;
        this.report = new Report(repoName, commitId);
        this.repoName = repoName;
    }

    /**
     * Runs all tests required by the tick on all files required to be tested by the tick.
     * Note: only runs static analysis if dynamic analysis succeeded
     */
    public void runTests(String crsId, String tickId, String commitId, WebInterface gitProxy, Status status, TestsApi testerProxyTest, String dynamicContainerId, String dynamicTestId)
    {
        log.info(crsId + " " + tickId + " " + commitId + ": Tick analysis started");	     

        try {
            int noOfTests = testingQueue.size()+2;
            report.setNoOfTests(noOfTests+2);
            status.setCurrentPositionInQueue(0);
            status.setMaxProgress(noOfTests + 3);
            status.setInfo("Loading tests");            
            
            String repo = ConfigurationLoader.getConfig().getRepoTemplate() + crsId + "/" + tickId.replace(',', '/');
            
            if (dynamicContainerId != null && dynamicTestId != null) {
            	log.info(crsId + " " + tickId + " " + commitId + ": Running dynamic tests");
            	runDynamicTests(testerProxyTest,crsId,tickId,dynamicContainerId,dynamicTestId,status,repo);
            	log.info(crsId + " " + tickId + " " + commitId + ": Dynamic tests complete");
            }
            //TODO: will need to fill in checkstyles error bit
            else {
            	log.info(crsId + " " + tickId + " " + commitId + ": No dynamic tests specified");
            	dynamicPass = true;
            }
            
            if (dynamicPass)
            {
            	log.info(crsId + " " + tickId + " " + commitId + ": Running static checks");
                runStaticTests(commitId,gitProxy,status);
                log.info(crsId + " " + tickId + " " + commitId + ": Static checks complete");
            }  
            else {
            	log.info(crsId + " " + tickId + " " + commitId + ": Dynamic tests failed");
            }
            
            report.calculateProblemStatuses();

            log.info("Tick analysis finished successfully");
        }	
        catch (CheckstyleException | IOException | RepositoryNotFoundException e)
        {
            log.error("Tick analysis failed. Exception message: " + e.getMessage());
            report.setTestResult(ReportResult.UNDEFINED);
            failCause = e;
        }
        finally
        {
        	//TODO: Is this a viable option? error will appear as ticker comment but won't be able to sign up so
        	//shouldn't be overwritten
        	if (this.failCause != null) {
        		this.report.setTickerComments("Test failed to complete, error: " + this.failCause.getMessage());
        	}
            Report reportToAdd = this.report;
            TestService.getDatabase().addReport(crsId, tickId, reportToAdd);
            status.complete();
        }   
    }

    /**
     * Runs all dynamic analysis tests required by the tick
     */
    private void runDynamicTests(TestsApi testerProxyTest, String crsId, String tickId, String dynamicContainerId, String dynamicTestId, Status status, String repo)
    {
    	log.info(tickId + " " + crsId +": started dynamic analysis");
        try {
			TestInstance dynamicTest = testerProxyTest.startTest(crsId, dynamicContainerId, dynamicTestId, repo , "TODO: privateKey");
			String dynamicTestStatus = testerProxyTest.getTestStatus(crsId, dynamicContainerId).getStatus();
			int progress = 0;
			status.setProgress(1);
			status.setInfo("Compiling code");
			//poll status
			while(dynamicTestStatus.equals(TestInstance.STATUS_UNINITIALIZED) || dynamicTestStatus.equals(TestInstance.STATUS_STARTING) || dynamicTestStatus.equals(TestInstance.STATUS_RUNNING)) {
				log.info(tickId + " " + crsId +": status poll");
				dynamicTestStatus = testerProxyTest.getTestStatus(crsId, dynamicContainerId).getStatus();
				progress = testerProxyTest.getTestStatus(crsId, dynamicContainerId).getResults().size();
				if(progress != 1) {
					status.setProgress(2);
					status.setInfo("Running correctness tests");
				}
				delay(1000);
			}
			//test is finished, find result
			if(status.equals(TestInstance.STATUS_FAILED)) {
				log.info(tickId + " " + crsId +": failed dynamic tests");
				dynamicPass = false;
			}
			else {
				dynamicPass = true;
				log.info(tickId + " " + crsId +": passed dynamic tests");
			}
			log.info(tickId + " " + crsId +": putting dynamic test results in report");
			unpackResults(dynamicTest.getException(),dynamicTest.getResults());
			testerProxyTest.removeTest(crsId, dynamicContainerId);
        } 
        catch (GitRepositoryCloneException | InvalidNameException | TestNotFoundException | TestInstanceNotFoundException e) {
        	log.error("Dynamic analysis failed. Exception message: " + e.getMessage());
            report.setTestResult(ReportResult.UNDEFINED);
            failCause = e;
		}
        log.info("Dynamic analysis complete");
    }

    private void unpackResults(SerializableException exception,List<TestStep> results) {
		if (exception == null) {
			//tests ran successfully so put results in report
			log.info("There was no exception from the tester, writing report as normal");
			for(TestStep result: results) {
				log.info("Adding " + result.getName());
				if(result.getStatus().equals(TestStep.STATUS_FAIL)){
					log.info(result.getName() + " result = error");
					report.addProblem(result.getName() , Severity.ERROR);
				}
				else if(result.getStatus().equals(TestStep.STATUS_WARNING)) {
					log.info(result.getName() + " result = warning");
					report.addProblem(result.getName() , Severity.WARNING);
				}
				else {
					log.info(result.getName() + " result = pass");
					report.addProblem(result.getName() , Severity.WARNING);
					//passed so don't add details and move on to next one
					break;
				}
				log.info(result.getName() + " writing details for warning/error");
				String message = "";
				for(String m : result.getMessages()){
					message += m + "\n";
				}
				message += "Expected result: " + result.getExpected() + "\n";
				message += "Obtained result: " + result.getActual() + "\n";
				try {
					report.addDetail(result.getName() , result.getFileName(), (int) result.getStartLine(), message);
				} 
				catch (CategoryNotInReportException e) {
					// TODO Auto-generated catch block
					//should never be called
					log.error("category not found in report");
					e.printStackTrace();
				}
			}
		}
		else {
			//there was an exception so put what went wrong in report
			log.info("Dynamic tester threw an exception, see comment in report for details");
			report.setTestResult(ReportResult.UNDEFINED);
			report.setTickerComments(exception.getMessage());
			dynamicPass = false;
		}
	}

	/**
     * Runs all static analysis tests required by the tick
     * @throws CheckstyleException	Thrown if Checkstyle fails to run
     * @throws IOException 			Thrown if creating/making temp files fails
     * @throws RepositoryNotFoundException 		Thrown by git API
     */
    private void runStaticTests(String commitId, WebInterface gitProxy, Status status) throws CheckstyleException, IOException, RepositoryNotFoundException    {
        //get static tests from testingQueue
        Map<StaticOptions, LinkedList<String>> staticTests = getStaticTestItems(this.testingQueue);
        //run Static analysis on each test
        for (Map.Entry<StaticOptions, LinkedList<String>> e : staticTests.entrySet()) {
            delay(1000);
            status.addProgress();
            runStaticAnalysis(e.getKey(), e.getValue(), commitId, gitProxy);
        }
    }

    /**
     * Extracts only the tests with .java extensions from the testing queue
     * @param testingQueue   Map from which to extract .java tests
     * @return               Map containing only .java tests
     */
    public HashMap<String, LinkedList<String>> getDynamicTestItems(Map<String, LinkedList<String>> testingQueue)
    {
        HashMap<String, LinkedList<String>> mapReturn = new HashMap<>();
        for (Map.Entry<String, LinkedList<String>> e : testingQueue.entrySet()) {
            if ("java".equals(FilenameUtils.getExtension(e.getKey()))) {
                mapReturn.put(e.getKey(), e.getValue());
            }
        }
        return mapReturn;
    }

    /**
     * Extracts only the tests with .xml extensions from the testing queue
     * @param testingQueue   Map from which to extract .xml tests
     * @return               Map containing only .xml tests
     */
    public HashMap<StaticOptions, LinkedList<String>> getStaticTestItems(Map<ITestSetting, LinkedList<String>> testingQueue)
    {
        HashMap<StaticOptions, LinkedList<String>> mapReturn = new HashMap<>();
        for (Map.Entry<ITestSetting, LinkedList<String>> e : testingQueue.entrySet()) {
            if (e.getKey().getClass() == StaticOptions.class) {
                mapReturn.put((StaticOptions) e.getKey(), e.getValue());
            }
        }
        return mapReturn;
    }

    /**
     * Run CheckStyle, set up with the given config file, on all the files to which it should be applied
     * 
     * @param configFileName		Path to the config file needed by CheckStyle
     * @param fileNames				A list of paths to the files on which the static analyses tests are to be performed
     * @throws CheckstyleException	Thrown if Checkstyle fails to run
     * @throws IOException 			Thrown if creating/making temp files fails
     * @throws RepositoryNotFoundException 		Thrown by git API
     */
    public void runStaticAnalysis(StaticOptions configFileName, List<String> fileNames, String commitId, WebInterface gitProxy) throws CheckstyleException, IOException, RepositoryNotFoundException {
           StaticParser.test(configFileName, fileNames, report, repoName, commitId, gitProxy);
    }

    //GETTERS
    public Exception getFailCause()
    {
        return this.failCause;
    }

    public Report getReport() {
        return report;
    }

    private void delay(int timeMS) {
        try {
            Thread.sleep(timeMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
