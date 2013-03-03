package ch.hauth.youknow.source;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import ch.hauth.youknow.Config;

public class ThreadedMessageIndex {
	static final String MESSAGE_ID = "messageId";
	public static final String CONTENT = "content";
	public static final String NEW_CONTENT = "newContent";

	private final Analyzer analyzer;
	private Directory directory;

	public ThreadedMessageIndex() throws IOException {
		analyzer = new StandardAnalyzer(Version.LUCENE_30);
		File indexFile = new File(Config.get("luceneIndexFile"));
		directory = FSDirectory.open(indexFile);
	}

	public ThreadedMessageSearcher getSearcher() throws IOException {
		return getSearcher(false);
	}

	public ThreadedMessageSearcher getSearcher(final boolean loadToRam) throws IOException {
		Directory dir = this.directory;
		if (loadToRam) {
			dir = new RAMDirectory(dir);
		}
		return new ThreadedMessageSearcher(dir, this.analyzer);
	}

	public void build(final Iterable<ThreadedMessage> messages) throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = new IndexWriter(this.directory, this.analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
		for (ThreadedMessage message : messages) {
			Document doc = new Document();
			doc.add(new Field(MESSAGE_ID, message.getMessageId(), Field.Store.YES, Field.Index.NO));
			doc.add(new Field(CONTENT, message.getContent(), Field.Store.NO, Field.Index.ANALYZED));
			doc.add(new Field(NEW_CONTENT, message.getNewContent(), Field.Store.NO, Field.Index.ANALYZED));
			writer.addDocument(doc);
		}
		writer.optimize();
		writer.close();
	}

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
		new ThreadedMessageIndex().build(new ThreadedMessageStore().getMessages());
	}
}
