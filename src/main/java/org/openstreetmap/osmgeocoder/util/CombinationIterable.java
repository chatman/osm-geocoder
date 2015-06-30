package org.openstreetmap.osmgeocoder.util;

import java.util.Iterator;

class CombinationIterable<T> implements Iterable<T[]>
{
	private T input[];
	private int sampleSize;
	private int numElements;

	public CombinationIterable(int sampleSize, T... input)
	{
		this.sampleSize = sampleSize;
		this.input = input.clone();
		numElements = (int) Math.pow(input.length, sampleSize);
	}

	public Iterator<T[]> iterator()
	{
		return new Iterator<T[]>()
				{
			private int current = 0;
			private int chosen[] = new int[sampleSize];

			public boolean hasNext()
			{
				return current < numElements;
			}

			public T[] next()
			{
				@SuppressWarnings("unchecked")
				T result[] = (T[]) java.lang.reflect.Array.newInstance(
						input.getClass().getComponentType(), sampleSize);
				for (int i = 0; i < sampleSize; i++)
				{
					result[i] = input[chosen[i]];
				}
				increase();
				current++;
				return result;
			}

			private void increase()
			{
				int index = chosen.length - 1;
				while (index >= 0)
				{
					if (chosen[index] < input.length - 1)
					{
						chosen[index]++;
						return;
					}
					else
					{
						chosen[index] = 0;
						index--;
					}
				}
			}

			public void remove()
			{
				throw new UnsupportedOperationException(
						"May not remove elements from a combination");
			}
				};
	}
}