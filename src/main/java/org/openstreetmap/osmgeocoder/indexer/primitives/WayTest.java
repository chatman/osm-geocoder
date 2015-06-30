package org.openstreetmap.osmgeocoder.indexer.primitives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class WayTest extends TestCase {
  
  @Test
  public void testGetPoints() {
    List<Node> nodes = new ArrayList<Node>();
    nodes.add(new Node(0, 1));
    nodes.add(new Node(2, 3));
    nodes.add(new Node(4, 5));
    Way way = new Way("id", null, nodes);
    
    assertEquals(3, way.numNodes());
    assertEquals("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0]", Arrays.toString(way.getPoints()));
    assertEquals("[4.0, 5.0, 2.0, 3.0, 0.0, 1.0]", Arrays.toString(way.getPointsReversed()));
    assertEquals("[0.0, 1.0]", Arrays.toString(way.getNode(0)));
    assertEquals("[2.0, 3.0]", Arrays.toString(way.getNode(1)));
    assertEquals("[4.0, 5.0]", Arrays.toString(way.getNode(2)));
  }
}
