package ch.hauth.youknow.ri;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import ch.hauth.util.data.Pair;
import ch.hauth.youknow.Config;
import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.ri.content.IHaveContentWithId;

public class RandomIndex {
	private final IDescribeARandomIndex indexDescription;
	private final String indexName;
	private final RandomIndexStore store;
	private final WordContext wordContext;
	private final int clusterSize;

	public RandomIndex(final IDescribeARandomIndex indexDescription) {
		this(indexDescription, new RandomIndexStore(), new WordContext(indexDescription), Config.getInt("randomIndexClusterSize"));
	}

	public RandomIndex(final IDescribeARandomIndex indexDescription, final RandomIndexStore store, final WordContext wordContext, final int clusterSize) {
		this.indexDescription = indexDescription;
		this.indexName = indexDescription.getDocumentSource();
		this.store = store;
		this.wordContext = wordContext;
		this.clusterSize = clusterSize;
	}

	public Vector getDocumentContext(final IHaveContent content) {
		Vector documentVector = Vector.emptyVector();
		for (String word : content.getTerms()) {
			documentVector = documentVector.add(getWordContext(word), TermScaler.scalingFactor(word, content));
		}
		return documentVector;
	}

	public Vector getWordContext(String word) {
		return this.wordContext.getWordContext(word);
	}

	public void build() {
		this.store.clearRandomIndex(this.indexName, this.clusterSize);

		Map<String, Vector> updates = new HashMap<String, Vector>();
		for (IHaveContentWithId content : this.indexDescription.getContentsForDocuments()) {
			if (content.getTermCount() < 10) {
				continue;
			}
			updates.put(content.getId(), getDocumentContext(content));
			if (updates.size() == 1000) {
				this.store.updateRandomIndexClusterMeans(this.indexName, updates);
				updates.clear();
			}
		}
		if (!updates.isEmpty()) {
			this.store.updateRandomIndexClusterMeans(this.indexName, updates);
		}
	}

	public TopDocument<String>[] topNDocuments(int n, IHaveContent content) {
		Vector currentDocument = getDocumentContext(content);

		TopDocuments<String> topDocuments = new TopDocuments<String>(n);
		for (Pair<String, Vector> document : this.store.getClusterMeansDocumentVectors(this.indexName)) {
			double score = scoreDocument(document.getSecond(), currentDocument);
			if (score > 1.0f) {
				score = scoreDocument(document.getSecond(), currentDocument);
			}
			topDocuments.add(document.getFirst(), score);
		}
		return topDocuments.getDocuments();
	}

	private double scoreDocument(Vector v1, Vector v2) {
		float denominator = v1.abs() * v2.abs();
		if (denominator == 0.0f) {
			return 0.0f;
		}
		return v1.crossProduct(v2) / denominator;
	}

	public class TopDocument<T> implements Comparable<TopDocument<T>> {
		private final double score;
		private final T documentId;

		public TopDocument(T documentId, double score) {
			this.documentId = documentId;
			this.score = score;
		}

		@Override
		public int compareTo(TopDocument<T> other) {
			return Double.compare(this.score, other.score);
		}

		public T getDocumentId() {
			return this.documentId;
		}

		public double getScore() {
			return this.score;
		}
	}

	private class TopDocuments<T> {
		private final int count;
		private final LinkedList<TopDocument<T>> top;

		public TopDocuments(int count) {
			this.count = count;
			this.top = new LinkedList<TopDocument<T>>();
		}

		public void add(T documentId, double documentScore) {
			TopDocument<T> topDocument = new TopDocument<T>(documentId, documentScore);
			if (this.top.size() == this.count) {
				if (this.top.getLast().compareTo(topDocument) > 0) {
					return;
				}
				this.top.removeLast();
			}
			Iterator<TopDocument<T>> iterator = this.top.iterator();
			int i;
			for (i = 0; i < this.top.size(); i++, iterator.hasNext()) {
				TopDocument<T> other = iterator.next();
				if (other.compareTo(topDocument) <= 0) {
					break;
				}
			}
			this.top.add(i, topDocument);
		}

		@SuppressWarnings("unchecked")
		public TopDocument<T>[] getDocuments() {
			return this.top.toArray(new TopDocument[this.top.size()]);
		}
	}
}
