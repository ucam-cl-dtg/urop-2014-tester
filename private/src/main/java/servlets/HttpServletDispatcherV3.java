package servlets;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = { "/rest/*" },
	initParams = {
		@WebInitParam(name = "javax.ws.rs.Application", value = "servlets.ApplicationRegister"),
		@WebInitParam(name = "resteasy.servlet.mapping.prefix", value="/rest/")
	})

	public class HttpServletDispatcherV3 extends HttpServletDispatcher {}