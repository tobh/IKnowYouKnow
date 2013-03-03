package ch.hauth.youknow.source;

import java.util.Map;
import java.util.Set;

import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.source.terms.TermCollection;

public class Content implements IHaveContent {
	private final TermCollection termCollection;
	private final Map<String, Integer> termFrequencies;

	public Content(final String text, final TermCollection termCollection) {
		this.termCollection = termCollection;
		this.termFrequencies = this.termCollection.getTermFrequencies(text);
	}

	@Override
	public Set<String> getTerms() {
		return this.termFrequencies.keySet();
	}

	@Override
	public int getTermCount() {
		return this.termFrequencies.size();
	}

	@Override
	public int getTermFrequency(final String term) {
		Integer frequency = this.termFrequencies.get(term);
		if (frequency == null) {
			frequency = Integer.valueOf(0);
		}
		return frequency;
	}

	@Override
	public int getCollectionFrequency(final String term) {
		return this.termCollection.getCollectionFrequency(term);
	}

	@Override
	public int getDocumentFrequency(final String term) {
		return this.termCollection.getDocumentFrequency(term);
	}

	@Override
	public int getDocumentCount() {
		return this.termCollection.getDocumentCount();
	}
}
