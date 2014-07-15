import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import TestingHarness.Report;
import TestingHarness.TestHarnessException;
import TestingHarness.TestIDNotFoundException;
import TestingHarness.TestService;
import TestingHarness.Tester;
import TestingHarness.WrongFileTypeException;

/**
 * Used for unit testing of API functions 
 * @author as2388
 */
public class TestServiceTester
{
	
	
	
	
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

		//test - this should throw a TestIDNotFoundException
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
