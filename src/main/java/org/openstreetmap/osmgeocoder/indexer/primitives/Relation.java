package org.openstreetmap.osmgeocoder.indexer.primitives;

import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Persistent
public class Relation
implements Serializable
{
	private static final long serialVersionUID = -9117021745480892630L;
	public long id;
	public List<Member> members = new ArrayList<Member>();
	public Map<String, String> tags = new HashMap<String, String>();

	public Relation()
	{
	}

	public Relation(String id, Map<String, String> tags, List<Member> members) {
		this.id = Long.parseLong(id);
		this.members = members;
		this.tags = tags;
	}

	public String toString()
	{
		return "Tags=" + this.tags + ", Members={" + this.members + "}";
	}
}