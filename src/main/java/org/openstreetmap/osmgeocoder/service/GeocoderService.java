package org.openstreetmap.osmgeocoder.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class GeocoderService
{
	public static void main(String[] args)
			throws Exception
			{
		Server server = new Server(8080);

		ServletContextHandler context = new ServletContextHandler(1);
		context.setContextPath("/");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new GeocoderServlet()), "/*");

		server.start();
		server.join();
			}
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.service.GeocoderService
 * JD-Core Version:    0.6.2
 */