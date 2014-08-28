package publicinterfaces;
/**
 * Stores basic information about the progress of a test.
 * @author kls82
 */
public class Status {
	//stores original place of thread in queue (0 if straight into pool)
	private int originalPositionInQueue;
	//stores current place of thread in queue (0 when accepted in pool)
	private int currentPositionInQueue;
	//current level in the progress bar
    private int progress;
    //number of sections the progress bar should be split into
    private int maxProgress; 
    //string describing progress e.g. running test 1 of 3
    private String info; //string describing progress e.g. running test 1 of 3
    //boolean stating whether there are dynamic tests or not
    private boolean containsDynamic;
    
    public Status(int initQueuePos) {
        this.setOriginalPositionInQueue(initQueuePos);
        this.setCurrentPositionInQueue(initQueuePos);
        this.info = "In queue position " + initQueuePos;
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

    public void updateQueueStatus(int newPos) {
    	this.setCurrentPositionInQueue(newPos);
    	this.info = "In queue position " + this.currentPositionInQueue;
    }
    
    /**
     * Increment current test progress by 1
     */
    public void addProgress() {
        this.progress += 1;
        if (this.containsDynamic) {
        	this.info = "Running static check " + (this.progress - 2) + " of " + (this.maxProgress - 3);
        }
        else {
        	this.info = "Running static check " + this.progress + " of " + (this.maxProgress - 1);
        }
    }

    /**
     * set to complete
     */
    public void complete() {
        this.progress = maxProgress;
        this.info = "Complete";
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

	public int getOriginalPositionInQueue() {
		return originalPositionInQueue;
	}

	public void setOriginalPositionInQueue(int originalPositionInQueue) {
		this.originalPositionInQueue = originalPositionInQueue;
	}

	public int getCurrentPositionInQueue() {
		return currentPositionInQueue;
	}

	public void setCurrentPositionInQueue(int currentPositionInQueue) {
		this.currentPositionInQueue = currentPositionInQueue;
	}

	public boolean isContainsDynamic() {
		return containsDynamic;
	}

	public void setContainsDynamic(boolean containsDynamic) {
		this.containsDynamic = containsDynamic;
	}
}