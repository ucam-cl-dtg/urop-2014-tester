package publicinterfaces;

/**
 * Thrown when a test with a given Id can't be found in the database
 * i.e. it hasn't been created yet
 * @author kls82
 * @author as2388
 */
public class TestIDNotFoundException extends Exception {
    /**
     * @param message	expected to contain the id of the test not found
     */
    public TestIDNotFoundException(String message)
    {
        super(message);
    }
}
