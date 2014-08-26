package publicinterfaces;

import java.util.List;

public class TickSettings {
	//defines files for static tests
	private List<StaticOptions> checkstylesFiles;
	//defines id for relevant dynamic tests - if null then don't pre-select any on front end
	private String testId = null;
	
	public TickSettings(List<StaticOptions> checkstylesFiles, String testId) {
		this.checkstylesFiles = checkstylesFiles;
		this.testId = testId;
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
}
