package gitapidependencies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.ws.rs.core.Response;

public class GitService implements WebInterface {

    public Response listRepositories() {
        return null;
    }

    public LinkedList<String> listFiles(String repoName) throws IOException {
        LinkedList<String> files = new LinkedList<String>();
        //add files here!
        files.add("testfile.java");
        files.add("CheckstyleFormat.xml");
        
        return files;
    }   
    
    public Response getFile(String fileName, String repoName)
            throws IOException {
        /*String line;
        while ((line = br.readLine()) != null) {
            output += line + "\n";
        }
        br.close();
        return Response.status(200).entity(output).build();*/
        return Response.status(200).entity("").build();
        
    }
    
    @Override
    public Double getMeAnException() throws HereIsYourException {
        System.out.println("Get exception!");
        
        int x = 42;
        if (x == 42)
        {  
            System.out.println("throwing!");
            throw new HereIsYourException();
        }
        return new Double(x);
    }
}
