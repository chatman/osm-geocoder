package org.openstreetmap.osmgeocoder.indexer.primitives;

import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;
import java.util.Map;

@Persistent
public class Node
implements Serializable
{
	private static final long serialVersionUID = -7441071754104188808L;
	public float lat;
	public float lng;
	public long id;
	public Map<String, String> tags;

	public Node()
	{
	}

	 public Node(long id, float lat, float lng)
	  {
	    this.lat = lat;
	    this.lng = lng;
	    this.id = id;
	  }

	public Node(String id, String lat, String lng)
	{
		/*this.lat = Float.parseFloat(lat);
		this.lng = Float.parseFloat(lng);
		this.id = Long.parseLong(id);*/
		this (Long.parseLong(id), Float.parseFloat(lat), Float.parseFloat(lng));
	}
	
	public Node(long id, String lat, String lng)
  {
    /*this.lat = Float.parseFloat(lat);
    this.lng = Float.parseFloat(lng);
    this.id = Long.parseLong(id);*/
    this (id, Float.parseFloat(lat), Float.parseFloat(lng));
  }
  

	public Node(float lat, float lng) {
		this.lat = lat; this.lng = lng;
	}

	public boolean equals(Object obj)
	{
		return ((this.lat == ((Node)obj).lat) && (this.lng == ((Node)obj).lng)) || 
				(obj.toString().equals(toString()));
	}

	public String toString() {
		return ""+id; //"[" + this.lat + "," + this.lng + "]";
	}
}
