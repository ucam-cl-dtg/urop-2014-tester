package TestingHarness;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

//TODO make these methods not static

public class StaticParser {
	
	public static void test(String test, String file, List<sReportItem> sReport) throws TestHarnessError{ 
		
	     //make .java a file in a List and check it exists
	     LinkedList<File> fileList = new LinkedList<File>();
	     
	     File javaFile = new File(file);
	     if (javaFile.exists()){
	    	 fileList.add(javaFile);
	     }
	     else {
		    	 throw new TestHarnessError("Could not find file: " + file);
	     }
	     
	     //get system properties
	     Properties properties = System.getProperties();
	     
	     //test the java file and use the listener to add each line with an error
	     //in it to the linked list of static report items
	     try {
	    	 Configuration config = ConfigurationLoader.loadConfiguration(test, new PropertiesExpander(properties));
	    	 AuditListener listener = new StaticLogger(sReport);
			 Checker c = createChecker(config, listener); 
			 int errs = c.process(fileList); 
			 c.destroy();
	     } 
	     catch (final CheckstyleException e) { 
	    	 throw new TestHarnessError("Could not find test file: " + test);
	     } 
	}
	
	private static Checker createChecker(Configuration config, AuditListener listener) {
		Checker c = null; 
         
        try {
			c = new Checker();
		} 
        catch (CheckstyleException e) {
        	System.out.println(e.getMessage());
		} 
        final ClassLoader moduleClassLoader = Checker.class.getClassLoader(); 
        c.setModuleClassLoader(moduleClassLoader); 
        try {
			c.configure(config);
        }
        catch (CheckstyleException e) {
        	System.out.println(e.getMessage());
		} 
        c.addListener(listener); 
        
        return c; 
	}
} 