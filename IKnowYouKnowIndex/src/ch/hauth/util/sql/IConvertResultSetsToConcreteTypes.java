package ch.hauth.util.sql;

import java.sql.ResultSet;

public interface IConvertResultSetsToConcreteTypes<T> {
	public T valueOf(final ResultSet result);
}
