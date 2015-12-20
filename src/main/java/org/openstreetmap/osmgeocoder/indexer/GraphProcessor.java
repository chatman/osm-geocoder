package org.openstreetmap.osmgeocoder.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openstreetmap.osmgeocoder.indexer.primitives.Node;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;

class GraphNode {
  String coordinates;
  int visits=0;

  List<GraphEdge> edges = new ArrayList<GraphEdge>();

  public float[] selfPath;

  public GraphNode(String c) {
    coordinates = c;
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getClass()!=obj.getClass())
      return false;

    return coordinates.equals(((GraphNode)obj).coordinates);
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return coordinates;
  }


}

class GraphEdge {
  public GraphNode source;
  public GraphNode dest;

  public String id;

  public float[] path;

  public GraphEdge(GraphNode s, GraphNode d, float[] path, String wayId) {
    source = s;
    dest = d;
    this.path = path;
    this.id = wayId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getClass()!=obj.getClass())
      return false;
    GraphEdge o = (GraphEdge)obj;
    return source.equals(o.source) && dest.equals(o.dest);
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return source.coordinates+"->"+dest.coordinates;
  }
}

public class GraphProcessor {
  final static boolean debug = false;

  public static void main(String[] args) throws IOException {
    /*List<GraphNode> graphNodes = new ArrayList<GraphNode>();
		List<GraphEdge> graphEdges = new ArrayList<GraphEdge>();

		BufferedReader br = new BufferedReader(new FileReader("graph_output.Leinster"));
		String line; int counter = 0;
		while((line=br.readLine())!=null) {
			String points[] = line.split("->");
			if (points.length!=2)
				continue;

			GraphNode src = new GraphNode(points[0]);
			GraphNode dest = new GraphNode(points[1]);
			if(graphNodes.contains(src))
				src = graphNodes.get(graphNodes.indexOf(src));
			else
				graphNodes.add(src);
			if(graphNodes.contains(dest))
				dest = graphNodes.get(graphNodes.indexOf(dest));
			else
				graphNodes.add(dest);

			GraphEdge to = new GraphEdge(src, dest, null, null);
			GraphEdge from = new GraphEdge(dest, src, null, null);

			if(src.edges.contains(to)==false)
				src.edges.add(to);
			if(dest.edges.contains(from)==false)
				dest.edges.add(from);

			graphEdges.add(to);
			graphEdges.add(from);

			//System.out.println(points[0]+"\t"+points[1]);
			counter++;
		}

		System.out.println("Input edges: "+counter);
		System.out.println("Input nodes: "+counter*2);

		System.out.println("Graph edges: "+graphEdges.size());
		System.out.println("Graph nodes: "+graphNodes.size());

		System.out.println();
		System.out.println("Traversing...");
		List<List<GraphNode>> loops = traverse(graphNodes);

		for(List<GraphNode> loop: loops) {
			System.out.println(">> "+loop.size()+" >> "+loop.get(0).coordinates+" TO "+loop.get(loop.size()-1).coordinates);
		}

		br.close();*/
  }

