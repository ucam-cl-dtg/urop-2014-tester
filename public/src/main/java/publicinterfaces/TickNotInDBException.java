package publicinterfaces;

public class TickNotInDBException extends Exception {
    public TickNotInDBException(String message) {
        super(message);
    }
}
