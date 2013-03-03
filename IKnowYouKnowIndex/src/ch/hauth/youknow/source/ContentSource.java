package ch.hauth.youknow.source;

import static ch.hauth.util.Strings.join;
import static ch.hauth.util.data.Sequence.flatmap;
import static ch.hauth.util.data.Sequence.map;

import java.util.ArrayList;
import java.util.List;

import ch.hauth.util.data.IConvertTypes;
import ch.hauth.util.data.StringPair;
import ch.hauth.youknow.ri.content.IHaveContentWithId;
import ch.hauth.youknow.source.terms.TermCollection;
import ch.hauth.youknow.source.terms.TermExtractor;

public enum ContentSource {
	WINDOWED_MESSAGES() {	// Only for word context
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return flatmap(STORE.getMessages(), new ThreadedMessageWindowedConverter(5));
		}
	},
	MESSAGES() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getMessages(), new ThreadedMessageConverter());
		}
	},
	AUTHORS() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getAuthorNewContents(), new StringPairConverter());
		}
	},
	THREADS() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getThreadNewContents(), new StringPairConverter());
		}
	},
	MESSAGES_WITHOUT_THREAD_STARTERS() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getMessagesWithoutThreadStarter(), new ThreadedMessageConverter());
		}
	},
	THREADS_WITHOUT_THREAD_STARTERS() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getThreadWithoutThreadStarterNewContents(), new StringPairConverter());
		}
	},
	AUTHORS_WITHOUT_THREAD_STARTERS() {
		@Override
		public Iterable<IHaveContentWithId> getContents() {
			return map(STORE.getAuthorWithoutThreadStarterNewContents(), new StringPairConverter());
		}
	};

	private static final ThreadedMessageStore STORE = new ThreadedMessageStore();

	public abstract Iterable<IHaveContentWithId> getContents();

	private static class ThreadedMessageWindowedConverter implements IConvertTypes<ThreadedMessage, Iterable<IHaveContentWithId>> {
		private static TermExtractor EXTRACTOR = new TermExtractor();

		private final int windowSize;

		public ThreadedMessageWindowedConverter(final int windowSize) {
			this.windowSize = windowSize;
		}

		@Override
		public Iterable<IHaveContentWithId> convert(ThreadedMessage original) {
			List<String> words = EXTRACTOR.from(original.getNewContent());
			if (words.size() < this.windowSize) {
				List<IHaveContentWithId> singleContent = new ArrayList<IHaveContentWithId>(1);
				singleContent.add(new ContentWithId(null, join(words, " "), TermCollection.getInstance()));
				return singleContent;
			}
			int slices = words.size() - this.windowSize + 1;
			List<IHaveContentWithId> contents = new ArrayList<IHaveContentWithId>(slices);
			for (int i = 0; i < slices; ++i) {
				contents.add(new ContentWithId(null, join(words.subList(i, i + this.windowSize), " "), TermCollection.getInstance()));
			}
			return contents;
		}
	}

	private static class ThreadedMessageConverter implements IConvertTypes<ThreadedMessage, IHaveContentWithId> {
		@Override
		public IHaveContentWithId convert(ThreadedMessage message) {
			return new ContentWithId(message.getMessageId(), message.getNewContent(), TermCollection.getInstance());
		}
	}

	private static class StringPairConverter implements IConvertTypes<StringPair, IHaveContentWithId> {
		@Override
		public IHaveContentWithId convert(StringPair pair) {
			return new ContentWithId(pair.getFirst(), pair.getSecond(), TermCollection.getInstance());
		}
	}
}
