package ch.hauth.youknow.ri.content;

public interface IHaveTermStats {
	public int getTermFrequency(final String term);
	public int getCollectionFrequency(final String term);
	public int getDocumentFrequency(final String term);
	public int getDocumentCount();
}
