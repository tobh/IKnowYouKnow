package ch.hauth.youknow.source;

import static ch.hauth.util.data.Sequence.map;
import static ch.hauth.util.data.Sequence.toList;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.hauth.util.data.IConvertTypes;
import ch.hauth.util.data.StringPair;
import ch.hauth.util.sql.IConvertResultSetsToConcreteTypes;
import ch.hauth.util.sql.MysqlStorage;
import ch.hauth.util.sql.ResultSetCursor;
import ch.hauth.util.sql.ResultSetToStringConverter;
import ch.hauth.youknow.Config;
import ch.hauth.youknow.source.terms.TermCollection;

public class ThreadedMessageStore extends MysqlStorage {
	private static final Logger LOGGER = Logger.getLogger(ThreadedMessageStore.class);

	private static final String MESSAGE_ID_COLUMN = "message_id";
	private static final String ORIGINS_COLUMN = "origins";
	private static final String AUTHOR_COLUMN = "author";
	private static final String TOPIC_COLUMN = "topic";
	private static final String CONTENT_COLUMN = "content";
	private static final String NEW_CONTENT_COLUMN = "new_content";
	private static final String DATE_COLUMN = "date";
	private static final String REFERENCE_IDS_COLUMN = "reference_id";
	private static final String THREAD_ID_COLUMN = "thread_id";

	private static final String NEW_CONTENT_KEY_COLUMN = "new_content_key";

	private static final String TERM_COLLECTION_COLUMN = "term_collection";

	private static final String COLLECTION_SEPARATOR = " ";

	private final String messageTable;
	private final String messagesWithoutThreadStarterTable;
	private final String authorNewContentTable;
	private final String authorWithoutThreadStarterNewContentTable;
	private final String threadNewContentTable;
	private final String threadWithoutThreadStarterNewContentTable;
	private final String termCollectionTable;

	public ThreadedMessageStore() {
		super(Config.get("messageStoreUrl"),
			  Config.get("messageStoreDb"),
			  Config.get("mysqlUser"),
			  Config.get("mysqlPassword"));

		this.messageTable = Config.get("messageTable");
		this.messagesWithoutThreadStarterTable = Config.get("messagesWithoutThreadStarterTable");
		this.authorNewContentTable = Config.get("authorNewContentTable");
		this.authorWithoutThreadStarterNewContentTable = Config.get("authorWithoutThreadStarterNewContentTable");
		this.threadNewContentTable = Config.get("threadNewContentTable");
		this.threadWithoutThreadStarterNewContentTable = Config.get("threadWithoutThreadStarterNewContentTable");
		this.termCollectionTable = Config.get("termCollectionTable");
	}

	private void buildMessagesWithoutThreadStarterTable() {
		String table = this.messagesWithoutThreadStarterTable;
		createMessageTable(table);

		Set<ThreadedMessage> notFromThreadStarterMessages = new HashSet<ThreadedMessage>();
		for (ThreadedMessagePack pack : getThreadMessagePacks(this.messageTable)) {
			List<ThreadedMessage> messages = pack.getMessageTimeline();
			ThreadedMessage firstMessage = messages.get(0);
			if (!firstMessage.getReferences().isEmpty()) {
				continue;
			}
			String authorOfFirstMessage = firstMessage.getAuthor();
			for (ThreadedMessage message : messages) {
				if (!message.getAuthor().equals(authorOfFirstMessage)) {
					notFromThreadStarterMessages.add(message);
				}
			}
			if (notFromThreadStarterMessages.size() > 10000) {
				saveMessages(table, notFromThreadStarterMessages);
				notFromThreadStarterMessages.clear();
			}
		}
		saveMessages(table, notFromThreadStarterMessages);
	}

	private void buildAuthorsNewContentTable() {
		String originTable = this.messageTable;
		buildNewContentTable(this.authorNewContentTable, originTable, AUTHOR_COLUMN, toList(getAuthors(originTable)));
	}

