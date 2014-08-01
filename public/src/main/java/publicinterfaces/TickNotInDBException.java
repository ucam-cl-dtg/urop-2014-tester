package publicinterfaces;

/**
 * Thrown when a tick for a given tickId can't be found to be submitted by a 
 * given user so there is no record of any reports for it in the database
 * @author kls82
 * @author as2388
 */
public class TickNotInDBException extends Exception {
    public TickNotInDBException(String message) {
        super(message);
    }
}
