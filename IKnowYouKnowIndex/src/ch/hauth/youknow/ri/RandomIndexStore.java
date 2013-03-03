package ch.hauth.youknow.ri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.hauth.util.data.IConvertTypes;
import ch.hauth.util.data.Pair;
import ch.hauth.util.sql.IConvertResultSetsToConcreteTypes;
import ch.hauth.util.sql.MysqlStorage;
import ch.hauth.util.sql.ResultSetCursor;
import ch.hauth.youknow.Config;
import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.source.ThreadedMessageStore;


public class RandomIndexStore extends MysqlStorage {
	private static final Logger LOGGER = Logger.getLogger(RandomIndexStore.class);

	private static final String KEY_COLUMN = "vectorId";
	private static final String VECTOR_COLUMN = "vector";

	private final String wordContextTable;
	private final String randomIndexClusterMeansTable;
	private final String randomIndexClusterTablePrefix;

	public RandomIndexStore() {
		super(Config.get("randomIndexStoreUrl"),
			  Config.get("randomIndexStoreDb"),
			  Config.get("mysqlUser"),
			  Config.get("mysqlPassword"));

		this.wordContextTable = Config.get("wordContextTable");
		this.randomIndexClusterMeansTable = Config.get("randomIndexClusterMeansTable");
		this.randomIndexClusterTablePrefix = Config.get("randomIndexClusterTablePrefix");
	}

	public void setWordContext(final String name, final Map<String, Vector> contexts) {
		rebuildTable(getWordContextTable(name), contexts);
	}

	public Vector getWordContext(final String name, final String term) {
		return getVector(getWordContextTable(name), term);
	}

	private String getWordContextTable(final String name) {
		return name + "_" + this.wordContextTable;
	}

	public boolean doesWordContextExist(final String name) {
		return doesTableExist(getWordContextTable(name));
	}

	public void clearRandomIndex(final String name, int clusterSize) {
		initVectorTable(getRandomIndexClusterMeansTable(name));
		for (int i = 0; i < clusterSize; ++i) {
			initVectorTable(getClusterTableName(name, i));
		}
	}

	private String getRandomIndexClusterMeansTable(final String name) {
		return name + "_" + this.randomIndexClusterMeansTable;
	}

	private String getClusterTableName(final String name, final int clusterId) {
		return name + "_" + this.randomIndexClusterTablePrefix + clusterId;
	}

	public void updateRandomIndexClusterMeans(final String name, final Map<String, Vector> documentVectors) {
		save(getRandomIndexClusterMeansTable(name), documentVectors);
	}

	public void updateRandomIndexCluster(final String name, final int clusterId, final Map<String, Vector> documentVectors) {
		save(getClusterTableName(name, clusterId), documentVectors);
	}

	public Iterable<Pair<String, Vector>> getClusterMeansDocumentVectors(final String name) {
		return getDocumentVectors(getRandomIndexClusterMeansTable(name));
	}

	@SuppressWarnings("unused")
	private static class StringPairToIntConverter implements IConvertTypes<Pair<String, Vector>, Pair<Integer, Vector>> {
		@Override
		public Pair<Integer, Vector> convert(Pair<String, Vector> original) {
			return new Pair<Integer, Vector>(Integer.parseInt(original.getFirst()), original.getSecond());
		}
	}

	public Iterable<Pair<String, Vector>> getClusterDocumentVectors(final String name, int clusterId) {
		return getDocumentVectors(getClusterTableName(name, clusterId));
	}

	private Iterable<Pair<String, Vector>> getDocumentVectors(String table) {
		return new ResultSetCursor<Pair<String, Vector>>(this, sqlStringToIterateOverAllEntries(table), new IdVectorPairResultSetHandler(), 10000);
	}

	private void rebuildTable(final String table, final Map<String, Vector> vectors) {
		initVectorTable(table);
		save(table, vectors);
	}

	private void initVectorTable(final String table) {
		dropTable(table);
		String idColumn = "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY";
		String keyColumn = KEY_COLUMN + " VARCHAR(" + ThreadedMessageStore.VARCHAR_INDEX_LENGTH + ") " + UTF8_CHARSET + " NOT NULL UNIQUE KEY";
		String vectorColumn = VECTOR_COLUMN + " BLOB";
		createTable(table, idColumn, keyColumn, vectorColumn);
	}

	private <T> void save(final String table, final Map<T, Vector> vectors) {
		try {
			String insert = "INSERT INTO " + table + " ( " + KEY_COLUMN + ", " + VECTOR_COLUMN + ") VALUES (?, ?)";
			PreparedStatement statement = createPreparedStatement(insert);
			int count = 0;
			for (Entry<T, Vector> entry: vectors.entrySet()) {
				statement.setString(1, String.valueOf(entry.getKey()));
				statement.setObject(2, entry.getValue());
				statement.addBatch();

				if ((++count % 1000) == 0) {
					executeBatch(statement);
					statement.clearBatch();
				}
			}
			executeBatch(statement);
			statement.close();
		} catch (SQLException e) {
			LOGGER.fatal("Couldn't save to table \'" + table + "\':\n" + e);
		}
	}

	private Vector getVector(final String table, final String key) {
		Vector vector = null;
		try {
			String fetchVector = "SELECT * FROM " + table + " WHERE " + KEY_COLUMN + " = ?";
			PreparedStatement statement = createPreparedStatement(fetchVector);
			statement.setString(1, key);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				vector = (Vector) results.getObject(VECTOR_COLUMN);
			}
			statement.close();
		} catch (SQLException e) {
			LOGGER.debug("Couldn't retrieve vector from table \'" + table + "\' with key \'" + key + "\':\n" + e);
		}
		return vector;
	}

	private class IdVectorPairResultSetHandler implements IConvertResultSetsToConcreteTypes<Pair<String, Vector>> {
		@Override
		public Pair<String, Vector> valueOf(ResultSet result) {
			String id = null;
			Vector vector = null;
			try {
				id = result.getString(KEY_COLUMN);
				vector = (Vector) result.getObject(VECTOR_COLUMN);
			} catch (SQLException e) {
				LOGGER.fatal("Couldn't get id and vector from result:\n" + e);
			}
			return new Pair<String, Vector>(id, vector);
		}
	}
}
