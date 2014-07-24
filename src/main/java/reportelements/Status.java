package reportelements;

/**
 * Stores basic information about the progress of a test.
 * @author kls82
 */
public class Status {
    private int progress; //number of completed tests
    private int maxProgress; //total number of tests which wil be run
    private String info; //string describing progress e.g. running test 1 of 3

    public Status(String init, int maxProg) {
        this.progress = 0;
        this.maxProgress = maxProg;
        this.info = init;
    }

    /**
     * Use for creation of Status objects for finished tests
     * @param maxProgress   Total number of tests which were run
     */
    public Status(int maxProgress) {
        this.progress = maxProgress;
        this.maxProgress = maxProgress;
        info = "Done";
    }

    public void addProgress() {
        this.progress += 1;
        this.info = "running test " + this.progress + " of " + (this.maxProgress - 1);
    }

    public void complete() {
        this.progress = maxProgress;
        this.info = "complete";
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public String getInfo() {
        return this.info;
    }
}