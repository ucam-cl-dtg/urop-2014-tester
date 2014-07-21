package exceptions;

/**
 * Thrown when users of TestServiceInterface poll or get a report
 * using an id which does not map to a test available to retrieve
 * from memory.
 * @author as2388
 *
 */
public class TestIDNotFoundException extends Exception {
    /**
     * @param message	should contain the id of the test not found
     */
    public TestIDNotFoundException(String message)
    {
        super(message);
    }
}
