package org.openstreetmap.osmgeocoder.service;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GeocoderServlet extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
			{
		super.doGet(req, resp);
			}
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.service.GeocoderServlet
 * JD-Core Version:    0.6.2
 */