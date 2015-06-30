/*    */ package org.openstreetmap.osmgeocoder.geocoder;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import org.openstreetmap.osmgeocoder.util.BloomFilter;
/*    */ 
/*    */ public class Filter
/*    */ {
/*    */   String symbol;
/*    */   BloomFilter bf;
/*    */ 
/*    */   public Filter(String symbol, String filename)
/*    */     throws IOException
/*    */   {
/* 12 */     this.symbol = symbol;
/* 13 */     this.bf = new BloomFilter(filename);
/*    */   }
/*    */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.geocoder.Filter
 * JD-Core Version:    0.6.2
 */