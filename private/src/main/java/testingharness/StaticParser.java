package testingharness;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;



import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.AbstractReport;
import publicinterfaces.Report;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/* import uk.ac.cam.cl.git.public_interfaces.WebInterface; */

/**
 * Provides function for running Checkstyle with a given config file and .java file
 * @author kls82
 *
 */
public class StaticParser {
    static Logger log = LoggerFactory.getLogger(StaticParser.class);

    /**
     * Runs Checkstyle with a given config file and .java file, and puts results into a linked list of static report items
     * @param test                      Path to Checkstyle configuration file to use
     * @param files                     Paths to .java file on which to run Checkstyle
     * @param report                    Reference to Report object into which found problems should go
     * @param repoName                  Name of git repository in which {@code file} is located
     * @throws CheckstyleException      
     * @throws IOException              
     * @throws uk.ac.cam.cl.git.api.RepositoryNotFoundException 
     */
    public static void test(XMLTestSettings test, List<String> files, AbstractReport report, String repoName) throws CheckstyleException, IOException, RepositoryNotFoundException, uk.ac.cam.cl.git.api.RepositoryNotFoundException{
        //must be in list for .process to work
    	log.info("starting to run test with URL " + test.getTestFile());
        LinkedList<File> fileList = new LinkedList<>();

        for (String file : files) {
	        String contents = TestService.gitProxy.getFile(file, repoName);
	
	        File javaFile = File.createTempFile(file.substring(0,file.lastIndexOf(".")),".java"); 
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
            AuditListener listener = new StaticLogger(report,test);
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