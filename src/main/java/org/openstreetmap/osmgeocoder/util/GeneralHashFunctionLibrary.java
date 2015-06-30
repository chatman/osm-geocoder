/*     */ package org.openstreetmap.osmgeocoder.util;
/*     */ 
/*     */ class GeneralHashFunctionLibrary
/*     */ {
/*     */   public long RSHash(String str)
/*     */   {
/*  27 */     int b = 378551;
/*  28 */     int a = 63689;
/*  29 */     long hash = 0L;
/*     */ 
/*  31 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/*  33 */       hash = hash * a + str.charAt(i);
/*  34 */       a *= b;
/*     */     }
/*     */ 
/*  37 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long JSHash(String str)
/*     */   {
/*  44 */     long hash = 1315423911L;
/*     */ 
/*  46 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/*  48 */       hash ^= (hash << 5) + str.charAt(i) + (hash >> 2);
/*     */     }
/*     */ 
/*  51 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long PJWHash(String str)
/*     */   {
/*  58 */     long BitsInUnsignedInt = 32L;
/*  59 */     long ThreeQuarters = BitsInUnsignedInt * 3L / 4L;
/*  60 */     long OneEighth = BitsInUnsignedInt / 8L;
/*  61 */     long HighBits = -1L << (int)(BitsInUnsignedInt - OneEighth);
/*  62 */     long hash = 0L;
/*  63 */     long test = 0L;
/*     */ 
/*  65 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/*  67 */       hash = (hash << (int)OneEighth) + str.charAt(i);
/*     */ 
/*  69 */       if ((test = hash & HighBits) != 0L)
/*     */       {
/*  71 */         hash = (hash ^ test >> (int)ThreeQuarters) & (HighBits ^ 0xFFFFFFFF);
/*     */       }
/*     */     }
/*     */ 
/*  75 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long ELFHash(String str)
/*     */   {
/*  82 */     long hash = 0L;
/*  83 */     long x = 0L;
/*     */ 
/*  85 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/*  87 */       hash = (hash << 4) + str.charAt(i);
/*     */ 
/*  89 */       if ((x = hash & 0xF0000000) != 0L)
/*     */       {
/*  91 */         hash ^= x >> 24;
/*  92 */         hash &= (x ^ 0xFFFFFFFF);
/*     */       }
/*     */     }
/*     */ 
/*  96 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long BKDRHash(String str)
/*     */   {
/* 103 */     long seed = 131L;
/* 104 */     long hash = 0L;
/*     */ 
/* 106 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 108 */       hash = hash * seed + str.charAt(i);
/*     */     }
/*     */ 
/* 111 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long SDBMHash(String str)
/*     */   {
/* 118 */     long hash = 0L;
/*     */ 
/* 120 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 122 */       hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
/*     */     }
/*     */ 
/* 125 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long DJBHash(String str)
/*     */   {
/* 132 */     long hash = 5381L;
/*     */ 
/* 134 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 136 */       hash = (hash << 5) + hash + str.charAt(i);
/*     */     }
/*     */ 
/* 139 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long DEKHash(String str)
/*     */   {
/* 146 */     long hash = str.length();
/*     */ 
/* 148 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 150 */       hash = hash << 5 ^ hash >> 27 ^ str.charAt(i);
/*     */     }
/*     */ 
/* 153 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long BPHash(String str)
/*     */   {
/* 160 */     long hash = 0L;
/*     */ 
/* 162 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 164 */       hash = hash << 7 ^ str.charAt(i);
/*     */     }
/*     */ 
/* 167 */     return Math.abs(hash);
/*     */   }
/*     */ 
/*     */   public long APHash(String str)
/*     */   {
/* 174 */     long hash = 0L;
/*     */ 
/* 176 */     for (int i = 0; i < str.length(); i++)
/*     */     {
/* 178 */       if ((i & 0x1) == 0) {
/* 179 */         hash ^= hash << 7 ^ str.charAt(i) ^ hash >> 3;
/*     */       }
/*     */       else
/*     */       {
/* 183 */         hash ^= hash << 11 ^ str.charAt(i) ^ hash >> 5 ^ 0xFFFFFFFF;
/*     */       }
/*     */     }
/*     */ 
/* 187 */     return Math.abs(hash);
/*     */   }
/*     */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.util.GeneralHashFunctionLibrary
 * JD-Core Version:    0.6.2
 */