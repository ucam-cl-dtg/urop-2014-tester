package reportelements;

public class Status {
    private int progress;
    private int maxProgress;
    private String info;

    public Status(String init,int maxProg) {
        this.progress = 0;
        this.maxProgress = maxProg;
        this.info = init;
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