	private void buildAuthorsWithoutThreadStarterNewContentTable() {
		String originTable = this.messagesWithoutThreadStarterTable;
		buildNewContentTable(this.authorWithoutThreadStarterNewContentTable, originTable, AUTHOR_COLUMN, toList(getAuthors(originTable)));
	}

	private void buildThreadsNewContentTable() {
		String originTable = this.messageTable;
		buildNewContentTable(this.threadNewContentTable, originTable, THREAD_ID_COLUMN, toList(getThreadIds(originTable)));
	}

	private void buildThreadsWithoutThreadStarterNewContentTable() {
		String originTable = this.messagesWithoutThreadStarterTable;
		buildNewContentTable(this.threadWithoutThreadStarterNewContentTable, originTable, THREAD_ID_COLUMN, toList(getThreadIds(originTable)));
	}

	private void buildNewContentTable(final String targetTable, final String originTable, final String keyColumn, final List<String> keys) {
		dropTable(targetTable);
		createTable(targetTable, "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY",
								NEW_CONTENT_KEY_COLUMN + " VARCHAR(" + VARCHAR_INDEX_LENGTH + ") " + UTF8_CHARSET,
								NEW_CONTENT_COLUMN + " LONGBLOB",
								"INDEX (" + NEW_CONTENT_KEY_COLUMN + ")");

		List<ThreadedMessagePack> packs = new ArrayList<ThreadedMessagePack>(1000);
		for (ThreadedMessagePack pack : getMessagePack(originTable, keyColumn, keys)) {
			packs.add(pack);
			if (packs.size() == 1000) {
				saveNewContents(targetTable, packs);
				packs.clear();
			}
		}
		saveNewContents(targetTable, packs);
	}

	public Iterable<ThreadedMessage> getMessages() {
		return getMessages(this.messageTable);
	}

	public Iterable<ThreadedMessage> getMessagesWithoutThreadStarter() {
		return getMessages(this.messagesWithoutThreadStarterTable);
	}

	private Iterable<ThreadedMessage> getMessages(final String table) {
		return getMessagesWhere(table, "", new String[0]);
	}

	public Iterable<StringPair> getAuthorNewContents() {
		return getNewContents(this.authorNewContentTable);
	}

	public Iterable<StringPair> getAuthorWithoutThreadStarterNewContents() {
		return getNewContents(this.authorWithoutThreadStarterNewContentTable);
	}

	public Iterable<StringPair> getThreadNewContents() {
		return getNewContents(this.threadNewContentTable);
	}

	public Iterable<StringPair> getThreadWithoutThreadStarterNewContents() {
		return getNewContents(this.threadWithoutThreadStarterNewContentTable);
	}

	private Iterable<StringPair> getNewContents(final String table) {
		return new ResultSetCursor<StringPair>(this, sqlStringToIterateOverAllEntries(table), new NewContentResultSetHandler() , 1000);
	}

	public Iterable<ThreadedMessage> getMessagesOfAuthor(final String author) {
		return getMessagesOfAuthor(this.messageTable, author);
	}

	public Iterable<ThreadedMessage> getMessagesOfAuthorWithoutThreadStarter(final String author) {
		return getMessagesOfAuthor(this.messagesWithoutThreadStarterTable, author);
	}

	private Iterable<ThreadedMessage> getMessagesOfAuthor(final String table, final String author) {
		return getMessagesWhere(table, AUTHOR_COLUMN + " = ?", new String[] {author});
	}

	public Iterable<ThreadedMessage> getMessageById(String messageId) {
		return getMessagesWhere(this.messageTable, MESSAGE_ID_COLUMN + " = ?", new String[] {messageId});
	}

	public Iterable<ThreadedMessage> getMessagesByThreadId(final String threadId) {
		return getMessagesByThreadId(this.messageTable, threadId);
	}

	public Iterable<ThreadedMessage> getMessagesByThreadIdWithoutThreadStarter(final String threadId) {
		return getMessagesByThreadId(this.messagesWithoutThreadStarterTable, threadId);
	}

	private Iterable<ThreadedMessage> getMessagesByThreadId(final String table, String threadId) {
		return getMessagesWhere(table, THREAD_ID_COLUMN + " = ?", new String[] {threadId});
	}

