package publicinterfaces;

import futuratedreportelements.AbstractReport;
import futuratedreportelements.Report;
import reportelements.Status;

import javax.ws.rs.*;

import database.DBUser;

import java.util.List;

@Path("/testerAPI/v2/")
@Produces("application/json")
public interface ITestService {

    @GET //TODO: change to POST (GET is being used for testing)
    @Path("/{crsId}/{tickId}/{repoName}")
    public void runNewTest(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId,
                           @PathParam("repoName") String repoName);

    //TODO: should we also do a GET for this?

    @GET
    @Path("{crsId}/{tickId}/poll")
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);

    /**
     * TODO: finish JavaDoc
     * Note: this function returns the most recently generated report for a given crsID and tickID, not the report
     * associated with the newest commit, because these are not expected to be different
     *
     * @param crsId
     * @param tickId
     * @return
     */
    @GET
    @Path("/{crsId}/{tickId}/last")
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);

    @GET
    @Path("/{crsId}/{tickId}/all")
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);

    @DELETE
    @Path("/{crsId}")
    public void deleteStudentReportData(@PathParam("crsId") String crsId);

    @DELETE
    @Path("/{crsId}/{tickId}")
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);

    @DELETE
    @Path("/{crsId}/{tickId}/{TODO}")
    public void deleteStudentTickCommit(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);
}
