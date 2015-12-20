package org.openstreetmap.osmgeocoder.indexer.primitives;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Persistent
public class Way
implements Serializable
{
  private static final long serialVersionUID = -9117021745470896630L;
  public long id;
  private float[] nodes;

  public Map<String, String> tags = new HashMap<String, String>();

  public Way()
  {
  }

  public Way(String id, Map<String, String> tags, List<Node> nodes) {
    this.id = Long.parseLong(id);
    this.tags = tags;

    this.nodes = new float[nodes.size()*2];
    for(int i=0; i<nodes.size(); i++) {
      this.nodes[2*i] = nodes.get(i).lat;
      this.nodes[2*i+1] = nodes.get(i).lng;
    }
  }

  public int numNodes() {
    return this.nodes.length/2;
  }

  public float[] getNode(int i) {
    return new float[] {nodes[i*2], nodes[i*2+1]};
  }

  public float[] getPointsReversed() {
    float rev[] = new float[nodes.length];
    for(int i=nodes.length-2; i>=0; i-=2) {
      int index = nodes.length/2 - i/2 -1;
      rev[index*2] = nodes[i];
      rev[index*2+1] = nodes[i+1];
    }
    return rev;
  }

  public float[] getPoints() {
    return Arrays.copyOf(nodes, nodes.length);
  }

  public String toString()
  {
    return "Tags=" + this.tags + ", Nodes={" + this.nodes + "}";
  }
}

