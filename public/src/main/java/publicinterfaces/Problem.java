package publicinterfaces;


/* class that stores all information to do with one aspect/possible "problem" with
 * the code being tested
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Problem implements Comparable<Problem>{
	//general problem description - to be put in left hand bar of UI
	private String problemDescription;
	//hypothetical severity of the problem in case it is found in the code
    private Severity severity;
    //stores files where the corresponding problem was found (the string)  with the details describing where it is (the FileItem)
    //note this map is empty if all given java files pass the test for the problem being checked for
    private List<FileItem> fileDetails = new LinkedList<>();
	//outcome from testing this single aspect i.e. PASS/WARNING/ERROR - to help with order in UI
	private Outcome outcome;
	
	public Problem(String des,Severity severity) {
		this.problemDescription = des;
		this.severity = severity;
	}
	
	public void addDetail(String file, int lineNumber, String details) {
		fileDetails.add(new FileItem(lineNumber , details , file));
	} 
	
	@Override
	public int compareTo(Problem m) {
		if (m.getOutcome() == this.getOutcome()) {
			return 0;
		}
		else {
			if (this.getOutcome() == Outcome.ERROR) {
				return -1;
			}
			else if (this.getOutcome() == Outcome.WARNING) {
				if (m.getOutcome() == Outcome.ERROR) {
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
	
	//For JSON serialisation
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
