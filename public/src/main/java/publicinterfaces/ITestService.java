package publicinterfaces;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import uk.ac.cam.cl.dtg.teaching.containers.api.exceptions.TestNotFoundException;
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
     *                                      found, thrown by gitAPI
     * @throws TestIDNotFoundException      Thrown if a test doesn't exist with the given Id
     * @throws NoCommitsToRepoException     Thrown if repo hasn't been commited to yet i.e. there is no code
     */
    @GET
    @Path("/{crsId}/{tickId}/{repoName}")
    public String runNewTest(@QueryParam("securityToken") String securityToken,
                             @PathParam("crsId") final String crsId,
                             @PathParam("tickId") final String tickId,
                             @PathParam("repoName") String repoName)
            throws IOException, TestStillRunningException, TestIDNotFoundException, RepositoryNotFoundException, NoCommitsToRepoException;

    /**
     * Returns the status of the latest test running/ran for a given user's tick
     * @param crsId                Id of user whose test should be polled
     * @param tickId               Id of tick whose latest test should be polled
     * @return                     Current status of most recently submitted corresponding test
     * @throws NoSuchTestException Thrown if no test has been started for this user/tick combination
     */
    @GET
    @Path("{crsId}/{tickId}/poll")
    public Status pollStatus(@QueryParam("securityToken") String securityToken,
                             @PathParam("crsId") String crsId,
                             @PathParam("tickId") String tickId)
            throws NoSuchTestException;

    /**
     * Returns the most-recently generated report for a given user's tick
     * Note: this function returns the most recently generated report for a given crsID and tickID, not the report
     * associated with the newest commit, because these are not expected to be different.
     * @param crsId                 Id of user whose report is to be returned
     * @param tickId                Id of tick for which the latest report should be returned
     * @return                      Most recently generated report of type Report
     * @throws UserNotInDBException     Thrown if user record can't be found for the given crsid
     * @throws TickNotInDBException     Thrown if user's report for given tickId can't be found
     */
    @GET
    @Path("/{crsId}/{tickId}/last")
    public Report getLastReport(@QueryParam("securityToken") String securityToken,
                                @PathParam("crsId") String crsId,
                                @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException;

    /**
     * Returns all generated reports for a given user's tick
     * @param crsId                 Id of user whose reports should be returned
     * @param tickId                Id of tick for which the latest reports should be returned
     * @return                      List of all generated reports for a given user's tick of type List<Report>
     * @throws UserNotInDBException   Thrown if user record can't be found for the given crsid
     * @throws TickNotInDBException   Thrown if user's reports for given tickId can't be found
     */
    @GET
    @Path("/{crsId}/{tickId}/all")
    public List<Report> getAllReports(@QueryParam("securityToken") String securityToken,
                                      @PathParam("crsId") String crsId,
                                      @PathParam("tickId") String tickId)
            throws UserNotInDBException, TickNotInDBException;
    /**
     * Deletes all student reports for every tick they have done
     * @param crsId                 Id of user whose reports should be deleted
     * @throws UserNotInDBException   Thrown if user record can't be found for the given crsid
     */
    @DELETE
    @Path("/{crsId}")
    public void deleteStudentReportData(@QueryParam("securityToken") String securityToken,
                                        @PathParam("crsId") String crsId)
            throws UserNotInDBException;

    /**
     * Deletes all generated reports for a given user and one of their ticks
     * @param crsId                 Id of user whose reports should be deleted
     * @param tickId                Id of tick for which the reports will be deleted
     * @throws UserNotInDBException   Thrown if user record can't be found for the given crsid
     * @throws TickNotInDBException   Thrown if user's reports for given tickId can't be found
     */
    @DELETE
    @Path("/{crsId}/{tickId}")
    public void deleteStudentTick(@QueryParam("securityToken") String securityToken,
                                  @PathParam("crsId") String crsId,
                                  @PathParam("tickId") String tickId)
            throws TickNotInDBException, UserNotInDBException;

    /**
     * create a new test referred to by the given tickId with all the settings passed in, or if the test
     * already exists in the database, update its settings
     * @param tickId                Unique Id of the new test being created
     * @param checkstyleOpts        list containing xml files and settings for checkstyles
     * @param containerId           forDynamicTests
     * @param testId				for dynamic tests
     */
    @POST
    @Path("/{tickId}/create")
    @Consumes("application/json")
	public Response createNewTest(@QueryParam("securityToken") String securityToken,
                                  @PathParam("tickId") String tickId, List<StaticOptions> checkstyleOpts,
                                  @QueryParam("containerId") String containerId,  @QueryParam("testId") String testId);

    /**
     * returns all default static test settings that are available for tick setters to use
     * @return      		Object containing dynamic tests available and default tests
     * @throws IOException  Default files to return are stored on disk. IOException is thrown if there was a problem
     *                      reading these
     */
    @GET
    @Path("/testFiles")
    public TickSettings getTestFiles(@QueryParam("securityToken") String securityToken) throws IOException, TestNotFoundException;
    
    /**
     * returns all static test settings that are available for the specified tick (for updating purposes)
     * @param tickId                    Unique Id of the test with the test files being asked for
     * @return                          List of the names and settings of the static tests for the tick
     * @throws TestIDNotFoundException  Thrown if tick doesn't exist
     */
    @GET
    @Path("{tickId}/testFiles")
    public TickSettings getTestFiles(@QueryParam("securityToken") String securityToken,
                                     @PathParam("tickId") String tickId)
            throws TestIDNotFoundException, TestNotFoundException;
    
    /**
     * Allows ticker to pass/fail a student's tick by adding a tickerResult and tickerComment to one of their
     * reports
     * @param crsid                     Id of user whose reports should be edited
     * @param tickId                    Id of tick for which a report will be edited
     * @param tickerResult			    ReportResult defining PASS or FAIL according to the ticker
     * @param tickerComments		    String containing any comments the ticker may have
     * @param commitId				    commitId corresponding to the report that the ticker wants to edit
     * @throws UserNotInDBException     Thrown if user record can't be found for the given crsid
     * @throws TickNotInDBException     Thrown if user's reports for given tickId can't be found
     * @throws ReportNotFoundException  Thrown if report with the commitId doesn't exist
     */
    @POST
    @Path("/{crsid}/{tickId}/set/tickerResult")
    @Consumes("application/json")
    public void setTickerResult(@QueryParam("securityToken") String securityToken,
                                @PathParam("crsid") String crsid ,
                                @PathParam("tickId") String tickId ,
    		                    @QueryParam("tickerResult") ReportResult tickerResult,
                                @QueryParam("tickerComments") String tickerComments,
    		 	                @QueryParam("commitId") String commitId,
                                @QueryParam("date") long date)
    				throws UserNotInDBException, TickNotInDBException, ReportNotFoundException;

//    @GET
//    @Path("/unitTests")
//    public Response getAvailableDynamicTests(@QueryParam("securityToken") String securityToken);
    
    //TODO: cancel test
    //@DELETE
    //@Path("/{crsid}/{tickid}/running")
    //public void cancelRunningTest(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId)
    //  throws NoTestRunningException;
}
