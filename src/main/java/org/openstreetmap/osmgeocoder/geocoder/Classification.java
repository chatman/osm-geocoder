package org.openstreetmap.osmgeocoder.geocoder;

import java.util.List;

public class Classification
{
	List<Integer> tokenPositions;
	String text;
	String classification;

	public Classification(String classification, String text, List<Integer> tokens)
	{
		this.tokenPositions = tokens;
		this.text = text;
		this.classification = classification;
	}

	public String toString()
	{
		return this.tokenPositions + "=" + this.classification;
	}
}
