package publicinterfaces;

/**
 * Exception thrown when a requested crsid can't be found in the database
 * @author as2388
 * @author kls82
 */
public class UserNotInDBException extends Exception {
    public UserNotInDBException(String message) {
        super(message);
    }
}
