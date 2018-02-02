package fi.dep;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dep.utils.SystemOutLogger;
import fi.wordnet.FinWordNetServlet;

public class FinDepServletServer {

	private static Logger logger = LoggerFactory.getLogger(FinDepServletServer.class);

	public static void main(String[] args) {
		// Create a basic jetty server object that will listen on port 8080.
		// Note that if you set this to port 0 then a randomly available port
		// will be assigned that you can either look in the logs for the port,
		// or programmatically obtain it for use in test cases.
		Server server = new Server(9876);

		// The ServletHandler is a dead simple way to create a context handler
		// that is backed by an instance of a Servlet.
		// This handler then needs to be registered with the Server object.
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		// set log level at the end of the init method
		// log level
		// 0=no log after start
		// 1=log elapsed time and parsed text excerpt
		// default is 1
		SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();
		String _logLevel = System.getenv("log_level");
		if ("0".equals(_logLevel)) {
			SYSOUTLOGGER.LOG_LEVEL = 0;
		} else {
			SYSOUTLOGGER.LOG_LEVEL = 1;
		}
		logger.info("System.out log level:  {}", SYSOUTLOGGER.LOG_LEVEL);
		if (SYSOUTLOGGER.LOG_LEVEL == 0) {
			logger.info("Parsed texts are not logged to System.out.");
			logger.info("Use browser and statistics page to see latest parsed texts.");
		}

		try {
			// Passing in the class for the Servlet allows jetty to instantiate
			// an
			// instance of that Servlet and mount it on a given context path.

			// server.feature is one of: DEP,LEMMA,FWN,ALL
			// default is ALL
			String serverFeature = System.getenv("server_feature");
			if (serverFeature == null) {
				serverFeature = "ALL";
			}

			// These is a raw Servlet, not a Servlet that has been configured
			// through a web.xml @WebServlet annotation, or anything similar.

			if (serverFeature.contains("ALL") || serverFeature.contains("FWN")) {
				handler.addServletWithMapping(FinWordNetServlet.class, "/finwordnet").setInitOrder(0);

			}

			if (serverFeature.contains("ALL") || serverFeature.contains("DEP")) {
				handler.addServletWithMapping(IS2ParserServlet.class, "/annaparser").setInitOrder(1);
				handler.addServletWithMapping(OmorfiServlet.class, "/omorfi").setInitOrder(1);
				handler.addServletWithMapping(MarmotServlet.class, "/marmot").setInitOrder(1);
				handler.addServletWithMapping(FinDepServlet.class, "/").setInitOrder(2);
			}

			if (serverFeature.contains("ALL") || serverFeature.contains("LEMMA")) {
				handler.addServletWithMapping(PortedServlet.class, "/lemma").setInitOrder(3);
			}

			// Start things up!
			server.start();

			// The use of server.join() the will make the current thread join
			// and
			// wait until the server is done executing.
			// See
			// http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
