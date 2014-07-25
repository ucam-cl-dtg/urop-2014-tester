package publicinterfaces;


/* class that stores all information to do with one aspect/possible "problem" with
 * the code being tested
 */

public class Problem implements Comparable<Problem>{
	//general problem description - to be put in left hand bar of UI
	private final String problemDescription;
	//details of places where the problem was found in the code (if it was) - to be used for code highlighting
	private final ProblemDetails problemDetails;
	//outcome from testing this single aspect i.e. PASS/WARNING/ERROR - to help with order in UI
	private Outcome outcome;
	
	public Problem(String des,ProblemDetails probs) {
		this.problemDescription = des;
		this.problemDetails = probs;
	}
	
	public Problem(String des,Severity severity) {
		this.problemDescription = des;
		this.problemDetails = new ProblemDetails(severity);
	}

	public String getCategory() {
		return this.problemDescription;
	}
	
	public ProblemDetails getProblems() {
		return this.problemDetails;
	}
	
	public void setOutcome(Outcome o) {
		this.outcome = o;
	}
	
	public Outcome getOutcome() {
		return this.outcome;
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
	
	
}