	private Iterable<ThreadedMessage> getMessagesWhere(final String table, final String whereStatementPart, String[] whereStatementArguments) {
		String sql;
		if (whereStatementArguments.length == 0) {
			return new ResultSetCursor<ThreadedMessage>(this, sqlStringToIterateOverAllEntries(table), new ThreadedMessageResultSetHandler(), 10000);
		} else {
			sql = "SELECT * FROM " + table + " WHERE " + whereStatementPart + " LIMIT ?, ?";
			return new ResultSetCursor<ThreadedMessage>(this, sql, whereStatementArguments, new ThreadedMessageResultSetHandler(), 10000);
		}
	}

	public Iterable<ThreadedMessagePack> getAuthorMessagePacks() {
		return getAuthorMessagePacks(this.messageTable);
	}

	public Iterable<ThreadedMessagePack> getAuthorMessagePacksWithoutThreadStarter() {
		return getAuthorMessagePacks(this.messagesWithoutThreadStarterTable);
	}

	private Iterable<ThreadedMessagePack> getAuthorMessagePacks(final String table) {
		return getMessagePack(table, AUTHOR_COLUMN, getAuthors(table));
	}

	public Iterable<ThreadedMessagePack> getThreadMessagePacks() {
		return getThreadMessagePacks(this.messageTable);
	}

	public Iterable<ThreadedMessagePack> getThreadMessagePacksWithoutThreadStarter() {
		return getThreadMessagePacks(this.messagesWithoutThreadStarterTable);
	}

	private Iterable<ThreadedMessagePack> getThreadMessagePacks(final String table) {
		return getMessagePack(table, THREAD_ID_COLUMN, getThreadIds(table));
	}

	private Iterable<ThreadedMessagePack> getMessagePack(final String table, final String column, final Iterable<String> uniqueValues) {
		return map(uniqueValues, new IConvertTypes<String, ThreadedMessagePack>() {
			@Override
			public ThreadedMessagePack convert(String id) {
				return new ThreadedMessagePack(id, getMessagesWhere(table, column + " = ?", new String[] {id}));
			}
		});
	}

	private Iterable<String> getAuthors(final String table) {
		return getDistinct(table, AUTHOR_COLUMN, 100000);
	}

	private Iterable<String> getThreadIds(final String table) {
		return getDistinct(table, THREAD_ID_COLUMN, 100000);
	}

	private Iterable<String> getDistinct(final String table, final String column, int cacheSize) {
		String fetchValues = "SELECT DISTINCT " + column + " FROM " + table + " LIMIT ?, ?";
		return new ResultSetCursor<String>(this, fetchValues, new ResultSetToStringConverter(column), cacheSize);
	}

	public void add(final Iterable<ThreadedMessage> messages) {
		saveMessages(this.messageTable, messages);
	}

	public void initializeMessageTable() {
		createMessageTable(this.messageTable);
	}

	private void createMessageTable(final String tableName) {
		createTable(tableName, "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY",
								MESSAGE_ID_COLUMN + " VARCHAR(" + VARCHAR_INDEX_LENGTH + ") " + UTF8_CHARSET,
								ORIGINS_COLUMN + " TEXT " + UTF8_CHARSET,
								AUTHOR_COLUMN + " VARCHAR(" + VARCHAR_INDEX_LENGTH + ") " + UTF8_CHARSET,
								TOPIC_COLUMN + " BLOB",
								CONTENT_COLUMN + " LONGBLOB",
								NEW_CONTENT_COLUMN + " LONGBLOB",
								DATE_COLUMN + " BIGINT",
								REFERENCE_IDS_COLUMN + " LONGTEXT " + UTF8_CHARSET,
								THREAD_ID_COLUMN + " VARCHAR(" + VARCHAR_INDEX_LENGTH + ") " + UTF8_CHARSET,
								"INDEX (" + MESSAGE_ID_COLUMN + ")",
								"INDEX (" + AUTHOR_COLUMN + ")",
								"INDEX (" + DATE_COLUMN + ")",
								"INDEX (" + THREAD_ID_COLUMN + ")");
	}

