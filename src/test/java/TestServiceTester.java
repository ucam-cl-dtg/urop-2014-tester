/* import uk.ac.cam.cl.git.public_interfaces.WebInterface; */

/**
 * Used for unit testing of API functions 
 * @author as2388
 */
public class TestServiceTester
{
    /* @Test
    public void testRunNewTestNormal() throws IOException, WrongFileTypeException, RepositoryNotFoundException
    {
        //mock proxy
        String[] filePaths = {"config.xml", "testfile1.java", "testfile2.java"};
        WebInterface proxy = buildMockedProxy(filePaths, false);

        //build map which runNewTest() should create
        SortedMap<String, LinkedList<String>> testMap = new TreeMap<>();
        LinkedList<String> ll = new LinkedList<>();
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

    @Test(expected = RepositoryNotFoundException.class)
    public void testRunNewTestRepoNotFound() throws IOException, WrongFileTypeException, RepositoryNotFoundException
    {
        //mock proxy
        String[] filePaths = {"config.xml", "testfile1.java", "testfile2.java"};
        WebInterface proxy = buildMockedProxy(filePaths, true);

        //mock tester factory and the tester runNewTest() would normally create
        TesterFactory mockedTesterFactory = EasyMock.createMock(TesterFactory.class);
        Tester mockedTester = EasyMock.createMock(Tester.class);
        //mockedTester.runTests();
        EasyMock.replay(mockedTester);
        //EasyMock.expect(mockedTesterFactory.createNewTester(testMap)).andReturn(mockedTester);
        EasyMock.replay(mockedTesterFactory);
        TestService ts = new TestService(proxy, mockedTesterFactory);
        
        //run test
        ts.runNewTest("");
    }

    private WebInterface buildMockedProxy(String[] filePaths, boolean throwIOException) throws IOException,
            RepositoryNotFoundException
    {
        WebInterface proxy = EasyMock.createMock(WebInterface.class);			
        LinkedList<String> files = new LinkedList<>();

        files.addAll(Arrays.asList(filePaths));

        if (throwIOException)
        {
            EasyMock.expect(proxy.listFiles("")).andThrow(new RepositoryNotFoundException());
        }
        else
        {
            EasyMock.expect(proxy.listFiles("")).andReturn(files);
        }
        EasyMock.replay(proxy);	
        return proxy;
    }

    @Test
    public void testPollStatusNormal() throws TestIDNotFoundException 
    {
        TestService ts = buildTestServiceForPoll();
        assertEquals("Polling status of completed test in memory should return 'complete'", "complete", ts.pollStatus("testID").getInfo());
    }

    @Test(expected = TestIDNotFoundException.class)
    public void testPollStatusNotFound() throws TestIDNotFoundException
    {
        TestService ts = buildTestServiceForPoll();
        assertEquals("Polling status of test not in memory should throw TestIDNotFoundException", "complete", ts.pollStatus("junkID").getInfo());
    }

    @Test
    public void testGetReportNormal() throws TestIDNotFoundException, CheckstyleException, WrongFileTypeException,
            IOException, TestStillRunningException, RepositoryNotFoundException
    {
        //set up
        Report r = EasyMock.createMock(Report.class);
        EasyMock.replay(r);

        Tester t = EasyMock.createMock(Tester.class);
        Status s = EasyMock.createMock(Status.class);
        t.setStatus(s);
        
        EasyMock.expect(t.getStatus()).andReturn(s);
        EasyMock.expect(s.getInfo()).andReturn("complete");
        EasyMock.expect(t.getFailCause()).andReturn(null);
        EasyMock.expect(t.getReport()).andReturn(r);
        EasyMock.replay(s);
        EasyMock.replay(t);

        Map<String, Tester> ticksInProgress = new HashMap<>();
        ticksInProgress.put("testID", t);

        TestService ts = new TestService(ticksInProgress);

        //test
        assertEquals("Getting a report in memory should return a report", r, ts.getReport("testID"));
        assertEquals("Getting a report should remove it from memory", false, ticksInProgress.containsKey("testID"));
    }

    @Test(expected = TestIDNotFoundException.class)
    public void testGetReportNotFound() throws TestIDNotFoundException, CheckstyleException, WrongFileTypeException,
            IOException, TestStillRunningException, RepositoryNotFoundException
    {
        //set up
        Report r = EasyMock.createMock(Report.class);
        EasyMock.replay(r);

        Tester t = EasyMock.createMock(Tester.class);
        Status s = EasyMock.createMock(Status.class);
        t.setStatus(s);
        
        EasyMock.expect(t.getReport()).andReturn(r);
        EasyMock.replay(s);
        EasyMock.replay(t);

        Map<String, Tester> ticksInProgress = new HashMap<>();
        ticksInProgress.put("testID", t);

        TestService ts = new TestService(ticksInProgress);

        //test - this should throw a TestIDNotFoundException
        ts.getReport("badID");
    }	

    @Test(expected = IOException.class)
    public void testGetReportFailedToRun() throws TestIDNotFoundException, CheckstyleException,
            IOException, TestStillRunningException, RepositoryNotFoundException
    {
        //set up
        Tester t = EasyMock.createMock(Tester.class);
        Status s = EasyMock.createMock(Status.class);
        t.setStatus(s);
        
        EasyMock.expect(t.getStatus()).andReturn(s);
        EasyMock.expect(s.getInfo()).andReturn("complete");
       
        EasyMock.expect(t.getFailCause()).andReturn(new IOException());
        EasyMock.expect(t.getFailCause()).andReturn(new IOException());
        EasyMock.replay(s);
        EasyMock.replay(t);

        Map<String, Tester> ticksInProgress = new HashMap<>();
        ticksInProgress.put("testID", t);

        TestService ts = new TestService(ticksInProgress);

        //test - this should throw a WrongFileTypeException
        ts.getReport("testID");
    }

    private TestService buildTestServiceForPoll()
    {
        // mock a Tester
        Tester t = EasyMock.createMock(Tester.class);
        Status s = EasyMock.createMock(Status.class);
        t.setStatus(s);
        
        EasyMock.expect(t.getStatus()).andReturn(s);
        EasyMock.expect(s.getInfo()).andReturn("complete");
        EasyMock.replay(s);
        EasyMock.replay(t);

        // Create dependency for TestService and insert the mocked tester
        Map<String, Tester> ticksInProgress = new HashMap<>();
        ticksInProgress.put("testID", t);

        // create and return TestService
        return new TestService(ticksInProgress);
    } */

}
