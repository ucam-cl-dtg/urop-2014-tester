package gitapidependencies;

import java.io.IOException;
import java.util.LinkedList;

public class GitService implements WebInterface {

    @Override
    public LinkedList<String> listFiles(String repoName) throws IOException {
        LinkedList<String> files = new LinkedList<>();
        //add files here!
        files.add("testfile.java");
        files.add("CheckstyleFormat.xml");
        
        return files;
    }   
    
    public String getFile(String fileName, String repoName)
            throws IOException {
        /*String line;
        while ((line = br.readLine()) != null) {
            output += line + "\n";
        }
        br.close();
        return Response.status(200).entity(output).build();*/
        return "";
        
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
