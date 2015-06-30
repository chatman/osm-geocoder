package org.openstreetmap.osmgeocoder.geocoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.openstreetmap.osmgeocoder.indexer.StreetIndexer;
import org.openstreetmap.osmgeocoder.util.AllCombinationIteratable;

public class SolrDataStore
implements DataStore
{
	Set<String> validPermutations = new LinkedHashSet<String>(
			Arrays.asList(new String[] { 
					"2", "4", "5", "6", "7", "42", "52", "62", "54", "64", "74", "65", "75", "76", "542", "642", "742", "652", "752", "654", "765", 
					"S2", "S4", "S5", "S6", "S7", "S42", "S52", "S62", "S54", "S64", "S74", "S65", "S75", "S76", "S542", "S642", "S742", "S652", "S752", "S654", "S765", 
					"X2", "X4", "X5", "X6", "X7", "X42", "X52", "X62", "X54", "X64", "X74", "X65", "X75", "X76", "X542", "X642", "X742", "X652", "X752", "X654", "X765", 
					"XS2", "XS4", "XS5", "XS6", "XS7", "XS42", "XS52", "XS62", "XS54", "XS64", "XS74", "XS65", "XS75", "XS76", "XS542", "XS642", "XS742", "XS652", "XS752", "XS654", "XS765", 
					"X", "S" }));

	HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr");

	public List<GeocoderResult> process(List<String> queryTokens, List<Classification> classifications) {
		LinkedHashMap<List<Integer>, List<Classification>> map = new LinkedHashMap<List<Integer>, List<Classification>>();
		for (Classification c : classifications) {
			List<Classification> values = map.containsKey(c.tokenPositions) ? map.get(c.tokenPositions) : new ArrayList<Classification>();
			values.add(c);
			map.put(c.tokenPositions, values);
		}
		System.out.println("Map: " + map);

		List<Classification[]> matrix = new ArrayList<Classification[]>();
		for (List<Integer> key: map.keySet()) {
			List<Classification> list = map.get(key);
			list.add(null);
			Classification[] arr = new Classification[list.size()];
			arr = (Classification[])list.toArray(arr);
			matrix.add(arr);
		}

		Classification[] combination = new Classification[map.size()];
		AllCombinationIteratable<Classification> iterator = new AllCombinationIteratable<Classification>(matrix, combination);

		List<Permutation> perms = new ArrayList<Permutation>();

		while (iterator.hasNext()) {
			combination = (Classification[])iterator.next();

			int counter = 0;
			Set<Integer> tokenSet = new LinkedHashSet<Integer>();
			for (Classification c : combination) {
				if (c != null) {
					counter += c.tokenPositions.size();
					tokenSet.addAll(c.tokenPositions);
				}
			}
			if (counter == tokenSet.size()) {
				Permutation perm = new Permutation(combination, queryTokens);
				perms.add(perm);
			}
		}

		int discarded = 0;

		for (int i = 0; i < perms.size(); i++) {
			Permutation p = (Permutation)perms.get(i);
			boolean discard = false;
			if (p.shortAnnotation.length() > 2) {
				for (int j = 1; j < p.shortAnnotation.length() - 1; j++)
					if (p.shortAnnotation.charAt(j) == '.') {
						discard = true; break;
					}
				if ((p.shortAnnotation.startsWith(".")) && 
						(p.shortAnnotation.endsWith("."))) {
					discard = true;
				}

			}

			if ((!discard) && (!this.validPermutations.contains(p.poisCondensedAnnotation)))
			{
				discard = true;
			}

			if ((p.annotation.contains("S")) && (!p.containsStreetPrefix) && (!p.containsStreetType)) {
				discard = true;
			}
			if (discard) {
				perms.remove(i);
				i--;
				discarded++;
			}

		}

		System.out.println("Discarded: " + discarded);
		System.out.println("Remaining permutations: " + perms.size());
		Collections.sort(perms);

		for (int i = 0; i < perms.size(); i++) {
			boolean found = false;
			for (int j = 0; j < i; j++) {
				Permutation prev = (Permutation)perms.get(j);
				Permutation current = (Permutation)perms.get(i);

				if (current.poisCondensedAnnotation.equals(prev.poisCondensedAnnotation))
				{
					if (current.annotation.replaceAll("C", ".").replaceAll("P", ".")
							.equals(prev.annotation.replaceAll("C", ".").replaceAll("P", ".")))
						found = true; 
				}
			}
			if (found) {
				perms.remove(i);
				i--;
			}
		}

		long startTime = System.currentTimeMillis();
		List<Permutation> validPerms = new ArrayList<Permutation>();
		List<GeocoderResult> results = new ArrayList<GeocoderResult>();

		int validCounter = 0;
		for (Permutation p : perms) {
			boolean isValid = isValid(this.server, p);
			if (isValid) {
				validCounter++;
				validPerms.add(p);
				results.add(geocode(this.server, p));
			}
		}

		System.out.println("Validation time: " + (System.currentTimeMillis() - startTime));

		System.out.println("Valid counter: " + validCounter);
		System.out.println("Valid: " + validPerms.size());
		System.out.println("Results: " + results.size());

		return results;
	}

	boolean isValid(HttpSolrServer server, Permutation p)
	{
		StringBuilder queryStr = new StringBuilder();

		int level = 0;
		for (Classification c : p.classifications) {
			if (c != null) {
				String fld = getField(c.classification);
				if (fld != null) {
					queryStr.append("+" + fld + ":\"" + c.text + "\" ");
				}

				if (Character.isDigit(c.classification.charAt(0)))
					level = Math.max(level, c.classification.charAt(0) - '0');
				if (c.classification.charAt(0) == 'S') {
					level = 20;
				}
			}
		}

		queryStr.append("+level:" + level + " ");

		SolrQuery query = new SolrQuery();
		query.setRows(Integer.valueOf(0));
		query.setQuery(queryStr.toString());

		QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return false;
		}

		if (response.getResults().getNumFound() > 0L) {
			return true;
		}
		return false;
	}

	GeocoderResult geocode(HttpSolrServer server, Permutation p)
	{
		StringBuilder queryStr = new StringBuilder();

		int level = 0;
		for (Classification c : p.classifications) {
			if (c != null) {
				String fld = getField(c.classification);
				if (fld != null) {
					queryStr.append("+" + fld + ":\"" + c.text + "\" ");
				}

				if (Character.isDigit(c.classification.charAt(0)))
					level = Math.max(level, c.classification.charAt(0) - '0');
				if (c.classification.charAt(0) == 'S') {
					level = 20;
				}
			}
		}

		queryStr.append("+level:" + level + " ");

		SolrQuery query = new SolrQuery();
		query.setRows(Integer.valueOf(1));
		query.setQuery(queryStr.toString());

		QueryResponse response = null;
		try {
			response = server.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return null;
		}

		GeocoderResult result = new GeocoderResult();
		if (response.getResults().getNumFound() >= 1L) {
			System.out.println("From SOLR: " + response);
			SolrDocument doc = (SolrDocument)response.getResults().get(0);
			result.setAdmin2((String)doc.getFirstValue("admin2"));
			result.setAdmin4((String)doc.getFirstValue("admin4"));
			result.setAdmin5((String)doc.getFirstValue("admin5"));
			result.setAdmin6((String)doc.getFirstValue("admin6"));
			result.setAdmin7((String)doc.getFirstValue("admin7"));

			String docLevel = doc.containsKey("level") ? doc.getFirstValue("level").toString() : null;
			if (docLevel.equals("20"))
				result.setStreet((String)doc.getFirstValue("name"));
			result.setGeo((String)doc.getFirstValue("geo"));

			StringBuilder unmatched = new StringBuilder();
			for (int i = 0; i < p.annotation.length(); i++) {
				char c = p.annotation.charAt(i);
				if ((c == '.') || (c == 'P') || (c == 'C'))
					unmatched.append((String)p.queryTokens.get(i) + " ");
			}
			result.setPermutation(p);

			result.setUnmatched(unmatched.toString().trim());
		}

		return result;
	}

	private String getField(String classificationSymbol)
	{
		if (Character.isDigit(classificationSymbol.charAt(0)))
			return "admin" + classificationSymbol.charAt(0);
		if (classificationSymbol.charAt(0) == 'S')
			return "street";
		return null;
	}

	class Permutation
	implements Comparable<Permutation>
	{
		Classification[] classifications;
		List<String> queryTokens;
		String annotation;
		String shortAnnotation;
		String poisCondensedAnnotation;
		boolean containsStreetType = false;
		boolean containsStreetPrefix = false;

		public Permutation(Classification[] classifications, List<String> queryTokens) {
			this.classifications = classifications;
			this.queryTokens = queryTokens;

			char[] symbols = new char[queryTokens.size()];
			for (int i = 0; i < symbols.length; i++)
				symbols[i] = '.';
			for (Classification c : classifications)
				if (c != null)
					for (int pos: c.tokenPositions)
						symbols[pos] = c.classification.charAt(0);

			if (symbols.length > 1) {
				for (int i = 0; i < symbols.length - 1; i++) {
					if ((symbols[i] == '.') && (symbols[(i + 1)] == 'S') && (isStreetPrefix((String)queryTokens.get(i))))
					{
						boolean streetsBefore = false;
						for (int j = i; j >= 0; j--)
							if (symbols[j] == 'S')
								streetsBefore = true;
						if (!streetsBefore) {
							this.containsStreetPrefix = true;
							symbols[i] = 'S';
						}
					} else if ((symbols[i] == 'S') && (symbols[(i + 1)] == '.') && (isStreetType((String)queryTokens.get(i + 1)))) {
						boolean streetsAfter = false;
						for (int j = i + 1; j < symbols.length; j++)
							if (symbols[j] == 'S')
								streetsAfter = true;
						if (!streetsAfter) {
							this.containsStreetType = true;
							symbols[(i + 1)] = 'S';
						}
					}
				}
			}

			this.annotation = new String(symbols);

			StringBuilder sb = new StringBuilder();
			for (char c : symbols) {
				if ((sb.length() == 0) || (sb.charAt(sb.length() - 1) != c)) {
					sb.append(c);
				}
			}
			this.shortAnnotation = sb.toString();

			this.poisCondensedAnnotation = sb.toString().replaceAll("\\.", "X").replaceAll("C", "X").replaceAll("P", "X").replaceAll("XX", "X");
		}

		private boolean isStreetPrefix(String token)
		{
			return Pattern.matches("[0-9]+", token);
		}

		private boolean isStreetType(String token)
		{
			return StreetIndexer.acceptedTypes.contains(token);
		}

		public String toString()
		{
			return this.annotation + "(" + this.shortAnnotation + "," + this.poisCondensedAnnotation + ")";
		}

		public int compareTo(Permutation o) {
			int targetUnknowns = 0; int sourceUnknowns = 0;
			for (char c : o.annotation.toCharArray())
				if (c == '.')
					targetUnknowns++;
			for (char c : this.annotation.toCharArray())
				if (c == '.')
					sourceUnknowns++;
			return sourceUnknowns - targetUnknowns;
		}
	}
}
