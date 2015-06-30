package org.openstreetmap.osmgeocoder.service;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.openstreetmap.osmgeocoder.geocoder.Classification;
import org.openstreetmap.osmgeocoder.geocoder.Classifier;
import org.openstreetmap.osmgeocoder.geocoder.DataStore;
import org.openstreetmap.osmgeocoder.geocoder.GeocoderResult;
import org.openstreetmap.osmgeocoder.geocoder.SolrDataStore;
import org.openstreetmap.osmgeocoder.util.LuceneTokenizer;

public class Geocoder
{
	static Classifier c = new Classifier();
	static DataStore datastore = new SolrDataStore();

	public static void main(String[] args)
			throws IOException
			{
		String query = "northern ireland";

		LuceneTokenizer tokenizer = new LuceneTokenizer(new StandardAnalyzer(Version.LUCENE_43));
		List<String> queryTokens = tokenizer.getTokens(query);

		long startTime = System.currentTimeMillis();

		List<Classification> classifications = c.classify(queryTokens);
		System.out.println("Classification time: " + (System.currentTimeMillis() - startTime));
		System.out.println(query);
		System.out.println(classifications);

		startTime = System.currentTimeMillis();
		List<GeocoderResult> results = datastore.process(queryTokens, classifications);
		System.out.println("Processing time: " + (System.currentTimeMillis() - startTime));

		for (GeocoderResult result : results)
			System.out.println(result);
			}
}
