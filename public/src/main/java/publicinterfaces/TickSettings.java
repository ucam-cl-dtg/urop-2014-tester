package publicinterfaces;

import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInfo;

import java.util.List;

/**
 * Used for returning tick settings data to users of API
 */
public class TickSettings {
	//defines files for static tests
	private List<StaticOptions> checkstylesFiles;
	//defines id for relevant dynamic tests - if null then don't pre-select any on front end
	private String testId = null;
    //defines available dynamic tests which can be run
    private List<TestInfo> dynamicAvailable;
	
	public TickSettings(List<StaticOptions> checkstylesFiles, String testId, List<TestInfo> dynamicAvailable) {
		this.checkstylesFiles = checkstylesFiles;
		this.testId = testId;
        this.dynamicAvailable = dynamicAvailable;
	}
	
	public TickSettings() {}
	
	public List<StaticOptions> getCheckstylesFiles() {
		return checkstylesFiles;
	}
	public void setCheckstylesFiles(List<StaticOptions> checkstylesFiles) {
		this.checkstylesFiles = checkstylesFiles;
	}
	
	public String getTestId() {
		return testId;
	}
	public void setTestId(String testId) {
		this.testId = testId;
	}

    public List<TestInfo> getDynamicAvailable() {
        return dynamicAvailable;
    }

    public void setDynamicAvailable(List<TestInfo> dynamicAvailable) {
        this.dynamicAvailable = dynamicAvailable;
    }
}
