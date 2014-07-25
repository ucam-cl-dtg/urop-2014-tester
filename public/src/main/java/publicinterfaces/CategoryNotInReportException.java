package publicinterfaces;

public class CategoryNotInReportException extends Exception {
    public CategoryNotInReportException(String message) {
        super(message);
    }
}
