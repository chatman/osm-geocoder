package org.openstreetmap.osmgeocoder.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class LuceneTokenizer
{
	private Analyzer analyzer;
	private int maxTokensSize;

	public String[] tokenize(String query)
			throws IOException
			{
		List tokens = getTokens(query);
		String[] tokensArr = new String[tokens.size()];
		tokens.toArray(tokensArr);
		return tokensArr;
			}

	public List<String> getTokens(String query) throws IOException
	{
		query = query.replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\{", " ").replaceAll("\\}", " ");

		query = join(query);
		query = query.trim();

		TokenStream tokenStream = this.analyzer.tokenStream("", new StringReader(query));
		CharTermAttribute charTermAttr = (CharTermAttribute)tokenStream.addAttribute(CharTermAttribute.class);
		List tokens = new ArrayList();
		int cnt = 0;

		tokenStream.reset();
		while (tokenStream.incrementToken())
		{
			String term = charTermAttr.toString();
			String text = term;
			if (text != null)
			{
				tokens.add(term);
				cnt++;

				if (cnt >= this.maxTokensSize)
					break;
			}
		}
		return tokens;
	}

	private String join(String query) {
		StringBuilder builder = new StringBuilder();

		if ((query == null) || (query.length() <= 1)) {
			return query;
		}
		char oldchar = query.charAt(0);
		builder.append(oldchar);
		for (int i = 1; i < query.length(); i++) {
			char c = query.charAt(i);
			if ((Character.isLetterOrDigit(c)) || (Character.isWhitespace(c))) {
				builder.append(c);
				oldchar = c;
			} else if (c != oldchar) {
				builder.append(c);
				oldchar = c;
			}
		}

		return builder.toString();
	}

	public LuceneTokenizer(Analyzer analyzer)
	{
		this.analyzer = analyzer;
		this.maxTokensSize = 15;
	}
}