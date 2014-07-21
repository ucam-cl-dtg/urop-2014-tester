package TestingHarness;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Runs all the static and dynamic analysis tests for a given tick, and produces a report,
 * stored in memory until the object is destroyed
 * 
 * @author as2388
 * @author kls82
 *
 */
public class Tester {
    static Logger log = Logger.getLogger(Tester.class.getName()); //initialise log4j logger

    private List<StaticReportItem> sReport = new LinkedList<StaticReportItem>();    //list of static report items
    private List<DynamicReportItem> dReport = new LinkedList<DynamicReportItem>();  //list of dynamic report items
    private Report report;              //Report object into which all the report items will ultimately go
    private Status status;
    private Exception failCause;        //if the report fails, save it here, so that it can be thrown when
                                        //the report is requested
    private String repoName;
    //Maps the path of a test (either static or dynamic) to a list of paths to files on which that test should be run
    private SortedMap<String, LinkedList<String>> testingQueue = new TreeMap<String, LinkedList<String>>();

    /**
     * Creates a new Tester
     */
    public Tester(SortedMap<String, LinkedList<String>> testingQueue, String repoName)  {
        this.testingQueue = testingQueue;
        this.repoName = repoName;
        System.out.println("testing Queue contains:");
        for (Map.Entry<String, LinkedList<String>> entry : testingQueue.entrySet()) {
            System.out.println(entry.getKey());
        }
        assert validateTestingQueueIsSorted();
    }

    /**
     * Ensures that the testing queue contains .java files first,
     * then .xml files
     */
    private boolean validateTestingQueueIsSorted() 
    {
        boolean foundXML = false;
        for (String key : testingQueue.keySet())
        {
            String ext = FilenameUtils.getExtension(key);
            if (foundXML)
            {
                if (ext.equals("java"))
                {
                    log.error("testingQueue files are not ordered java then xml");
                    return false;
                }
            }
            else
            {
                if (ext.equals("xml"))
                {
                    foundXML = true;
                }
            }
        }
        return true;
    }
    
    /**
     * Runs all tests required by the tick on all files required to be tested by the tick
     * @throws IOException 
     */
    public void runTests() 
    {
        log.info("Tick analysis started");	     
                
        try {
            int noOfTests = testingQueue.size();
            this.status = new Status("loading tests",noOfTests + 1);
            //loop through each test, decide what type of test it is and run it, adding the result to outputs
            for (Map.Entry<String, LinkedList<String>> e : testingQueue.entrySet()) {
                this.status.addProgress();
                String testFileName = e.getKey();
                LinkedList<String> fileNames = e.getValue();

                String ext = testFileName.substring(testFileName.lastIndexOf('.') + 1, testFileName.length());

                if ("xml".equals(ext)) {
                    //run static analysis tests
                    runStaticAnalysis(testFileName, fileNames);
                }
                else if ("java".equals(ext)) {
                    //TODO: run dynamic analysis tests
                }
                else { 
                    throw new WrongFileTypeException();
                } 
                //tempoarary for testing purposes
                try  {
                	Thread.sleep(7000);
                }
                catch (InterruptedException err) {
                	err.printStackTrace();
                }
            } 
            //Once the for loop is complete, all tests to be run have finished
            log.info("Tick analysis finished successfully");

            //build the final report from the static and dynamic results
            this.report = new Report(sReport, dReport);

            //TODO: remove this. For now, print result to the console
            printReport();
        }	
        catch (CheckstyleException | WrongFileTypeException | IOException e)
        {
            log.error("Tick analysis failed. Exception message: " + e.getMessage());
            failCause = e;
        }
        finally
        {
            this.status.complete();
        }
    }

    private void printReport()
    {
        //print the overall test result
        System.out.println("Your result: " + report.getResult());
        System.out.println();

        //Print each error
        for (StaticReportItem i : report.getsReport()){
            System.out.print(i.getSeverity() + ": file " + i.getFileName() + "  at line(s) "); 
            for (int l : i.getLineNumbers()) {
                System.out.print(l + ", ");		//print the line numbers of ass the instances on which this particular error was found
            }
            System.out.println(i.getMessage());
        }		
    }

    /**
     * Run CheckStyle, set up with the given config file, on all the files to which it should be applied
     * 
     * @param configFileName		Path to the config file needed by CheckStyle
     * @param fileNames				A list of paths to the files on which the static analyses tests are to be performed
     * @throws CheckstyleException	
     * @throws IOException 
     */
    public void runStaticAnalysis(String configFileName, List<String> fileNames) throws CheckstyleException, IOException {
        for (String file : fileNames) {
            StaticParser.test(configFileName, file, this.sReport, repoName);
        }
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
        return report;
    }
}
