package publicinterfaces;

public class CategoryAlreadyExistsException extends Exception {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}