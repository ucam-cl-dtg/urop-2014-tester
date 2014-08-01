package publicinterfaces;

/**
 * Exception for when details about a problem category are trying to be added to the 
 * report but the problem category doesn't exist - this should not happen
 * @author as2388
 * @author kls82
 */
public class CategoryNotInReportException extends Exception {
    public CategoryNotInReportException(String message) {
        super(message);
    }
}
