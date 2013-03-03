package ch.hauth.youknow.source.newsgroup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.hauth.youknow.source.Message;
import ch.hauth.youknow.source.ThreadedMessageStore;

public class Newsgroups {
	private static final Logger LOGGER = Logger.getLogger(Newsgroups.class);

	public static final String MESSAGE_ORIGIN = "message-origin";
	public static final String MESSAGE_ID = "message-id";
	public static final String MESSAGE_NEWSGROUPS = "newsgroups";
	public static final String AUTHOR = "from";
	public static final String TOPIC = "subject";
	public static final String CONTENT = "content";
	public static final String NEW_CONTENT = "new-content";
	public static final String DATE = "date";
	public static final String REFERENCES = "references";
	public static final String THREAD_ID = "thread-id";
	public static final String THREAD_MESSAGE_IDS = "thread-message-ids";
	public static final String THREAD_STARTER_MESSAGE_IDS = "thread-starter-message-ids";
	public static final String IS_FROM_THREAD_STARTER = "from-thread-starter";

	private final File mboxDirectory;

	public Newsgroups() {
		File homeDirectory = new File(System.getProperty("user.home"));
		this.mboxDirectory = new File(homeDirectory, "newsgroups");
	}

	public void storeMessages() {
		ThreadedMessageStore store = new ThreadedMessageStore();
		store.initializeMessageTable();
		ThreadedMessageBuilder messageBuilder = new ThreadedMessageBuilder();
		for (File file : newsgroupFiles(this.mboxDirectory)) {
			LOGGER.info("Processing " + file.getName());
			store.add(messageBuilder.from(readMBox(file)));
			LOGGER.info("Finished " + file.getName());
		}
	}

	private List<Message> readMBox(File file) {
		List<Message> messages = new ArrayList<Message>();
		for (MBoxMessage mboxMessage : MBoxReader.from(file)) {
			Message message = mboxMessage.toMessage(file.getName());
			if (message != null) {
				messages.add(message);
			}
		}
		return messages;
	}

	private File[] newsgroupFiles(File dir) {
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("de.") && !name.endsWith(".lock");
			}
		});
	}

	public static void main(String[] args) {
		new Newsgroups().storeMessages();
	}
}
