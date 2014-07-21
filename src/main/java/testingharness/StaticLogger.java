package testingharness;

import java.util.List;

import reportelements.StaticReportItem;

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
    List<StaticReportItem> output;	//reference to the list in the report containing the static report items
    String fileName;
    /**
     * Constructor for StaticLogger
     * @param output List where generated report items will go.
     */
    public StaticLogger(List<StaticReportItem> output, String file)
    {
        this.output = output;
        this.fileName = file;
    }

    /**
     * Processes an AuditEvent (raised when CheckStyle finds a problem).
     * If an sReportItem with the same message has already been found, add the line number of the problem to that sReportItem,
     * otherwise create a new sReportItem
     */
    @Override
    public void addError(AuditEvent event)
    {
        String fileName = this.fileName;

        //split the message into the problem and detail components (by splitting the message about the tilde symbol)
        //At the moment, remove the apostrophes (which CheckStyle insists on putting around parameters).
        //	TODO: these could ultimately be left in and replaced with <em> tags by the UI team
        String[] messages = event.getMessage().replaceAll("\'", "").split(" ~ ");

        //extract the problem and, if it exists, the detail
        String problem = messages[0];
        String detail = "";
        if (messages.length > 1) {
            detail = messages[1];
        }

        //search to see if the message has already been found. If it has, add the line number to the existing sReportItem
        boolean exists = false;
        for (StaticReportItem i : output) {
            if (i.getFileName().equals(fileName) & i.getProblem().equals(problem) & i.getDetail().equals(detail)) {
                exists = true;
                i.addErrorAtLine(event.getLine());
            }
        }

        //if the message has not been found, create a new report item
        if (!exists) {
            StaticReportItem newItem = new StaticReportItem(event.getSeverityLevel().toString(),fileName,event.getLine(),problem,detail);							
            //add the item to the linked list in the report
            output.add(newItem);
        }
    }

    @Override
    public void addException(AuditEvent arg0, Throwable arg1)
    {
        String fileName = this.fileName;
        String problem = "Malformed java";
        String detail = "";

        System.out.println("ERR MSG: " + arg0.getMessage());

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

    //The following four functions are needed to implement AuditListener, but we have no use for them
    @Override public void auditFinished(AuditEvent arg0){}
    @Override public void auditStarted(AuditEvent arg0){}
    @Override public void fileFinished(AuditEvent arg0){}
    @Override public void fileStarted(AuditEvent arg0){}
}