package testingharness;

import Security.*;
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
    public static void test(StaticOptions test, List<File> fileList, Report report, String repoName, String commitId, Map<String,String> filePathMap) throws CheckstyleException, IOException, RepositoryNotFoundException {
    	log.info("starting to run test: " + test.getText());
        
        log.info("Testing: java files");
        log.info("creating temp file for test " + test.getText());
        File testFile = File.createTempFile("testFile",".xml"); 
	    log.info("file temporarily stored at: " + testFile.getAbsolutePath());
	
	    //write xml test code string to a temp file
	    log.info("writing test data to " + testFile.getAbsolutePath());
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
        	log.info("Deleted temp test file: " + testFile.getAbsolutePath());
        }
        else {
           	log.warn("Failed to delete temp test file: " + testFile.getAbsolutePath());
        }
        log.info("test for " + test.getText() + " complete");
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