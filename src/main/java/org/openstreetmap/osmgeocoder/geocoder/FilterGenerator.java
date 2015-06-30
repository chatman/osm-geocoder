package org.openstreetmap.osmgeocoder.geocoder;

import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.openstreetmap.osmgeocoder.util.BloomFilter;

public class FilterGenerator
{
	void generate(HttpSolrServer server, String field, String filename)
			throws SolrServerException, IOException
			{
		generate(server, field, filename, 0);
			}
	void generate(HttpSolrServer server, String field, String filename, int minThreshold) throws SolrServerException, IOException {
		BloomFilter filter = new BloomFilter(10L);

		SolrQuery query = new SolrQuery();
		query.setParam("qt", new String[] { "/terms" });
		query.setParam("terms", true);
		query.setParam("terms.limit", new String[] { "-1" });
		query.setParam("terms.fl", new String[] { field });

		QueryResponse response = server.query(query);
		List<TermsResponse.Term> terms = response.getTermsResponse().getTerms(field);

		int counter = 0;
		for (TermsResponse.Term term : terms) {
			if ((minThreshold >= 0) && (term.getFrequency() >= minThreshold)) {
				filter.addWord(term.getTerm());
				counter++;
			}
		}

		filter.writeToFile(filename);

		System.out.println("Written " + filename + " with filter containing " + counter + " entries.");
	}

	public static void main(String[] args) throws SolrServerException, IOException {
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");
		HttpSolrServer placesServer = new HttpSolrServer("http://localhost:8983/solr/places");

		FilterGenerator fg = new FilterGenerator();
		fg.generate(server, "street", "streets.bf");
		fg.generate(server, "admin2", "admin2.bf");
		fg.generate(server, "admin4", "admin4.bf");
		fg.generate(server, "admin5", "admin5.bf");
		fg.generate(server, "admin6", "admin6.bf");
		fg.generate(server, "admin7", "admin7.bf");
		fg.generate(placesServer, "name_exact", "places.bf");
		fg.generate(placesServer, "category", "categories.bf", 10);

		BloomFilter bf = new BloomFilter("streets.bf");
		System.out.println(bf.wordExists("main"));
		System.out.println(bf.wordExists("lansdowne"));
		System.out.println(bf.wordExists("open street map"));
	}
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.geocoder.FilterGenerator
 * JD-Core Version:    0.6.2
 */