	private void saveMessages(final String table, final Iterable<ThreadedMessage> messages) {
		try {
			String insertMessage = "INSERT INTO " + table + " " +
								   "(" + MESSAGE_ID_COLUMN + ", " +
								         ORIGINS_COLUMN + ", " +
								         AUTHOR_COLUMN + ", " +
								         TOPIC_COLUMN + ", " +
								         CONTENT_COLUMN + ", " +
								         NEW_CONTENT_COLUMN + ", " +
								         DATE_COLUMN + ", " +
								         REFERENCE_IDS_COLUMN + ", " +
								         THREAD_ID_COLUMN + ") " +
								   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


			PreparedStatement statement = createPreparedStatement(insertMessage);
			int count = 0;
			for (ThreadedMessage message : messages) {
				statement.setString(1, trimToMaxLength(message.getMessageId(), VARCHAR_INDEX_LENGTH));
				statement.setString(2, collectionToString(message.getMessageOrigins()));
				statement.setString(3, trimToMaxLength(message.getAuthor(), VARCHAR_INDEX_LENGTH));
				statement.setBytes(4, toUTF16Bytes(message.getTopic()));
				statement.setBytes(5, toUTF16Bytes(message.getContent()));
				statement.setBytes(6, toUTF16Bytes(message.getNewContent()));
				statement.setLong(7, message.getDate().getTime());
				statement.setString(8, collectionToString(message.getReferences()));
				statement.setString(9, trimToMaxLength(message.getThreadId(), VARCHAR_INDEX_LENGTH));
				statement.addBatch();

				if ((++count % 1000) == 0) {
					executeBatch(statement);
					statement.clearBatch();
				}
			}
			executeBatch(statement);
			statement.close();
		} catch (SQLException e) {
			LOGGER.error("Couldn't save messages to table \'" + table + "\':\n" + e);
		}
	}

	private void saveNewContents(final String table, final Iterable<ThreadedMessagePack> packs) {
		try {
			String insertMessage = "INSERT INTO " + table + " " +
								   "(" + NEW_CONTENT_KEY_COLUMN + ", " +
								         NEW_CONTENT_COLUMN + ") " +
								   "VALUES (?, ?)";

			PreparedStatement statement = createPreparedStatement(insertMessage);
			int count = 0;
			for (ThreadedMessagePack pack : packs) {
				statement.setString(1, trimToMaxLength(pack.getId(), VARCHAR_INDEX_LENGTH));
				statement.setBytes(2, toUTF16Bytes(getMessagePackNewContent(pack.getMessageTimeline())));
				statement.addBatch();

				if ((++count % 1000) == 0) {
					executeBatch(statement);
					statement.clearBatch();
				}
			}
			executeBatch(statement);
			statement.close();
		} catch (SQLException e) {
			LOGGER.error("Couldn't save messages to table \'" + table + "\':\n" + e);
		}
	}

	private String getMessagePackNewContent(List<ThreadedMessage> messageTimeline) {
		StringBuilder sb = new StringBuilder();
		for (ThreadedMessage message : messageTimeline) {
			sb.append(message.getNewContent());
			sb.append("\n");
		}
		return sb.toString();
	}

	private byte[] toUTF16Bytes(final String s) {
		try {
			return s.getBytes("UTF-16");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Couldn't decode string: \'" + s + "\':\n");
			e.printStackTrace();
		}
		return new byte[0];
	}

	private String fromUTF16Bytes(final byte[] byteString) {
		try {
			return new String(byteString, "UTF-16");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Couldn't decode bytes to string:\n");
			e.printStackTrace();
		}
		return "";
	}

	private String trimToMaxLength(final String original, final int maxLength) {
		if (original.length() < maxLength) {
			return original;
		}
		return original.substring(0, maxLength);
	}

	private String collectionToString(final Collection<String> coll) {
		if (coll.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String elem : coll) {
			sb.append(elem);
			sb.append(COLLECTION_SEPARATOR);
		}
		return sb.substring(0, sb.length() - 1);
	}

