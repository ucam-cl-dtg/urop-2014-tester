package threadcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import publicinterfaces.Status;
import testingharness.TestService;
import testingharness.Tester;
import uk.ac.cam.cl.git.interfaces.WebInterface;

public class TesterThread implements Runnable {
	private static Logger log = LoggerFactory.getLogger(TesterThread.class);
	private Tester tester;
	private String crsId;
	private String tickId;
	private String commitId;
	private WebInterface gitProxy;
	private Status status = null;
	
	public TesterThread(Tester tester, String crsId, String tickId, String commitId, WebInterface gitProxy) {
		this.tester = tester;
		this.crsId = crsId;
		this.tickId = tickId;
		this.commitId = commitId;
		this.gitProxy = gitProxy;
		log.debug(crsId + " " + tickId + " " + commitId + ": Thread created");
	}

	@Override
	public void run() {
		if(this.status == null) {
			log.debug(crsId + " " + tickId + " " + commitId + ": Test was put straight in pool, creating status");
			this.status = new Status();
		}
		log.debug(crsId + " " + tickId + " " + commitId + ": Thread starting to run");
		tester.runTests(crsId, tickId, commitId,gitProxy,status);
		log.debug(crsId + " " + tickId + " " + commitId + ": Tests finished in thread");
        assert TestService.getTicksInProgress().containsKey(crsId + tickId);
        log.debug(crsId + " " + tickId + " " + commitId + ": removing thread from current active threads");
        TestService.getTicksInProgress().remove(crsId + tickId);
        log.debug(crsId + " " + tickId + " " + commitId + ": Thread removed");
	}

	public String getCrsId() {
		return crsId;
	}

	public String getTickId() {
		return tickId;
	}

	public void setStatus(Status s) {
		this.status = s;
	}
	
	public Status getStatus() {
		return status;
	}
}
