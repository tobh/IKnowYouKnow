package ch.hauth.youknow.ri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.hauth.youknow.ri.content.IHaveContentWithId;

public class TestContent implements IHaveContentWithId {
	private final String id;
	private final Map<String, Integer> words;

	public TestContent(String id) {
		this.id = id;
		this.words = new HashMap<String, Integer>();
	}

	@Override
	public Set<String> getTerms() {
		return this.words.keySet();
	}

	public void addContext(String word, int count) {
		this.words.put(word, count);
	}

	@Override
	public int getDocumentCount() {
		return this.words.size();
	}

	@Override
	public int getCollectionFrequency(String term) {
		return getTermFrequency(term);
	}

	@Override
	public int getDocumentFrequency(String term) {
		return getDocumentCount();
	}

	@Override
	public int getTermFrequency(String term) {
		Integer wordCount = this.words.get(term);
		if (wordCount == null) {
			return 0;
		}
		return wordCount;
	}

	@Override
	public int getTermCount() {
		int count = 0;
		for (Integer wordCount : this.words.values()) {
			count += wordCount;
		}
		return count;
	}

	@Override
	public String getId() {
		return this.id;
	}
}
