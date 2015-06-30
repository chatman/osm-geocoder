package org.openstreetmap.osmgeocoder.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.solr.handler.dataimport.XPathRecordReader;
import org.apache.solr.handler.dataimport.XPathRecordReader.Handler;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Member;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.indexer.primitives.Relation;
import org.openstreetmap.osmgeocoder.indexer.primitives.Way;
import org.openstreetmap.osmgeocoder.util.BloomFilter;

public class OsmReaderFromSplit
{
  DB mapDb;
  Map<Long, Node> nodeStore;
  Map<Long, Way> wayStore;
  Map<Long, Way> wayStoreInMemory;
  Map<Long, Relation> relationStore;
  Map<Long, Node> poiStore;
  Map<Long, String> placeStore;
  List<Object> namedPlaces = new ArrayList<>();

  BloomFilter nodesFilter = new BloomFilter(255);
  BloomFilter waysFilter = new BloomFilter(128);
  BloomFilter relsFilter = new BloomFilter(64);


  String nodesInputFile = null, waysInputFile = null, relationsInputFile = null;

  public OsmReaderFromSplit(String nodesFile, String waysFile, String relationsFile) {
    this.nodesInputFile = nodesFile;
    this.waysInputFile = waysFile;
    this.relationsInputFile = relationsFile;
  }

  final int NODES = 256;
  final int WAYS = 512;
  final int RELATIONS = 1024;

  final int PROCESS = 8;
  final int PREPROCESS = 16;

  void read(DB db) throws FileNotFoundException, ClassNotFoundException, IOException {
    this.mapDb = db;

    this.nodeStore = db.getTreeMap("nodeStore");
    this.wayStore = db.getTreeMap("wayStore");
    this.wayStoreInMemory = new TreeMap<Long, Way>();
    this.relationStore = db.getTreeMap("relationStore");


    System.out.println(new Date());
    parse(RELATIONS, PREPROCESS);
    System.out.println(new Date());
    parse(WAYS, PREPROCESS);
    System.out.println(new Date());
    parse(NODES, PROCESS);
    System.out.println(new Date());
    System.out.println("Compacting...");
    db.compact();
    db.commit();
    System.out.println(new Date());
    parse(WAYS, PROCESS);
    System.out.println(new Date());
    parse(RELATIONS, PROCESS);
    System.out.println(new Date());
  }
  
