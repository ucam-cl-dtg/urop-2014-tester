package gitapidependencies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GitService implements WebInterface {

    public List<String> listRepositories() {
        return null;
    }

    public LinkedList<String> listFiles(String repoName) throws IOException, RepositoryNotFoundException{
        LinkedList<String> files = new LinkedList<>();
        //add files here!
        
        return files;
    }   
    
    public String getFile(String fileName, String repoName)
            throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader(/* path */ fileName));
    	String output = "";
    	String line;
    	while ((line = br.readLine()) != null) {
    	output += line + "\n";
    	}
    	br.close();
    	return output;
        
    }
    
    @Override
    public Double getMeAnException() throws HereIsYourException {
        System.out.println("Get exception!");
        
        double x = 42;
        if (x == 42)
        {  
            System.out.println("throwing!");
            throw new HereIsYourException();
        }
        return x;
    }
}
