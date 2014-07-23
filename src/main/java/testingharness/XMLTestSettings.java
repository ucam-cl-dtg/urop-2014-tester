package testingharness;

import reportelements.Severity;
import configuration.ConfigurationLoader;

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

	public boolean isDefault() {
		return defaultSettings;
	}
}
