/*    */ package org.apache.solr.util;
/*    */ 
/*    */ import java.io.InputStream;
/*    */ import javax.xml.parsers.SAXParserFactory;
/*    */ import javax.xml.stream.XMLInputFactory;
/*    */ import javax.xml.stream.XMLResolver;
/*    */ import org.apache.commons.io.input.ClosedInputStream;
/*    */ import org.xml.sax.EntityResolver;
/*    */ import org.xml.sax.InputSource;
/*    */ 
/*    */ public final class EmptyEntityResolver
/*    */ {
/* 44 */   public static final EntityResolver SAX_INSTANCE = new EntityResolver() {
/*    */     public InputSource resolveEntity(String publicId, String systemId) {
/* 46 */       return new InputSource(ClosedInputStream.CLOSED_INPUT_STREAM);
/*    */     }
/* 44 */   };
/*    */ 
/* 50 */   public static final XMLResolver STAX_INSTANCE = new XMLResolver() {
/*    */     public InputStream resolveEntity(String publicId, String systemId, String baseURI, String namespace) {
/* 52 */       return ClosedInputStream.CLOSED_INPUT_STREAM;
/*    */     }
/* 50 */   };
/*    */ 
/*    */   private static void trySetSAXFeature(SAXParserFactory saxFactory, String feature, boolean enabled)
/*    */   {
/*    */     try
/*    */     {
/* 61 */       saxFactory.setFeature(feature, enabled);
/*    */     }
/*    */     catch (Exception localException)
/*    */     {
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void configureSAXParserFactory(SAXParserFactory saxFactory)
/*    */   {
/* 73 */     saxFactory.setValidating(false);
/*    */ 
/* 75 */     trySetSAXFeature(saxFactory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
/*    */   }
/*    */ 
/*    */   private static void trySetStAXProperty(XMLInputFactory inputFactory, String key, Object value) {
/*    */     try {
/* 80 */       inputFactory.setProperty(key, value);
/*    */     }
/*    */     catch (Exception localException)
/*    */     {
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void configureXMLInputFactory(XMLInputFactory inputFactory)
/*    */   {
/* 91 */     trySetStAXProperty(inputFactory, "javax.xml.stream.isValidating", Boolean.FALSE);
/*    */ 
/* 93 */     trySetStAXProperty(inputFactory, "javax.xml.stream.isSupportingExternalEntities", Boolean.TRUE);
/* 94 */     inputFactory.setXMLResolver(STAX_INSTANCE);
/*    */   }
/*    */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.apache.solr.util.EmptyEntityResolver
 * JD-Core Version:    0.6.2
 */