package temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import configuration.ConfigurationLoader;
import publicinterfaces.ITestService;
import publicinterfaces.StaticOptions;
import publicinterfaces.TestIDNotFoundException;
import testingharness.TestService;
import uk.ac.cam.cl.dtg.teaching.containers.api.TestsApi;

public class Main {
	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		StaticOptions o1 = new StaticOptions("empty blocks",2,getCode("emptyBlocks.xml"));
		StaticOptions o2 = new StaticOptions("illegal catch",2,getCode("illegalCatch.xml"));
		StaticOptions o3 = new StaticOptions("indentation",1,getCode("indentation.xml"));
		StaticOptions o4 = new StaticOptions("naming conventions",1,getCode("namingConventions.xml"));
		StaticOptions o5 = new StaticOptions("TODO or FIXME",2,getCode("TODOorFIXME.xml"));
		
		List<StaticOptions> x = new LinkedList<>();
		x.add(o1);
		x.add(o2);
		x.add(o3);
		x.add(o4);
		x.add(o5);
		
		ResteasyClient rc = new ResteasyClientBuilder().build();
		ResteasyWebTarget forTester = rc.target("http://localhost:8080/TestingSystem/rest");
		ITestService proxy = forTester.proxy(ITestService.class);
	}
	
	public static String getCode(String file) throws FileNotFoundException, URISyntaxException {
		URI dir = (TestService.class.getClassLoader().getResource("checkstyleFiles")).toURI();
		File path = new File(dir);
		BufferedReader b = new BufferedReader(new FileReader(path + "/" + file));
		String line;
		String fileContents = "";
		try {
			while ((line = b.readLine()) != null) {
				fileContents += line + System.getProperty("line.separator");
			}
		} 
		catch (IOException e) {
			System.out.println("IO failed, unable to read from " + file);
			b.close();
			e.printStackTrace();
		} 
		finally {
			return fileContents;
		}
	}
}
