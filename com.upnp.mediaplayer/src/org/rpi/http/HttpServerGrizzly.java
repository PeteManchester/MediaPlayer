package org.rpi.http;

import java.net.URI;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

//http://localhost:8090/MainPage.html

/**
 * Main class.
 * 
 */
public class HttpServerGrizzly {
	
	private Logger log = Logger.getLogger(this.getClass());
	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://0.0.0.0:";
	boolean run = true;
	private  HttpServer server =null;
	
	public HttpServerGrizzly(String port) {
		if(port==null || port.equalsIgnoreCase(""))
		{
			port = "8088";
		}
		Grizzly.logger(this.getClass()).getParent().setLevel(Level.ALL);
		String uri = BASE_URI + port + "/myapp/";
		log.info("HTTP Server URI: " + uri);
		server = startServer(uri);

		log.info("BaseDir: " + HttpServer.class.getClassLoader().toString());
		log.info("Jersey app started: "+ uri);

		StaticHttpHandler handler = new StaticHttpHandler("./web", "/static");
		handler.setFileCacheEnabled(false);
		server.getServerConfiguration().addHttpHandler(handler);
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	private HttpServer startServer(String uri) {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in org.rpi.web.rest package
		log.info("Starting HTTP Server: " + uri);
		final ResourceConfig rc = new ResourceConfig().packages("org.rpi.web.rest");
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
	}
	
	public void shutdown()
	{
		if(server !=null)
		{
			try
			{
				server.shutdownNow();
			}
			catch(Exception e)
			{
				log.error("Error Shutting Down HTTP Server", e);
			}
		}
	}
}
