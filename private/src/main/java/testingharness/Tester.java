package testingharness;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.Report;
import publicinterfaces.Status;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;

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
    private Status status; 
    private Exception failCause; //if the report fails, save it here so that it can be thrown when the report is requested
    private String repoName;
    //Maps the path of a test (either static or dynamic) to a list of paths to files on which that test should be run
    private Map<XMLTestSettings, LinkedList<String>> testingQueue;


    /**
     * Creates a new Tester
     */
    public Tester(Map<XMLTestSettings, LinkedList<String>> testingQueue, String repoName)  {
        this.testingQueue = testingQueue;
        this.report = new Report();
        this.repoName = repoName;
        System.out.println(testingQueue.size());
    }

    /**
     * Runs all tests required by the tick on all files required to be tested by the tick.
     * Note: only runs static analysis if dynamic analysis succeeded
     * @throws uk.ac.cam.cl.git.api.RepositoryNotFoundException 
     */
    public void runTests(String crsId, String tickId, String commitId)
    {
        log.info("Tick analysis started");	     

        try {
            int noOfTests = testingQueue.size();
            this.status = new Status("loading tests", noOfTests + 1);            

            runDynamicTests();

            if (dynamicPass())
            {
                runStaticTests();
            }           

            log.info("Tick analysis finished successfully");

            //TODO: remove this. For now, print result to the console
            printReport();
        }	
        catch (CheckstyleException | IOException | RepositoryNotFoundException e)
        {
            log.error("Tick analysis failed. Exception message: " + e.getMessage());
            failCause = e;
        }
        finally
        {
            this.status.complete();
    	    //TODO: should we make another interface?
            Report reportToAdd = this.report;
            //TODO: should we add the report even if it failed to generate?
            TestService.getDatabase().addReport(crsId, tickId, reportToAdd);
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
     * @throws CheckstyleException
     * @throws IOException
     * @throws uk.ac.cam.cl.git.api.RepositoryNotFoundException 
     */
    private void runStaticTests() throws CheckstyleException, IOException, RepositoryNotFoundException, uk.ac.cam.cl.git.api.RepositoryNotFoundException
    {
        log.info("Starting static analysis");
        //get static tests from testingQueue
        Map<XMLTestSettings, LinkedList<String>> staticTests = getStaticTestItems(this.testingQueue);
        //run Static analysis on each test
        for (Map.Entry<XMLTestSettings, LinkedList<String>> e : staticTests.entrySet()) {
            status.addProgress();
            System.out.println("running test " + e.getKey().getTestFile());
            runStaticAnalysis(e.getKey(), e.getValue());
            System.out.println("in tester the report has " + report.getProblems().size() + " items");
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
    public HashMap<XMLTestSettings, LinkedList<String>> getStaticTestItems(Map<XMLTestSettings, LinkedList<String>> testingQueue)
    {
        HashMap<XMLTestSettings, LinkedList<String>> mapReturn = new HashMap<>();
        for (Map.Entry<XMLTestSettings, LinkedList<String>> e : testingQueue.entrySet()) {
            if ("xml".equals(FilenameUtils.getExtension(e.getKey().getTestFile()))) {
                mapReturn.put(e.getKey(), e.getValue());
            }
        }
        return mapReturn;
    }

    private void printReport()
    {
        //print the overall test result
       /* System.out.println("Your result: " + report.getResult());
        System.out.println();

        //Print each error
        for (StaticReportItem i : report.getsReport()){
            System.out.print(i.getSeverity() + ": file " + i.getFileName() + "  at line(s) "); 
            for (int l : i.getLineNumbers()) {
                System.out.print(l + ", ");		//print the line numbers of ass the instances on which this particular error was found
            }
            System.out.println(i.getMessage()); 
        }		*/
    }

    /**
     * Run CheckStyle, set up with the given config file, on all the files to which it should be applied
     * 
     * @param configFileName		Path to the config file needed by CheckStyle
     * @param fileNames				A list of paths to the files on which the static analyses tests are to be performed
     * @throws CheckstyleException	
     * @throws IOException 
     * @throws uk.ac.cam.cl.git.api.RepositoryNotFoundException 
     */
    public void runStaticAnalysis(XMLTestSettings configFileName, List<String> fileNames) throws CheckstyleException, IOException, RepositoryNotFoundException, uk.ac.cam.cl.git.api.RepositoryNotFoundException {
           StaticParser.test(configFileName, fileNames, report, repoName);
    }

    //GETTERS
    public Exception getFailCause()
    {
        return this.failCause;
    }

    public Status getStatus()
    {
        return this.status;
    }

    public Report getReport() {
    	report.calculateProblemStatuses();
        return report;
    }

    //TODO: temporary, delete
    public void setStatus(Status s) {
        this.status = s;
    }
}
