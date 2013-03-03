package ch.hauth.youknow;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.hauth.youknow.ri.IDescribeARandomIndex;
import ch.hauth.youknow.ri.RandomIndex;
import ch.hauth.youknow.ri.WordContext;
import ch.hauth.youknow.source.ContentSource;

public class RandomIndexer {
	private static final Logger LOGGER = Logger.getLogger(RandomIndexer.class);

	public static void build(final IDescribeARandomIndex indexDescription) {
		LOGGER.info("Creating word context...");
		new WordContext(indexDescription).build();
		LOGGER.info("Creating random index...");
		new RandomIndex(indexDescription).build();
		LOGGER.info("Finished");
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		LOGGER.info("Building author index (without thread starter messages) ...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.AUTHORS_WITHOUT_THREAD_STARTERS));
		LOGGER.info("Building author index...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.AUTHORS));
		LOGGER.info("Building thread index (without thread starter messages) ...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.THREADS_WITHOUT_THREAD_STARTERS));
		LOGGER.info("Building thread index...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.THREADS));
		LOGGER.info("Building message index (without thread starter messages) ...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.MESSAGES_WITHOUT_THREAD_STARTERS));
		LOGGER.info("Building message index...");
		build(new RandomIndexDescription(ContentSource.WINDOWED_MESSAGES, ContentSource.MESSAGES));
		LOGGER.info("Random indexing process finished.");
	}
}
