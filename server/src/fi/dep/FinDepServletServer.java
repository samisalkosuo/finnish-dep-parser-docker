package findep;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import findep.utils.SystemOutLogger;
import finwordnet.FinWordNetServlet;

public class FinDepServletServer {

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
		// 1=not so much log, text excerpt
		// 2=more log, all text
		// default is 1
		int LOG_LEVEL = 1;
		SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();

		String _logLevel = System.getenv("log_level");
		if (_logLevel == null) {
			_logLevel = "1";
		}
		try {
			int logLevel = Integer.parseInt(_logLevel);

			switch (logLevel) {
			case 0:
				LOG_LEVEL = 0;
				break;
			case 1:
				LOG_LEVEL = 1;
				break;
			case 2:
				LOG_LEVEL = 2;
				break;

			default:
				LOG_LEVEL = 1;
				break;
			}

		} catch (NumberFormatException nfe) {
			SYSOUTLOGGER.LOG_LEVEL = LOG_LEVEL;
		}

		SYSOUTLOGGER.LOG_LEVEL = LOG_LEVEL;
		SYSOUTLOGGER.sysout(-1, "using log level: " + LOG_LEVEL);

		if (LOG_LEVEL == 0) {
			SYSOUTLOGGER.sysout(-1, "After start, nothing but errors logged.");
		}

		try {
			// Passing in the class for the Servlet allows jetty to instantiate
			// an
			// instance of that Servlet and mount it on a given context path.

			// server.feature is one of: DEP,LEMMA,ALL
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
			SYSOUTLOGGER.sysout(-1, "Server started.");

			// The use of server.join() the will make the current thread join
			// and
			// wait until the server is done executing.
			// See
			// http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
