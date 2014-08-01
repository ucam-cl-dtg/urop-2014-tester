package publicinterfaces;
/**
 * Exception for when a problem category is already in the report and it's trying to be added again
 * - this should not happen
 * @author as2388
 * @author kls82
 */
public class CategoryAlreadyExistsException extends Exception {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
