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
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;

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
    public static void test(XMLTestSetting test, List<String> files, Report report, String repoName, String commitId) throws CheckstyleException, IOException, RepositoryNotFoundException {
        //must be in list for .process to work
    	Map<String,String> filePathMap = new HashMap<>();
    	log.info("starting to run test with URL " + test.getTestFile());
        LinkedList<File> fileList = new LinkedList<>();

        for (String file : files) {
        	log.info("obtaining " + file + " version " + commitId + " from " + repoName + " to test");
	        String contents = TestService.gitProxy.getFile(file, commitId, repoName);
	        log.info("obtained file " + file + " version " + commitId + " from " + repoName + " to test");
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
	
	        if (javaFile.exists()){
	            fileList.add(javaFile);
	            filePathMap.put(javaFile.getAbsolutePath(),file);
	        }
	        else {
	            throw new IOException("Could not find file: " + file);
	        }
        }

        //test the java file and use the listener to add each line with an error
        //in it to the linked list of static report items
        try {
            log.info("Testing: java files");
            Configuration config = ConfigurationLoader.loadConfiguration(test.getTestFile(), 
                    new PropertiesExpander(System.getProperties()));
            AuditListener listener = new StaticLogger(report,test,filePathMap);
            Checker c = createChecker(config, listener); 
            c.process(fileList); 
            c.destroy();
            log.info("Finished");
        }
        finally {
            //try to delete all the temp files that were created
        	for (File javaFile : fileList) {
	            if( javaFile.delete()) {
	                log.info("Deleted temp file: " + javaFile.getAbsolutePath());
	            }
	            else {
	                log.error("Failed to delete temp file: " + javaFile.getAbsoluteFile());
	            }
        	}
        }
    }

    private static Checker createChecker(Configuration config, AuditListener listener) throws CheckstyleException {
        Checker c = null; 

        try {
            c = new Checker();
        } 
        catch (CheckstyleException e) {
            System.out.println(e.getMessage());
        } 
        final ClassLoader moduleClassLoader = Checker.class.getClassLoader(); 
        c.setModuleClassLoader(moduleClassLoader); 
        c.configure(config);
        c.addListener(listener); 

        return c; 
    }
} 