package TestingHarness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class Tester {
	private List<sReportItem> sReport = new LinkedList<sReportItem>();
	private List<dReportItem> dReport = new LinkedList<dReportItem>();
	private Report report;
	// private final String dir = System.getProperty("user.dir");
	
	//instantiated in constructor when joined with other project
	private String crsid = "eg1";
	private HashMap<String, LinkedList<String>> testingQueue = new HashMap<String, LinkedList<String>>();
	
	//temporary
	public static void main(String[] args) {
		Tester t = new Tester();
	}
	
	public Tester(/* some class */) {
		/*
		 * WHEN WE RECIEVE CLASS:
		 * this.crsid = class.getCrsid;
		 * this.testingQueue = class.getTests();
		 */
		
		//temporary files used to test this works
		LinkedList<String> ll = new LinkedList<String>();
		// may need: /src/main/resources/
		ll.add("C:\\ResourcesForUROP/TestResource.java");
		ll.add("C:\\ResourcesForUROP/TestResource2.java");
		testingQueue.put("C:\\ResourcesForUROP/CheckstyleFormat.xml",ll);
		
		try {
			//loop through each test, decide what type of test it is and run it, adding the result to outputs
			for (Map.Entry<String, LinkedList<String>> e : testingQueue.entrySet()) {
				String testFileName = e.getKey();
				LinkedList<String> fileNames = e.getValue();
				
				String ext = testFileName.substring( testFileName.lastIndexOf('.') + 1, testFileName.length());
				
				if ("xml".equals(ext)) {
					//run static analysis tests
					runStaticAnalysis(testFileName,fileNames);
				}
				else if ("java".equals(ext)) {
					//run dynamic analysis tests
				}
				else { 
					throw new WrongFileTypeError(); 
				} 
			}
			
			//all tests have finished
			
			//return report with results of all tests
			Report report = new Report(sReport,dReport);
			this.report = report;
			
			System.out.println("Your result: " + report.getResult());
			System.out.println();
			for(sReportItem i : report.getStaticResults()){
				System.out.print(i.getSeverity() + ": file " + i.getFileName() + "  at line(s) "); 
				for(int l : i.getLineNumbers()) {
					System.out.print(l + ", ");
				}
				System.out.println(i.getMessage());
			}		
		}
		
		//TODO: change report status and return it such that the error encountered is obvious
		catch (CheckstyleException err){
			Report report = new Report(err.getMessage());
			System.out.println(err.getMessage());
			this.report = report;
		}
		catch (WrongFileTypeError err) {
			Report report = new Report(err.getMessage());
			System.out.println(err.getMessage());
			this.report = report;
		} 
		catch (TestHarnessError err) {
			System.out.println();
			Report report = new Report(err.getMessage());
			System.out.println(err.getMessage());
			this.report = report;
		} 
	}
	
	//loops through files to test statically
	public void runStaticAnalysis(String testFileName,LinkedList<String> fileNames) throws CheckstyleException, TestHarnessError {
		for (String file : fileNames) {
			StaticParser.test(testFileName, file, this.sReport);
		}
	}
	
	//temporary to get data to html file
	public static Report getReport() {
		Tester t = new Tester();
		return t.report;
	}
}
