package org.openstreetmap.osmgeocoder.indexer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.vividsolutions.jts.io.ParseException;

public class IndexerMain
{
  static String country = "asia";
  static String jdbm = "jdbm/test";
  static String nodesFilename = "/bigdata/datasets/nodes.xml";
  static String waysFilename = "/bigdata/datasets/ways.xml";
  static String relationsFilename = "/bigdata/datasets/relations.xml";

  static SolrServer server;
  static SolrServer placesServer;
  static DB mapDb;
  static boolean delete = true;
  static boolean osmRead = true;

  static PlacesIndexer placesIndexer;

  public static void main(String[] args) throws Exception
  {
    if (args.length >= 5) {
      country = args[0];
      jdbm = args[1];
      nodesFilename = args[2];
      waysFilename = args[3];
      relationsFilename = args[4];
    }

    if (args.length >= 6)
      delete = Boolean.parseBoolean(args[5]);
    if (args.length >= 7) {
      osmRead = Boolean.parseBoolean(args[6]);
    }

    server = new ConcurrentUpdateSolrServer("http://localhost:8983/solr/collection1", 64, 16);
    placesServer = new ConcurrentUpdateSolrServer("http://localhost:8983/solr/places", 32, 16);

    mapDb = DBMaker.newFileDB(new File(jdbm)).closeOnJvmShutdown().transactionDisable().asyncWriteEnable().make();

    if (delete) {
      System.out.println("Deleting docs..");
      server.deleteByQuery("*:*");
      server.commit();
      System.out.println("Deleted.");
    }

    if (osmRead) {
      System.out.println("---- OSM reader starting ----" + new Date());
      OsmReaderFromSplit osmReader = new OsmReaderFromSplit(nodesFilename, waysFilename, relationsFilename);
      osmReader.read(mapDb);
      System.out.println("---- OSM reader done ----" + new Date());
    }
    System.out.println("---- Polygon indexing starting ----" + new Date());

    Thread thread1 = new Thread() {
      public void run() {
        AdminPolygonIndexer polygonIndexer = new AdminPolygonIndexer();
        try
        {
          polygonIndexer.indexPolygons(IndexerMain.server, IndexerMain.mapDb, 2);
          polygonIndexer.indexPolygons(IndexerMain.server, IndexerMain.mapDb, 4);
          polygonIndexer.indexPolygons(IndexerMain.server, IndexerMain.mapDb, 5);
          System.out.println("Committing...");
          IndexerMain.server.commit();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    thread1.start();

    System.out.println("---- Places reader starting ----" + new Date());
    Thread thread2 = new Thread() {
      public void run() {
        PlacesReader reader = new PlacesReader();
        try {
          reader.read(IndexerMain.mapDb, "places." + IndexerMain.country + ".ser", "pois." + IndexerMain.country + ".ser");
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    thread2.start();

    thread1.join();
    System.out.println("---- Polygon indexing done ----" + new Date());

    thread2.join();
    System.out.println("---- Places reader done ----" + new Date());

    System.out.println("---- Places indexing starting ----" + new Date());

    placesIndexer = new PlacesIndexer("places." + country + ".ser");
    placesIndexer.indexCitiesAndTowns(server);
    placesIndexer.indexLocalitiesAndSuburbs(server);

    Thread thread3 = new Thread() {
      public void run() {
        try {
          placesIndexer.indexPlaces(placesServer, server, mapDb, "pois." + country + ".ser");
        } catch (ClassNotFoundException | IOException | SolrServerException
            | ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };


    System.out.println("---- Streets indexing started ----" + new Date());
    Thread thread4 = new Thread() {
      public void run() {
        try {
          StreetIndexer indexer = new StreetIndexer();
          indexer.indexStreets(server, mapDb);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    thread3.start();
    thread4.start();

    thread4.join();
    System.out.println("---- Streets indexing done ----" + new Date());

    thread3.join();
    System.out.println("---- Places indexing done ----" + new Date());

    server.commit();
    placesServer.commit();
    mapDb.close();
  }
}
