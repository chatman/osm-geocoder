package org.openstreetmap.osmgeocoder.indexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.osmgeocoder.indexer.primitives.Member;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;
import org.openstreetmap.osmgeocoder.indexer.primitives.Relation;
import org.openstreetmap.osmgeocoder.indexer.primitives.Way;
import org.openstreetmap.osmgeocoder.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


public class AdminPolygonReader {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  List<AdminPolygon> places = new ArrayList<AdminPolygon>();


  public void read(DB mapDb, String adminLevel) throws IOException, ClassNotFoundException {
    List<Relation> relations = new ArrayList<Relation>();


    Map<Long, Relation> relationStore = mapDb.getTreeMap("relationStore");
    int totalRelCounter = 0;

    for (Long id: relationStore.keySet()) {
      totalRelCounter++;
      if(totalRelCounter%10000==0)
        log.info("Rel counter: "+totalRelCounter+", adminLevels added="+relations.size()/*+relations.size()*/);

      Relation rel = relationStore.get(id);
      if (adminLevel.equals(rel.tags.get("admin_level")))
        relations.add(rel);
    }

    log.info("Found "+relations.size()+" relations. Processing...");


    //	log.info();
    FileWriter out = new FileWriter("states.txt");
    Map<String, FileWriter> countryPolyFiles = new HashMap<String, FileWriter>();
    for (Relation rel: relations) {

      String name = rel.tags.get("name");

      int totalNodes = 0;

      for (Member m: rel.members) {
        if (m.member instanceof Way) {
          totalNodes+=((Way)m.member).numNodes();
        }
      }

      int ways = 0, nodes = 0;
      Set<String> roles = new HashSet<String>();

      List<List<Node>> multipoly = new ArrayList<List<Node>>();
      List<String> rolesList = new ArrayList<String>();

      List<GraphNode> graphNodes = new ArrayList<GraphNode>();
      List<GraphEdge> graphEdges = new ArrayList<GraphEdge>();

      for (Member m: rel.members) {
        if (m.member instanceof Way) {
          ways++;
          roles.add(m.role);

          Way way = (Way)m.member;

          //log.info(way.nodes.get(0)+"->"+way.nodes.get(way.nodes.size()-1));
          GraphNode src = new GraphNode(Arrays.toString(way.getNode(0)));
          GraphNode dest = new GraphNode(Arrays.toString(way.getNode(way.numNodes()-1)));

          log.debug(src+"->"+dest+", size="+way.numNodes());
          /*for (int i=0; i<way.numNodes(); i++) {
					  log.info("Way point "+i+": "+Arrays.toString(way.getNode(i)));
					}*/

          if(graphNodes.contains(src))
            src = graphNodes.get(graphNodes.indexOf(src));
          else
            graphNodes.add(src);
          if(graphNodes.contains(dest))
            dest = graphNodes.get(graphNodes.indexOf(dest));
          else
            graphNodes.add(dest);


          float[] reversedWayNodes = way.getPointsReversed();
          float[] wayNodes = way.getPoints();

          if(src.equals(dest)) {
            src.selfPath=wayNodes;
            dest.selfPath=wayNodes;
          }

          GraphEdge to = new GraphEdge(src, dest, wayNodes, ""+way.id);
          GraphEdge from = new GraphEdge(dest, src, reversedWayNodes, ""+way.id);


          //log.info("ALL: "+to+", id="+to.id);
          //          if (way.id.equals("219576421"))
          //            log.info("219576421: "+to.path);

          //if(src.edges.contains(to)==false)
          src.edges.add(to);
          //if(dest.edges.contains(from)==false)
          dest.edges.add(from);

          graphEdges.add(to);
          graphEdges.add(from);

        }
        else if (m.member instanceof Node) {
          nodes++;
        }
      }

      List<List<GraphNode>> loops = GraphProcessor.traverse(graphNodes);

      for(List<GraphNode> loop: loops) {
        List<Node> poly = new ArrayList<Node>();
        if (loop.size()==1) {
          log.debug("Self loop came: "+loop);
          poly.addAll(Utils.nodesFromPoints(loop.get(0).selfPath));
        }
        else
          for (int i=1; i<loop.size(); i++) {
            GraphNode prev = loop.get(i-1);
            GraphNode cur = loop.get(i);
            if(prev.equals(cur)){
              //poly.addAll(prev.selfPath);
              continue;
            }
            else {
              /*GraphEdge tmpEdge = new GraphEdge(prev, cur, null);
						GraphEdge actualEdge = graphEdges.get(loops.indexOf(tmpEdge));*/
              GraphEdge actualEdge = null;
              for(GraphEdge e: prev.edges)
                if(e.dest.equals(cur))
                  actualEdge = e;
              if(actualEdge==null) {
                System.err.println("ERROR: Prev: "+prev+", cur="+cur+", prev.edges: "+prev.edges);
              } else {
                poly.addAll(Utils.nodesFromPoints(actualEdge.path));
              }
            }
          }
        if(poly.size()>0)
          multipoly.add(poly);
      }


      //log.info("Max size poly = "+maxSizePoly);
      int processedNodes = 0;
      for(List<Node> poly: multipoly)
        processedNodes+=poly.size();
      if(totalNodes>0)
        log.info(name+": "+processedNodes+"/"+totalNodes+" ("+(processedNodes*100/totalNodes)+"%), polygons="+multipoly.size());

      if (multipoly.size()>0 && rel.tags.containsKey("name")) {
        places.add(new AdminPolygon(rel.id, rel.tags, rolesList, multipoly));

        //log.info(rel.tags.get("name")+": "+ways+", "+nodes+", roles="+roles+", Multipoly="+multipoly.size()+", RolesList="+rolesList.size());
        //log.info("isin: "+rel.tags.get("is_in:country_code"));
        //log.info(rel.tags);
      }
      //}
      //}
    }
    out.close();
    for(String c: countryPolyFiles.keySet())
      countryPolyFiles.get(c).close();
  }

  public static void main(String[] args) throws Exception {
    DB db = DBMaker.newFileDB(new File("jdbms/india")).closeOnJvmShutdown().transactionDisable().asyncWriteEnable().make();
    AdminPolygonReader reader = new AdminPolygonReader();
    reader.read(db, ""+4);

    System.out.println(reader.places.size());

//    for (AdminPolygon poly: reader.places) {
//      if (poly.tags.get("name").startsWith("Uttar ")) {
//        log.info(poly.tags.get("name")+"\t"+poly.multipoly.size() + " polygons.");
//        for (List<Node> p: poly.multipoly) {
//          log.info("  >>  "+p.size()+", \t");
//          createGeom(p);
//        }
//      }
//    }

    System.out.println(new Date());
  }
  
  public static void createGeom (List<Node> nodes) {
    StringBuilder wkt = new StringBuilder();
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
    }

    Polygon pol = (Polygon)geom;

    if (!pol.isValid()) {
      Geometry repaired = pol.buffer(0.0D);
      log.info("Invalid polygon detected. Is fixed? " + repaired.isValid());
      wkt = new StringBuilder(repaired.toText());
    }
    
    try {
      System.out.println(pol.contains(wktreader.read("POINT(82 25)")));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

}

