package ch.hauth.util.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ICreatePreparedStatements {
	public PreparedStatement createPreparedStatement(final String sql) throws SQLException ;
}
