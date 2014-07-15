package TestingHarness;

import java.io.IOException;
import java.util.LinkedList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

//temporary - simulates git teams API
//TODO remove
@Path("/")
public interface WebInterface {
	 @GET
	 @Path("/git")
	 @Produces("application/json")
	 public Response listRepositories();

	 @GET
	 @Path("/git/{repoName:.*}.git")
	 @Produces("application/json")
	 public LinkedList<String> listFiles(@PathParam("repoName") String repoName) throws IOException;

	 @GET
	 @Path("/git/{repoName:.*}.git/{fileName:.*}")
	 @Produces("text/plain")
	 public Response getFile(@PathParam("fileName") String fileName
	                          , @PathParam("repoName") String repoName) throws IOException;
	 /*
	 @POST
	 @Path("/fork")
	 public Response getForkURL(ForkRequestInterface details);
	 */
}
