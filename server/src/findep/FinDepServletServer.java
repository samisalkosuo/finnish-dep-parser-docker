package findep;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

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



		try {
			// Passing in the class for the Servlet allows jetty to instantiate an
			// instance of that Servlet and mount it on a given context path.

			// IMPORTANT:
			// This is a raw Servlet, not a Servlet that has been configured
			// through a web.xml @WebServlet annotation, or anything similar.
			//handler.addServletWithMapping(FinDepServlet.class, "/").setInitOrder(0);
			handler.addServletWithMapping(OmorfiServlet.class, "/omorfi").setInitOrder(0);
			handler.addServletWithMapping(MarmotServlet.class, "/marmot").setInitOrder(0);
			handler.addServletWithMapping(PortedServlet.class, "/lemma").setInitOrder(1);
			handler.addServletWithMapping(FinDepServlet.class, "/").setInitOrder(0);
			
			// Start things up!			
			server.start();
			
			
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
