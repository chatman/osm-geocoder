package org.openstreetmap.osmgeocoder.util;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiMap extends HashMap
implements Cloneable, Serializable
{
	public MultiMap()
	{
	}

	public MultiMap(int size)
	{
	}

	public MultiMap(Map map)
	{
		putAll(map);
	}

	public List getValues(Object name)
	{
		return LazyList.getList(super.get(name), true);
	}

	public Object getValue(Object name, int i)
	{
		Object l = super.get(name);
		if ((i == 0) && (LazyList.size(l) == 0))
			return null;
		return LazyList.get(l, i);
	}

	public String getString(Object name)
	{
		Object l = super.get(name);
		switch (LazyList.size(l))
		{
		case 0:
			return null;
		case 1:
			Object o = LazyList.get(l, 0);
			return o == null ? null : o.toString();
		}
		StringBuffer values = new StringBuffer(128);
		synchronized (values)
		{
			for (int i = 0; i < LazyList.size(l); i++)
			{
				Object e = LazyList.get(l, i);
				if (e != null)
				{
					if (values.length() > 0)
						values.append(',');
					values.append(e.toString());
				}
			}
			return values.toString();
		}
	}

	public Object get(Object name)
	{
		Object l = super.get(name);
		switch (LazyList.size(l))
		{
		case 0:
			return null;
		case 1:
			Object o = LazyList.get(l, 0);
			return o;
		}
		return LazyList.getList(l, true);
	}

	public Object put(Object name, Object value)
	{
		return super.put(name, LazyList.add(null, value));
	}

	public Object putValues(Object name, List values)
	{
		return super.put(name, values);
	}

	public Object putValues(Object name, String[] values)
	{
		Object list = null;
		for (int i = 0; i < values.length; i++)
			list = LazyList.add(list, values[i]);
		return put(name, list);
	}

	public void add(Object name, Object value)
	{
		Object lo = super.get(name);
		Object ln = LazyList.add(lo, value);
		if (lo != ln)
			super.put(name, ln);
	}

	public void addValues(Object name, List values)
	{
		Object lo = super.get(name);
		Object ln = LazyList.add(lo, values);
		if (lo != ln)
			super.put(name, ln);
	}

	public void addValues(Object name, String[] values)
	{
		Object lo = super.get(name);
		Object ln = LazyList.add(lo, Arrays.asList(values));
		if (lo != ln)
			super.put(name, ln);
	}

	public boolean removeValue(Object name, Object value)
	{
		Object lo = super.get(name);
		Object ln = lo;
		int s = LazyList.size(lo);
		if (s > 0)
			ln = LazyList.remove(lo, value);
		return LazyList.size(ln) != s;
	}

	public void putAll(Map m)
	{
		Iterator i = m.entrySet().iterator();
		boolean multi = m instanceof MultiMap;
		while (i.hasNext())
		{
			Map.Entry entry = 
					(Map.Entry)i.next();
			if (multi)
				super.put(entry.getKey(), LazyList.clone(entry.getValue()));
			else
				put(entry.getKey(), entry.getValue());
		}
	}

	public Map toStringArrayMap()
	{
		HashMap map = new HashMap(size() * 3 / 2);

		Iterator i = super.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry entry = (Map.Entry)i.next();
			Object l = entry.getValue();
			map.put(entry.getKey(), LazyList.toStringArray(l));
		}
		return map;
	}

	public Object clone()
	{
		return new MultiMap(this);
	}

	public MultiMap readFromFile(String filename)
	{
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try
		{
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);

			Object ob = ois.readObject();

			if (ob != null)
			{
				return (MultiMap)ob;
			}

		}
		catch (EOFException e)
		{
			System.err.println("==End Of File reached==\n\n");
		}
		catch (Exception e)
		{
			System.err.println("== Error ===" + e + "\n");
		}

		return null;
	}

	public void writeToFile(String filename) throws IOException
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try
		{
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);

			oos.writeObject(this);
			System.out.println("Written...");
		}
		catch (Exception localException)
		{
		}
		finally {
			if (oos != null)
			{
				oos.close();
			}
		}
	}

	public static void main(String[] args) {
		MultiMap mm = new MultiMap();

		mm.add("london", "USA");
		mm.add("paris", "france");
		mm.add("london", "england");

		System.out.println(mm.getValues("london"));
	}
}