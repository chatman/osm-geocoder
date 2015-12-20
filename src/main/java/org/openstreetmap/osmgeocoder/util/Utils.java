package org.openstreetmap.osmgeocoder.util;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;

public class Utils
{
  public static boolean debug = false; 
  
  public static SolrDocument getContainingPolygon(SolrServer server, Node place, int level)
      throws ParseException, SolrServerException
      {
    return getContainingPolygon(server, place.lat, place.lng, level);
      }

  public static SolrDocument getContainingPolygonLowest(SolrServer server, Node place, int level)
      throws ParseException, SolrServerException
      {
    return getContainingPolygonLowest(server, place.lat, place.lng);
      }

  public static List<Node> nodesFromPoints(float[] points) {
    List<Node> nodes = new ArrayList<Node>();
    for(int i=0; i<points.length; i+=2)
      nodes.add(new Node(points[i], points[i+1]));
    return nodes;
  }

  public static double distance(Node a, Node b)
  {
    return 111.3D * Math.sqrt((a.lat - b.lat) * (a.lat - b.lat) + (a.lng - b.lng) * (a.lng - b.lng));
  }

  public static SolrDocument getContainingPolygon(SolrServer server, float lat, float lng, int level) throws ParseException, SolrServerException {
    WKTReader wktreader = new WKTReader();
    Geometry point = wktreader.read("POINT(" + lng + " " + lat + ")");

    SolrQuery query = new SolrQuery("level:" + level);
    query.addFilterQuery(new String[] { "geo:\"Contains(" + lat + "," + lng + ")\"" });

    int counter = 0;

    if ((lat < -90.0F) || (lat > 90.0F) || (lng < -180.0F) || (lng > 180.0F)) {
      return null;
    }

    if(debug) System.out.println("Trying get parent query: "+query);
    QueryResponse response = server.query(query);
    SolrDocument ans = null;

    for (int i = 0; i < response.getResults().getNumFound(); i++) {
      SolrDocument doc = (SolrDocument)response.getResults().get(i);
      //String geo = doc.get("geo").toString();

      //geo = geo.substring(1, geo.length() - 1);

      for (Object val: doc.getFieldValues("geo")) {
        String geo = val.toString();

        Geometry geom = wktreader.read(geo.toString());

        if (geom.contains(point)) {
          ans = doc;
          counter++;
          if(debug) System.out.println("Passed to contain geometry: "+geom.getNumPoints()+", "+point);
        } else {
          if(debug) System.out.println("Failed to contain geometry: "+geom.getNumPoints()+", "+point);
        }
      }

    }

    if (counter != 1) {
      if(debug) System.out.println("Counter was: "+counter);
      return null;
    }
    return ans;
  }

  public static SolrDocument getContainingPolygonLowest(SolrServer server, float lat, float lng) throws ParseException, SolrServerException {
    WKTReader wktreader = new WKTReader();
    Geometry point = wktreader.read("POINT(" + lng + " " + lat + ")");

    SolrQuery query = new SolrQuery("*:*");
    query.addFilterQuery(new String[] { "geo:\"Contains(" + lat + "," + lng + ")\"" });
    query.addSort("level", ORDER.desc);
    query.setFields("admin2", "admin3", "admin4", "admin5", "admin6", "admin7", "level");
    query.setRows(1);

    int counter = 0;

    if ((lat < -90.0F) || (lat > 90.0F) || (lng < -180.0F) || (lng > 180.0F)) {
      return null;
    }

    QueryResponse response = server.query(query);

    for (int i = 0; i < response.getResults().getNumFound(); i++) {
      SolrDocument doc = (SolrDocument)response.getResults().get(i);
      return doc;
    }
    return null;
  }


  public static SolrDocument getNearestPlace(SolrServer server, Node place, int adminLevel, String nearest, double distance)
  {
    return getNearestPlace(server, place.lat, place.lng, adminLevel, nearest, distance);
  }

  public static SolrDocument getNearestPlace(SolrServer server, float lat, float lng, int adminLevel, String nearest, double distance) {
    String[] nearestValues = nearest.split("\\|");
    SolrQuery query = new SolrQuery();
    query.setQuery("{!geofilt score=distance sfield=geo pt=" + lat + "," + lng + " d=" + distance + "}");

    String filterQuery = "level:" + adminLevel + " AND (";
    for (String nearestVal : nearestValues)
      filterQuery = filterQuery + "place:" + nearestVal + " ";
    filterQuery = filterQuery + ")";
    query.setFilterQueries(new String[] { filterQuery });

    query.setSort("score", SolrQuery.ORDER.asc);
    query.setFields(new String[] { "*", "score" });
    query.setRows(Integer.valueOf(1));

    QueryResponse response = null;
    try {
      response = server.query(query);
    }
    catch (SolrServerException e) {
      e.printStackTrace();
    }

    if ((response == null) || (response.getResults().getNumFound() == 0L)) {
      return null;
    }
    return (SolrDocument)response.getResults().get(0);
  }
}
