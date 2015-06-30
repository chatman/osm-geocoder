package org.openstreetmap.osmgeocoder.util;

import java.util.Iterator;

class PermutationIterable<T> implements Iterable<T[]>
{
    private T input[];
    private int numPermutations = 0;
 
    public PermutationIterable(T... input)
    {
        this.input = input.clone();
        numPermutations = factorial(input.length);
    }
 
    public Iterator<T[]> iterator()
    {
        return new Iterator<T[]>()
        {
            int current = 0;
 
            public boolean hasNext()
            {
                return current < numPermutations;
            }
 
            // Adapted from [url=http://en.wikipedia.org/wiki/Permutation]Permutation - Wikipedia, the free encyclopedia[/url]
            public T[] next()
            {
                T result[] = input.clone();
                int factorial = numPermutations / input.length;
                for (int i = 0; i < result.length - 1; i++)
                {
                    int tempIndex = (current / factorial) %
                        (result.length - i);
                    T temp = result[i + tempIndex];
                    for (int j = i + tempIndex; j > i; j--)
                    {
                        result[j] = result[j - 1];
                    }
                    result[i] = temp;
                    factorial /= (result.length - (i + 1));
                }
                current++;
                return result;
            }
 
            public void remove()
            {
                throw new UnsupportedOperationException(
                    "May not remove elements from a permutation");
            }
        };
    }
 
 
 
    /**
     * Utility method for computing the factorial n! of a number n.
     * The factorial of a number n is n*(n-1)*(n-2)*...*1, or more
     * formally:<br />
     * 0! = 1 <br />
     * 1! = 1 <br />
     * n! = n*(n-1)!<br />
     *
     * @param n The number of which the factorial should be computed
     * @return The factorial, i.e. n!
     */
    public static int factorial(int n)
    {
        int f = 1;
        for (int i = 2; i <= n; i++)
        {
            f *= i;
        }
        return f;
    }
 
}