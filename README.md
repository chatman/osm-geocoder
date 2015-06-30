Indexing
--------
1. Download the planet file. For India, you can get it from GeoFabrik: http://download.geofabrik.de/asia/india-latest.osm.bz2

2. Split the file into separate files for nodes, ways and relations:

	bzcat india-latest.osm.bz2 | python osmsplitter.py

3. Start Solr in a separate terminal window:
	cd solr
	java -Xmx4G -jar start.jar

4. Build the project and start the indexing:
	mvn clean compile assembly:single
	java -cp target/osmgeocoder-0.0.1-SNAPSHOT-jar-with-dependencies.jar:. org.openstreetmap.osmgeocoder.indexer.IndexerMain india jdbms/india nodes.xml ways.xml relations.xml 

Searching
---------

TODO
