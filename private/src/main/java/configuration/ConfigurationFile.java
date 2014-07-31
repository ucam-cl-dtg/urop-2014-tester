package configuration;

import java.util.Map;

public class ConfigurationFile {
    private String gitAPIPath;
    private String checkstyleResourcesPath;
    private String mongoHost;
    private String mongoReportDBName;
    private String databaseTestsPath;
    private String mongoUsersCollectionName;
    private String mongoXMLSettingsCollectionName;
    private int mongoPort;
    private Map<String,Integer> xmlTestSettings;
	private Map<String, String> xmlTestReadable;
    
    public String getGitAPIPath()
    {
        return gitAPIPath;
    }
    
    public void setGitAPIPath(String gitAPIPath)
    {
        this.gitAPIPath = gitAPIPath;
    }
    
    public String getCheckstyleResourcesPath()
    {
        return checkstyleResourcesPath;
    }
    
    public void setCheckstyleResourcesPath(String checkstyleResourcesPath)
    {
        this.checkstyleResourcesPath = checkstyleResourcesPath;
    }

    public String getMongoHost() {
        return mongoHost;
    }

    public void setMongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
    }

    public int getMongoPort() {
        return mongoPort;
    }

    public void setMongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
    }

    public String getMongoReportDBName() {
        return mongoReportDBName;
    }

    public void setMongoReportDBName(String mongoReportDBName) {
        this.mongoReportDBName = mongoReportDBName;
    }

	public String getDatabaseTestsPath() {
		return databaseTestsPath;
	}

	public void setDatabaseTestsPath(String databaseTestsPath) {
		this.databaseTestsPath = databaseTestsPath;
	}

    public String getMongoXMLSettingsCollectionName() {
        return mongoXMLSettingsCollectionName;
    }

    public String getMongoUsersCollectionName() {
        return mongoUsersCollectionName;
    }

    public void setMongoXMLSettingsCollectionName(String mongoXMLSettingsCollectionName) {
        this.mongoXMLSettingsCollectionName = mongoXMLSettingsCollectionName;
    }

    public void setMongoUsersCollectionName(String mongoUsersCollectionName) {
        this.mongoUsersCollectionName = mongoUsersCollectionName;
    }

    public void setxmlTestSettings(Map<String,Integer> xmlTestSettings) {
    	this.xmlTestSettings = xmlTestSettings;
    }
    
	public int getSeverity(String test) {
		return this.xmlTestSettings.get(test);
	}

	public String getDescription(String test) {
		return this.xmlTestReadable.get(test);
	}

	public void setXmlTestReadable(Map<String, String> xmlTestReadable) {
		this.xmlTestReadable = xmlTestReadable;
	}
    
	
	
}

