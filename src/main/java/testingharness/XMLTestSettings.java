package testingharness;

import com.fasterxml.jackson.annotation.JsonIgnore;
import configuration.ConfigurationLoader;
import reportelements.Severity;

public class XMLTestSettings {
	private String testFile;
	//use this severity instead of the one in the xml files if defaultSettings is not true
	private Severity severity;
	private String testDefinition;
	private boolean defaultSettings;
	
	public XMLTestSettings(String testFile,Severity severity,String definition) {
		this.testFile = ConfigurationLoader.getConfig().getCheckstyleResourcesPath() + testFile + ".xml";
		this.severity = severity;
		this.testDefinition = definition;
		this.defaultSettings = false;
	}

	public String getTestFile() {
		return testFile;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getTestDefinition() {
		return testDefinition;
	}

    @JsonIgnore
	public boolean isDefault() {
		return defaultSettings;
	}

    //for JSON serialisation
    public XMLTestSettings() {}

    public boolean getDefaultSettings() {
        return defaultSettings;
    }

    public void setDefaultSettings(boolean defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setTestDefinition(String testDefinition) {
        this.testDefinition = testDefinition;
    }

    public void setTestFile(String testFile) {
        this.testFile = testFile;
    }
}
