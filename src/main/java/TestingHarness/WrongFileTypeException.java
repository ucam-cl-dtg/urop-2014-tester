package TestingHarness;

public class WrongFileTypeException extends Exception {	
	public WrongFileTypeException() {
		super("Error: Test files must end in .xml or .java only");
	}
}
