package org.openstreetmap.osmgeocoder.geocoder;

import java.util.List;

public abstract interface DataStore
{
  public abstract List<GeocoderResult> process(List<String> paramList, List<Classification> paramList1);
}

/* Location:           /data/indexer-main.jar
 * Qualified Name:     org.openstreetmap.osmgeocoder.geocoder.DataStore
 * JD-Core Version:    0.6.2
 */