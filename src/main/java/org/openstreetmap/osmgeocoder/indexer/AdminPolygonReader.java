package org.openstreetmap.osmgeocoder.indexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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


public class AdminPolygonReader {
  public final static boolean debug = false;
  List<AdminPolygon> places = new ArrayList<AdminPolygon>();


  public void read(DB mapDb, String adminLevel) throws IOException, ClassNotFoundException {
    List<Relation> relations = new ArrayList<Relation>();


    Map<Long, Relation> relationStore = mapDb.getTreeMap("relationStore");
    int totalRelCounter = 0;

    for (Long id: relationStore.keySet()) {
      totalRelCounter++;
      if(totalRelCounter%10000==0)
        System.out.println("Rel counter: "+totalRelCounter+", adminLevels added="+relations.size()/*+relations.size()*/);

      Relation rel = relationStore.get(id);
      if (adminLevel.equals(rel.tags.get("admin_level")))
        relations.add(rel);
    }

    System.out.println("Found "+relations.size()+" relations. Processing...");


    //	System.out.println();
    FileWriter out = new FileWriter("states.txt");
    Map<String, FileWriter> countryPolyFiles = new HashMap<String, FileWriter>();
    for (Relation rel: relations) {

      String name = rel.tags.get("name");

      if(debug)if("Japan".equals(name)==false)
        continue;

      int totalNodes = 0;

      for (Member m: rel.members) {
        if (m.member instanceof Way) {
          totalNodes+=((Way)m.member).numNodes();
        }
      }

      if(debug)
        if((adminLevel.equals("2") ) && name!=null) {
          countryPolyFiles.put(name, new FileWriter("graph_output_postrefactor."+name));
          //System.out.println("Found country: "+name);
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

          //System.out.println(way.nodes.get(0)+"->"+way.nodes.get(way.nodes.size()-1));
          if(debug)if((adminLevel.equals("2") ) && name!=null)
            countryPolyFiles.get(name).write(Arrays.toString(way.getNode(0))+"->"+Arrays.toString(way.getNode(way.numNodes()-1))+"\n");

          GraphNode src = new GraphNode(Arrays.toString(way.getNode(0)));
          GraphNode dest = new GraphNode(Arrays.toString(way.getNode(way.numNodes()-1)));

          if(debug)System.out.println(src+"->"+dest+", size="+way.numNodes());
          /*for (int i=0; i<way.numNodes(); i++) {
					  System.out.println("Way point "+i+": "+Arrays.toString(way.getNode(i)));
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


          //System.out.println("ALL: "+to+", id="+to.id);
          //          if (way.id.equals("219576421"))
          //            System.out.println("219576421: "+to.path);

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
          if(debug)System.out.println("Self loop came: "+loop);
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


      //System.out.println("Max size poly = "+maxSizePoly);
      int processedNodes = 0;
      for(List<Node> poly: multipoly)
        processedNodes+=poly.size();
      if(totalNodes>0)
        System.out.println(name+": "+processedNodes+"/"+totalNodes+" ("+(processedNodes*100/totalNodes)+"%), polygons="+multipoly.size());

      if (multipoly.size()>0 && rel.tags.containsKey("name")) {
        places.add(new AdminPolygon(rel.id, rel.tags, rolesList, multipoly));

        //System.out.println(rel.tags.get("name")+": "+ways+", "+nodes+", roles="+roles+", Multipoly="+multipoly.size()+", RolesList="+rolesList.size());
        //System.out.println("isin: "+rel.tags.get("is_in:country_code"));
        //System.out.println(rel.tags);
      }
      //}
      //}
    }
    out.close();
    if(debug)for(String c: countryPolyFiles.keySet())
      countryPolyFiles.get(c).close();
  }

  public static void main(String[] args) throws Exception {
    DB db = DBMaker.newFileDB(new File("jdbm/test")).closeOnJvmShutdown().make();
    AdminPolygonReader reader = new AdminPolygonReader();
    reader.read(db, ""+2);

    System.out.println(reader.places.size());

    for (AdminPolygon poly: reader.places) {

      //System.out.print(poly.tags.get("name")+"\t");
      //System.out.println(poly.multipoly.size() + " polygons.");
      //for (List<Node> p: poly.multipoly)
      //System.out.println("  >>  "+p.size()+", \t"+p);
      //for (List<Node> node: poly.multipoly)
      //System.out.println("Size: "+node.size());
    }

    System.out.println(new Date());
  }

}

