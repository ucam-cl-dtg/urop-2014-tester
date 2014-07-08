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

	public void addError(AuditEvent arg0)
	{
        //System.out.println("Error " + arg0.getFileName() + "@" + arg0.getLine() + ": " + arg0.getMessage());
		sReportItem newItem = new sReportItem();	//create a new report item. TODO: add data to the report item
		output.add(newItem);						//add the item to the linked list in the report
	}

	public void addException(AuditEvent arg0, Throwable arg1)
	{
        System.out.println("Exception " + arg0.getFileName() + "@" + arg0.getLine() + ": " + arg0.getMessage());
	}

	public void auditFinished(AuditEvent arg0)
	{
        System.out.println("Audit finished!");
	}

	public void auditStarted(AuditEvent arg0)
	{
        System.out.println("Audit started!");
	}

	public void fileFinished(AuditEvent arg0)
	{
       
	}

	public void fileStarted(AuditEvent arg0)
	{
        
	}
}