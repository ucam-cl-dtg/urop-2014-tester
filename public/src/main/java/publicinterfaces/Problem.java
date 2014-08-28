package publicinterfaces;

import java.util.LinkedList;
import java.util.List;

/**
 * class that stores all information to do with one aspect/possible "problem" with
 * the code being tested
 * @author kls82
 */
public class Problem implements Comparable<Problem>{
	//general problem description - to be put in left hand bar of UI
	private String problemDescription;
	//hypothetical severity of the problem in case it is found in the code
    private Severity severity;
    //stores the places in files the problem was found (if any)
    //note this list is empty if all given java files pass the test for the problem being checked for
    private List<FileItem> fileDetails = new LinkedList<>();
	//outcome from testing this single aspect i.e. PASS/WARNING/ERROR - to help with order in UI
	private Outcome outcome;
	
	/**
	 * Constructor for Problem use in code
	 * @param des		defines the human readable version of the description defining the problem
	 * @param severity  defines the severity of the problem in case it happens to be found in the code
	 */
	public Problem(String des,Severity severity) {
		this.problemDescription = des;
		this.severity = severity;
	}
	
	/**
	 * Used to add a detail about where the problem has been found in the code
	 * @param file		the file where the problem has found
	 * @param lineNumber  the line number the problem has been found at in the file
	 * @param details    details about the problem specific to this file and line number
	 */
	public void addDetail(String file, int lineNumber, String details) {
		fileDetails.add(new FileItem(lineNumber , details , file));
	} 
	
	/**
	 * Used as a comparator to sort lists of problems with respect to problem outcome
	 */
	@Override
	public int compareTo(Problem m) {
		if (m.getOutcome() == this.getOutcome()) {
			return 0;
		}
		else {
			if (this.getOutcome() == Outcome.MANUALCHECK) {
				return -1;
			}
			else if (this.getOutcome() == Outcome.ERROR) {
				if (m.getOutcome() == Outcome.MANUALCHECK){
					return 1;
				}
				else {
					return -1;
				}
			}
			else if (this.getOutcome() == Outcome.WARNING) {
				if (m.getOutcome() == Outcome.MANUALCHECK || m.getOutcome() == Outcome.ERROR){
					return 1;
				}
				else {
					return -1;
				}
			}
			else {
				return 1;
			}
		}
	}
	
	//Constructor and getters and setters for serialising json
    public Problem() {}

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String p) {
        this.problemDescription = p;
    }

    public void setOutcome(Outcome o) {
        this.outcome = o;
    }

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public List<FileItem> getFileDetails() {
		return fileDetails;
	}

	public void setFileDetails(List<FileItem> fileDetails) {
		this.fileDetails = fileDetails;
	}
	
	public Outcome getOutcome() {
        return this.outcome;
    }
}
