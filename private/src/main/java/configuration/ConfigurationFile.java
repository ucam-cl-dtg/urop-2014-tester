package configuration;

import java.util.Map;

public class ConfigurationFile {
    private String gitAPIPath;
    private String testerPath;
    private String checkstyleResourcesPath;
    private String mongoHost;
    private String mongoReportDBName;
    private String databaseTestsPath;
    private String mongoUsersCollectionName;
    private String mongoTickSettingsCollectionName;
    private int mongoPort;
    private Map<String,Integer> xmlTestSettings;
	private Map<String, String> xmlTestReadable;
	private String filePath;
	private String header;
	private int threadNumber;
	private String repoTemplate;
    private String securityToken;
 
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

    public String getMongoUsersCollectionName() {
        return mongoUsersCollectionName;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	public String getTesterPath() {
		return testerPath;
	}

	public void setTesterPath(String testerPath) {
		this.testerPath = testerPath;
	}

	public String getMongoTickSettingsCollectionName() {
		return mongoTickSettingsCollectionName;
	}

	public void setMongoTickSettingsCollectionName(
			String mongoTickSettingsCollectionName) {
		this.mongoTickSettingsCollectionName = mongoTickSettingsCollectionName;
	}

	public String getRepoTemplate() {
		return repoTemplate;
	}

	public void setRepoTemplate(String repoTemplate) {
		this.repoTemplate = repoTemplate;
	}
    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}

