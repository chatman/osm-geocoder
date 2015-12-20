package org.openstreetmap.osmgeocoder.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.osmgeocoder.indexer.primitives.Node;

public class AdminPolygon
{
  List<List<Node>> multipoly = new ArrayList<List<Node>>();
  List<String> rolesList = new ArrayList<String>();
  Map<String, String> tags = new HashMap<String, String>();
  long id;

  public AdminPolygon(long id, Map<String, String> tags, List<String> rolesList, List<List<Node>> multipoly)
  {
    this.id = id;
    this.tags = tags;
    this.rolesList = rolesList;
    this.multipoly = multipoly;
  }
}

