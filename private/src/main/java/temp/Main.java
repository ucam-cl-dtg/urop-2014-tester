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
import publicinterfaces.StaticOptions;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

public class Main {
	public static void main(String[] args) throws TestIDAlreadyExistsException, FailedToMakeTestException, TestIDNotFoundException, IOException {
		ResteasyClient rc = new ResteasyClientBuilder().build();
	    ResteasyWebTarget p = rc.target("http://localhost:8080/TestingSystem/rest");
	    ITestService proxy = p.proxy(ITestService.class);
	    
	    StaticOptions a = new StaticOptions("Unused imports",1,getCode("emptyBlocks"));
	    StaticOptions b = new StaticOptions("TODO or FIXME",1,getCode("indentation"));
	    StaticOptions c = new StaticOptions("Indentation",2,getCode("unusedImports"));
	    
	    List<StaticOptions> x = new LinkedList<>();
	    x.add(a);
	    x.add(b);
	    x.add(c);
	    proxy.createNewTest("testTickForDB", x);
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
