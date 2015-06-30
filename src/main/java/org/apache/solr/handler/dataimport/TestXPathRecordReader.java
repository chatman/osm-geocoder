/*     */ package org.apache.solr.handler.dataimport;
/*     */ 
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import junit.framework.TestCase;
/*     */ import org.junit.Test;
/*     */ 
/*     */ public class TestXPathRecordReader extends TestCase
/*     */ {
/*     */   @Test
/*     */   public void testBasic()
/*     */   {
/*  37 */     String xml = "<root>\n   <b><c>Hello C1</c>\n      <c>Hello C1</c>\n      </b>\n   <b><c>Hello C2</c>\n     </b>\n</root>";
/*     */ 
/*  44 */     XPathRecordReader rr = new XPathRecordReader("/root/b");
/*  45 */     rr.addField("c", "/root/b/c", true);
/*  46 */     List l = rr.getAllRecords(new StringReader(xml));
/*  47 */     assertEquals(2, l.size());
/*  48 */     assertEquals(2, ((List)((Map)l.get(0)).get("c")).size());
/*  49 */     assertEquals(1, ((List)((Map)l.get(1)).get("c")).size());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttributes() {
/*  54 */     String xml = "<root>\n   <b a=\"x0\" b=\"y0\" />\n   <b a=\"x1\" b=\"y1\" />\n   <b a=\"x2\" b=\"y2\" />\n</root>";
/*     */ 
/*  59 */     XPathRecordReader rr = new XPathRecordReader("/root/b");
/*  60 */     rr.addField("a", "/root/b/@a", false);
/*  61 */     rr.addField("b", "/root/b/@b", false);
/*  62 */     List l = rr.getAllRecords(new StringReader(xml));
/*  63 */     assertEquals(3, l.size());
/*  64 */     assertEquals("x0", ((Map)l.get(0)).get("a"));
/*  65 */     assertEquals("x1", ((Map)l.get(1)).get("a"));
/*  66 */     assertEquals("x2", ((Map)l.get(2)).get("a"));
/*  67 */     assertEquals("y0", ((Map)l.get(0)).get("b"));
/*  68 */     assertEquals("y1", ((Map)l.get(1)).get("b"));
/*  69 */     assertEquals("y2", ((Map)l.get(2)).get("b"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttrInRoot() {
/*  74 */     String xml = "<r>\n<merchantProduct id=\"814636051\" mid=\"189973\">\n                   <in_stock type=\"stock-4\" />\n                   <condition type=\"cond-0\" />\n                   <price>301.46</price>\n   </merchantProduct>\n<merchantProduct id=\"814636052\" mid=\"189974\">\n                   <in_stock type=\"stock-5\" />\n                   <condition type=\"cond-1\" />\n                   <price>302.46</price>\n   </merchantProduct>\n\n</r>";
/*     */ 
/*  87 */     XPathRecordReader rr = new XPathRecordReader("/r/merchantProduct");
/*  88 */     rr.addField("id", "/r/merchantProduct/@id", false);
/*  89 */     rr.addField("mid", "/r/merchantProduct/@mid", false);
/*  90 */     rr.addField("price", "/r/merchantProduct/price", false);
/*  91 */     rr.addField("conditionType", "/r/merchantProduct/condition/@type", false);
/*  92 */     List l = rr.getAllRecords(new StringReader(xml));
/*  93 */     Map m = (Map)l.get(0);
/*  94 */     assertEquals("814636051", m.get("id"));
/*  95 */     assertEquals("189973", m.get("mid"));
/*  96 */     assertEquals("301.46", m.get("price"));
/*  97 */     assertEquals("cond-0", m.get("conditionType"));
/*     */ 
/*  99 */     m = (Map)l.get(1);
/* 100 */     assertEquals("814636052", m.get("id"));
/* 101 */     assertEquals("189974", m.get("mid"));
/* 102 */     assertEquals("302.46", m.get("price"));
/* 103 */     assertEquals("cond-1", m.get("conditionType"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttributes2Level() {
/* 108 */     String xml = "<root>\n<a>\n  <b a=\"x0\" b=\"y0\" />\n       <b a=\"x1\" b=\"y1\" />\n       <b a=\"x2\" b=\"y2\" />\n       </a></root>";
/*     */ 
/* 114 */     XPathRecordReader rr = new XPathRecordReader("/root/a/b");
/* 115 */     rr.addField("a", "/root/a/b/@a", false);
/* 116 */     rr.addField("b", "/root/a/b/@b", false);
/* 117 */     List l = rr.getAllRecords(new StringReader(xml));
/* 118 */     assertEquals(3, l.size());
/* 119 */     assertEquals("x0", ((Map)l.get(0)).get("a"));
/* 120 */     assertEquals("y1", ((Map)l.get(1)).get("b"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttributes2LevelHetero() {
/* 125 */     String xml = "<root>\n<a>\n   <b a=\"x0\" b=\"y0\" />\n        <b a=\"x1\" b=\"y1\" />\n        <b a=\"x2\" b=\"y2\" />\n        </a><x>\n   <b a=\"x4\" b=\"y4\" />\n        <b a=\"x5\" b=\"y5\" />\n        <b a=\"x6\" b=\"y6\" />\n        </x></root>";
/*     */ 
/* 135 */     XPathRecordReader rr = new XPathRecordReader("/root/a | /root/x");
/* 136 */     rr.addField("a", "/root/a/b/@a", false);
/* 137 */     rr.addField("b", "/root/a/b/@b", false);
/* 138 */     rr.addField("a", "/root/x/b/@a", false);
/* 139 */     rr.addField("b", "/root/x/b/@b", false);
/*     */ 
/* 141 */     final List a = new ArrayList();
/* 142 */     final List x = new ArrayList();
/* 143 */     rr.streamRecords(new StringReader(xml), new XPathRecordReader.Handler() {
/*     */       public void handle(Map<String, Object> record, String xpath) {
/* 145 */         if (record == null) return;
/* 146 */         if (xpath.equals("/root/a")) a.add(record);
/* 147 */         if (xpath.equals("/root/x")) x.add(record);
/*     */       }
/*     */     });
/* 151 */     assertEquals(1, a.size());
/* 152 */     assertEquals(1, x.size());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttributes2LevelMissingAttrVal() {
/* 157 */     String xml = "<root>\n<a>\n  <b a=\"x0\" b=\"y0\" />\n       <b a=\"x1\" b=\"y1\" />\n       </a><a>\n  <b a=\"x3\"  />\n       <b b=\"y4\" />\n       </a></root>";
/*     */ 
/* 165 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 166 */     rr.addField("a", "/root/a/b/@a", true);
/* 167 */     rr.addField("b", "/root/a/b/@b", true);
/* 168 */     List l = rr.getAllRecords(new StringReader(xml));
/* 169 */     assertEquals(2, l.size());
/* 170 */     assertNull(((List)((Map)l.get(1)).get("a")).get(1));
/* 171 */     assertNull(((List)((Map)l.get(1)).get("b")).get(0));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testElems2LevelMissing() {
/* 176 */     String xml = "<root>\n\t<a>\n\t   <b>\n\t  <x>x0</x>\n\t            <y>y0</y>\n\t            </b>\n\t   <b>\n\t  <x>x1</x>\n\t            <y>y1</y>\n\t            </b>\n\t   </a>\n\t<a>\n\t   <b>\n\t  <x>x3</x>\n\t   </b>\n\t   <b>\n\t  <y>y4</y>\n\t   </b>\n\t   </a>\n</root>";
/*     */ 
/* 190 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 191 */     rr.addField("a", "/root/a/b/x", true);
/* 192 */     rr.addField("b", "/root/a/b/y", true);
/* 193 */     List l = rr.getAllRecords(new StringReader(xml));
/* 194 */     assertEquals(2, l.size());
/* 195 */     assertNull(((List)((Map)l.get(1)).get("a")).get(1));
/* 196 */     assertNull(((List)((Map)l.get(1)).get("b")).get(0));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testElems2LevelEmpty() {
/* 201 */     String xml = "<root>\n\t<a>\n\t   <b>\n\t  <x>x0</x>\n\t            <y>y0</y>\n\t   </b>\n\t   <b>\n\t  <x></x>\n\t            <y>y1</y>\n\t   </b>\n\t</a>\n</root>";
/*     */ 
/* 211 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 212 */     rr.addField("a", "/root/a/b/x", true);
/* 213 */     rr.addField("b", "/root/a/b/y", true);
/* 214 */     List l = rr.getAllRecords(new StringReader(xml));
/* 215 */     assertEquals(1, l.size());
/* 216 */     assertEquals("x0", ((List)((Map)l.get(0)).get("a")).get(0));
/* 217 */     assertEquals("y0", ((List)((Map)l.get(0)).get("b")).get(0));
/* 218 */     assertEquals("", ((List)((Map)l.get(0)).get("a")).get(1));
/* 219 */     assertEquals("y1", ((List)((Map)l.get(0)).get("b")).get(1));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testMixedContent() {
/* 224 */     String xml = "<xhtml:p xmlns:xhtml=\"http://xhtml.com/\" >This text is \n  <xhtml:b>bold</xhtml:b> and this text is \n  <xhtml:u>underlined</xhtml:u>!\n</xhtml:p>";
/*     */ 
/* 228 */     XPathRecordReader rr = new XPathRecordReader("/p");
/* 229 */     rr.addField("p", "/p", true);
/* 230 */     rr.addField("b", "/p/b", true);
/* 231 */     rr.addField("u", "/p/u", true);
/* 232 */     List l = rr.getAllRecords(new StringReader(xml));
/* 233 */     Map row = (Map)l.get(0);
/*     */ 
/* 235 */     assertEquals("bold", ((List)row.get("b")).get(0));
/* 236 */     assertEquals("underlined", ((List)row.get("u")).get(0));
/* 237 */     String p = (String)((List)row.get("p")).get(0);
/* 238 */     assertTrue(p.contains("This text is"));
/* 239 */     assertTrue(p.contains("and this text is"));
/* 240 */     assertTrue(p.contains("!"));
/*     */ 
/* 242 */     assertFalse(p.contains("bold"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testMixedContentFlattened() {
/* 247 */     String xml = "<xhtml:p xmlns:xhtml=\"http://xhtml.com/\" >This text is \n  <xhtml:b>bold</xhtml:b> and this text is \n  <xhtml:u>underlined</xhtml:u>!\n</xhtml:p>";
/*     */ 
/* 251 */     XPathRecordReader rr = new XPathRecordReader("/p");
/* 252 */     rr.addField("p", "/p", false, 1);
/* 253 */     List l = rr.getAllRecords(new StringReader(xml));
/* 254 */     Map row = (Map)l.get(0);
/* 255 */     assertEquals("This text is \n  bold and this text is \n  underlined!", 
/* 257 */       ((String)row.get("p")).trim());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testElems2LevelWithAttrib() {
/* 262 */     String xml = "<root>\n\t<a>\n\t   <b k=\"x\">\n\t                        <x>x0</x>\n\t                        <y></y>\n\t                        </b>\n\t                     <b k=\"y\">\n\t                        <x></x>\n\t                        <y>y1</y>\n\t                        </b>\n\t                     <b k=\"z\">\n\t                        <x>x2</x>\n\t                        <y>y2</y>\n\t                        </b>\n\t                </a>\n\t           <a>\n\t   <b>\n\t                        <x>x3</x>\n\t                        </b>\n\t                     <b>\n\t                     <y>y4</y>\n\t                        </b>\n\t               </a>\n</root>";
/*     */ 
/* 283 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 284 */     rr.addField("x", "/root/a/b[@k]/x", true);
/* 285 */     rr.addField("y", "/root/a/b[@k]/y", true);
/* 286 */     List l = rr.getAllRecords(new StringReader(xml));
/* 287 */     assertEquals(2, l.size());
/* 288 */     assertEquals(3, ((List)((Map)l.get(0)).get("x")).size());
/* 289 */     assertEquals(3, ((List)((Map)l.get(0)).get("y")).size());
/* 290 */     assertEquals("x0", ((List)((Map)l.get(0)).get("x")).get(0));
/* 291 */     assertEquals("", ((List)((Map)l.get(0)).get("y")).get(0));
/* 292 */     assertEquals("", ((List)((Map)l.get(0)).get("x")).get(1));
/* 293 */     assertEquals("y1", ((List)((Map)l.get(0)).get("y")).get(1));
/* 294 */     assertEquals("x2", ((List)((Map)l.get(0)).get("x")).get(2));
/* 295 */     assertEquals("y2", ((List)((Map)l.get(0)).get("y")).get(2));
/* 296 */     assertEquals(0, ((Map)l.get(1)).size());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testElems2LevelWithAttribMultiple() {
/* 301 */     String xml = "<root>\n\t<a>\n\t   <b k=\"x\" m=\"n\" >\n\t             <x>x0</x>\n\t             <y>y0</y>\n\t             </b>\n\t          <b k=\"y\" m=\"p\">\n\t             <x>x1</x>\n\t             <y>y1</y>\n\t             </b>\n\t   </a>\n\t<a>\n\t   <b k=\"x\">\n\t             <x>x3</x>\n\t             </b>\n\t          <b m=\"n\">\n\t             <y>y4</y>\n\t             </b>\n\t   </a>\n</root>";
/*     */ 
/* 319 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 320 */     rr.addField("x", "/root/a/b[@k][@m='n']/x", true);
/* 321 */     rr.addField("y", "/root/a/b[@k][@m='n']/y", true);
/* 322 */     List l = rr.getAllRecords(new StringReader(xml));
/* 323 */     assertEquals(2, l.size());
/* 324 */     assertEquals(1, ((List)((Map)l.get(0)).get("x")).size());
/* 325 */     assertEquals(1, ((List)((Map)l.get(0)).get("y")).size());
/* 326 */     assertEquals(0, ((Map)l.get(1)).size());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testElems2LevelWithAttribVal() {
/* 331 */     String xml = "<root>\n\t<a>\n   <b k=\"x\">\n\t                  <x>x0</x>\n\t                  <y>y0</y>\n\t                  </b>\n\t                <b k=\"y\">\n\t                  <x>x1</x>\n\t                  <y>y1</y>\n\t                  </b>\n\t                </a>\n\t        <a>\n   <b><x>x3</x></b>\n\t                <b><y>y4</y></b>\n\t</a>\n</root>";
/*     */ 
/* 343 */     XPathRecordReader rr = new XPathRecordReader("/root/a");
/* 344 */     rr.addField("x", "/root/a/b[@k='x']/x", true);
/* 345 */     rr.addField("y", "/root/a/b[@k='x']/y", true);
/* 346 */     List l = rr.getAllRecords(new StringReader(xml));
/* 347 */     assertEquals(2, l.size());
/* 348 */     assertEquals(1, ((List)((Map)l.get(0)).get("x")).size());
/* 349 */     assertEquals(1, ((List)((Map)l.get(0)).get("y")).size());
/* 350 */     assertEquals(0, ((Map)l.get(1)).size());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAttribValWithSlash() {
/* 355 */     String xml = "<root><b>\n  <a x=\"a/b\" h=\"hello-A\"/>  \n</b></root>";
/*     */ 
/* 358 */     XPathRecordReader rr = new XPathRecordReader("/root/b");
/* 359 */     rr.addField("x", "/root/b/a[@x='a/b']/@h", false);
/* 360 */     List l = rr.getAllRecords(new StringReader(xml));
/* 361 */     assertEquals(1, l.size());
/* 362 */     Map m = (Map)l.get(0);
/* 363 */     assertEquals("hello-A", m.get("x"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testUnsupported_Xpaths() {
/* 368 */     String xml = "<root><b><a x=\"a/b\" h=\"hello-A\"/>  </b></root>";
/* 369 */     XPathRecordReader rr = null;
/*     */     try {
/* 371 */       rr = new XPathRecordReader("//b");
/* 372 */       fail("A RuntimeException was expected: //b forEach cannot begin with '//'.");
/*     */     } catch (RuntimeException localRuntimeException) {
/*     */     }
/*     */     try {
/* 376 */       rr.addField("bold", "b", false);
/* 377 */       fail("A RuntimeException was expected: 'b' xpaths must begin with '/'.");
/*     */     } catch (RuntimeException localRuntimeException1) {
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAny_decendent_from_root() {
/* 384 */     XPathRecordReader rr = new XPathRecordReader("/anyd/contenido");
/* 385 */     rr.addField("descdend", "//boo", true);
/* 386 */     rr.addField("inr_descd", "//boo/i", false);
/* 387 */     rr.addField("cont", "/anyd/contenido", false);
/* 388 */     rr.addField("id", "/anyd/contenido/@id", false);
/* 389 */     rr.addField("status", "/anyd/status", false);
/* 390 */     rr.addField("title", "/anyd/contenido/titulo", false, 1);
/* 391 */     rr.addField("resume", "/anyd/contenido/resumen", false);
/* 392 */     rr.addField("text", "/anyd/contenido/texto", false);
/*     */ 
/* 394 */     String xml = "<anyd>\n  this <boo>top level</boo> is ignored because it is external to the forEach\n  <status>as is <boo>this element</boo></status>\n  <contenido id=\"10097\" idioma=\"cat\">\n    This one is <boo>not ignored as its</boo> inside a forEach\n    <antetitulo><i> big <boo>antler</boo></i></antetitulo>\n    <titulo>  My <i>flattened <boo>title</boo></i> </titulo>\n    <resumen> My summary <i>skip this!</i>  </resumen>\n    <texto>   <boo>Within the body of</boo>My text</texto>\n    <p>Access <boo>inner <i>sub clauses</i> as well</boo></p>\n    </contenido>\n</anyd>";
/*     */ 
/* 407 */     List l = rr.getAllRecords(new StringReader(xml));
/* 408 */     assertEquals(1, l.size());
/* 409 */     Map m = (Map)l.get(0);
/* 410 */     assertEquals("This one is  inside a forEach", m.get("cont").toString().trim());
/* 411 */     assertEquals("10097", m.get("id"));
/* 412 */     assertEquals("My flattened title", m.get("title").toString().trim());
/* 413 */     assertEquals("My summary", m.get("resume").toString().trim());
/* 414 */     assertEquals("My text", m.get("text").toString().trim());
/* 415 */     assertEquals("not ignored as its", (String)((List)m.get("descdend")).get(0));
/* 416 */     assertEquals("antler", (String)((List)m.get("descdend")).get(1));
/* 417 */     assertEquals("Within the body of", (String)((List)m.get("descdend")).get(2));
/* 418 */     assertEquals("inner  as well", (String)((List)m.get("descdend")).get(3));
/* 419 */     assertEquals("sub clauses", m.get("inr_descd").toString().trim());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAny_decendent_of_a_child1() {
/* 424 */     XPathRecordReader rr = new XPathRecordReader("/anycd");
/* 425 */     rr.addField("descdend", "/anycd//boo", true);
/*     */ 
/* 428 */     String xml = "<anycd>\n  this <boo>top level</boo> is ignored because it is external to the forEach\n  <status>as is <boo>this element</boo></status>\n  <contenido id=\"10097\" idioma=\"cat\">\n    This one is <boo>not ignored as its</boo> inside a forEach\n    <antetitulo><i> big <boo>antler</boo></i></antetitulo>\n    <titulo>  My <i>flattened <boo>title</boo></i> </titulo>\n    <resumen> My summary <i>skip this!</i>  </resumen>\n    <texto>   <boo>Within the body of</boo>My text</texto>\n    <p>Access <boo>inner <i>sub clauses</i> as well</boo></p>\n    </contenido>\n</anycd>";
/*     */ 
/* 441 */     List l = rr.getAllRecords(new StringReader(xml));
/* 442 */     assertEquals(1, l.size());
/* 443 */     Map m = (Map)l.get(0);
/* 444 */     assertEquals("top level", (String)((List)m.get("descdend")).get(0));
/* 445 */     assertEquals("this element", (String)((List)m.get("descdend")).get(1));
/* 446 */     assertEquals("not ignored as its", (String)((List)m.get("descdend")).get(2));
/* 447 */     assertEquals("antler", (String)((List)m.get("descdend")).get(3));
/* 448 */     assertEquals("title", (String)((List)m.get("descdend")).get(4));
/* 449 */     assertEquals("Within the body of", (String)((List)m.get("descdend")).get(5));
/* 450 */     assertEquals("inner  as well", (String)((List)m.get("descdend")).get(6));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAny_decendent_of_a_child2() {
/* 455 */     XPathRecordReader rr = new XPathRecordReader("/anycd");
/* 456 */     rr.addField("descdend", "/anycd/contenido//boo", true);
/*     */ 
/* 459 */     String xml = "<anycd>\n  this <boo>top level</boo> is ignored because it is external to the forEach\n  <status>as is <boo>this element</boo></status>\n  <contenido id=\"10097\" idioma=\"cat\">\n    This one is <boo>not ignored as its</boo> inside a forEach\n    <antetitulo><i> big <boo>antler</boo></i></antetitulo>\n    <titulo>  My <i>flattened <boo>title</boo></i> </titulo>\n    <resumen> My summary <i>skip this!</i>  </resumen>\n    <texto>   <boo>Within the body of</boo>My text</texto>\n    <p>Access <boo>inner <i>sub clauses</i> as well</boo></p>\n    </contenido>\n</anycd>";
/*     */ 
/* 472 */     List l = rr.getAllRecords(new StringReader(xml));
/* 473 */     assertEquals(1, l.size());
/* 474 */     Map m = (Map)l.get(0);
/* 475 */     assertEquals("not ignored as its", ((List)m.get("descdend")).get(0));
/* 476 */     assertEquals("antler", ((List)m.get("descdend")).get(1));
/* 477 */     assertEquals("title", ((List)m.get("descdend")).get(2));
/* 478 */     assertEquals("Within the body of", ((List)m.get("descdend")).get(3));
/* 479 */     assertEquals("inner  as well", ((List)m.get("descdend")).get(4));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testAnother() {
/* 484 */     String xml = "<root>\n       <contenido id=\"10097\" idioma=\"cat\">\n    <antetitulo></antetitulo>\n    <titulo>    This is my title             </titulo>\n    <resumen>   This is my summary           </resumen>\n    <texto>     This is the body of my text  </texto>\n    </contenido>\n</root>";
/*     */ 
/* 492 */     XPathRecordReader rr = new XPathRecordReader("/root/contenido");
/* 493 */     rr.addField("id", "/root/contenido/@id", false);
/* 494 */     rr.addField("title", "/root/contenido/titulo", false);
/* 495 */     rr.addField("resume", "/root/contenido/resumen", false);
/* 496 */     rr.addField("text", "/root/contenido/texto", false);
/*     */ 
/* 498 */     List l = rr.getAllRecords(new StringReader(xml));
/* 499 */     assertEquals(1, l.size());
/* 500 */     Map m = (Map)l.get(0);
/* 501 */     assertEquals("10097", m.get("id"));
/* 502 */     assertEquals("This is my title", m.get("title").toString().trim());
/* 503 */     assertEquals("This is my summary", m.get("resume").toString().trim());
/* 504 */     assertEquals("This is the body of my text", m.get("text").toString()
/* 505 */       .trim());
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testSameForEachAndXpath() {
/* 510 */     String xml = "<root>\n   <cat>\n     <name>hello</name>\n   </cat>\n   <item name=\"item name\"/>\n</root>";
/*     */ 
/* 516 */     XPathRecordReader rr = new XPathRecordReader("/root/cat/name");
/* 517 */     rr.addField("catName", "/root/cat/name", false);
/* 518 */     List l = rr.getAllRecords(new StringReader(xml));
/* 519 */     assertEquals("hello", ((Map)l.get(0)).get("catName"));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testPutNullTest() {
/* 524 */     String xml = "<root>\n  <i>\n    <x>\n      <a>A.1.1</a>\n      <b>B.1.1</b>\n    </x>\n    <x>\n      <b>B.1.2</b>\n      <c>C.1.2</c>\n    </x>\n  </i>\n  <i>\n    <x>\n      <a>A.2.1</a>\n      <c>C.2.1</c>\n    </x>\n    <x>\n      <b>B.2.2</b>\n      <c>C.2.2</c>\n    </x>\n  </i>\n</root>";
/*     */ 
/* 546 */     XPathRecordReader rr = new XPathRecordReader("/root/i");
/* 547 */     rr.addField("a", "/root/i/x/a", true);
/* 548 */     rr.addField("b", "/root/i/x/b", true);
/* 549 */     rr.addField("c", "/root/i/x/c", true);
/* 550 */     List l = rr.getAllRecords(new StringReader(xml));
/* 551 */     Map map = (Map)l.get(0);
/* 552 */     List a = (List)map.get("a");
/* 553 */     List b = (List)map.get("b");
/* 554 */     List c = (List)map.get("c");
/*     */ 
/* 556 */     assertEquals("A.1.1", (String)a.get(0));
/* 557 */     assertEquals("B.1.1", (String)b.get(0));
/* 558 */     assertNull(c.get(0));
/*     */ 
/* 560 */     assertNull(a.get(1));
/* 561 */     assertEquals("B.1.2", (String)b.get(1));
/* 562 */     assertEquals("C.1.2", (String)c.get(1));
/*     */ 
/* 564 */     map = (Map)l.get(1);
/* 565 */     a = (List)map.get("a");
/* 566 */     b = (List)map.get("b");
/* 567 */     c = (List)map.get("c");
/* 568 */     assertEquals("A.2.1", (String)a.get(0));
/* 569 */     assertNull(b.get(0));
/* 570 */     assertEquals("C.2.1", (String)c.get(0));
/*     */ 
/* 572 */     assertNull(a.get(1));
/* 573 */     assertEquals("B.2.2", (String)b.get(1));
/* 574 */     assertEquals("C.2.2", (String)c.get(1));
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void testError()
/*     */   {
/* 580 */     String malformedXml = "<root>\n    <node>\n        <id>1</id>\n        <desc>test1</desc>\n    </node>\n    <node>\n        <id>2</id>\n        <desc>test2</desc>\n    </node>\n    <node>\n        <id/>3</id>\n        <desc>test3</desc>\n    </node>\n</root>";
/*     */ 
/* 594 */     XPathRecordReader rr = new XPathRecordReader("/root/node");
/* 595 */     rr.addField("id", "/root/node/id", true);
/* 596 */     rr.addField("desc", "/root/node/desc", true);
/*     */     try {
/* 598 */       rr.getAllRecords(new StringReader(malformedXml));
/* 599 */       fail("A RuntimeException was expected: the input XML is invalid.");
/*     */     }
/*     */     catch (Exception localException)
/*     */     {
/*     */     }
/*     */   }
/*     */ }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.apache.solr.handler.dataimport.TestXPathRecordReader
 * JD-Core Version:    0.6.2
 */