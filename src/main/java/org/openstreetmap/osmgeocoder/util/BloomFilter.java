/*     */ package org.openstreetmap.osmgeocoder.util;
/*     */ 
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
import java.io.ObjectInputStream;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.util.BitSet;
/*     */ 
/*     */ public class BloomFilter
/*     */ {
/*     */   public static final int HASHES = 10;
/*     */   public static final boolean DEBUG = false;
/*  19 */   public static GeneralHashFunctionLibrary lib = new GeneralHashFunctionLibrary();
/*     */   private BitSet[] bits;
/*     */   private long SIZE;
/*     */ 
/*     */   public BloomFilter(String filename)
/*     */     throws IOException
/*     */   {
/*  30 */     this.bits = getBytesFromFile(new File(filename));
/*  31 */     for (BitSet bs : this.bits)
/*  32 */       this.SIZE += bs.length();
/*  33 */     this.SIZE -= 1L;
/*     */   }
/*     */ 
/*     */   public BloomFilter(long input)
/*     */   {
/*  48 */     input = 8L * input * 1024L * 1024L - 1L;
/*     */ 
/*  53 */     this.SIZE = input;
/*     */ 
/*  55 */     int noOfBitsets = (int)(input / 2147483648L) + 1;
/*     */ 
/*  57 */     this.bits = new BitSet[noOfBitsets];
/*     */ 
				int i;
/*  59 */     for ( i = 0; i < noOfBitsets - 1; i++)
/*     */     {
/*  61 */       this.bits[i] = new BitSet(2147483647);
/*  62 */       this.bits[i].set(2147483646);
/*     */     }
/*  64 */     int remaining = (int)(input % 2147483648L);
/*  65 */     remaining = remaining - remaining % 8 + 8;
/*  66 */     this.bits[i] = new BitSet(remaining);
/*  67 */     this.bits[i].set(remaining - 1);
/*     */   }
/*     */ 
/*     */   private void setBits(long hash)
/*     */   {
/*  72 */     hash %= this.SIZE;
/*  73 */     int setNum = (int)(hash / 2147483648L);
/*  74 */     int position = (int)(hash % 2147483648L);
/*     */ 
/*  76 */     this.bits[setNum].set(position);
/*     */   }
/*     */ 
/*     */   public void addWord(String word)
/*     */   {
		if(word==null) return;
/*  84 */     long hash = lib.APHash(word);
/*  85 */     setBits(hash);
/*  86 */     hash = lib.BKDRHash(word);
/*  87 */     setBits(hash);
/*  88 */     hash = lib.DEKHash(word);
/*  89 */     setBits(hash);
/*  90 */     hash = lib.SDBMHash(word);
/*  91 */     setBits(hash);
/*  92 */     hash = lib.BPHash(word);
/*  93 */     setBits(hash);
/*  94 */     hash = lib.RSHash(word);
/*  95 */     setBits(hash);
/*  96 */     hash = lib.JSHash(word);
/*  97 */     setBits(hash);
/*  98 */     hash = lib.PJWHash(word);
/*  99 */     setBits(hash);
/* 100 */     hash = lib.ELFHash(word);
/* 101 */     setBits(hash);
/* 102 */     hash = lib.DJBHash(word);
/* 103 */     setBits(hash);
/*     */   }
/*     */ 
/*     */   private boolean checkHash(long hash)
/*     */   {
/* 109 */     hash %= this.SIZE;
/* 110 */     int setNum = (int)(hash / 2147483648L);
/* 111 */     int position = (int)(hash % 2147483648L);
/* 112 */     return this.bits[setNum].get(position);
/*     */   }
/*     */ 
/*     */   public boolean wordExists(String word)
/*     */   {
/* 117 */     if (!checkHash(lib.APHash(word)))
/* 118 */       return false;
/* 119 */     if (!checkHash(lib.BKDRHash(word)))
/* 120 */       return false;
/* 121 */     if (!checkHash(lib.DEKHash(word)))
/* 122 */       return false;
/* 123 */     if (!checkHash(lib.SDBMHash(word)))
/* 124 */       return false;
/* 125 */     if (!checkHash(lib.BPHash(word))) {
/* 126 */       return false;
/*     */     }
/* 128 */     if (!checkHash(lib.RSHash(word)))
/* 129 */       return false;
/* 130 */     if (!checkHash(lib.JSHash(word)))
/* 131 */       return false;
/* 132 */     if (!checkHash(lib.ELFHash(word)))
/* 133 */       return false;
/* 134 */     if (!checkHash(lib.PJWHash(word)))
/* 135 */       return false;
/* 136 */     if (!checkHash(lib.DJBHash(word))) {
/* 137 */       return false;
/*     */     }
/* 139 */     return true;
/*     */   }
/*     */ 
/*     */   private BitSet fromByteArray(byte[] bytes)
/*     */   {
/* 147 */     BitSet bits = new BitSet(bytes.length * 8 - 1);
/* 148 */     for (int i = 0; i < bytes.length * 8 - 1; i++) {
/* 149 */       if ((bytes[(bytes.length - i / 8 - 1)] & 1 << i % 8) > 0) {
/* 150 */         bits.set(i);
/*     */       }
/*     */     }
/* 153 */     return bits;
/*     */   }
/*     */ 
/*     */   private byte[] toByteArray(BitSet bits) {
/* 157 */     byte[] bytes = new byte[bits.length() / 8 + 1];
/* 158 */     for (int i = 0; i < bits.length(); i++) {
/* 159 */       if (bits.get(i))
/*     */       {
/*     */         int tmp35_34 = (bytes.length - i / 8 - 1);
/*     */         byte[] tmp35_25 = bytes; tmp35_25[tmp35_34] = ((byte)(tmp35_25[tmp35_34] | 1 << i % 8));
/*     */       }
/*     */     }
/* 163 */     return bytes;
/*     */   }
/*     */ 
/*     */   public BitSet[] getBytesFromFile(File file) throws IOException
/*     */   {
/* 168 */     RandomAccessFile raf = new RandomAccessFile(file, "r");
/*     */ 
/* 170 */     long offset = 0L;
/* 171 */     long length = file.length();
/*     */ 
/* 175 */     int SIZE = (int)Math.ceil(length / 268435456.0D);
/* 176 */     BitSet[] bits = new BitSet[SIZE];
/*     */ 
/* 181 */     int bsCounter = 0;
/* 182 */     while (offset < length)
/*     */     {
/*     */       byte[] bytes;
/* 186 */       if (length - offset > 268435456L)
/* 187 */         bytes = new byte[268435456];
/*     */       else {
/* 189 */         bytes = new byte[(int)(length - offset)];
/*     */       }
/*     */ 
/* 193 */       raf.seek(offset);
/*     */ 
/* 197 */       raf.read(bytes);
/*     */ 
/* 199 */       BitSet bs = fromByteArray(bytes);
/* 200 */       bits[bsCounter] = bs;
/* 201 */       offset += bytes.length;
/*     */ 
/* 205 */       bsCounter++;
/*     */     }
/*     */ 
/* 209 */     raf.close();
/* 210 */     return bits;
/*     */   }
/*     */ 
/*     */   public void writeToFile(String filename)
/*     */     throws IOException
/*     */   {
/* 240 */     byte[][] bytes = new byte[this.bits.length][];
/*     */ 
/* 242 */     File file = new File(filename);
/* 243 */     FileOutputStream fos = new FileOutputStream(file);
/* 244 */     BufferedOutputStream bos = new BufferedOutputStream(fos);
/*     */ 
/* 246 */     long length = 0L;
/*     */     try
/*     */     {
/* 249 */       for (int i = 0; i < this.bits.length; i++)
/*     */       {
/* 251 */         bytes[i] = toByteArray(this.bits[i]);
/*     */ 
/* 254 */         bos.write(bytes[i]);
/* 255 */         length += bytes[i].length;
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 264 */       bos.close();
/*     */     }
/*     */   }

            public static BloomFilter readFromFile (File file) throws IOException, ClassNotFoundException {
              return null;
            }
/*     */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.util.BloomFilter
 * JD-Core Version:    0.6.2
 */
