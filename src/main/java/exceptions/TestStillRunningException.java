package exceptions;

/**
 * Thrown when users of TestServiceInterface try to get the report
 * of an incomplete test
 * @author as2388
 */
public class TestStillRunningException extends Exception{
    /**
     * @param message   expected to contain the id of the test not found
     */
    public TestStillRunningException(String message)
    {
        super(message);
    }
}
