package org.openstreetmap.osmgeocoder.indexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.indexer.primitives.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlacesReader
{
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  void read(DB mapDb, String placesFile, String poisFile)
      throws IOException
  {
    Map<Long, Node> nodeStore = mapDb.getTreeMap("nodeStore");
    Map<Long, Way> wayStore = mapDb.getTreeMap("wayStore");
    Map<Long, Object> poiStore = mapDb.getTreeMap("poiStore");
    //Map<String, Relation> relationStore = mapDb.getTreeMap("relationStore");

    Set<String> poiKeys = new HashSet<String>(Arrays.asList(new String[] { "leisure", "amenity", 
        "building", "craft", "man_made", "landuse", "natural", "railway", "shop", 
        "sport", "tourism", "aeroway" }));

    int counter = 0;

    List<Node> places = new ArrayList<Node>();
    List<Node> pois = new ArrayList<Node>();

    for (Long id : nodeStore.keySet()) {
      Node node = nodeStore.get(id);

      if (node.tags.containsKey("name")) {
        if ((node.tags.containsKey("admin_level")) || (node.tags.containsKey("place"))) {
          places.add(node);
        }
        if (!Collections.disjoint(poiKeys, node.tags.keySet())) {
          pois.add(node);
        }
      }

      counter++;

      if (counter % 1000000 == 0) {
        log.info(counter + " (Places: " + places.size() + ", Pois: " + pois.size() + ")");
        mapDb.commit();
      }
    }

    log.info("Reading ways...");
    counter = 0;
    for (Long id : wayStore.keySet()) {
      Way way = (Way)wayStore.get(id);

      if  (!Collections.disjoint(poiKeys, way.tags.keySet())) 
      {
        poiStore.put(id, way);
      }
      counter++;
      try {
        if (counter % 1000 == 0)
          Thread.sleep(1L);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (counter % 100000 == 0) {
        log.info(counter + " (Places: " + places.size() + ", Pois: " + pois.size() + ")");
      }
    }
    FileOutputStream fout = new FileOutputStream(placesFile);
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(places);
    oos.close();

    fout = new FileOutputStream(poisFile);
    oos = new ObjectOutputStream(fout);
    oos.writeObject(pois);
    oos.close();

    mapDb.commit();
    log.info("Time: " + new Date());
  }

  public static void main(String[] args)
      throws IOException
  {
    DB db = DBMaker.newFileDB(new File("asia_jdbm/asia")).closeOnJvmShutdown().encryptionEnable("password").make();
    PlacesReader reader = new PlacesReader();
    reader.read(db, "places.ser", "pois.ser");
  }
}

