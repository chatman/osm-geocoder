/*    */ package org.openstreetmap.osmgeocoder.indexer.primitives;
/*    */ 
/*    */ import com.sleepycat.persist.model.Persistent;
/*    */ import java.io.Serializable;
/*    */ 
/*    */ @Persistent
/*    */ public class Member
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 3864617249278794035L;
/*    */   public Object member;
/*    */   public String role;
/*    */ 
/*    */   public Member()
/*    */   {
/*    */   }
/*    */ 
/*    */   public Member(Object member, String role)
/*    */   {
/* 21 */     this.member = member;
/* 22 */     this.role = role;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 28 */     return this.member.getClass().getSimpleName() + ":" + this.member.toString();
/*    */   }
/*    */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.indexer.primitives.Member
 * JD-Core Version:    0.6.2
 */