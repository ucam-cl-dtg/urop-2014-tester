package TestingHarness;

import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

public class StaticLogger implements AuditListener
{
	List<sReportItem> output;	//reference to the list in the report containing the static report items
	
	/**
	 * 
	 * @param output List where generated report items will go.
	 */
	public StaticLogger(List<sReportItem> output)
	{
		this.output=output;
	}
	
	private String getName(String filePath) {
		String name = "";
		for(int i = filePath.lastIndexOf("\\") + 1; i<filePath.length();i++) {
			name += filePath.charAt(i);
		}
		return name;
	}

	public void addError(AuditEvent arg0)
	{
		boolean exists = false;
		String fileName = getName(arg0.getFileName());
		String[] messages = arg0.getMessage().split(" ~ ");
		String problem = messages[0].replaceAll("\'","");
		String detail = "";
		if (messages.length > 1) {
			detail = messages[1].replaceAll("\'","");
		}
		
		for(sReportItem i : output) {
			if (i.getFileName().equals(fileName) & i.getProblem().equals(problem) & i.getDetail().equals(detail)) {
				exists = true;
				i.addErrorAtLine(arg0.getLine());
			}
		}
	
		if (!exists) {
			//create a new report item.
			sReportItem newItem = new sReportItem(arg0.getSeverityLevel().toString(),fileName,arg0.getLine(),problem,detail);							
			//add the item to the linked list in the report
			output.add(newItem);
		}
	}

	public void addException(AuditEvent arg0, Throwable arg1)
	{
        System.out.println("Exception " + arg0.getFileName() + "@" + arg0.getLine() + ": " + arg0.getMessage());
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