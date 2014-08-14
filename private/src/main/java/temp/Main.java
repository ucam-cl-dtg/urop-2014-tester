package temp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import publicinterfaces.FailedToMakeTestException;
import publicinterfaces.ITestService;
import publicinterfaces.NoCommitsToRepoException;
import publicinterfaces.StaticOptions;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TestStillRunningException;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

public class Main {
	public static void main(String[] args) throws TestIDAlreadyExistsException, FailedToMakeTestException, TestIDNotFoundException, IOException {

		ResteasyClient rc = new ResteasyClientBuilder().build();
	    ResteasyWebTarget p = rc.target("http://localhost:8080/TestingSystem/rest");
	    ITestService proxy = p.proxy(ITestService.class);
	    
	    try {
			proxy.runNewTest("kls82", "testTickForDB", "tester");
			proxy.runNewTest("abc12", "testTickForDB", "tester");
			proxy.runNewTest("def12", "testTickForDB", "tester");
			proxy.runNewTest("ghi82", "testTickForDB", "tester");
			proxy.runNewTest("jkl82", "testTickForDB", "tester");
			proxy.runNewTest("dnfda12", "testTickForDB", "tester");
			proxy.runNewTest("vnaso82", "testTickForDB", "tester");
			proxy.runNewTest("vndka82", "testTickForDB", "tester");
			proxy.runNewTest("vbfmie12", "testTickForDB", "tester");
			proxy.runNewTest("vdsjmop82", "testTickForDB", "tester");
			proxy.runNewTest("dsjovf2", "testTickForDB", "tester");
			proxy.runNewTest("bkn82", "testTickForDB", "tester");
			proxy.runNewTest("a12", "testTickForDB", "tester");
			proxy.runNewTest("v82", "testTickForDB", "tester");
			proxy.runNewTest("va82", "testTickForDB", "tester");
			proxy.runNewTest("vmie12", "testTickForDB", "tester");
			proxy.runNewTest("vjmop82", "testTickForDB", "tester");
			proxy.runNewTest("dovf2", "testTickForDB", "tester");
			
		} catch (TestStillRunningException | RepositoryNotFoundException
				| NoCommitsToRepoException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	    /*
	    StaticOptions a = new StaticOptions("Unused imports",0,getCode("unusedImports"));
	    StaticOptions b = new StaticOptions("Indentation",1,getCode("indentation"));
	    StaticOptions c = new StaticOptions("Empty blocks",2,getCode("emptyBlocks"));
	    
	    List<StaticOptions> x = new LinkedList<>();
	    x.add(a);
	    x.add(b);
	    x.add(c);
	    proxy.createNewTest("testTickForDB", x);
	    */
	}
	
	public static String getCode(String name) throws IOException {
		String output = "";
		URL url = new URL("http://localhost:8080/TestingSystem/" + name + ".xml");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = in.readLine()) != null) {
            output += line + "\n";
        }
        in.close();
        return output;
	}
}
