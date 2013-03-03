package ch.hauth.util.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class ResultSetToStringConverter implements IConvertResultSetsToConcreteTypes<String> {
	private static final Logger LOGGER = Logger.getLogger(ResultSetToStringConverter.class);

	private final String column;

	public ResultSetToStringConverter(final String column) {
		this.column = column;
	}

	@Override
	public String valueOf(ResultSet result) {
		String value = null;
		try {
			value = result.getString(this.column);
		} catch (SQLException e) {
			LOGGER.debug("Couldn't retrieve column \'" + this.column + "\':\n" + e);
		}
		return value;
	}
}
