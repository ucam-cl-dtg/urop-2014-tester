package TestingHarness;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.server.servlet.ServletUtil;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class StaticParser {
	
	public static void test(String test, String file, List<sReportItem> sReport, String repoAddress) throws TestHarnessException, CheckstyleException, URISyntaxException, IOException{ 		
		//must be in list for .process to work
	    LinkedList<File> fileList = new LinkedList<File>();
	    
	    String filex = "x";
	    File javaFile = new File(filex);
	    //read contents of file from git and store in a temporary file
	    ResteasyClient rc = new ResteasyClientBuilder().build();
		ResteasyWebTarget t = rc.target("http://localhost:8080/TestingSystem/");
		WebInterface proxy = t.proxy(WebInterface.class);
		Response response = proxy.getFile(file, repoAddress);
        String contents = response.readEntity(String.class);
        System.out.println(contents);
        response.close();  
		
        /*
        File temp = File.createTempFile(file.substring(0,file.lastIndexOf(".")),".java"); 
 	    System.out.println("Temp file : " + temp.getAbsolutePath());
 	    
        final OutputStream output = new FileOutputStream("/tmp/out/" + repoAddress + "/" + file);
        final PrintStream printStream = new PrintStream(output);
        printStream.print(contents);
        printStream.close();
        
        File file = new File("/tmp/out/" + repoAddress + "/" + file); 
         */
	    if (javaFile.exists()){
	    	//TODO: make this a call to the git API to recieve the file
	    	fileList.add(javaFile);
	    }
	    else {
	    	throw new TestHarnessException("Could not find file: " + file);
	    }
	     
	    //get system properties
	    Properties properties = System.getProperties();
	     
	    //test the java file and use the listener to add each line with an error
	    //in it to the linked list of static report items
	    
	    try {
	    	//TODO: instead of just passing in test we'll probably need to get the file reference from the git API
		    Configuration config = ConfigurationLoader.loadConfiguration("http://localhost:8080/TestingSystem/git/" + repoAddress + "/" + test, new PropertiesExpander(properties));
		    AuditListener listener = new StaticLogger(sReport);
			Checker c = createChecker(config, listener); 
			c.process(fileList); 
			c.destroy();
	    }
	    catch (CheckstyleException err) {
	    	throw new TestHarnessException("Could not find test file: " + getName(test));
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
	
	private static String getName(String filePath) {
		String name = "";
		for(int i = filePath.lastIndexOf("/") + 1; i<filePath.length();i++) {
			name += filePath.charAt(i);
		}
		return name;
	}
} 