  private void parse(int type, int mode) throws FileNotFoundException, IOException, ClassNotFoundException {
    XPathRecordReader nodeReader = null;
    InputStream is = null;

    switch(type) {

    case NODES:
      nodeReader = new XPathRecordReader("/osm/node");
      nodeReader.addField("node_lat", "/osm/node/@lat", false);
      nodeReader.addField("node_lon", "/osm/node/@lon", false);
      nodeReader.addField("node_id", "/osm/node/@id", false);
      nodeReader.addField("node_tag_keys", "/osm/node/tag/@k", true);
      nodeReader.addField("node_tag_values", "/osm/node/tag/@v", true);
      if(nodesInputFile.endsWith(".gz"))
        is = new GZIPInputStream(new FileInputStream(new File(nodesInputFile)));
      else if (nodesInputFile.endsWith(".bz2"))
        is = new BZip2CompressorInputStream(new FileInputStream(new File(nodesInputFile)));
      else
        is = new FileInputStream(new File(nodesInputFile));
      break;

    case WAYS:
      nodeReader = new XPathRecordReader("/osm/way");
      nodeReader.addField("way_id", "/osm/way/@id", false);
      nodeReader.addField("way_nd_refs", "/osm/way/nd/@ref", true);
      nodeReader.addField("way_tag_keys", "/osm/way/tag/@k", true);
      nodeReader.addField("way_tag_values", "/osm/way/tag/@v", true);
      if(nodesInputFile.endsWith(".gz"))
        is = new GZIPInputStream(new FileInputStream(new File(waysInputFile)));
      else if (nodesInputFile.endsWith(".bz2"))
        is = new BZip2CompressorInputStream(new FileInputStream(new File(waysInputFile)));
      else
        is = new FileInputStream(new File(waysInputFile));
      break;

    case RELATIONS:
      nodeReader = new XPathRecordReader("/osm/relation");
      nodeReader.addField("rel_id", "/osm/relation/@id", false);
      nodeReader.addField("rel_member_type", "/osm/relation/member/@type", true);
      nodeReader.addField("rel_member_ref", "/osm/relation/member/@ref", true);
      nodeReader.addField("rel_member_role", "/osm/relation/member/@role", true);
      nodeReader.addField("rel_tag_keys", "/osm/relation/tag/@k", true);
      nodeReader.addField("rel_tag_values", "/osm/relation/tag/@v", true);
      if(nodesInputFile.endsWith(".gz"))
        is = new GZIPInputStream(new FileInputStream(new File(relationsInputFile)));
      else if (nodesInputFile.endsWith(".bz2"))
        is = new BZip2CompressorInputStream(new FileInputStream(new File(relationsInputFile)));
      else
        is = new FileInputStream(new File(relationsInputFile));
    }

    Handler nodesProcessingHandler = new XPathRecordReader.Handler() {
      long INTERVAL = 1000000, counter = 0, passed = 0;
      public void handle(Map<String, Object> record, String xpath)
      {
        if (record == null) return;
        counter++;
        if (xpath.equals("/osm/node")) {
          String id = record.containsKey("node_id") ? record.get("node_id").toString() : null;
          String lat = record.containsKey("node_lat") ? record.get("node_lat").toString() : null;
          String lng = record.containsKey("node_lon") ? record.get("node_lon").toString() : null;

          if ((id != null) && (lat != null) && (lng != null)) {
            if(nodesFilter.wordExists(id) || record.containsKey("node_tag_keys") && 
                ((List<String>)record.get("node_tag_keys")).contains("name")) {
              passed++;
              Map<String, String> tags = OsmReaderFromSplit.collectTags(record, "node_tag_keys", "node_tag_values");
              if ((tags.containsKey("name:en")) && (((String)tags.get("name:en")).length() > 0)) 
                tags.put("name", (String)tags.get("name:en"));

              Node node = new Node(id, lat, lng);
              node.tags = tags;
              OsmReaderFromSplit.this.nodeStore.put(Long.parseLong(id), node);
            }
          }
        }

        if (this.counter % (INTERVAL * 1) == 0) {
          System.out.println("Nodes: "+passed+"/"+counter+" ("+((passed*100.0)/(double)counter)+")");
          OsmReaderFromSplit.this.mapDb.commit();
        }
      }
    };

    Handler waysPreProcessingHandler = new XPathRecordReader.Handler() {
      int INTERVAL = 1000000, counter = 0, passed = 0;
      public void handle(Map<String, Object> record, String xpath)
      {
        if (record == null) return;
        if (xpath.equals("/osm/way")) {
          counter++;
          String id = record.containsKey("way_id") ? record.get("way_id").toString() : null;
          Map<String, String> tags = OsmReaderFromSplit.collectTags(record, "way_tag_keys", "way_tag_values");
          if(waysFilter.wordExists(id) || tags.containsKey("name")) {
            passed++;
            waysFilter.addWord(id);
            for (String ref : (List<String>)record.get("way_nd_refs")) 
              nodesFilter.addWord(ref);
          }
        }
        if (this.counter % (INTERVAL * 1) == 0) {
          System.out.println("Ways: "+passed+"/"+counter+" ("+((passed*100.0)/(double)counter)+")");
        }
      }
    };

    Handler waysProcessingHandler = new XPathRecordReader.Handler() {
      int INTERVAL = 1000000;
      long counter = 0;
      long passed = 0;
      long failed = 0;
      public void handle(Map<String, Object> record, String xpath)
      {
        if (record == null) return;
        if (xpath.equals("/osm/way")) {
          counter++;
          String id = record.containsKey("way_id") ? record.get("way_id").toString() : null;
          if(waysFilter.wordExists(id)) {
            passed++;
            List<Node> wayNodes = new ArrayList<Node>();
            if (record.containsKey("way_nd_refs")) 
              for (String ref : (List<String>)record.get("way_nd_refs")) {
                long idRef = Long.parseLong(ref);
                if(OsmReaderFromSplit.this.nodeStore.containsKey(idRef)!=false)
                  wayNodes.add((Node)OsmReaderFromSplit.this.nodeStore.get(idRef));
                else
                  failed++;
              }
            Map<String, String> tags = OsmReaderFromSplit.collectTags(record, "way_tag_keys", "way_tag_values");
            Way way = new Way(id, tags, wayNodes);
            OsmReaderFromSplit.this.wayStore.put(Long.parseLong(id), way);
            //OsmReaderFromSplit.this.wayStoreInMemory.put(id, way);
          }
        }
        if (this.counter % (INTERVAL * 1) == 0) {
          System.out.println("Ways: "+passed+"/"+counter+" ("+((passed*100.0)/(double)counter)+")");
          System.out.println("Failed node lookups: "+failed);
          OsmReaderFromSplit.this.mapDb.commit();
        }
      }
    };

    Handler relationsPreProcessingHandler = new XPathRecordReader.Handler() {
      int INTERVAL = 1000;
      int counter = 0;
      int passed = 0;

      public void handle(Map<String, Object> record, String xpath)
      {
        if (record == null) return;
        counter++;
        if (xpath.equals("/osm/relation")) {
          String id = record.containsKey("rel_id") ? record.get("rel_id").toString() : null;
          Map<String, String> tags = OsmReaderFromSplit.collectTags(record, "rel_tag_keys", "rel_tag_values");

          if(tags.containsKey("name") && (tags.containsKey("admin_level") /*|| tags.containsKey("boundary")*/) ) {
            passed++;
            if ((record.containsKey("rel_member_type")) && (record.containsKey("rel_member_ref")) && (record.containsKey("rel_member_role")) && 
                (((List<String>)record.get("rel_member_type")).size() == ((List<String>)record.get("rel_member_ref")).size())) {
              List<String> typesList = (List<String>)record.get("rel_member_type");
              List<String> refsList = (List<String>)record.get("rel_member_ref");

              for (int i = 0; i < typesList.size(); i++) {
                String type = (String)typesList.get(i);
                String ref = (String)refsList.get(i);

                if("node".equals(type))
                  nodesFilter.addWord(ref);
                else if("way".equals(type))
                  waysFilter.addWord(ref); 
              }

              relsFilter.addWord(id);
            }
          }
        }

        if (this.counter % (INTERVAL * 1) == 0) {
          System.out.println("Relations: "+passed+"/"+counter+" ("+((passed*100.0)/(double)counter)+")");
          OsmReaderFromSplit.this.mapDb.commit();
        }

      }
    };


    Handler relationsProcessingHandler = new XPathRecordReader.Handler() {
      int INTERVAL = 1000, counter = 0, passed = 0;
      public void handle(Map<String, Object> record, String xpath)
      {
        if (record == null) return;
        counter++;
        if (xpath.equals("/osm/relation")) {
          String id = record.containsKey("rel_id") ? record.get("rel_id").toString() : null;

          if(relsFilter.wordExists(id)) {
            passed++;
            Map<String, String> tags = OsmReaderFromSplit.collectTags(record, "rel_tag_keys", "rel_tag_values");
            List<Member> members = new ArrayList<Member>();
            if (record.containsKey("rel_member_type") && record.containsKey("rel_member_ref") && record.containsKey("rel_member_role") && 
                (((List<String>)record.get("rel_member_type")).size() == ((List<String>)record.get("rel_member_ref")).size())) {
              List<String> typesList = (List<String>)record.get("rel_member_type");
              List<String> refsList = (List<String>)record.get("rel_member_ref");
              List<String> rolesList = (List<String>)record.get("rel_member_role");

              for (int i = 0; i < typesList.size(); i++) {
                String type = (String)typesList.get(i);
                long ref = Long.parseLong((String)refsList.get(i));
                String role = (String)rolesList.get(i);

                Member member = null;
                if (type.equalsIgnoreCase("node"))
                  member = new Member(OsmReaderFromSplit.this.nodeStore.get(ref), role);
                else if (type.equalsIgnoreCase("way"))
                  member = new Member(OsmReaderFromSplit.this.wayStore.get(ref), role);
                if ((member != null) && (member.member != null)) {
                  members.add(member);
                } 
              }
            }

            if ((tags.containsKey("name")) && (tags.containsKey("name:en")) && (((String)tags.get("name:en")).length() > 0)) 
              tags.put("name", (String)tags.get("name:en"));

            Relation relation = new Relation(id, tags, members);
            OsmReaderFromSplit.this.relationStore.put(Long.parseLong(id), relation);
          }
        }

        if (this.counter % (INTERVAL * 1) == 0) {
          System.out.println("Relations: "+passed+"/"+counter+" ("+((passed*100.0)/(double)counter)+")");
          OsmReaderFromSplit.this.mapDb.commit();
        }

      }
    };

    if(type==NODES)
      if (mode==PROCESS)
        nodeReader.streamRecords(new InputStreamReader(is), nodesProcessingHandler);
    if(type==WAYS)
      if (mode==PREPROCESS)
        nodeReader.streamRecords(new InputStreamReader(is), waysPreProcessingHandler);
      else if (mode==PROCESS)
        nodeReader.streamRecords(new InputStreamReader(is), waysProcessingHandler);
    if(type==RELATIONS)
      if (mode==PREPROCESS)
        nodeReader.streamRecords(new InputStreamReader(is), relationsPreProcessingHandler);
      else if (mode==PROCESS)
        nodeReader.streamRecords(new InputStreamReader(is), relationsProcessingHandler);

    this.mapDb.commit();

    nodesFilter.writeToFile("nodesFilter.bf");
    waysFilter.writeToFile("waysFilter.bf");
    relsFilter.writeToFile("relsFilter.bf");

  }

  private static Map<String, String> collectTags(Map<String, Object> record, String keyList, String valueList) {
    HashMap<String, String> tags = new HashMap<String, String>();
    if ((record.containsKey(keyList)) && (record.containsKey(valueList)) && 
        (((List<String>)record.get(keyList)).size() == ((List<String>)record.get(valueList)).size())) {
      List<String> keysList = (List<String>)record.get(keyList);
      List<String> valuesList = (List<String>)record.get(valueList);
      for (int i = 0; i < keysList.size(); i++) {
        tags.put((String)keysList.get(i), (String)valuesList.get(i));
      }
    }
    return tags;
  }



  public static void main(String[] args) throws Exception
  {
    DB db = DBMaker.newFileDB(new File("jdbm/test")).closeOnJvmShutdown().asyncWriteEnable().make();
    OsmReaderFromSplit reader = new OsmReaderFromSplit("/data/datasets/nodes.xml", "/data/datasets/ways.xml", "/data/datasets/relations.xml");
    reader.read(db);

    System.out.println("Done! "+new Date());

  }
}

