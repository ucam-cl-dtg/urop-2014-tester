package testingharness;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.Report;
import publicinterfaces.StaticOptions;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides function for running Checkstyle with a given config file and .java file
 * @author kls82
 */
public class StaticParser {
    static Logger log = LoggerFactory.getLogger(StaticParser.class);

    /**
     * Runs Checkstyle with a given config file and .java file, and puts results into a linked list of static report items
     * @param test                      Path to Checkstyle configuration file to use
     * @param files                     Paths to .java file on which to run Checkstyle
     * @param report                    Reference to Report object into which found problems should go
     * @param repoName                  Name of git repository in which {@code file} is located
     * @throws CheckstyleException      Thrown if Checkstyle fails to run
     * @throws IOException              Thrown if creating/making temp files fails
     * @throws RepositoryNotFoundException 		Thrown by git API
     */
    public static void test(StaticOptions test, List<String> files, Report report, String repoName, String commitId, WebInterface gitProxy) throws CheckstyleException, IOException, RepositoryNotFoundException {
    	Map<String,String> filePathMap = new HashMap<>();
    	log.debug("starting to run test: " + test.getText());
    	//must be in list for .process to work
        LinkedList<File> fileList = new LinkedList<>();

        //get .java files to test
        for (String file : files) {
        	log.debug("obtaining " + file + " version " + commitId + " from " + repoName + " to test");
	        String contents = gitProxy.getFile(file, commitId, repoName);
	        log.debug("obtained file " + file + " version " + commitId + " from " + repoName + " to test");
	        String fileName = file.substring(0,file.lastIndexOf("."));
	        
	        File javaFile = File.createTempFile(fileName,".java"); 
	        log.debug("file temporarily stored at: " + javaFile.getAbsolutePath());
	
	        //write string to temp file
	        log.debug("writing data to " + javaFile.getAbsolutePath());
	        FileOutputStream output = new FileOutputStream(javaFile.getAbsolutePath());
	        byte[] bytes = contents.getBytes();
	        output.write(bytes);
	        output.flush();
	        output.close();
	        log.debug("Data transferred to " + javaFile.getAbsolutePath());
	        
	        if (javaFile.exists()){
	            fileList.add(javaFile);
	            filePathMap.put(javaFile.getAbsolutePath(),file);
	        }
	        else {
	        	log.warn("could not find file " + javaFile.getAbsolutePath());
	            throw new IOException("Could not find file: " + file);
	        }
        }

        //test the java file and use the listener to add each line with an error
        //in it to the linked list of static report items
        
        log.debug("Testing: java files");
        log.debug("creating temp file for test " + test.getText());
        File testFile = File.createTempFile("testFile",".xml"); 
	    log.debug("file temporarily stored at: " + testFile.getAbsolutePath());
	
	    //write xml test code string to a temp file
	    log.debug("writing test data to " + testFile.getAbsolutePath());
	    FileOutputStream output = new FileOutputStream(testFile.getAbsolutePath());
	    byte[] codebytes = test.getCode().getBytes();
	    byte[] headerbytes = configuration.ConfigurationLoader.getConfig().getHeader().getBytes();
	    output.write(headerbytes);
	    output.write(codebytes);
	    output.flush();
	    output.close();
        Configuration config = ConfigurationLoader.loadConfiguration(testFile.getAbsolutePath(), 
                    new PropertiesExpander(System.getProperties()));
        AuditListener listener = new StaticLogger(report,test,filePathMap);
        Checker c = createChecker(config, listener); 
        c.process(fileList); 
        c.destroy();
        if (testFile.delete()) {
        	log.debug("Deleted temp test file: " + testFile.getAbsolutePath());
        }
        else {
           	log.warn("Failed to delete temp test file: " + testFile.getAbsolutePath());
        }
        log.debug("test for " + test.getText() + " complete");
        
        //try to delete all the temp files that were created
        for (File javaFile : fileList) {
	        if( javaFile.delete()) {
	            log.debug("Deleted temp file: " + javaFile.getAbsolutePath());
	        }
	        else {
	            log.warn("Failed to delete temp file: " + javaFile.getAbsolutePath());
	        }
        }
    }

    private static Checker createChecker(Configuration config, AuditListener listener) throws CheckstyleException {
        Checker c = null; 

        try {
            c = new Checker();
        } 
        catch (CheckstyleException e) {
            log.warn("when creating checker, checkstyle failed with message: " + e.getMessage());
        } 
        final ClassLoader moduleClassLoader = Checker.class.getClassLoader(); 
        c.setModuleClassLoader(moduleClassLoader); 
        c.configure(config);
        c.addListener(listener); 

        return c; 
    }
} 