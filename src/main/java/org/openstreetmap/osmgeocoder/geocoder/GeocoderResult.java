package org.openstreetmap.osmgeocoder.geocoder;

public class GeocoderResult
{
	private String admin2;
	private String admin4;
	private String admin5;
	private String admin6;
	private String admin7;
	private String street;
	private String geo;
	private String unmatched;
	private SolrDataStore.Permutation permutation;

	public String toString()
	{
		return this.permutation.annotation + ": 2=" + this.admin2 + ",4=" + this.admin4 + ",5=" + this.admin5 + ",6=" + this.admin6 + ",7=" + this.admin7 + ",street=" + this.street + ",unmatched=" + this.unmatched + ", geo=" + this.geo;
	}

	public String getAdmin2() {
		return this.admin2;
	}

	public void setAdmin2(String admin2) {
		this.admin2 = admin2;
	}

	public String getAdmin4() {
		return this.admin4;
	}

	public void setAdmin4(String admin4) {
		this.admin4 = admin4;
	}

	public String getAdmin5() {
		return this.admin5;
	}

	public void setAdmin5(String admin5) {
		this.admin5 = admin5;
	}

	public String getAdmin6() {
		return this.admin6;
	}

	public void setAdmin6(String admin6) {
		this.admin6 = admin6;
	}

	public String getAdmin7() {
		return this.admin7;
	}

	public void setAdmin7(String admin7) {
		this.admin7 = admin7;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getGeo() {
		return this.geo;
	}

	public void setGeo(String geo) {
		this.geo = geo;
	}

	public String getUnmatched() {
		return this.unmatched;
	}

	public void setUnmatched(String unmatched) {
		this.unmatched = unmatched;
	}

	public SolrDataStore.Permutation getPermutation() {
		return this.permutation;
	}

	public void setPermutation(SolrDataStore.Permutation permutation) {
		this.permutation = permutation;
	}
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.geocoder.GeocoderResult
 * JD-Core Version:    0.6.2
 */