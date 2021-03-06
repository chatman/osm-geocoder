package org.openstreetmap.osmgeocoder.indexer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminPolygonIndexer
{
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  final int PRINTINTERVAL = 1;

  void indexPolygons(SolrServer server, DB mapDb, int adminLevel)
      throws ClassNotFoundException, IOException, com.vividsolutions.jts.io.ParseException, SolrServerException
  {
    AdminPolygonReader reader = new AdminPolygonReader();
    reader.read(mapDb, ""+adminLevel);
    log.info("AdminPolygonReader done.");

    log.info("Numbers of admin areas read: "+reader.places.size());

    int counter = 0;

    for (AdminPolygon admin : reader.places)
    {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("name", admin.tags.get("name"));
      doc.addField("admin" + adminLevel, admin.tags.get("name"));
      doc.addField("id", admin.id);
      doc.addField("level", Integer.valueOf(adminLevel));

      int maxSize = 0;
      Point centroid = null;

      for (List<Node> nodes : admin.multipoly)
      {
        StringBuilder wkt = new StringBuilder();

        if (counter % PRINTINTERVAL == 0) {
          log.info(counter + ": " + doc.get("name") + ",\t" + "Points = " + nodes.size());
        }
        if (nodes.size() >= 2)
        {
          for (Node node : nodes)
            wkt.append(node.lng + " " + node.lat + ", ");
          wkt.delete(wkt.length() - 2, wkt.length());

          wkt.insert(0, "POLYGON((");
          wkt.append("))");

          WKTReader wktreader = new WKTReader();
          Geometry geom = null;
          try
          {
            geom = wktreader.read(wkt.toString());
          } catch (Exception ex) {
            ex.printStackTrace();
            continue;
          }

          if (nodes.size() > maxSize) {
            centroid = geom.getCentroid();
            maxSize = nodes.size();
          }

          Polygon pol = (Polygon)geom;

          if (!pol.isValid()) {
            Geometry repaired = pol.buffer(0.0D);
            log.info("Invalid polygon detected. Is fixed? " + repaired.isValid());
            wkt = new StringBuilder(repaired.toText());
          }

          doc.addField("geo", wkt);
        }
      }
      if (centroid != null)
        for (int l = 1; l < adminLevel; l++) {
          SolrDocument parent = Utils.getContainingPolygon(server, (float)centroid.getY(), (float)centroid.getX(), l);
          if (parent != null) {
            doc.addField("admin" + l, parent.get("name"));
          }
          log.debug("Parent of "+admin.tags.get("name")+" at level "+l+": "+(parent!=null?parent.get("name"):null));
        }
      try
      {
        server.add(doc);
      } catch (Exception ex) {
        System.err.println("Document failed: " + ex);
      }

      counter++;
    }

    server.commit();
  }

  public static void main(String[] args)
      throws SolrServerException, IOException, ClassNotFoundException, java.text.ParseException, com.vividsolutions.jts.io.ParseException
  {
    SolrServer server = new ConcurrentUpdateSolrServer("http://localhost:8983/solr", 16, 8);

    AdminPolygonIndexer indexer = new AdminPolygonIndexer();

    DB db = DBMaker.newFileDB(new File("jdbms/india")).closeOnJvmShutdown().transactionDisable().asyncWriteEnable().make();

    //indexer.indexPolygons(server, db, 2);
    indexer.indexPolygons(server, db, 4);
    //indexer.indexPolygons(server, db, 5);

    log.info("Committing...");
    server.commit();

    log.info("Done! "+new Date());
  }
}