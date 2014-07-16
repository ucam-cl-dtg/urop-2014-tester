import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;

import com.google.inject.Guice;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import TestingHarness.Report;
import TestingHarness.TestHarnessException;
import TestingHarness.TestIDNotFoundException;
import TestingHarness.TestModule;
import TestingHarness.TestService;
import TestingHarness.Tester;
import TestingHarness.TesterFactory;
import TestingHarness.WebInterface;
import TestingHarness.WrongFileTypeException;

/**
 * Used for unit testing of API functions 
 * @author as2388
 */
public class TestServiceTester
{
	@Test
	public void testRunNewTestNormal() throws IOException, URISyntaxException
	{
		//mock proxy
		String[] filePaths = {"config.xml", "testfile1.java", "testfile2.java"};
		WebInterface proxy = buildMockedProxy(filePaths);
		
		//build map which runNewTest() should create
		Map<String, LinkedList<String>> testMap = new HashMap<String, LinkedList<String>>();
		LinkedList<String> ll = new LinkedList<String>();
		ll.add("testfile1.java");
		ll.add("testfile2.java");
		testMap.put("config.xml", ll);
		
		//mock tester factory and the tester runNewTest() should create
		TesterFactory mockedTesterFactory = EasyMock.createMock(TesterFactory.class);
		Tester mockedTester = EasyMock.createMock(Tester.class);
		mockedTester.runTests();
		EasyMock.replay(mockedTester);
		EasyMock.expect(mockedTesterFactory.createNewTester(testMap, "")).andReturn(mockedTester);
		EasyMock.replay(mockedTesterFactory);
		TestService ts = new TestService(proxy, mockedTesterFactory);
		
		//Test that TestService returns a non-empty string
		String result = ts.runNewTest("");
		assertNotEquals("", result, null);
		assertNotEquals("", result, "");
		//Test that a new tester was created with the expected arguments
		EasyMock.verify(mockedTesterFactory);
		//Test that the created tester was run (i.e. check that mockedTester.runTests() was called)
		EasyMock.verify(mockedTester);
	}
	
	//@Test(expected = IOException.class)
	public void testRunNewTestRepoNotFound() throws IOException
	{
		//mock proxy
		String[] filePaths = {"config.xml", "testfile1.java", "testfile2.java"};
		WebInterface proxy = buildMockedProxy(filePaths);
			
		//build map which runNewTest() should create
		Map<String, LinkedList<String>> testMap = new HashMap<String, LinkedList<String>>();
		LinkedList<String> ll = new LinkedList<String>();
		ll.add("testfile1.java");
		ll.add("testfile2.java");
		testMap.put("config.xml", ll);
		
		//mock tester factory and the tester runNewTest() would normally create
		TesterFactory mockedTesterFactory = EasyMock.createMock(TesterFactory.class);
		Tester mockedTester = EasyMock.createMock(Tester.class);
		//mockedTester.runTests();
		EasyMock.replay(mockedTester);
		//EasyMock.expect(mockedTesterFactory.createNewTester(testMap)).andReturn(mockedTester);
		EasyMock.replay(mockedTesterFactory);
		TestService ts = new TestService(proxy, mockedTesterFactory);
		
		//
	}
	
	private WebInterface buildMockedProxy(String[] filePaths) throws IOException
	{
		WebInterface proxy = EasyMock.createMock(WebInterface.class);			
		LinkedList<String> files = new LinkedList<String>();
		
		for (int i = 0; i < filePaths.length; i++)
		{
			files.add(filePaths[i]);
		}
		
		EasyMock.expect(proxy.listFiles("")).andReturn(files);
		EasyMock.replay(proxy);	
		return proxy;
	}
	
	@Test
	public void testPollStatusNormal() throws TestIDNotFoundException 
	{
		TestService ts = buildTestServiceForPoll();
		assertEquals("Polling status of completed test in memory should return 'complete'", "complete", ts.pollStatus("testID"));
	}

	@Test(expected = TestIDNotFoundException.class)
	public void testPollStatusNotFound() throws TestIDNotFoundException
	{
		TestService ts = buildTestServiceForPoll();
		assertEquals("Polling status of test not in memory should throw TestIDNotFoundException", "complete", ts.pollStatus("junkID"));
	}
	
	@Test
	public void testGetReportNormal() throws TestIDNotFoundException, CheckstyleException, WrongFileTypeException, TestHarnessException
	{
		//set up
		Report r = EasyMock.createMock(Report.class);
		EasyMock.replay(r);
		
		Tester t = EasyMock.createMock(Tester.class);
		EasyMock.expect(t.getStatus()).andReturn("complete");
		EasyMock.expect(t.getReport()).andReturn(r);
		EasyMock.replay(t);
		
		Map<String, Tester> ticksInProgress = new HashMap<String, Tester>();
		ticksInProgress.put("testID", t);
		
		TestService ts = new TestService(ticksInProgress);

		//test
		assertEquals("Getting a report in memory should return a report", r, ts.getReport("testID"));
		assertEquals("Getting a report should remove it from memory", false, ticksInProgress.containsKey("testID"));
	}
	
	@Test(expected = TestIDNotFoundException.class)
	public void testGetReportNotFound() throws TestIDNotFoundException, CheckstyleException, WrongFileTypeException, TestHarnessException
	{
		//set up
		Report r = EasyMock.createMock(Report.class);
		EasyMock.replay(r);
		
		Tester t = EasyMock.createMock(Tester.class);
		EasyMock.expect(t.getStatus()).andReturn("complete");
		EasyMock.expect(t.getReport()).andReturn(r);
		EasyMock.replay(t);
		
		Map<String, Tester> ticksInProgress = new HashMap<String, Tester>();
		ticksInProgress.put("testID", t);
		
		TestService ts = new TestService(ticksInProgress);

		//test - this should throw a TestIDNotFoundException
		ts.getReport("badID");
	}	
	
	@Test(expected = WrongFileTypeException.class)
	public void testGetReportFailedToRun() throws TestIDNotFoundException, CheckstyleException, WrongFileTypeException, TestHarnessException
	{
		//set up
		Tester t = EasyMock.createMock(Tester.class);
		EasyMock.expect(t.getStatus()).andReturn("error");
		EasyMock.expect(t.getFailCause()).andReturn((Exception) new WrongFileTypeException());
		EasyMock.replay(t);
		
		Map<String, Tester> ticksInProgress = new HashMap<String, Tester>();
		ticksInProgress.put("testID", t);
		
		TestService ts = new TestService(ticksInProgress);

		//test - this should throw a WrongFileTypeException
		ts.getReport("testID");
	}

	private TestService buildTestServiceForPoll()
	{
		// mock a Tester
		Tester t = EasyMock.createMock(Tester.class);
		EasyMock.expect(t.getStatus()).andReturn("complete");
		EasyMock.replay(t);

		// Create dependency for TestService and insert the mocked tester
		Map<String, Tester> ticksInProgress = new HashMap<String, Tester>();
		ticksInProgress.put("testID", t);

		// create and return TestService
		return new TestService(ticksInProgress);
	}
	
}
