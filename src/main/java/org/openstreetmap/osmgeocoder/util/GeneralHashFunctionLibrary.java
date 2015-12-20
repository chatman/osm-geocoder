 package org.openstreetmap.osmgeocoder.util;
 
 class GeneralHashFunctionLibrary
 {
   public long RSHash(String str)
   {
     int b = 378551;
     int a = 63689;
     long hash = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = hash * a + str.charAt(i);
       a *= b;
     }
 
     return Math.abs(hash);
   }
 
   public long JSHash(String str)
   {
     long hash = 1315423911L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash ^= (hash << 5) + str.charAt(i) + (hash >> 2);
     }
 
     return Math.abs(hash);
   }
 
   public long PJWHash(String str)
   {
     long BitsInUnsignedInt = 32L;
     long ThreeQuarters = BitsInUnsignedInt * 3L / 4L;
     long OneEighth = BitsInUnsignedInt / 8L;
     long HighBits = -1L << (int)(BitsInUnsignedInt - OneEighth);
     long hash = 0L;
     long test = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = (hash << (int)OneEighth) + str.charAt(i);
 
       if ((test = hash & HighBits) != 0L)
       {
         hash = (hash ^ test >> (int)ThreeQuarters) & (HighBits ^ 0xFFFFFFFF);
       }
     }
 
     return Math.abs(hash);
   }
 
   public long ELFHash(String str)
   {
     long hash = 0L;
     long x = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = (hash << 4) + str.charAt(i);
 
       if ((x = hash & 0xF0000000) != 0L)
       {
         hash ^= x >> 24;
         hash &= (x ^ 0xFFFFFFFF);
       }
     }
 
     return Math.abs(hash);
   }
 
   public long BKDRHash(String str)
   {
     long seed = 131L;
     long hash = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = hash * seed + str.charAt(i);
     }
 
     return Math.abs(hash);
   }
 
   public long SDBMHash(String str)
   {
     long hash = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
     }
 
     return Math.abs(hash);
   }
 
   public long DJBHash(String str)
   {
     long hash = 5381L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = (hash << 5) + hash + str.charAt(i);
     }
 
     return Math.abs(hash);
   }
 
   public long DEKHash(String str)
   {
     long hash = str.length();
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = hash << 5 ^ hash >> 27 ^ str.charAt(i);
     }
 
     return Math.abs(hash);
   }
 
   public long BPHash(String str)
   {
     long hash = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       hash = hash << 7 ^ str.charAt(i);
     }
 
     return Math.abs(hash);
   }
 
   public long APHash(String str)
   {
     long hash = 0L;
 
     for (int i = 0; i < str.length(); i++)
     {
       if ((i & 0x1) == 0) {
         hash ^= hash << 7 ^ str.charAt(i) ^ hash >> 3;
       }
       else
       {
         hash ^= hash << 11 ^ str.charAt(i) ^ hash >> 5 ^ 0xFFFFFFFF;
       }
     }
 
     return Math.abs(hash);
   }
 }
