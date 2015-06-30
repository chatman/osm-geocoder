package org.openstreetmap.osmgeocoder.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LazyList
implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String[] __EMTPY_STRING_ARRAY = new String[0];

	public static Object add(Object list, Object item)
	{
		if (list == null)
		{
			if (((item instanceof List)) || (item == null))
			{
				List l = new ArrayList();
				l.add(item);
				return l;
			}

			return item;
		}

		if ((list instanceof List))
		{
			((List)list).add(item);
			return list;
		}

		List l = new ArrayList();
		l.add(list);
		l.add(item);
		return l;
	}

	public static Object add(Object list, Collection collection)
	{
		Iterator i = collection.iterator();
		while (i.hasNext())
			list = add(list, i.next());
		return list;
	}

	public static Object add(Object list, int initialSize, Object item)
	{
		if (list == null)
		{
			if (((item instanceof List)) || (item == null))
			{
				List l = new ArrayList(initialSize);
				l.add(item);
				return l;
			}

			return item;
		}

		if ((list instanceof List))
		{
			((List)list).add(item);
			return list;
		}

		List l = new ArrayList(initialSize);
		l.add(list);
		l.add(item);
		return l;
	}

	public static Object remove(Object list, Object o)
	{
		if (list == null) {
			return null;
		}
		if ((list instanceof List))
		{
			List l = (List)list;
			l.remove(o);
			if (l.size() == 0)
				return null;
			return list;
		}

		return null;
	}

	public static List getList(Object list)
	{
		return getList(list, false);
	}

	public static List getList(Object list, boolean nullForEmpty)
	{
		if (list == null)
			return nullForEmpty ? null : Collections.EMPTY_LIST;
		if ((list instanceof List)) {
			return (List)list;
		}
		List l = new ArrayList(1);
		l.add(list);
		return l;
	}

	public static String[] toStringArray(Object list)
	{
		if (list == null) {
			return __EMTPY_STRING_ARRAY;
		}
		if ((list instanceof List))
		{
			List l = (List)list;

			String[] a = new String[l.size()];
			for (int i = l.size(); i-- > 0; )
			{
				Object o = l.get(i);
				if (o != null)
					a[i] = o.toString();
			}
			return a;
		}

		return new String[] { list.toString() };
	}

	public static int size(Object list)
	{
		if (list == null)
			return 0;
		if ((list instanceof List))
			return ((List)list).size();
		return 1;
	}

	public static Object get(Object list, int i)
	{
		if (list == null) {
			throw new IndexOutOfBoundsException();
		}
		if ((list instanceof List)) {
			return ((List)list).get(i);
		}
		if (i == 0) {
			return list;
		}
		throw new IndexOutOfBoundsException();
	}

	public static boolean contains(Object list, Object item)
	{
		if (list == null) {
			return false;
		}
		if ((list instanceof List)) {
			return ((List)list).contains(item);
		}
		return list.equals(item);
	}

	public static Object clone(Object list)
	{
		if (list == null)
			return null;
		if ((list instanceof List))
			return new ArrayList((List)list);
		return list;
	}

	public static String toString(Object list)
	{
		if (list == null)
			return "[]";
		if ((list instanceof List))
			return ((List)list).toString();
		return "[" + list + "]";
	}

	public static Iterator iterator(Object list)
	{
		if (list == null)
			return Collections.EMPTY_LIST.iterator();
		if ((list instanceof List))
			return ((List)list).iterator();
		return getList(list).iterator();
	}

	public static ListIterator listIterator(Object list)
	{
		if (list == null)
			return Collections.EMPTY_LIST.listIterator();
		if ((list instanceof List))
			return ((List)list).listIterator();
		return getList(list).listIterator();
	}
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.util.LazyList
 * JD-Core Version:    0.6.2
 */