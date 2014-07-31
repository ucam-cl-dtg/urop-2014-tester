package temp;

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
	public static void main(String[] args) throws TestIDAlreadyExistsException, FailedToMakeTestException, TestIDNotFoundException {
		ResteasyClient rc = new ResteasyClientBuilder().build();
	    ResteasyWebTarget p = rc.target("http://localhost:8080/TestingSystem/rest");
	    ITestService proxy = p.proxy(ITestService.class);
	    
	    StaticOptions a = new StaticOptions("Unused imports",1,"xml code......");
	    StaticOptions b = new StaticOptions("TODO or FIXME",2,"xml code edited!......");
	    StaticOptions c = new StaticOptions("Indentation",1,"xml code......");
	    
	    List<StaticOptions> x = new LinkedList<>();
	    x.add(a);
	    x.add(b);
	    x.add(c);
	    proxy.createNewTest("testTickForDB2", x);
	}
}
