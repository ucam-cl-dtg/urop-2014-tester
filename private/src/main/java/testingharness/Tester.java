package testingharness;

import com.jcraft.jsch.JSchException;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import configuration.ConfigurationLoader;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.CategoryNotInReportException;
import publicinterfaces.FileItem;
import publicinterfaces.ITestSetting;
import publicinterfaces.NewAttachment;
import publicinterfaces.Outcome;
import publicinterfaces.Problem;
import publicinterfaces.Report;
import publicinterfaces.ReportResult;
import publicinterfaces.Severity;
import publicinterfaces.StaticOptions;
import publicinterfaces.Status;
//changed here
import uk.ac.cam.cl.dtg.teaching.containers.api.TestsApi;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.GitRepositoryCloneException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.InvalidNameException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestInstanceNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
//changed here
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInstance;
import exceptions.DynamicException;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.Attachment;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestStep;
import uk.ac.cam.cl.dtg.teaching.exceptions.SerializableException;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;

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
    private List<String> filesToTest;
    private List<StaticOptions> testingQueue = null;
    private boolean dynamicPass;
    private String message = "Tick analysis failed, exception message : ";

    /**
     * Creates a new Tester
     */
    public Tester(List<StaticOptions> tests, List<String> filesToTest, String repoName, String commitId)  {
        this.testingQueue = tests;
        this.filesToTest = filesToTest;
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
        	if(dynamicContainerId != null && dynamicTestId != null) {
        		status.setContainsDynamic(true); 
	        	if (testingQueue != null) {
		            int noOfTests = testingQueue.size()+2;
		            report.setNoOfTests(noOfTests);
		            status.setCurrentPositionInQueue(0);
		            status.setMaxProgress(noOfTests + 1);
		            status.setInfo("Loading tests"); 
	        	}
	        	else {
	        		int noOfTests = 2;
	        		report.setNoOfTests(noOfTests);
	        		status.setCurrentPositionInQueue(0);
		            status.setMaxProgress(noOfTests + 1);
		            status.setInfo("Loading tests");
	        	}
        	}
        	else {
        		status.setContainsDynamic(false);
        		if (testingQueue != null) { 
		            int noOfTests = testingQueue.size();
		            report.setNoOfTests(noOfTests);
		            status.setCurrentPositionInQueue(0);
		            status.setMaxProgress(noOfTests + 1);
		            status.setInfo("Loading tests"); 
	        	}
	        	else {
	        		int noOfTests = 0;
	        		report.setNoOfTests(noOfTests);
	        		status.setCurrentPositionInQueue(0);
		            status.setMaxProgress(noOfTests + 1);
		            status.setInfo("Loading tests");
	        	}
        	}
            
            String repo = ConfigurationLoader.getConfig().getRepoTemplate() + this.repoName + ".git";
            
            if (dynamicContainerId != null && dynamicTestId != null) {
            	log.info(crsId + " " + tickId + " " + commitId + ": Running dynamic tests");
            	runDynamicTests(testerProxyTest,crsId,tickId,dynamicContainerId,dynamicTestId,status,repo, gitProxy);
            	log.info(crsId + " " + tickId + " " + commitId + ": Dynamic tests complete");
            }
            //TODO: will need to fill in checkstyles error bit
            else {
            	log.info(crsId + " " + tickId + " " + commitId + ": No dynamic tests specified");
            	dynamicPass = true;
            }
            
            //now run checkstyles if required
            if (dynamicPass && testingQueue.size()>0)
            {
            	log.info(crsId + " " + tickId + " " + commitId + ": Running static checks");
                runStaticJavaTests(commitId,gitProxy,status);
                log.info(crsId + " " + tickId + " " + commitId + ": Static checks complete");
            }  
            else {
            	log.info(crsId + " " + tickId + " " + commitId + ": Dynamic tests failed");
            }
            
            if (this.failCause == null) {
            	report.calculateProblemStatuses();
            }
            
            log.info("Tick analysis finished");
        }	
        catch (CheckstyleException | IOException | RepositoryNotFoundException e)
        {
            log.error("Tick analysis failed. Exception message: " + e.getMessage());
            report.setTestResult(ReportResult.UNDEFINED);
            failCause = e;
        }
        finally
        {
        	if (this.failCause != null) {
        		this.report.setFailCause(this.message + " " + this.failCause.getMessage());
        	}
            Report reportToAdd = this.report;
            TestService.getDatabase().addReport(crsId, tickId, reportToAdd);
            status.complete();
        }   
    }

    /**
     * Runs all dynamic analysis tests required by the tick
     */
    private void runDynamicTests(TestsApi testerProxyTest, String crsId, String tickId, String dynamicContainerId, String dynamicTestId, Status status, String repo, WebInterface gitProxy)
    {
    	String privateKey = "";
    	boolean compiling = true;
    	try {
			privateKey = gitProxy.getPrivateKey(ConfigurationLoader.getConfig().getSecurityToken(),crsId);
		} catch (IOException | JSchException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	log.info(tickId + " " + crsId +": started dynamic analysis");
        try {
        	String dynamicTestStatus = "";
        	TestInstance testInstance = testerProxyTest.startTest(crsId, dynamicContainerId, dynamicTestId, repo , privateKey);
        	String containerId = testInstance.getContainerID();
        	dynamicTestStatus  = testInstance.getStatus();
			int progress = 0;
			status.setProgress(1);
			status.setInfo("Compiling code");
			log.info("Compiling ");
			//poll status
			while(dynamicTestStatus.equals(TestInstance.STATUS_UNINITIALIZED) || dynamicTestStatus.equals(TestInstance.STATUS_STARTING) || dynamicTestStatus.equals(TestInstance.STATUS_RUNNING)) {
				dynamicTestStatus = testerProxyTest.getTestStatus(crsId, containerId).getStatus();
				log.info(tickId + " " + crsId +": status poll = " + dynamicTestStatus);
				List<TestStep> results = testerProxyTest.getTestStatus(crsId, containerId).getResults();
				if (!results.isEmpty()) {
					progress = testerProxyTest.getTestStatus(crsId, containerId).getResults().size();
					if(results.get(progress-1).getName().equals("Compilation")) {
						log.info("Compiling");
					}
					else {
						compiling = false;
						log.info("Running correctness tests");
						status.setProgress(2);
						status.setInfo("Running correctness tests");
					}
				}
				delay(1000);
			}
			//test is finished, find result
			if(dynamicTestStatus.equals(TestInstance.STATUS_FAILED)) {
				List<TestStep> results = testerProxyTest.getTestStatus(crsId, containerId).getResults();
				if (results.get(progress-1).getName().equals("Compilation")) {
					log.info(tickId + " " + crsId + " " + dynamicTestStatus + ": failed to compile");
				}
				else {
					log.info(tickId + " " + crsId + " " + dynamicTestStatus + ": failed dynamic tests");
				}
				dynamicPass = false;
			}
			else {
				dynamicPass = true;
				log.info(tickId + " " + crsId + " " + dynamicTestStatus + ": passed dynamic tests");
			}
			log.info(tickId + " " + crsId +": putting dynamic test results in report");
			unpackResults(compiling, testerProxyTest.getTestStatus(crsId, containerId).getException(),testerProxyTest.getTestStatus(crsId, containerId).getResults());
			testerProxyTest.removeTest(crsId, containerId);
        } 
        catch (InternalServerErrorException | GitRepositoryCloneException | InvalidNameException | TestNotFoundException | TestInstanceNotFoundException e) {
        	//TODO change back to error
			if(compiling) {
				log.info("Dynamic compilation failed. Exception message: " + e.getMessage());
				report.setTestResult(ReportResult.UNDEFINED);
				this.message = "Failed to compile test file : ";
			}
			else {
	        	log.info("Dynamic analysis failed. Exception message: " + e.getMessage());
	            report.setTestResult(ReportResult.UNDEFINED);
			}
            failCause = e;
		}
        log.info("Dynamic analysis complete");
    }

    private void unpackResults(boolean compiling, SerializableException exception,List<TestStep> results) {
		if (exception == null) {
			//tests ran successfully so put results in report
			log.info("There was no exception from the tester, writing report as normal");
			for(TestStep result: results) {
				List<Problem> problems = report.getItems();
				boolean found = false;
				log.info("Adding " + result.getName());
				if (result.getStatus().equals(TestStep.STATUS_MANUALCHECK)){
					log.info(result.getName() + " adding attachments");
					List<Attachment> attachments = result.getAttachments();
					int i = 1;
					Problem p = new Problem(result.getName() , Severity.MANUALCHECK);
					report.addProblem(p);
					for(Attachment attachment : attachments) {
						String name = result.getName() + "_" + i;
						report.getAttachmentList().add(new NewAttachment(name , 
								attachment.getContent() , attachment.getMimeType()));
						try {
							report.addDetail(result.getName(), name, 1, "Ticker should review this");
						} catch (CategoryNotInReportException e) {
							// This should never happen
							e.printStackTrace();
						}
						i++;
					}
				}
				else {
					if(result.getStatus().equals(TestStep.STATUS_PASS)){
						for(Problem p : problems) {
							if(p.getProblemDescription().equals(result.getName())) {
								if (!(p.getSeverity().equals(Severity.ERROR)) && !(p.getSeverity().equals(Severity.WARNING))) {
									log.info("setting result for " + result.getName() + " as PASS");
									p.setSeverity(Severity.PASS);
								}
								found = true;
								break;
							}
						}
						if(!found) {
							log.info(result.getName() + " result = pass");
							report.addProblem(result.getName() , Severity.PASS);
						}
					}
					else if(result.getStatus().equals(TestStep.STATUS_FAIL)){
						for(Problem p : problems) {
							if(p.getProblemDescription().equals(result.getName())) {
								log.info("setting result for " + result.getName() + " as ERROR");
								p.setSeverity(Severity.ERROR);
								found = true;
								break;
							}
						}
						if(!found) {
							log.info(result.getName() + " result = error");
							report.addProblem(result.getName() , Severity.ERROR);
						}
					}
					else if(result.getStatus().equals(TestStep.STATUS_WARNING)) {
						for(Problem p : problems) {
							if(p.getProblemDescription().equals(result.getName())) {
								if (!p.getSeverity().equals(Severity.ERROR)) {
									log.info("setting result for " + result.getName() + " as WARNING");
									p.setSeverity(Severity.WARNING);
								}
								found = true;
								break;
							}
						}
						if(!found) {
							log.info(result.getName() + " result = warning");
							report.addProblem(result.getName() , Severity.WARNING);
						}
					}
					if(result.getFileName() != null) {
						log.info(result.getName() + " " + " writing details");
						String message = "";
						for(String m : result.getMessages()){
							message += m + "\n";
						}
						message += "Expected result: " + result.getExpected() + "\n";
						message += "Obtained result: " + result.getActual() + "\n";
						try {
							String fileName = result.getFileName();
							//fileName = fileName.split(".git")[1];
							//fileName = fileName.substring(1,fileName.length());
							report.addDetail(result.getName() , fileName, (int) result.getStartLine(), message);
						} 
						catch (CategoryNotInReportException e) {
							// TODO Auto-generated catch block
							//should never be called
							log.error("category not found in report");
							e.printStackTrace();
						}
					}
				}
			}
		}
		else {
			//there was an exception so put what went wrong in report
			log.info("Dynamic tester threw an exception, see comment in report for details");
			report.setTestResult(ReportResult.UNDEFINED);
			this.failCause = new DynamicException(exception.getMessage());
			if(compiling){
				this.message = "Tick analysis failed, tester threw an exception compiling : ";
			}
			dynamicPass = false;
		}
	}

	/**
     * Runs all static analysis tests required by the tick if files are .java
     * @throws CheckstyleException	Thrown if Checkstyle fails to run
     * @throws IOException 			Thrown if creating/making temp files fails
     * @throws RepositoryNotFoundException 		Thrown by git API
     */
    private void runStaticJavaTests(String commitId, WebInterface gitProxy, Status status) throws CheckstyleException, IOException, RepositoryNotFoundException    {
        List<String> javaFiles = getStaticTestFiles(this.filesToTest, "java");
    	List<File> fileList = new LinkedList<>();
    	Map<String,String> filePathMap = new HashMap<>();
    	//create temp files to run checkstyles on
    	for(String file : javaFiles) {
            log.debug("obtaining " + file + " version " + commitId + " from " + repoName + " to test");
    	    String contents = gitProxy.getFile(Security.SecurityManager.getSecurityToken(), file, commitId, repoName);
    	    log.debug("obtained file " + file + " version " + commitId + " from " + repoName + " to test");
    	       
    	    String fileName = file.substring(0,file.lastIndexOf("."));
    	        
    	    File javaFile = File.createTempFile(fileName,".java"); 
    	    log.info("file temporarily stored at: " + javaFile.getAbsolutePath());
    	
    	    //write string to temp file
    	    log.info("writing data to " + javaFile.getAbsolutePath());
    	    FileOutputStream output = new FileOutputStream(javaFile.getAbsolutePath());
    	    byte[] bytes = contents.getBytes();
    	    output.write(bytes);
    	    output.flush();
    	    output.close();
    	    log.info("Data transferred to " + javaFile.getAbsolutePath());
    	        
    	    if (javaFile.exists()){
    	        fileList.add(javaFile);
    	        filePathMap.put(javaFile.getAbsolutePath(),file);
    	    }
    	    else {
    	     	log.warn("could not find file " + javaFile.getAbsolutePath());
    	        throw new IOException("Could not find file: " + file);
    	    }
      	}
    	
        //run static analysis on each file
        for (StaticOptions o : this.testingQueue) {
            delay(ConfigurationLoader.getConfig().getTestDelay());
            status.addProgress();
            runStaticAnalysis(o, fileList, commitId, filePathMap);
        }
        
        //try to delete all the temp files that were created
        for (File javaFile : fileList) {
	        if( javaFile.delete()) {
	            log.info("Deleted temp file: " + javaFile.getAbsolutePath());
	        }
	        else {
	            log.warn("Failed to delete temp file: " + javaFile.getAbsolutePath());
	        }
        }
    }

    /**
     * Extracts only the tests with .{ext} extensions for the static checks
     * @param files   	list of files from the corresponding repo
     * @param ext		extension used to filter the files to the ones required to be tested statically
     * @return          List containing only .{ext} files
     */
    public List<String> getStaticTestFiles(List<String> files, String ext)
    {
        List<String> toReturn = new LinkedList<>();
        for (String s : files) {
        	//TODO check this is right
            if (s.substring(s.length() - (ext.length())).equals(ext)) {
                toReturn.add(s);
            }
        }
        return toReturn;
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
    public void runStaticAnalysis(StaticOptions configFileName, List<File> fileNames, String commitId, Map<String,String> filePathMap) throws CheckstyleException, IOException, RepositoryNotFoundException {
           StaticParser.test(configFileName, fileNames, report, repoName, commitId, filePathMap);
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
