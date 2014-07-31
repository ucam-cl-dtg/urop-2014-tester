package testingharness;

import publicinterfaces.Severity;

import configuration.ConfigurationLoader;

/**
 * Stores the settings for an xml static test file to be used in a particular tick
 * 
 * @author kls82
 */

public class XMLTestSetting {
	//Stores path of the xml file
	private String testFile;
	//Stores the severity incase the corresponding problem the test checks for is found 
	private Severity severity;
	//defines what the test checks for in a human readable way
	private String testDefinition;
	
	public XMLTestSetting(String testFile,int severityInt,String definition) {
		this.testFile = testFile;
		if (severityInt == 1) {
			this.severity = Severity.WARNING;
		}
		else {
			this.severity = Severity.ERROR;
		}
		this.testDefinition = definition;
	}

	//Constuctor and getter/setters for json serialisation
	public String getTestFile() {
		return testFile;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getTestDefinition() {
		return testDefinition;
	}

    public XMLTestSetting() {}

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
