package testingharness;

import database.IDBReportManager;
import database.Mongo;
import database.MongoDBReportManager;
import reportelements.AbstractReport;
import futuratedreportelements.Severity;
import reportelements.SimpleReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.ITestService;
import reportelements.Status;

import javax.ws.rs.PathParam;

import java.util.*;

public class TestServiceTwo implements ITestService {
    // initialise log4j logger
    private static Logger log = LoggerFactory.getLogger(TestService.class);
    private IDBReportManager db = new MongoDBReportManager(Mongo.getDb());

    @Override
    public void runNewTest(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId, @PathParam("commitId") String commitId) {
        AbstractReport reportToAdd = new SimpleReport();
        reportToAdd.addDetail("bad indentation", Severity.WARNING, "eg.java", 7, "Expected 12 spaces, found 10");
        db.addReport(crsId, tickId, commitId, reportToAdd);
    }

    @Override
    public Status pollStatus(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {
        return null;
    }

    @Override
    public AbstractReport getLastReport(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {
        return db.getLastReport(crsId, tickId);
    }

    @Override
    public List<AbstractReport> getAllReports(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {
        return db.getAllReports(crsId, tickId);
    }

    @Override
    public void deleteStudentReportData(@PathParam("crsId") String crsId) {

    }

    @Override
    public void deleteStudentTick(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {

    }

    @Override
    public void deleteStudentTickCommit(@PathParam("crsId") String crsId, @PathParam("tickId") String tickId) {

    }
}
