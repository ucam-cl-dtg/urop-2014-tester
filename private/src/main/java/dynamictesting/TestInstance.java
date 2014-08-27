package dynamictesting;

import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.cl.dtg.teaching.containers.api.model.TestStep;
import uk.ac.cam.cl.dtg.teaching.exceptions.SerializableException;

public class TestInstance {
	public static final String STATUS_UNINITIALIZED = "UNINITIALIZED";
	public static final String STATUS_STARTING = "STARTING";
	public static final String STATUS_RUNNING = "RUNNING";
	public static final String STATUS_PASSED = TestStep.STATUS_PASS;
	public static final String STATUS_FAILED = TestStep.STATUS_FAIL;
	public static final String STATUS_WARNING = TestStep.STATUS_WARNING;
	public static final String STATUS_MANUALCHECK = TestStep.STATUS_MANUALCHECK;
	private List<TestStep> results = new LinkedList<>();
	private String status = STATUS_UNINITIALIZED;
	private String testContainerID;
	private String testID;
	private String gitURL;
	private String crsid;
	private String containerID;
	private String name;
	private SerializableException exception;

	public TestInstance(String testContainerID, String testID, String gitURL,
			String crsid, String containerID, String name) {
		this.testContainerID = testContainerID;
		this.testID = testID;
		this.gitURL = gitURL;
		this.crsid = crsid;
		this.containerID = containerID;
		this.name = name;
	}

	public TestInstance() {}
	
	public List<TestStep> getResults() {
		return results;
	}

	public void setResults(List<TestStep> results) {
		this.results = results;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public SerializableException getException() {
		return exception;
	}

	public void setException(SerializableException exception) {
		this.exception = exception;
	}

	public String getTestContainerID() {
		return testContainerID;
	}

	public void setTestContainerID(String testContainerID) {
		this.testContainerID = testContainerID;
	}

	public String getTestID() {
		return testID;
	}

	public void setTestID(String testID) {
		this.testID = testID;
	}

	public String getGitURL() {
		return gitURL;
	}

	public void setGitURL(String gitURL) {
		this.gitURL = gitURL;
	}

	public String getCrsid() {
		return crsid;
	}

	public void setCrsid(String crsid) {
		this.crsid = crsid;
	}

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
