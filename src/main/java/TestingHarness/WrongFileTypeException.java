package TestingHarness;

/**
 * Should be thrown when the provided list of files containing the tests to be 
 * run contains a file which is not a .xml (for static analysis) or 
 * .java (for dynamic analysis)
 * @author kls2510
 */
public class WrongFileTypeException extends Exception {	
    public WrongFileTypeException() {
        super("Error: Test files must end in .xml or .java only");
    }
}
