package TestingHarness;

import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

/**
 * Listener class for problems found by CheckStyle
 * 
 * @author as2388
 * @author kls2510
 * @author kr2
 *
 */
public class StaticLogger implements AuditListener
{
	List<sReportItem> output;	//reference to the list in the report containing the static report items
	
	/**
	 * Constructor for StaticLogger
	 * @param output List where generated report items will go.
	 */
	public StaticLogger(List<sReportItem> output)
	{
		this.output=output;
	}
	
	
	/**
	 * Extracts the file name from a path. 
	 * e.g. input of "C:\Users\...\File.ext" returns "File.ext" 
	 * @param filePath	The path from which to extract the file name
	 * @return			The file name extracted from the given path
	 */
	private String getName(String filePath) {
		String name = "";
		for(int i = filePath.lastIndexOf("\\") + 1; i<filePath.length();i++) {
			name += filePath.charAt(i);
		}
		return name;
	}

	/**
	 * Processes an AuditEvent (raised when CheckStyle finds a problem).
	 * If an sReportItem with the same message has already been found, add the line number of the problem to that sReportItem,
	 * otherwise create a new sReportItem
	 */
	public void addError(AuditEvent arg0)
	{
		String fileName = getName(arg0.getFileName());
		
		//split the message into the problem and detail components (by splitting the message about the tilde symbol)
		//At the moment, remove the apostrophes (which CheckStyle insists on putting around parameters).
		//	TODO: these could ultimately be left in and replaced with <em> tags by the UI team
		String[] messages = arg0.getMessage().replaceAll("\'", "").split(" ~ ");
		
		//extract the problem and, if it exists, the detail
		String problem = messages[0];
		String detail = "";
		if (messages.length > 1) {
			detail = messages[1];
		}
		
		//search to see if the message has already been found. If it has, add the line number to the existing sReportItem
		boolean exists = false;
		for(sReportItem i : output) {
			if (i.getFileName().equals(fileName) & i.getProblem().equals(problem) & i.getDetail().equals(detail)) {
				exists = true;
				i.addErrorAtLine(arg0.getLine());
			}
		}
	
		//if the message has not been found, create a new report item
		if (!exists) {
			sReportItem newItem = new sReportItem(arg0.getSeverityLevel().toString(),fileName,arg0.getLine(),problem,detail);							
			//add the item to the linked list in the report
			output.add(newItem);
		}
	}

	public void addException(AuditEvent arg0, Throwable arg1)
	{
        //System.out.println("Exception " + arg0.getFileName() + "@" + arg0.getLine() + ": " + arg0.getMessage());
		String fileName = getName(arg0.getFileName());
		String problem = "Malformed .java";
		String detail = "";
		
		//search to see if the message has already been found. If it has, add the line number to the existing sReportItem
		boolean exists = false;
		for(sReportItem i : output) {
			if (i.getFileName().equals(fileName) & i.getProblem().equals(problem) & i.getDetail().equals(detail)) {
				exists = true;
				i.addErrorAtLine(arg0.getLine());
			}
		}
				//if the message has not been found, create a new report item
		if (!exists) {
			sReportItem newItem = new sReportItem(arg0.getSeverityLevel().toString(),fileName,arg0.getLine(),problem,detail);							
			//add the item to the linked list in the report
			output.add(newItem);
		}
	}

	
	public void auditFinished(AuditEvent arg0)
	{
       
	}

	public void auditStarted(AuditEvent arg0)
	{
    
	}
	
	public void fileFinished(AuditEvent arg0)
	{
       
	}

	public void fileStarted(AuditEvent arg0)
	{
        
	}
}