	private class ThreadedMessageResultSetHandler implements IConvertResultSetsToConcreteTypes<ThreadedMessage> {
		@Override
		public ThreadedMessage valueOf(final ResultSet result) {
			String messageId = null;
			Set<String> origins = null;
			String author = null;
			String topic = null;
			String content = null;
			String newContent = null;
			Date date = null;
			Set<String> references = null;
			String threadId = null;
			try {
				messageId = result.getString(MESSAGE_ID_COLUMN);
				origins = new HashSet<String>(Arrays.asList(result.getString(ORIGINS_COLUMN).split(COLLECTION_SEPARATOR)));
				author = result.getString(AUTHOR_COLUMN);
				topic = fromUTF16Bytes(result.getBytes(TOPIC_COLUMN));
				content = fromUTF16Bytes(result.getBytes(CONTENT_COLUMN));
				newContent = fromUTF16Bytes(result.getBytes(NEW_CONTENT_COLUMN));
				date = new Date(result.getLong(DATE_COLUMN));
				references = new HashSet<String>(Arrays.asList(result.getString(REFERENCE_IDS_COLUMN).split(COLLECTION_SEPARATOR)));
				threadId = result.getString(THREAD_ID_COLUMN);
			} catch (SQLException e) {
				LOGGER.debug("Couldn't create ThreadedMessage from ResultSet:\n" + e);
			}
			return new ThreadedMessage(origins, messageId, author, topic, content, newContent, date, references, threadId);
		}
	}

	private class NewContentResultSetHandler implements IConvertResultSetsToConcreteTypes<StringPair> {
		@Override
		public StringPair valueOf(final ResultSet result) {
			String key = null;
			String newContent = null;
			try {
				key = result.getString(NEW_CONTENT_KEY_COLUMN);
				newContent = fromUTF16Bytes(result.getBytes(NEW_CONTENT_COLUMN));
			} catch (SQLException e) {
				LOGGER.debug("Couldn't create NewContent from ResultSet:\n" + e);
			}
			return new StringPair(key, newContent);
		}
	}

	public void saveTermCollection(final TermCollection termCollection) {
		dropTable(this.termCollectionTable);
		createTable(this.termCollectionTable, TERM_COLLECTION_COLUMN + " LONGBLOB");
		String insert = "INSERT INTO " + this.termCollectionTable + " ( " + TERM_COLLECTION_COLUMN + " ) VALUES (?)";
		try {
			PreparedStatement statement = createPreparedStatement(insert);
			statement.setObject(1, termCollection);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			LOGGER.fatal("Couldn't save to table \'" + this.termCollectionTable + "\':\n");
			e.printStackTrace();
		}
	}

	public TermCollection getTermCollection() {
		String fetchTermCollection = "SELECT * FROM " + this.termCollectionTable;
		try {
			PreparedStatement statement = createPreparedStatement(fetchTermCollection);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				TermCollection termCollection = (TermCollection) result.getObject(TERM_COLLECTION_COLUMN);
				LOGGER.info("TermCollection loaded");
				result.close();
				return termCollection;
			}
		} catch (SQLException e) {
		}
		LOGGER.fatal("Couldn't load TermCollection from table \'" + this.termCollectionTable + "\'");
		return null;
	}

	public static void main(String[] args) {
		ThreadedMessageStore store = new ThreadedMessageStore();
		LOGGER.info("Build MessagesWithoutThreadStarterTable");
		store.buildMessagesWithoutThreadStarterTable();
		LOGGER.info("Build AuthorsWithoutThreadStarterNewContentTable");
		store.buildAuthorsWithoutThreadStarterNewContentTable();
		LOGGER.info("Build AuthorsNewContentTable");
		store.buildAuthorsNewContentTable();
		LOGGER.info("Build ThreadsWithoutThreadStarterNewContentTable");
		store.buildThreadsWithoutThreadStarterNewContentTable();
		LOGGER.info("Build ThreadsNewContentTable");
		store.buildThreadsNewContentTable();
	}
}
