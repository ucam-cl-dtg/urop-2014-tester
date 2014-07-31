package publicinterfaces;
/**
 * Thrown when a report is searched for with a specific commitId and it doesn't
 * exist
 */	
public class ReportNotFoundException extends Exception {
	public ReportNotFoundException(String message) {
		super(message);
	}
}
