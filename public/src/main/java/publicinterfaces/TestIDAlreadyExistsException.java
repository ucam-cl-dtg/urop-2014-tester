package publicinterfaces;

/**
 * Exception for when a new test attempts to be created but its id isn't unique
 * and already exists
 * @author as2388
 * @author kls82
 */
public class TestIDAlreadyExistsException extends Exception {
    public TestIDAlreadyExistsException(String message) {
        super(message);
    }
}
