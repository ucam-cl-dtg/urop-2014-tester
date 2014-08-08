package publicinterfaces;
/**
 * Stores basic information about the progress of a test.
 * @author kls82
 */
public class Status {
	//current level in the progress bar
    private int progress;
    //number of sections the progress bar should be split into
    private int maxProgress; 
    //string describing progress e.g. running test 1 of 3
    private String info; //string describing progress e.g. running test 1 of 3


    /**
     * Constructor for use when a new test begins to run
     * @param init          String describing current progress
     * @param maxProgress   Total number of tests which are to be run + 1 (as "Done" is an extra state)
     */
    public Status(String init, int maxProgress) {
        this.progress = 0;
        this.maxProgress = maxProgress;
        this.info = init;
    }

    /**
     * Use for creation of Status objects for finished tests
     * @param maxProgress   Total number of tests which were run + 1 (as "Done" is an extra state)
     * @param reportResult  Whether or not the user passed or failed the automated tests
     */
    public Status(int maxProgress, ReportResult reportResult) {
        this.progress = maxProgress;
        this.maxProgress = maxProgress;
        info = reportResult.toString();
    }

    /**
     * Increment current test progress by 1
     */
    public void addProgress() {
        this.progress += 1;
        this.info = "Running test " + this.progress + " of " + (this.maxProgress - 1);
    }

    /**
     * set to complete
     */
    public void complete() {
        this.progress = maxProgress;
        this.info = "complete";
    }

    //Constructor and getters/setters for json serialisation
    public Status() {}

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}