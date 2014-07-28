package testingharness;

import publicinterfaces.AbstractReport;
import publicinterfaces.CategoryNotInReportException;
import publicinterfaces.Report;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

/**
 * Listener class for problems found by CheckStyle
 * 
 * @author as2388
 * @author kls82
 * @author kr2
 *
 */
public class StaticLogger implements AuditListener
{
    private AbstractReport report;	//reference to the list in the report containing the static report items
    private String testDef;
    /**
     * Constructor for StaticLogger
     * @param report Report where generated report items will go.
     * @param test   Checkstyle config data
     */
    public StaticLogger(AbstractReport report, XMLTestSettings test)
    {
        this.report = report;
        this.testDef = test.getTestDefinition();
        System.out.println("added " + testDef + " to report");
        report.addProblem(this.testDef, test.getSeverity());
    }

    /**
     * Processes an AuditEvent (raised when CheckStyle finds a problem).
     * If an sReportItem with the same message has already been found, add the line number of the problem to that sReportItem,
     * otherwise create a new sReportItem
     */
    @Override
    public void addError(AuditEvent event)
    {
        try {
        	//want to remove random number at the end of the temp file
        	//note this means no .java file submitted can contain a number!
        	String file = event.getFileName().replaceAll("[0-9]","");
        	String fileInput = file.substring(file.lastIndexOf("\\")+1,file.length());
			report.addDetail(testDef,fileInput,event.getLine(),event.getMessage());
			System.out.println("checkstyles found something!");
		} catch (CategoryNotInReportException e) {
			// TODO how to handle?
			e.printStackTrace();
		}
    }

    //TODO: what is this???
    //as2388: If Checkstyle fails because of malformed java, it raises addException. Because we only run Checkstyle if
    //the dynamic tests were successful (and hence compiled), this shouldn't be raised, but just in case, this is here
    //to deal with it. So, TODO: update implementation to insert into new report
    @Override
    public void addException(AuditEvent event, Throwable t){}
    /*
    {
        String fileName = event.getFileName();
        String problem = "Malformed java";
        String detail = "";

        System.out.println("ERR MSG: " + event.getMessage());

        //search to see if the message has already been found. If it has, add the line number to the existing sReportItem
        boolean exists = false;
        for (StaticReportItem i : output) {
            if (i.getFileName().equals(fileName) & i.getProblem().equals(problem) & i.getDetail().equals(detail)) {
                exists = true;
                i.addErrorAtLine(arg0.getLine());
            }
        }

        //if the message has not been found, create a new report item
        if (!exists) {
            StaticReportItem newItem = new StaticReportItem(arg0.getSeverityLevel().toString(),fileName,arg0.getLine(),problem,detail);							
            //add the item to the linked list in the report
            output.add(newItem);
        }
    }
    */

    //The following four functions are needed to implement AuditListener, but we have no use for them
    @Override public void auditFinished(AuditEvent arg0){
    	//System.out.println("test complete now report has " + report.getProblems().size() + " items!");
    }
    @Override public void auditStarted(AuditEvent arg0){}
    @Override public void fileFinished(AuditEvent arg0){}
    @Override public void fileStarted(AuditEvent arg0){}
}