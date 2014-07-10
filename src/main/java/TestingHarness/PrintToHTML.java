package TestingHarness;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/")
public class PrintToHTML {
	@POST
	@Path("getReport")
	@Produces("application/json")
	public Response printReport() {
		Report report = Tester.getReport();
		return Response.status(200).entity(report).build();
	}
}
