package gitapidependencies;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.LinkedList;

//temporary - simulates git teams API
@Path("/")
public interface WebInterface {
    @GET
    @Path("/git/{repoName:.*}.git")
    @Produces("application/json")
    public LinkedList<String> listFiles(@PathParam("repoName") String repoName) throws IOException, RepositoryNotFoundException;

    @GET
    @Path("/git/{repoName:.*}.git/{fileName:.*}")
    @Produces("text/plain")
    public String getFile(@PathParam("fileName") String fileName
            , @PathParam("repoName") String repoName) throws IOException, RepositoryNotFoundException;
    
    @GET
    @Path("/exception-please")
    public Double getMeAnException() throws HereIsYourException;
}
