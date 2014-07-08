package TestingHarness;

//stores each line with details of the error
public class ReportItem {
	String type; //eg indentation, TODO
	String line; //for now
	
	public ReportItem(String line) {
		this.line = line;
	}
}
