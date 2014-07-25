package publicinterfaces;

import javax.ws.rs.*;

import uk.ac.cam.cl.git.api.RepositoryNotFoundException;

import java.io.IOException;
import java.util.List;

/**
 * Interface for all API functions provided by UROP TestingSystem project
 * @author as2388
 * @author kls82
 */
@Path("/testerAPI/v2/")
@Produces("application/json")
public interface ITestService {

    /**
     * Starts a new test.
     * @param crsId                         Id of user for whom test is to be run
     * @param tickId                        Id of tick for which test is
     * @param repoName                      Name of git repository to access to obtain the student's code
     * @return                              "Test Started"
     * @throws TestStillRunningException    Thrown if the user already has a submission processing for this tick
     * @throws IOException                  Thrown by git API
     * @throws RepositoryNotFoundException  Thrown if the repository allegedly containing the student's code couldn't be
     *                                      found
     * @throws TestIDNotFoundException
     */
    @GET
    @Path("/{crsId}/{tickId}/{repoName}")
    @Consumes("application/json")
    public String runNewTest(@PathParam("crsId") final String crsId, @PathParam("tickId") final String tickId,
                           @PathParam("repoName") String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException;

    /**
     * Returns the status of the latest test running/ran for a given user's tick
     * @param crsId                Id of user whose test should be polled
     * @param tickId               Id of tick whose latest test should be polled
     * @return                     Current status of test
     * @throws NoSuchTestException Thrown if no test has been started for this user/tick combination
     */
    @GET
    @Path("{crsId}/{tickId}/running")
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws NoSuchTestException;

    /**
     * TODO: finish JavaDoc
     * Returns the most-recently generated report.
     * Note: this function returns the most recently generated report for a given crsID and tickID, not the report
     * associated with the newest commit, because these are not expected to be different.
     * @param crsId                 Id of user whose report is to be returned
     * @param tickId                Id of tick for which the latest report should be returned
     * @return                      Most recently generated report
     * @throws UserNotInDBException
     * @throws TickNotInDBException
     */
    @GET
    @Path("/{crsId}/{tickId}/last")
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException;

    /**
     * Returns all generated reports for a given user's tick
     * @param crsId                 Id of user whose reports should be returned
     * @param tickId                Id of tick for which the latest reports should be returned
     * @return                      List of all generated reports for a given user's tick
     * @throws UserNotInDBException
     * @throws TickNotInDBException
     */
    @GET
    @Path("/{crsId}/{tickId}/all")
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException;

    @DELETE
    @Path("/{crsId}")
    public void deleteStudentReportData(@PathParam("crsId") String crsId) throws UserNotInDBException;

    @DELETE
    @Path("/{crsId}/{tickId}")
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
            throws TestIDNotFoundException, UserNotInDBException;

    @GET
    @Path("/{tickId}/create")
	public void createNewTest(@PathParam("tickId") String tickId /* , List<XMLTestSettings> checkstyleOpts */)
            throws TestIDAlreadyExistsException;

    //TODO: cancel test?
    //@DELETE
    //@Path("/{crsid}/{tickid}/running")
    //public void cancelRunningTest(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
    //  throws NoTestRunningException;
}