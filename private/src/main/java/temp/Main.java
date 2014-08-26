package temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import configuration.ConfigurationLoader;
import publicinterfaces.FailedToMakeTestException;
import publicinterfaces.ITestService;
import publicinterfaces.NoCommitsToRepoException;
import publicinterfaces.StaticOptions;
import publicinterfaces.TestIDAlreadyExistsException;
import publicinterfaces.TestIDNotFoundException;
import publicinterfaces.TestStillRunningException;
import servlets.HttpServletDispatcherV3;
import testingharness.TestService;
import uk.ac.cam.cl.dtg.teaching.containers.api.TestsApi;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInfo;
import uk.ac.cam.cl.git.api.RepositoryNotFoundException;
import uk.ac.cam.cl.git.interfaces.WebInterface;

public class Main {
	public static void main(String[] args) throws TestIDAlreadyExistsException, FailedToMakeTestException, TestIDNotFoundException, IOException {
		ResteasyClient rc = new ResteasyClientBuilder().build();
		ResteasyWebTarget forTester = rc.target("http://localhost:8080/");
	    ITestService testerProxyTest = forTester.proxy(ITestService.class);	
	    
	    Response tests = testerProxyTest.getAvailableDynamicTests();
	    
		/*
		ResteasyClient rc = new ResteasyClientBuilder().build();
	    ResteasyWebTarget p = rc.target("http://localhost:8080/TestingSystem/rest");
	    ITestService proxy = p.proxy(ITestService.class);
	    
	    StaticOptions a = new StaticOptions("Unused imports",0,getCode("unusedImports"));
	    StaticOptions b = new StaticOptions("Indentation",1,getCode("indentation"));
	    StaticOptions c = new StaticOptions("Empty blocks",2,getCode("emptyBlocks"));
	    
	    List<StaticOptions> x = new LinkedList<>();
	    x.add(a);
	    x.add(b);
	    x.add(c);
	    
	    proxy.getTestFiles("vndskoa");
	    */
	}
	
	public static String getCode(String name) throws IOException {
		String output = "";
		String dir = TestService.class.getClassLoader().getResource("checkstyleFiles") + "/" + name + ".xml";
		URL url = new URL(dir);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = in.readLine()) != null) {
            output += line + "\n";
        }
        in.close();
        return output;
	}
}
