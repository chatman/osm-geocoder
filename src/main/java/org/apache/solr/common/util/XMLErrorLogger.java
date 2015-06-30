/*    */ package org.apache.solr.common.util;
/*    */ 
/*    */ import javax.xml.stream.Location;
/*    */ import javax.xml.stream.XMLReporter;
/*    */ import javax.xml.transform.ErrorListener;
/*    */ import javax.xml.transform.TransformerException;
/*    */ import org.slf4j.Logger;
/*    */ import org.xml.sax.ErrorHandler;
/*    */ import org.xml.sax.SAXException;
/*    */ import org.xml.sax.SAXParseException;
/*    */ 
/*    */ public final class XMLErrorLogger
/*    */   implements ErrorHandler, ErrorListener, XMLReporter
/*    */ {
/*    */   private final Logger log;
/*    */ 
/*    */   public XMLErrorLogger(Logger log)
/*    */   {
/* 35 */     this.log = log;
/*    */   }
/*    */ 
/*    */   public void warning(SAXParseException e)
/*    */   {
/* 41 */     this.log.warn("XML parse warning in \"" + e.getSystemId() + "\", line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage());
/*    */   }
/*    */ 
/*    */   public void error(SAXParseException e) throws SAXException {
/* 45 */     throw e;
/*    */   }
/*    */ 
/*    */   public void fatalError(SAXParseException e) throws SAXException {
/* 49 */     throw e;
/*    */   }
/*    */ 
/*    */   public void warning(TransformerException e)
/*    */   {
/* 55 */     this.log.warn(e.getMessageAndLocation());
/*    */   }
/*    */ 
/*    */   public void error(TransformerException e) throws TransformerException {
/* 59 */     throw e;
/*    */   }
/*    */ 
/*    */   public void fatalError(TransformerException e) throws TransformerException {
/* 63 */     throw e;
/*    */   }
/*    */ 
/*    */   public void report(String message, String errorType, Object relatedInformation, Location loc)
/*    */   {
/* 69 */     StringBuilder sb = new StringBuilder("XML parser reported ").append(errorType);
/* 70 */     if (loc != null) {
/* 71 */       sb.append(" in \"").append(loc.getSystemId()).append("\", line ")
/* 72 */         .append(loc.getLineNumber()).append(", column ").append(loc.getColumnNumber());
/*    */     }
/* 74 */     this.log.warn(": " + message);
/*    */   }
/*    */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.apache.solr.common.util.XMLErrorLogger
 * JD-Core Version:    0.6.2
 */