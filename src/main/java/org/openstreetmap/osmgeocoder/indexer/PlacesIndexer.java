package org.openstreetmap.osmgeocoder.indexer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.indexer.primitives.Way;
import org.openstreetmap.osmgeocoder.util.Utils;

public class PlacesIndexer
{
  String placesFilename;
  Set<String> poiKeys = new HashSet<String>(
      Arrays.asList(new String[] { "leisure", "amenity", 
          "building", "craft", "man_made", "landuse", "natural", "railway", "shop", 
          "sport", "tourism", "aeroway" }));

  public PlacesIndexer(String file)
  {
    this.placesFilename = file;
  }

  @SuppressWarnings("unchecked")
  void indexCitiesAndTowns(SolrServer server) throws ClassNotFoundException, IOException, SolrServerException, ParseException {
    FileInputStream fin = new FileInputStream(this.placesFilename);
    ObjectInputStream ois = new ObjectInputStream(fin);
    List<Node> places = (List<Node>)ois.readObject();
    ois.close();

    int counter = 0;

    for (Node place : places) {
      if (("city".equals(place.tags.get("place"))) || ("town".equals(place.tags.get("place"))))
      {
        SolrDocument admin2 = Utils.getContainingPolygon(server, place, 2);
        SolrDocument admin4 = Utils.getContainingPolygon(server, place, 4);
        SolrDocument admin5 = Utils.getContainingPolygon(server, place, 5);

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", place.id);
        doc.addField("name", place.tags.get("name"));
        doc.addField("admin6", place.tags.get("name"));
        doc.addField("place", place.tags.get("place"));
        doc.addField("level", Integer.valueOf(6));

        if (admin2 != null)
          doc.addField("admin2", admin2.getFirstValue("name"));
        if (admin4 != null)
          doc.addField("admin4", admin4.getFirstValue("name"));
        if (admin5 != null)
          doc.addField("admin5", admin5.getFirstValue("name"));
        doc.addField("geo", place.lat + "," + place.lng);

        if (counter % 100 == 0) {
          System.out.println(counter + ": " + doc);
        }

        server.add(doc);

        counter++;
      }
    }
    server.commit();
  }

  @SuppressWarnings("unchecked")
  void indexLocalitiesAndSuburbs(SolrServer server)
      throws IOException, ClassNotFoundException, ParseException, SolrServerException
      {
    FileInputStream fin = new FileInputStream(this.placesFilename);
    ObjectInputStream ois = new ObjectInputStream(fin);
    List<Node> places = (List<Node>)ois.readObject();
    ois.close();

    int counter = 0;
    for (Node place : places) {
      if ((place.tags.containsKey("name")) && (
          ("locality".equals(place.tags.get("place"))) || 
          ("suburb".equals(place.tags.get("place"))) || 
          ("neighbourhood".equals(place.tags.get("place"))) || 
          ("neighborhood".equals(place.tags.get("place")))))
      {
        int townThreshold = 2;
        int cityThreshold = 15;
        int nearestTownThreshold = 7;

        if ("village".equals(place.tags.get("place"))) {
          nearestTownThreshold = 0;
        }

        SolrDocument town = Utils.getNearestPlace(server, place, 6, "town", townThreshold);
        SolrDocument city = Utils.getNearestPlace(server, place, 6, "city", cityThreshold);

        if ((town == null) && (city == null)) {
          town = Utils.getNearestPlace(server, place, 6, "town", nearestTownThreshold);
        }
        SolrDocument parent = null;

        if (town != null)
          parent = town;
        else if (city != null) {
          parent = city;
        }

        if (parent == null) {
          counter++;
        }
        SolrDocument admin2 = Utils.getContainingPolygon(server, place, 2);
        SolrDocument admin4 = Utils.getContainingPolygon(server, place, 4);
        SolrDocument admin5 = Utils.getContainingPolygon(server, place, 5);

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("name", place.tags.get("name"));
        doc.addField("id", place.id);
        doc.addField("place", place.tags.get("place"));
        doc.addField("admin7", place.tags.get("name"));
        doc.addField("level", Integer.valueOf(7));

        if (parent != null)
          doc.addField("admin6", parent.getFirstValue("name"));
        if (admin2 != null)
          doc.addField("admin2", admin2.getFirstValue("name"));
        if (admin4 != null)
          doc.addField("admin4", admin4.getFirstValue("name"));
        if (admin5 != null)
          doc.addField("admin5", admin5.getFirstValue("name"));
        doc.addField("geo", place.lat + "," + place.lng);

        if (counter % 100 == 0) {
          System.out.println(counter + ": " + doc);
        }
        server.add(doc);

        counter++;
      }

    }

    System.out.println(counter);
    server.commit();
      }

