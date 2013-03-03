package ch.hauth.youknow.ri;

import java.util.HashMap;
import java.util.Map;

import ch.hauth.util.data.LeastRecentlyUsedCache;
import ch.hauth.youknow.Config;
import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.ri.content.IHaveContent;

public class WordContext {
	private final IDescribeARandomIndex indexDescription;
	private final String contextName;
	private final RandomIndexStore store;
	private final Map<String, Vector> cache;
	private final int minDocumentFrequency;

	public WordContext(final IDescribeARandomIndex indexDescription) {
		this.indexDescription = indexDescription;
		this.contextName = indexDescription.getWordContextSource();
		this.store = new RandomIndexStore();
		int cacheSize = Config.getInt("wordContextCacheSize");
		this.cache = new LeastRecentlyUsedCache<String, Vector>(cacheSize);
		this.minDocumentFrequency = Config.getInt("minDocumentFrequency");
	}

	public boolean isCreated() {
		return this.store.doesWordContextExist(this.contextName);
	}

	public void build() {
		if (isCreated()) {
			return;
		}

		Map<String, Vector> contexts = new HashMap<String, Vector>();
		for (IHaveContent content : this.indexDescription.getContentsForWordContext()) {
			Vector documentVector = RandomDocumentVectorCreator.create();
			for (String word : content.getTerms()) {
				if (content.getCollectionFrequency(word) < this.minDocumentFrequency) {
					continue;
				}

				Vector context = contexts.get(word);
				if (context == null) {
					context = Vector.emptyVector();
				}
				contexts.put(word, context.add(documentVector, TermScaler.scalingFactor(word, content)));
			}
		}
		this.store.setWordContext(this.contextName, contexts);
	}

	public Vector getWordContext(final String term) {
		Vector wordContext = this.cache.get(term);
		if (wordContext == null) {
			wordContext = this.store.getWordContext(this.contextName, term);
			if (wordContext != null) {
				this.cache.put(term, wordContext);
			} else {
				wordContext = Vector.emptyVector();
			}
		}
		return wordContext;
	}
}
