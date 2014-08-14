package testingharness;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.ITestSetting;
import publicinterfaces.Report;
import publicinterfaces.ReportResult;
import publicinterfaces.StaticOptions;
import publicinterfaces.Status;
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
    private Exception failCause; //if the report fails, save it here so that it can be thrown when the report is requested
    private String repoName;
    //Maps the path of a test (either static or dynamic) to a list of paths to files on which that test should be run
    private Map<ITestSetting, LinkedList<String>> testingQueue;


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
    public void runTests(String crsId, String tickId, String commitId, WebInterface gitProxy, Status status)
    {
        log.info("Tick analysis started");	     

        try {
            int noOfTests = testingQueue.size();
            report.setNoOfTests(noOfTests);
            status.setCurrentPositionInQueue(0);
            status.setMaxProgress(noOfTests + 1);
            status.setInfo("Loading tests");            

            runDynamicTests();

            if (dynamicPass())
            {
                runStaticTests(commitId,gitProxy,status);
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
            Report reportToAdd = this.report;
            TestService.getDatabase().addReport(crsId, tickId, reportToAdd);
            status.complete();
        }   
    }

    /**
     * Runs all dynamic analysis tests required by the tick
     */
    private void runDynamicTests()
    {
    	/*
        log.info("Started dynamic analysis");
        Map<String, LinkedList<String>> dynamicTests = getDynamicTestItems(this.testingQueue);
        //TODO: actually run some tests
        log.info("Dynamic analysis complete");
        */
    }

    /**
     * Runs all static analysis tests required by the tick
     * @throws CheckstyleException	Thrown if Checkstyle fails to run
     * @throws IOException 			Thrown if creating/making temp files fails
     * @throws RepositoryNotFoundException 		Thrown by git API
     */
    private void runStaticTests(String commitId, WebInterface gitProxy, Status status) throws CheckstyleException, IOException, RepositoryNotFoundException    {
        log.info("Starting static analysis");
        //get static tests from testingQueue
        Map<StaticOptions, LinkedList<String>> staticTests = getStaticTestItems(this.testingQueue);
        //run Static analysis on each test
        for (Map.Entry<StaticOptions, LinkedList<String>> e : staticTests.entrySet()) {
            delay(2000);
            status.addProgress();
            runStaticAnalysis(e.getKey(), e.getValue(), commitId, gitProxy);
        }
        log.info("Static analysis complete");
    }

    /**
     * Check if all the dynamic tests were passed
     * @return  true if all dynamic analysis tests were passed; false otherwise
     */
    private boolean dynamicPass()
    {
        //TODO
        return true;
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
