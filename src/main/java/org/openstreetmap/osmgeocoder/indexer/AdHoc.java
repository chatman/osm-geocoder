 package org.openstreetmap.osmgeocoder.indexer;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.mapdb.DB;
 import org.mapdb.DBMaker;
 
 public class AdHoc
 {
   public static void main(String[] args)
     throws Exception
   {
     Set poiKeys = new HashSet(Arrays.asList(new String[] { "leisure", "amenity", 
       "building", "craft", "man_made", "landuse", "natural", "railway", "shop", 
       "sport", "tourism", "aeroway" }));
 
     DB db = DBMaker.newFileDB(new File("asia_jdbm/asia"))
       .closeOnJvmShutdown()
       .encryptionEnable("password")
       .make();
 
     Map nodeStore = db.getTreeMap("nodeStore");
 
     int c = 0;
     int names = 0; int places = 0; int pois = 0;
 
     System.out.println(nodeStore.get("test"));
 
     db.commit();
     db.compact();
     db.close();
   }
 }

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.indexer.AdHoc
 * JD-Core Version:    0.6.2
 */