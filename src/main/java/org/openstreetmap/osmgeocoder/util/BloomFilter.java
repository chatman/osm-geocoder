package org.openstreetmap.osmgeocoder.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class BloomFilter
{
  public static final int HASHES = 10;
  public static final boolean DEBUG = false;
  public static GeneralHashFunctionLibrary lib = new GeneralHashFunctionLibrary();
  private BitSet[] bits;
  private long SIZE;

  public BloomFilter(String filename)
      throws IOException
  {
    this.bits = getBytesFromFile(new File(filename));
    for (BitSet bs : this.bits)
      this.SIZE += bs.length();
    this.SIZE -= 1L;
  }

  public BloomFilter(long input)
  {
    input = 8L * input * 1024L * 1024L - 1L;

    this.SIZE = input;

    int noOfBitsets = (int)(input / 2147483648L) + 1;

    this.bits = new BitSet[noOfBitsets];

    int i;
    for ( i = 0; i < noOfBitsets - 1; i++)
    {
      this.bits[i] = new BitSet(2147483647);
      this.bits[i].set(2147483646);
    }
    int remaining = (int)(input % 2147483648L);
    remaining = remaining - remaining % 8 + 8;
    this.bits[i] = new BitSet(remaining);
    this.bits[i].set(remaining - 1);
  }

  private void setBits(long hash)
  {
    hash %= this.SIZE;
    int setNum = (int)(hash / 2147483648L);
    int position = (int)(hash % 2147483648L);

    this.bits[setNum].set(position);
  }

  public void addWord(String word)
  {
    if(word==null) return;
    long hash = lib.APHash(word);
    setBits(hash);
    hash = lib.BKDRHash(word);
    setBits(hash);
    hash = lib.DEKHash(word);
    setBits(hash);
    hash = lib.SDBMHash(word);
    setBits(hash);
    hash = lib.BPHash(word);
    setBits(hash);
    hash = lib.RSHash(word);
    setBits(hash);
    hash = lib.JSHash(word);
    setBits(hash);
    hash = lib.PJWHash(word);
    setBits(hash);
    hash = lib.ELFHash(word);
    setBits(hash);
    hash = lib.DJBHash(word);
    setBits(hash);
  }

  private boolean checkHash(long hash)
  {
    hash %= this.SIZE;
    int setNum = (int)(hash / 2147483648L);
    int position = (int)(hash % 2147483648L);
    return this.bits[setNum].get(position);
  }

  public boolean wordExists(String word)
  {
    if (!checkHash(lib.APHash(word)))
      return false;
    if (!checkHash(lib.BKDRHash(word)))
      return false;
    if (!checkHash(lib.DEKHash(word)))
      return false;
    if (!checkHash(lib.SDBMHash(word)))
      return false;
    if (!checkHash(lib.BPHash(word))) {
      return false;
    }
    if (!checkHash(lib.RSHash(word)))
      return false;
    if (!checkHash(lib.JSHash(word)))
      return false;
    if (!checkHash(lib.ELFHash(word)))
      return false;
    if (!checkHash(lib.PJWHash(word)))
      return false;
    if (!checkHash(lib.DJBHash(word))) {
      return false;
    }
    return true;
  }

  private BitSet fromByteArray(byte[] bytes)
  {
    BitSet bits = new BitSet(bytes.length * 8 - 1);
    for (int i = 0; i < bytes.length * 8 - 1; i++) {
      if ((bytes[(bytes.length - i / 8 - 1)] & 1 << i % 8) > 0) {
        bits.set(i);
      }
    }
    return bits;
  }

  private byte[] toByteArray(BitSet bits) {
    byte[] bytes = new byte[bits.length() / 8 + 1];
    for (int i = 0; i < bits.length(); i++) {
      if (bits.get(i))
      {
        int tmp35_34 = (bytes.length - i / 8 - 1);
        byte[] tmp35_25 = bytes; tmp35_25[tmp35_34] = ((byte)(tmp35_25[tmp35_34] | 1 << i % 8));
      }
    }
    return bytes;
  }

  public BitSet[] getBytesFromFile(File file) throws IOException
  {
    RandomAccessFile raf = new RandomAccessFile(file, "r");

    long offset = 0L;
    long length = file.length();

    int SIZE = (int)Math.ceil(length / 268435456.0D);
    BitSet[] bits = new BitSet[SIZE];

    int bsCounter = 0;
    while (offset < length)
    {
      byte[] bytes;
      if (length - offset > 268435456L)
        bytes = new byte[268435456];
      else {
        bytes = new byte[(int)(length - offset)];
      }

      raf.seek(offset);

      raf.read(bytes);

      BitSet bs = fromByteArray(bytes);
      bits[bsCounter] = bs;
      offset += bytes.length;

      bsCounter++;
    }

    raf.close();
    return bits;
  }

  public void writeToFile(String filename)
      throws IOException
  {
    byte[][] bytes = new byte[this.bits.length][];

    File file = new File(filename);
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);

    long length = 0L;
    try
    {
      for (int i = 0; i < this.bits.length; i++)
      {
        bytes[i] = toByteArray(this.bits[i]);

        bos.write(bytes[i]);
        length += bytes[i].length;
      }

    }
    finally
    {
      bos.close();
    }
  }

  public static BloomFilter readFromFile (File file) throws IOException, ClassNotFoundException {
    return null;
  }
}
