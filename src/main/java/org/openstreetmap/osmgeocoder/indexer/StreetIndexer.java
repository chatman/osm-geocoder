package org.openstreetmap.osmgeocoder.indexer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.indexer.primitives.Way;
import org.openstreetmap.osmgeocoder.util.Utils;

public class StreetIndexer
{
  public static Set<String> acceptedTypes = new HashSet<String>(Arrays.asList(new String[] { "road", "street", "st", "rd", "marg", "lane", 
      "cross", "ln", "bridge", "path", "bypass", "main", "avenue", "trail", "flyover", "sarani", 
      "highway", "salai", "circle", "chowk", "ave", "gali", "link", "bazaar", "galli", "extension", 
      "ghat", "bazar", "park", "garden", "layout", "exit", "raod", "expressway", "market", "subway", 
      "way", "tunnel", "square", "wadi", "track", "place", "pass", "junction", "walkway", 
      "connector", "driveway" }));

  void indexStreets(SolrServer server, DB mapDb) throws Exception {
    Map<String, String> normalizer = new HashMap<String, String>();
    normalizer.put("st", "street");
    normalizer.put("rd", "road");
    normalizer.put("raod", "road");
    normalizer.put("ave", "avenue");
    normalizer.put("ln", "lane");
    normalizer.put("bazar", "bazaar");

    WKTReader wktReader = new WKTReader();

    Map<Long, Way> wayStore = mapDb.getTreeMap("wayStore");

    int counter = 0;
    int streetCounter = 0;

    System.out.println(wayStore.size() + " ways.");
    for (Long id : wayStore.keySet()) {
      Way way = (Way)wayStore.get(id);

      if ((way.tags.containsKey("highway")) && (way.tags.containsKey("name")))
      {
        counter++;

        StringBuilder shapeStr = new StringBuilder("LINESTRING(");
        if (way.numNodes() >= 2)
        {
          //					for (Node nd : way.nodes)
          //						shapeStr.append(nd.lng + " " + nd.lat + ",");
          for(int i=0; i<way.numNodes(); i++) {
            shapeStr.append(way.getNode(i)[1] + " " + way.getNode(i)[0] + ",");
          }

          shapeStr.deleteCharAt(shapeStr.length() - 1);
          shapeStr.append(")");

          String name = ((String)way.tags.get("name")).toLowerCase().trim();
          name = name.replaceAll("\\.", " ").replaceAll("\\(.*\\)", " ").replaceAll("  ", " ").trim();
          String[] split = name.split(" ");

          String basename = ""; String type = "";
          type = split[(split.length - 1)];
          for (int i = 0; i < split.length - 1; i++)
            basename = basename + split[i] + " ";
          basename = basename.trim();

          if (acceptedTypes.contains(type))
          {
            Geometry shape = wktReader.read(shapeStr.toString());
            Point centroid = shape.getCentroid();

            if ((centroid.getX() >= -180.0D) && (centroid.getX() <= 180.0D) && (centroid.getY() >= -90.0D) && (centroid.getY() <= 90.0D) && 
                (!Float.isNaN((float)centroid.getY())) && (!Float.isNaN((float)centroid.getX())))
            {
              SolrDocument localityOrSuburb = null; SolrDocument town = null; SolrDocument city = null;
              SolrDocument parent = null;
              localityOrSuburb = Utils.getNearestPlace(server, (float)centroid.getY(), (float)centroid.getX(), 7, "locality|suburb", 3.0D);
              parent = localityOrSuburb;
              if (parent == null) {
                town = Utils.getNearestPlace(server, (float)centroid.getY(), (float)centroid.getX(), 6, "town", 4.0D);
                parent = town;
              }
              if (parent == null) {
                city = Utils.getNearestPlace(server, (float)centroid.getY(), (float)centroid.getX(), 6, "city", 15.0D);
                parent = city;
              }
              if (parent == null) {
                town = Utils.getNearestPlace(server, (float)centroid.getY(), (float)centroid.getX(), 6, "town", 8.0D);
                parent = town;
              }
              if (parent != null)
              {
                if (parent != null) {
                  if (streetCounter % 100 == 0) {
                    System.out.println(streetCounter + ": " + name + ", " + parent.getFieldValue("name"));
                  }

                  streetCounter++;

                  type = normalizer.containsKey(type) ? (String)normalizer.get(type) : type;

                  SolrInputDocument doc = new SolrInputDocument();
                  doc.addField("name", way.tags.get("name"));
                  doc.addField("id", way.id);
                  doc.addField("level", Integer.valueOf(20));
                  doc.addField("street_type", type);
                  doc.addField("street", basename);

                  int parentLevel = Integer.parseInt(parent.get("level").toString());
                  if ((parentLevel >= 7) && (parent.getFieldNames().contains("admin7")))
                    doc.addField("admin7", parent.getFirstValue("admin7"));
                  if ((parentLevel >= 6) && (parent.getFieldNames().contains("admin6")))
                    doc.addField("admin6", parent.getFirstValue("admin6"));
                  if ((parentLevel >= 5) && (parent.getFieldNames().contains("admin5")))
                    doc.addField("admin5", parent.getFirstValue("admin5"));
                  if ((parentLevel >= 4) && (parent.getFieldNames().contains("admin4")))
                    doc.addField("admin4", parent.getFirstValue("admin4"));
                  if ((parentLevel >= 2) && (parent.getFieldNames().contains("admin2"))) {
                    doc.addField("admin2", parent.getFirstValue("admin2"));
                  }
                  doc.addField("geo", shapeStr);
                  doc.addField("geo", centroid.toText());
                  server.add(doc);
                }
              }
            }
          }
        }
      }
    }
    System.out.println(counter + " streets.");

    server.commit();
  }

  public static void main(String[] args) throws Exception {
    DB db = DBMaker.newFileDB(new File("jdbm/ireland-latest.osm")).closeOnJvmShutdown().encryptionEnable("password").make();
    HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");

    StreetIndexer indexer = new StreetIndexer();
    indexer.indexStreets(server, db);

    db.close();
  }
}
