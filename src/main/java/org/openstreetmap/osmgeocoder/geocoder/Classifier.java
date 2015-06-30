package org.openstreetmap.osmgeocoder.geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.openstreetmap.osmgeocoder.util.LuceneTokenizer;
import org.openstreetmap.osmgeocoder.util.OrderedChoiceIterable;

public class Classifier
{
	List<Filter> filters;

	public Classifier()
	{
		try
		{
			init();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	void init() throws IOException {
		this.filters = new ArrayList<Filter>();
		this.filters.add(new Filter("S", "streets.bf"));
		this.filters.add(new Filter("2", "admin2.bf"));
		this.filters.add(new Filter("4", "admin4.bf"));
		this.filters.add(new Filter("5", "admin5.bf"));
		this.filters.add(new Filter("6", "admin6.bf"));
		this.filters.add(new Filter("7", "admin7.bf"));
		this.filters.add(new Filter("C", "categories.bf"));
		this.filters.add(new Filter("P", "places.bf"));
	}

	public List<Classification> classify(List<String> queryTokens)
	{
		int cntTokens = queryTokens.size();
		int maxToken = cntTokens > 15 ? 15 : cntTokens;

		Integer[] tokenPositions = new Integer[maxToken];

		for (int i = 0; i < maxToken; i++) {
			tokenPositions[i] = Integer.valueOf(i);
		}
		List<Classification> classificationsList = new ArrayList<Classification>();

		OrderedChoiceIterable orderedChoiceIterable = new OrderedChoiceIterable(tokenPositions);
		for (Integer[] tokenPos : orderedChoiceIterable)
			if (tokenPos != null)
			{
				StringBuilder sb = new StringBuilder();
				TreeSet<Integer> tokenPositions1 = new TreeSet<Integer>();
				for (int k = 0; k < tokenPos.length; k++) {
					sb.append((String)queryTokens.get(tokenPos[k].intValue()) + " ");
					tokenPositions1.add(tokenPos[k]);
				}

				String searchTerm = sb.toString().trim();
				System.out.println("Term: " + searchTerm);
				if (searchTerm.length() != 0)
				{
					for (Filter f : this.filters)
						if (f.bf.wordExists(searchTerm)) {
							System.out.println(searchTerm + ": " + f.symbol);
							classificationsList.add(new Classification(f.symbol, searchTerm, Arrays.asList(tokenPos)));
						}
				}
			}
		return classificationsList;
	}

	public static void main(String[] args) throws IOException {
		String query = "hotel near civil lines, allahabad, india";

		LuceneTokenizer tokenizer = new LuceneTokenizer(new StandardAnalyzer(Version.LUCENE_43));

		List<String> tokens = tokenizer.getTokens(query);
		String[] arr = new String[tokens.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = ((String)tokens.get(i));
		}
		long start = System.currentTimeMillis();
		TokenStream sourceStream = new CustomizedTokenStream(arr);

		Map<String, String> a = new HashMap<String, String>();
		a.put("maxShingleSize", "15");
		ShingleFilterFactory factory = new ShingleFilterFactory(a);
		TokenStream stream = factory.create(sourceStream);
		System.out.println("Time: " + (start - System.currentTimeMillis()));

		OffsetAttribute offsetAttribute = (OffsetAttribute)stream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = (CharTermAttribute)stream.addAttribute(CharTermAttribute.class);
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (start - System.currentTimeMillis()));

		while (stream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();

			System.out.println(term);
		}
	}
}

