package servlets;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import publicinterfaces.ITestService;
import testingharness.TestService;
import uk.ac.cam.cl.dtg.teaching.exceptions.RemoteFailureHandler;
import uk.ac.cam.cl.dtg.teaching.exceptions.ExceptionHandler;

/**
* This class registers the resteasy handlers. The name is important since it is
* used as a String in HttpServletDispatcherV3
*
* @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
*
*/
public class ApplicationRegister extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> result = new HashSet<Class<?>>();
		result.add(TestService.class);
		result.add(RemoteFailureHandler.class);
		result.add(ExceptionHandler.class);
		return result;
	}
}