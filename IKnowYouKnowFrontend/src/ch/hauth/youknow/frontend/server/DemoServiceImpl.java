package ch.hauth.youknow.frontend.server;

import static ch.hauth.util.data.Sequence.emptyIterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import ch.hauth.youknow.RandomIndexDescription;
import ch.hauth.youknow.frontend.client.DemoService;
import ch.hauth.youknow.frontend.shared.DemoResult;
import ch.hauth.youknow.frontend.shared.Identifier;
import ch.hauth.youknow.frontend.shared.Post;
import ch.hauth.youknow.frontend.shared.ResultInfo;
import ch.hauth.youknow.frontend.shared.Source;
import ch.hauth.youknow.ri.RandomIndex;
import ch.hauth.youknow.ri.RandomIndex.TopDocument;
import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.source.ContentCreator;
import ch.hauth.youknow.source.ContentSource;
import ch.hauth.youknow.source.Message;
import ch.hauth.youknow.source.ThreadedMessage;
import ch.hauth.youknow.source.ThreadedMessageStore;
import ch.hauth.youknow.source.terms.TermCollection;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class DemoServiceImpl extends RemoteServiceServlet implements DemoService {
	private ThreadedMessageStore store;
	private ContentCreator contentCreator;
	private Map<Source, RandomIndex> randomIndexes = new HashMap<Source, RandomIndex>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			this.randomIndexes.put(Source.AUTHORS, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.AUTHORS)));
			this.randomIndexes.put(Source.AUTHORS_WITHOUT_THREAD_STARTERS, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.AUTHORS_WITHOUT_THREAD_STARTERS)));
			this.randomIndexes.put(Source.THREADS, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.THREADS)));
			this.randomIndexes.put(Source.THREADS_WITHOUT_THREAD_STARTERS, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.THREADS_WITHOUT_THREAD_STARTERS)));
			this.randomIndexes.put(Source.MESSAGES, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.MESSAGES)));
			this.randomIndexes.put(Source.MESSAGES_WITHOUT_THREAD_STARTERS, new RandomIndex(new RandomIndexDescription(ContentSource.MESSAGES, ContentSource.MESSAGES_WITHOUT_THREAD_STARTERS)));

			this.contentCreator = new ContentCreator(TermCollection.getInstance());
			this.store = new ThreadedMessageStore();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void load() throws IllegalArgumentException {
	}

	@Override
	public DemoResult[] processPost(Source source, Post post) throws IllegalArgumentException {
		List<DemoResult> results = new ArrayList<DemoResult>();
		IHaveContent content = this.contentCreator.from(post.getMessage());
		TopDocument<String>[] entries = this.randomIndexes.get(source).topNDocuments(20, content);
		for (TopDocument<String> entry : entries) {
			results.add(new DemoResult(new Identifier(source, entry.getDocumentId()), entry.getScore()));
		}
		return results.toArray(new DemoResult[results.size()]);
	}

	@Override
	public ResultInfo[] search(Identifier identifier) throws IllegalArgumentException {
		Iterable<ThreadedMessage> messages = getMessages(identifier);
		List<ResultInfo> resultInfos = new ArrayList<ResultInfo>();
		for (Message message : messages) {
			ResultInfo resultInfo = new ResultInfo(message.getContent(), message.getTopic());
			resultInfos.add(resultInfo);
		}
		return resultInfos.toArray(new ResultInfo[resultInfos.size()]);
	}

	private Iterable<ThreadedMessage> getMessages(Identifier identifier) {
		switch (identifier.getSource()) {
		case AUTHORS:
			return this.store.getMessagesOfAuthor(identifier.getId());
		case AUTHORS_WITHOUT_THREAD_STARTERS:
			return this.store.getMessagesOfAuthorWithoutThreadStarter(identifier.getId());
		case THREADS:
			return this.store.getMessagesByThreadId(identifier.getId());
		case THREADS_WITHOUT_THREAD_STARTERS:
			return this.store.getMessagesByThreadIdWithoutThreadStarter(identifier.getId());
		case MESSAGES:
		case MESSAGES_WITHOUT_THREAD_STARTERS:
			return this.store.getMessageById(identifier.getId());
		}
		return emptyIterable();
	}
}
