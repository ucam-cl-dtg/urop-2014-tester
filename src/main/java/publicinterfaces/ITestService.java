package publicinterfaces;

import reportelements.AbstractReport;
import reportelements.Status;
import testingharness.XMLTestSettings;

import javax.ws.rs.*;

import exceptions.TestIDAlreadyExistsException;
import exceptions.TickNotInDBException;
import exceptions.UserNotInDBException;
import exceptions.TestIDNotFoundException;
import exceptions.TestStillRunningException;
import exceptions.WrongFileTypeException;
import gitapidependencies.RepositoryNotFoundException;

import java.io.IOException;
import java.util.List;

@Path("/testerAPI/v2/")
@Produces("application/json")
public interface ITestService {

    @GET //TODO: change to POST (GET is being used for testing)
    @Path("/{crsId}/{tickId}/{repoName}")
    public void runNewTest(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId,
                           @PathParam("repoName") String repoName, @QueryParam("commitId") String commitId) throws TestStillRunningException, IOException, RepositoryNotFoundException, WrongFileTypeException, TestIDNotFoundException;

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
     * @throws TickNotInDBException 
     * @throws UserNotInDBException 
     */
    @GET
    @Path("/{crsId}/{tickId}/last")
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) throws UserNotInDBException, TickNotInDBException;

    @GET
    @Path("/{crsId}/{tickId}/all")
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) throws UserNotInDBException, TickNotInDBException;

    @DELETE
    @Path("/{crsId}")
    public void deleteStudentReportData(@PathParam("crsId") String crsId);

    @DELETE
    @Path("/{crsId}/{tickId}")
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);

    @DELETE
    @Path("/{crsId}/{tickId}/{TODO}")
    public void deleteStudentTickCommit(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId);
    
    @GET
    @Path("/createNewTest/{tickId}")
	void createNewTest(@PathParam("tickId") String tickId, List<XMLTestSettings> checkstyleOpts) throws TestIDAlreadyExistsException;
}