  static List<List<GraphNode>> traverse(List<GraphNode> nodes) throws IOException {
    /*FileWriter out = new FileWriter(new File("nodes.tmp"));
	  Map<String, Integer> map = new TreeMap<>();
	  for (GraphNode nd: nodes)
	    map.put(nd.toString(), map.containsKey(nd.toString()) ? map.get(nd.toString())+1 : 1);
	  for (String s: map.keySet())
	    out.write(s+"\t"+map.get(s)+"\n");

	  out.close();
     */
    /*for (GraphNode nd: nodes)
	    if (nd.edges.size()!=2)
	      System.out.println("--- "+nd+": "+nd.edges.size()+", "+nd.edges.get(0).id+", "+nd.edges.get(0).path);
     */

    //	  node: [43.225414, 146.0083]
    //	      Removing node: [44.29978, 145.63457

    for(GraphNode n: nodes) {
      if(n.toString().contains("43.225414, 146.0083") || n.toString().contains("44.29978, 145.63457")) {
        if(debug)System.out.println("Node: "+n+", edges: "+n.edges);
      }

    }

    int c=0;
    for (GraphNode n: nodes) {
      //System.out.println(">> "+n+", edges="+n.edges.size());
      c+=n.edges.size();
    }
    //System.out.println("Total edges: "+c);

    if(debug)System.out.println("Removing stray points, nodes="+nodes.size());
    for (int i=0; i<nodes.size(); i++) {
      GraphNode n = nodes.get(i);
      if (n.edges.size()==1) {
        GraphNode prev = n.edges.get(0).dest;
        if(prev == n)
          continue;
        if(debug)System.out.println("STRAY REMOVAL: prev node had edges: "+prev.edges.size());
        GraphEdge e = new GraphEdge(prev, n, null, "-1");
        prev.edges.remove(e);
        if(debug)System.out.println("STRAY REMOVAL: Now it has: "+prev.edges.size());
        if(debug)System.out.println("Previous node: "+prev);
        if(debug)System.out.println("Removing node: "+nodes.get(i)+"\n");
        nodes.remove(i); i--;
      }
    }
    if(debug)System.out.println("Size of nodes after stray removal: "+nodes.size());



    int dir=0;

    List<List<GraphNode>> loops = new ArrayList<List<GraphNode>>();
    while(true) {
      List<GraphNode> loop = new ArrayList<GraphNode>();

      int nextUnused = -1;
      for (int i=0; i<nodes.size(); i++) {
        if(nodes.get(i).visits==0) {
          nextUnused=i;
          break;
        }
      }
      if (nextUnused==-1)
        break;
      GraphNode start = nodes.get(nextUnused);
      GraphNode current = start;

      if(current.toString().contains("32.371445, 128.80559"))
        if(debug)System.out.println("Here begins debug");

      if(debug)System.out.println("Starting with: "+start+", has "+start.visits);

      if(current.selfPath!=null && current.selfPath.length>2) {
        current.visits++;
        loop.add(current);
        loops.add(loop);
        continue;
      }


      while(loop.contains(current)==false) {
        loop.add(current);
        current.visits++;
        if(debug)System.out.println(" >>> Visiting: "+current);
        GraphNode next = null;

        for (GraphEdge e: current.edges)
          if(e.dest!=current && (loop.contains(e.dest)==false)) {
            next = e.dest;
            if(next.visits==0)
              break;
          }
        if(next==null && current.edges.size()>0 && loop.size()>0 && current.edges.get(0).dest==loop.get(0))
          next = current.edges.get(0).dest;
        if (next==null) {
          if(debug)System.out.println("DEADEND");
          if(dir==1) {
            if(debug)System.out.println("Breaking here 1");
            break;
          }

          dir=1;
          Collections.reverse(loop);
          current = loop.get(loop.size()-1);
          loop.remove(loop.size()-1);
          current.visits--;

        } else
          current = next;
      }
      if(current.equals(loop.get(0))) {
        loop.add(current);

        if(debug)System.out.println("Adding current: "+current+", to the loop: "+loop);
      }

      if (loop.size()==1)
        if(debug)System.out.println("Size 1:"+ loop);
      List<Integer> canBeJoinedWith = new ArrayList<Integer>();
      for (int i=0; i<loops.size(); i++) {
        List<GraphNode> prev = loops.get(i);
        boolean midwayJoin = false;
        boolean overlap = false;
        if (prev.get(0).equals(loop.get(0)) || prev.get(0).equals(loop.get(loop.size()-1)) ||
            prev.get(prev.size()-1).equals(loop.get(0)) || prev.get(prev.size()-1).equals(loop.get(loop.size()-1))) {
          for (int j=1; j<prev.size()-1; j++) {
            GraphNode n = prev.get(j);
            if(loop.get(0).equals(n) || loop.get(loop.size()-1).equals(n)) {
              midwayJoin = true;
              break;
            }

          }

          Set<GraphNode> prevNodes = new HashSet<GraphNode>();
          for (int j=1; j<prev.size()-1; j++) {
            GraphNode n = prev.get(j);
            prevNodes.add(n);
          }

          for (GraphNode n: loop) 
            if(prevNodes.contains(n)) {
              overlap=true;
              break;
            }

          if (midwayJoin==false && overlap==false) {
            canBeJoinedWith.add(i);
          }
        }
      }
      if(canBeJoinedWith.size()>0) {
        int maxSize = 0; int maxLoop = -1;
        for (int i: canBeJoinedWith) {
          if(loops.get(i).size()>=maxSize)
            maxLoop = i;
          maxSize=Math.max(maxSize, loops.get(i).size());
        }

        List<GraphNode> prev = loops.get(maxLoop);
        if(prev.get(0).equals(loop.get(0))) {
          Collections.reverse(loop);
          prev.addAll(0, loop);
        } else if(prev.get(0).equals(loop.get(loop.size()-1))) {
          prev.addAll(0, loop);
        } else if(prev.get(prev.size()-1).equals(loop.get(0))) {
          prev.addAll(loop);
        } else if(prev.get(prev.size()-1).equals(loop.get(loop.size()-1))) {
          Collections.reverse(loop);
          prev.addAll(loop);
        }

        //System.out.println("Joined with loop: "+maxLoop+" of size: "+maxSize+". Now size of loop is: "+prev.size());

      } else {
        GraphEdge edge = new GraphEdge(loop.get(0), loop.get(loop.size()-1), null, "-2");
        if (loop.get(0).equals(loop.get(loop.size()-1)))
          loops.add(loop);
        else if (loop.get(0).edges.contains(edge)) {
          loop.add(loop.get(0));
          loops.add(loop);
        } else if (loop.size()==1 && loop.get(0).selfPath!=null && loop.get(0).selfPath.length>2) {
          loops.add(loop);
          if(debug)System.out.println("Self loop: "+loop);
        }

      }

    }		
    return loops;
  }

}
