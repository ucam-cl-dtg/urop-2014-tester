package TestingHarness;

public class WrongFileTypeError extends Exception {
	
	public WrongFileTypeError() {
		super("Error: Test files must end in .xml or .java only");
	}
}
