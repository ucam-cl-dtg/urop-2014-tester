package publicinterfaces;

public class UserNotInDBException extends Exception {
    public UserNotInDBException(String message) {
        super(message);
    }
}
