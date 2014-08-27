package dynamictesting;

import java.util.List;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.GitRepositoryCloneException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.InvalidNameException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestInstanceNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.Result;
import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestInfo;
import dynamictesting.TestInstance;

@Path("/tests")
@Produces("application/json")
public interface TestsApi {
	@GET
	@Path("/available")
	public List<TestInfo> listTests() throws TestNotFoundException;
	
	@GET
	@Path("/instances")
	public List<TestInstance> listTestInstances(
	@CookieParam("crsid") String crsid) throws InvalidNameException;
	
	@POST
	@Path("/instances")
	public TestInstance startTest(@CookieParam("crsid") String crsid,
	@FormParam("testContainerID") String testContainerID,
	@FormParam("testID") String testID,
	@FormParam("gitURL") String gitURL,
	@FormParam("privateKey") String privateKey) throws TestNotFoundException,
	GitRepositoryCloneException, InvalidNameException;
	
	@GET
	@Path("/load")
	public Result getSystemLoad();
	
	@GET
	@Path("/instances/{testID}")
	public TestInstance getTestStatus(@CookieParam("crsid") String crsid,
	@PathParam("testID") String containerID)
	throws TestInstanceNotFoundException, InvalidNameException;
	
	@DELETE
	@Path("/instances/{testID}")
	public Result removeTest(@CookieParam("crsid") String crsid,
	@PathParam("testID") String containerID)
	throws TestInstanceNotFoundException, InvalidNameException;
}
