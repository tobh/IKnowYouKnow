package ch.hauth.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public abstract class MysqlStorage implements ICreatePreparedStatements {
	private static final Logger LOGGER = Logger.getLogger(MysqlStorage.class);

	public static final String UTF8_CHARSET = "CHARACTER SET UTF8 COLLATE UTF8_BIN";
	public static final int VARCHAR_INDEX_LENGTH = 254;

	private final String host;
	private final String port;
	private final String dbName;
	private final String userName;
	private final String password;
	private final String driver;

	private Connection connection;

	public MysqlStorage(final String host,
						final String dbName,
						final String userName,
						final String password) {
		this(host, "3306", dbName, userName, password);
	}

	public MysqlStorage(final String host,
						final String port,
						final String dbName,
						final String userName,
						final String password) {
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
		this.driver = "com.mysql.jdbc.Driver";

		try {
			Class.forName(driver).newInstance();
			this.connection = getConnection();
		} catch (InstantiationException e) {
			LOGGER.error("Couldn't initialize MysqlStorage:\n" + e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Couldn't initialize MysqlStorage:\n" + e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Couldn't initialize MysqlStorage:\n" + e);
		} catch (SQLException e) {
			LOGGER.error("Couldn't initialize MysqlStorage:\n" + e);
		}
	}

	@Override
	public PreparedStatement createPreparedStatement(String sql) throws SQLException {
		return getConnection().prepareStatement(sql);
	}

	protected String sqlStringToIterateOverAllEntries(final String table) {
		return sqlStringToIterateOverAllEntries(table, "id");
	}

	protected String sqlStringToIterateOverAllEntries(final String table, final String key) {
		return "SELECT results.* FROM (SELECT " + key + " FROM " + table + " ORDER BY " + key + " LIMIT ?, ?) keyQuery JOIN " + table + " results ON results." + key + " = keyQuery." + key;
	}

	protected boolean doesTableExist(final String table) {
		try {
			PreparedStatement statement = createPreparedStatement("SELECT * FROM " + table + " LIMIT 1");
			statement.executeQuery();
			statement.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	protected void createTable(final String table, final String ... columns) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE " + table + " (");
			for (String column : columns) {
				sb.append(column);
				sb.append(", ");
			}
			String createTable = sb.substring(0, sb.length() - 2) + ") DEFAULT CHARSET=utf8";
			PreparedStatement statement = createPreparedStatement(createTable);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			LOGGER.error("Couldn't create table:\n" + e);
		}
	}

	protected void dropTable(final String table) {
		try {
			PreparedStatement statement = createPreparedStatement("DROP TABLE IF EXISTS " + table);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected <T> void executeBatch(final PreparedStatement statement) {
		setAutoCommit(false);
		try {
			statement.executeBatch();
		} catch (SQLException e) {
			LOGGER.fatal("Couldn't execute batch:\n" + e);
		}
		commit();
		setAutoCommit(true);
	}

	private Connection getConnection() throws SQLException {
		if (this.connection == null || !isConnectionValid()) {
			return DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.dbName + "?useUnicode=true&characterEncoding=UTF-8&autoDeserialize=true", userName, password);
		}
		return this.connection;
	}

	private boolean isConnectionValid() {
		try {
			return this.connection.isValid(3);
		} catch (SQLException e) {
			return false;
		}
	}

	private void setAutoCommit(final boolean isActivated) {
		try {
			getConnection().setAutoCommit(isActivated);
		} catch (SQLException e) {
			LOGGER.fatal("Couldn't set autocommit to " + isActivated + " :\n" + e);
		}
	}

	private void commit() {
		try {
			getConnection().commit();
		} catch (SQLException e) {
			LOGGER.fatal("Couldn't commit:\n" + e);
		}
	}
}
