package ch.hauth.youknow.source.terms;

import static ch.hauth.util.data.Sequence.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.hauth.util.data.IConvertTypes;
import ch.hauth.youknow.source.ThreadedMessage;
import ch.hauth.youknow.source.ThreadedMessageStore;


public class TermCollection implements Serializable {
	private static final long serialVersionUID = -2904077548690717966L;

	private static TermCollection TERM_COLLECTION = null;

	private transient TermExtractor termExtractor;
	private int documentCount;
	private final Map<String, Frequency> termCollectionFrequency;


	private TermCollection() {
		this.documentCount = 0;
		this.termExtractor = new TermExtractor();
		this.termCollectionFrequency = new HashMap<String, TermCollection.Frequency>();
	}

	public static TermCollection getInstance() {
		if (TERM_COLLECTION == null) {
			TERM_COLLECTION = new ThreadedMessageStore().getTermCollection();
		}
		return TERM_COLLECTION;
	}

	private void build(Iterable<String> documents) {
		for (String document : documents) {
			++this.documentCount;
			for (Entry<String, Integer> entry : getTermFrequencies(document).entrySet()) {
				Frequency frequency = this.termCollectionFrequency.get(entry.getKey());
				if (frequency == null) {
					frequency = new Frequency();
					this.termCollectionFrequency.put(entry.getKey(), frequency);
				}
				frequency.increaseCollectionFrequencyBy(entry.getValue());
				frequency.incrementDocumentFrequency();
			}
		}

		removeTermsWithTopNCollectionFrequency(1000);
	}

	private void removeTermsWithTopNCollectionFrequency(final int n) {
		List<Entry<String, Frequency>> sortedByCollectionFrequency = new ArrayList<Entry<String, Frequency>>(this.termCollectionFrequency.entrySet());
		Collections.sort(sortedByCollectionFrequency, new Comparator<Entry<String, Frequency>>() {
			@Override
			public int compare(Entry<String, Frequency> entry1, Entry<String, Frequency> entry2) {
				return Integer.valueOf(entry1.getValue().getCollectionFrequency()).compareTo(Integer.valueOf(entry2.getValue().getCollectionFrequency()));
			}
		});
		int startIndex = 0;
		if (sortedByCollectionFrequency.size() > n) {
			startIndex = sortedByCollectionFrequency.size() - n;
		}
		List<Entry<String, Frequency>> topN = sortedByCollectionFrequency.subList(startIndex, sortedByCollectionFrequency.size());
		for (Entry<String, Frequency> entry : topN) {
			this.termCollectionFrequency.remove(entry.getKey());
		}
	}

	public Map<String, Integer> getTermFrequencies(final String document) {
		Map<String, Integer> termFrequencies = new HashMap<String, Integer>();
		for (String term : this.termExtractor.from(document)) {
			Integer termFrequency = termFrequencies.get(term);
			if (termFrequency == null) {
				termFrequency = Integer.valueOf(0);
			}
			termFrequencies.put(term, termFrequency + 1);
		}
		return termFrequencies;
	}

	public int getDocumentCount() {
		return this.documentCount;
	}

	public int getCollectionFrequency(final String term) {
		return getFrequency(term).getCollectionFrequency();
	}

	public int getDocumentFrequency(final String term) {
		return getFrequency(term).getDocumentFrequency();
	}

	private Frequency getFrequency(final String term) {
		Frequency frequency = this.termCollectionFrequency.get(term);
		if (frequency == null) {
			frequency = new Frequency();
		}
		return frequency;
	}

	private static class Frequency implements Serializable {
		private static final long serialVersionUID = -6278674663891041515L;

		private int collectionFrequency = 0;
		private int documentFrequency = 0;

		public int getCollectionFrequency() {
			return this.collectionFrequency;
		}

		public void increaseCollectionFrequencyBy(final int count) {
			this.collectionFrequency += count;
		}

		public int getDocumentFrequency() {
			return this.documentFrequency;
		}

		public void incrementDocumentFrequency() {
			++this.documentFrequency;
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.termExtractor = new TermExtractor();
	}

	public static void main(String[] args) {
		ThreadedMessageStore store = new ThreadedMessageStore();
		Iterable<String> documents = map(store.getMessages(), new IConvertTypes<ThreadedMessage, String>() {
			@Override
			public String convert(ThreadedMessage original) {
				return original.getNewContent();
			}
		});

		TermCollection termCollection = new TermCollection();
		termCollection.build(documents);

		store.saveTermCollection(termCollection);
		System.out.println(termCollection.documentCount + " documents");
		System.out.println(termCollection.termCollectionFrequency.size() + " terms");
	}
}
