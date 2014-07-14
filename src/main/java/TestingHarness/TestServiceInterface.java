package TestingHarness;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/testerAPI")
@Produces("application/json")
public interface TestServiceInterface {
	@GET
	@Path("/runNewTest")
	public abstract String runNewTest(@QueryParam("repoAddress") String repoAddress);
	
	@GET
	@Path("/pollStatus")
	public abstract Response pollStatus(@QueryParam("testID") String testID);
	
	@GET
	@Path("/getReport")
	public abstract Report getReport(@QueryParam("testID") String testID);
	
	@GET
	@Path("/test")
	public abstract Report test(@QueryParam("testID") String testID);
}
