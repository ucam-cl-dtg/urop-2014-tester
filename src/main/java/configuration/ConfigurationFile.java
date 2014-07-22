package configuration;

public class ConfigurationFile {
    private String gitAPIPath;
    private String checkstyleResourcesPath;
    
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
}
