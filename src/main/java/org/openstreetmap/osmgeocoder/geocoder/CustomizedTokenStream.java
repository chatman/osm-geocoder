/*    */ package org.openstreetmap.osmgeocoder.geocoder;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import org.apache.lucene.analysis.TokenStream;
/*    */ import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
/*    */ import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
/*    */ import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
/*    */ 
/*    */ public class CustomizedTokenStream extends TokenStream
/*    */ {
/*    */   private String[] tokens;
/*    */   private int index;
/*    */   private int offset;
/* 41 */   private final CharTermAttribute termAtt = (CharTermAttribute)addAttribute(CharTermAttribute.class);
/* 42 */   private final OffsetAttribute offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
/* 43 */   private final PositionIncrementAttribute posIncrAtt = (PositionIncrementAttribute)addAttribute(PositionIncrementAttribute.class);
/*    */ 
/*    */   public CustomizedTokenStream(String[] data)
/*    */   {
/* 10 */     this.tokens = data;
/* 11 */     reset();
/*    */   }
/*    */ 
/*    */   public void reset()
/*    */   {
/* 19 */     this.index = 0;
/* 20 */     this.offset = 0;
/*    */   }
/*    */ 
/*    */   public boolean incrementToken() throws IOException {
/* 24 */     clearAttributes();
/* 25 */     if (this.index == this.tokens.length) {
/* 26 */       return false;
/*    */     }
/* 28 */     String tkn = this.tokens[this.index];
/* 29 */     int len = tkn.length();
/* 30 */     this.index += 1;
/* 31 */     this.offsetAtt.setOffset(this.offset, this.offset + len - 1);
/* 32 */     this.posIncrAtt.setPositionIncrement(1);
/* 33 */     this.termAtt.setEmpty();
/* 34 */     this.termAtt.resizeBuffer(len);
/* 35 */     this.termAtt.append(tkn);
/* 36 */     this.offset += len;
/* 37 */     return true;
/*    */   }
/*    */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.geocoder.CustomizedTokenStream
 * JD-Core Version:    0.6.2
 */