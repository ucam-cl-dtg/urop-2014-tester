package TestingHarness;

public class TestHarnessError extends Exception {
	private String message;
	
	public TestHarnessError(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
