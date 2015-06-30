package org.openstreetmap.osmgeocoder.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;

public class GraphProcessorTest extends TestCase {
  
 /* @Test
  public void testSingleLoop() throws IOException {
    List<GraphNode> nodes = new ArrayList<GraphNode>();
    
    //      1
    //     /  \
    //    2 -- 3
    
    
    GraphNode n1 = new GraphNode("1");
    GraphNode n2 = new GraphNode("2");
    GraphNode n3 = new GraphNode("3");
    
    addEdge(n1, n2);
    addEdge(n2, n3);
    addEdge(n3, n1);

    nodes.add(n1);
    nodes.add(n2);
    nodes.add(n3);
    
    List<List<GraphNode>> result = GraphProcessor.traverse(nodes);
    assertEquals(1, result.size());
    assertEquals(4, result.get(0).size());
    
  }*/

  @Test
  public void testTwoNodeLoop() throws IOException {
    List<GraphNode> nodes = new ArrayList<GraphNode>();
    
    //      1
    //     ( )
    //      2 

    
    
    GraphNode n1 = new GraphNode("1");
    GraphNode n2 = new GraphNode("2");

    
    addEdge(n2, n1);
    addEdge(n2, n1);
    
    
    nodes.add(n1);
    nodes.add(n2);
    
    List<List<GraphNode>> result = GraphProcessor.traverse(nodes);
    System.out.println("RESULT: "+result);

    assertEquals(1, result.size());
    assertEquals(3, result.get(0).size());
    
  }

  /*@Test
  public void testTwoLoopsJoined() throws IOException {
    List<GraphNode> nodes = new ArrayList<GraphNode>();
    
    //      1
    //     /  \
    //    2 -- 3
    //   / 
    //  4 - 5
    //   \  /
    //    6
    
    
    GraphNode n1 = new GraphNode("1");
    GraphNode n2 = new GraphNode("2");
    GraphNode n3 = new GraphNode("3");
    GraphNode n4 = new GraphNode("4");
    GraphNode n5 = new GraphNode("5");
    GraphNode n6 = new GraphNode("6");

    
    addEdge(n1, n2);
    addEdge(n3, n1);
    addEdge(n2, n4);
    addEdge(n2, n3);
    addEdge(n4, n5);
    addEdge(n5, n6);
    addEdge(n4, n6);
    
    nodes.add(n1);
    nodes.add(n2);
    nodes.add(n3);
    nodes.add(n4);
    nodes.add(n5);
    nodes.add(n6);
    
    List<List<GraphNode>> result = GraphProcessor.traverse(nodes);
    System.out.println("RESULT: "+result);

    assertEquals(2, result.size());
    assertEquals(4, result.get(0).size());
    assertEquals(4, result.get(1).size());
    
  }*/
  
  private void addEdge(GraphNode n1, GraphNode n2) {
    n1.edges.add(new GraphEdge(n1, n2, new float[0], null));
    n2.edges.add(new GraphEdge(n2, n1, new float[0], null));
  }

}
