import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import TestingHarness.TestIDNotFoundException;
import TestingHarness.TestService;
import TestingHarness.Tester;

public class TestServiceTester
{
	@Test
	public void testPollStatusNormal() throws TestIDNotFoundException 
	{
		TestService ts = buildTestService();
		assertEquals("Polling status of completed test in memory should return 'complete'", "complete", ts.pollStatus("testID"));
	}

	@Test(expected = TestIDNotFoundException.class)
	public void testPollStatusError() throws TestIDNotFoundException
	{
		TestService ts = buildTestService();
		assertEquals("Polling status of test not in memory should throw TestIDNotFoundException", "complete", ts.pollStatus("junkID"));
	}

	private TestService buildTestService()
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
