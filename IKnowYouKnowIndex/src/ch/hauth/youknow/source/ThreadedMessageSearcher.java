package ch.hauth.youknow.source;

import static ch.hauth.util.data.Sequence.emptyIterable;
import static ch.hauth.util.data.Sequence.toIterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import ch.hauth.util.data.ReadOnlyIterator;

public class ThreadedMessageSearcher {
	private static final Logger LOGGER = Logger.getLogger(ThreadedMessageSearcher.class);

	private final Analyzer analyzer;
	private final IndexSearcher searcher;

	public ThreadedMessageSearcher(final Directory directory, final Analyzer analyzer) throws IOException {
		this.analyzer = analyzer;
		this.searcher = new IndexSearcher(directory);
	}

	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public Iterable<String> searchContent(final String query) {
		return queryField(query, ThreadedMessageIndex.CONTENT);
	}

	public String[] searchContent(final String query, final int resultCount) {
		return queryField(query, ThreadedMessageIndex.CONTENT, resultCount);
	}

	public Iterable<String> searchNewContent(final String query) {
		return queryField(query, ThreadedMessageIndex.NEW_CONTENT);
	}

	public String[] searchNewContent(final String query, final int resultCount) {
		return queryField(query, ThreadedMessageIndex.NEW_CONTENT, resultCount);
	}

    public int docFrequency(final String term, final String field) {
        int resultCount;
        try {
        	resultCount = this.searcher.docFreq(new Term(field, term));
        } catch (IOException e) {
        	LOGGER.debug("Couldn't get document frequency for term \'" + term + "\':\n" + e);
        	resultCount = 0;
        }
        return resultCount;
    }

    public int documentCount() {
        return this.searcher.getIndexReader().numDocs();
    }

    private Iterable<String> queryField(final String queryString, final String defaultField) {
        int resultCount = docFrequency(queryString, defaultField);
        if (resultCount == 0) {
        	LOGGER.debug("No results for query \'" + queryString + "\' on field \'" + defaultField + "\'");
        	return emptyIterable();
        }
        return toIterable(new ScoreDocIterator(getScoreDocs(queryString, defaultField, resultCount)));
}

	private String[] queryField(final String queryString, final String defaultField, final int resultCount) {
        ScoreDoc[] scoreDocs = getScoreDocs(queryString, defaultField, resultCount);
        List<String> messageIds = new ArrayList<String>(scoreDocs.length);
        for (ScoreDoc scoreDoc : scoreDocs) {
        	Document document;
        	try {
        		document = this.searcher.doc(scoreDoc.doc);
        		messageIds.add(document.get(ThreadedMessageIndex.MESSAGE_ID));
        	} catch (CorruptIndexException e) {
        		LOGGER.error("Lucene index is corrupt:\n" + e);
        	} catch (IOException e) {
        		LOGGER.error("Couldn't read lucene index:\n" + e);
        	}
        }
        return messageIds.toArray(new String[messageIds.size()]);
	}

	private ScoreDoc[] getScoreDocs(String queryString, String defaultField, int resultCount) {
        QueryParser parser = new QueryParser(Version.LUCENE_30, defaultField, this.analyzer);
        try {
        	Query query = parser.parse(queryString);
        	TopDocs topDocs = this.searcher.search(query, resultCount);
        	ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        	return scoreDocs;
        } catch (ParseException e) {
        	LOGGER.debug("Couldn't parse query \'" + queryString + "\':\n" + e);
        	return new ScoreDoc[0];
        } catch (IOException e) {
        	LOGGER.debug("Couldn't read lucene index:\n" + e);
        	return new ScoreDoc[0];
        }
	}

	private class ScoreDocIterator extends ReadOnlyIterator<String> {
		private final ScoreDoc[] docs;
        private int pos = 0;
        private Document nextDoc = null;

        public ScoreDocIterator(final ScoreDoc[] docs) {
        	this.docs = docs;
		}

        @Override
        public boolean hasNext() {
        	if (this.docs.length > this.pos) {
        		if (this.nextDoc == null) {
        			ScoreDoc scoreDoc = this.docs[pos];
        			try {
        				this.nextDoc = searcher.doc(scoreDoc.doc);
        			} catch (CorruptIndexException e) {
        				LOGGER.error("Corrupt lucene index:\n" + e);
        				return false;
        			} catch (IOException e) {
        				LOGGER.error("Couldn't access lucene index:\n" + e);
        				return false;
        			}
        		}
        		return true;
        	}
        	return false;
        }

        @Override
        public String next() {
        	++pos;
        	String msg = nextDoc.get(ThreadedMessageIndex.MESSAGE_ID);
        	nextDoc = null;
        	return msg;
        }
	}
}