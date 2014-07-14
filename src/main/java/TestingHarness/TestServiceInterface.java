package TestingHarness;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/testerAPI")
public interface TestServiceInterface {
	@GET
	@Path("/runNewTest")
	public Response runNewTest(@QueryParam("repoAddress") String repoAddress);
	
	@GET
	@Path("/pollStatus")
	@Produces("text/plain")
	public Response pollStatus(@QueryParam("testID") String testID);
	
	@GET
	@Path("/getReport")
	@Produces("application/json")
	public Response getReport(@QueryParam("testID") String testID);
	
	@GET
	@Path("/test")
	@Produces("text/plain")
	public Response test();
}