  @SuppressWarnings("unchecked")
  void indexPlaces(SolrServer placesServer, SolrServer server, DB mapDb, String poiFilename)
      throws ClassNotFoundException, IOException, SolrServerException, ParseException
      {
    WKTReader wktReader = new WKTReader();

    FileInputStream fin = new FileInputStream(poiFilename);
    ObjectInputStream ois = new ObjectInputStream(fin);
    List<Object> poiStore = (List<Object>)ois.readObject();
    ois.close();

    int counter = 0;

    for (Object poi: poiStore) { 

      Way way = null;
      Node node = null;

      if ((poi instanceof Way)) {
        way = (Way)poi;

        StringBuilder shapeStr = new StringBuilder("POLYGON((");
        /*for (Node n : way.nodes)
					shapeStr.append(n.lng + " " + n.lat + ",");*/
        for(int i=0; i<way.numNodes(); i++) {
          shapeStr.append(way.getNode(i)[1] + " " + way.getNode(i)[0] + ",");
        }
        shapeStr.deleteCharAt(shapeStr.length() - 1);
        shapeStr.append("))");

        Geometry geom = null;
        try
        {
          geom = wktReader.read(shapeStr.toString());
        } catch (Exception ex) {
          System.err.println(ex);
        }
        if (geom == null)
          continue;
        node = new Node(way.id, ""+geom.getCentroid().getY(), ""+geom.getCentroid().getX());
        node.tags = way.tags;
      }
      else if ((poi instanceof Node)) {
        node = (Node)poi;
      }

      /*SolrDocument admin2 = Utils.getContainingPolygon(server, node, 2);
			SolrDocument admin4 = Utils.getContainingPolygon(server, node, 4);
			SolrDocument admin5 = Utils.getContainingPolygon(server, node, 5);*/
      SolrDocument parent = Utils.getContainingPolygonLowest(server, node, 5);
      
      SolrInputDocument doc = new SolrInputDocument();

      for (String key : this.poiKeys) {
        if (node.tags.containsKey(key)) {
          doc.addField("category", node.tags.get(key));
          break;
        }

      }

      doc.addField("id", node.id);
      doc.addField("name", node.tags.get("name"));

      if (parent != null) {
        doc.addField("admin2", parent.getFirstValue("admin2"));
        doc.addField("admin3", parent.getFirstValue("admin3"));
        doc.addField("admin4", parent.getFirstValue("admin4"));
        doc.addField("admin5", parent.getFirstValue("admin5"));
      }
      doc.addField("geo", node.lat + "," + node.lng);

      if (counter % 10000 == 0) {
        System.out.println(counter + ": " + doc);
      }
      if (counter % 100000 == 0) {
        System.out.println("Running gc, "+new Date());
        Runtime.getRuntime().gc();
        System.out.println("Done gc, "+new Date());
      }

      placesServer.add(doc);

      counter++;
    }

    placesServer.commit();
      }

  public static void main(String[] args)
      throws ClassNotFoundException, IOException, SolrServerException, ParseException
      {
    SolrServer server = new ConcurrentUpdateSolrServer("http://localhost:8983/solr", 8, 4);
    SolrServer placesServer = new ConcurrentUpdateSolrServer("http://localhost:8983/solr/places", 1024, 32);
    
    placesServer.deleteByQuery("*:*");
    placesServer.commit();

    DB db = DBMaker.newFileDB(new File("jdbm/test")).closeOnJvmShutdown().make();
    PlacesIndexer indexer = new PlacesIndexer("places.asia.ser");

    indexer.indexPlaces(placesServer, server, db, "pois.asia.ser");
    //indexer.indexCitiesAndTowns(placesServer);
    //indexer.indexLocalitiesAndSuburbs(placesServer);

    db.close();
      }
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.indexer.PlacesIndexer
 * JD-Core Version:    0.6.2
 */