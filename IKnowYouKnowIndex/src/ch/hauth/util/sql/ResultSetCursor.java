package ch.hauth.util.sql;

import static ch.hauth.util.data.Sequence.emptyIterator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.hauth.util.data.ReadOnlyIterator;

public class ResultSetCursor<E> implements Iterable<E> {
	private final static Logger LOGGER = Logger.getLogger(ResultSetCursor.class);

	private final ICreatePreparedStatements statementFactory;
	private final String sql;
	private final IConvertResultSetsToConcreteTypes<E> resultHandler;
	private final int cacheSize;
	private final String[] parameterValues;

	public ResultSetCursor(final ICreatePreparedStatements statementFactory, final String sql, final IConvertResultSetsToConcreteTypes<E> resultHandler, final int cacheSize) {
		this(statementFactory, sql, new String[0], resultHandler, cacheSize);
	}

	public ResultSetCursor(final ICreatePreparedStatements statementFactory, final String sql, String[] parameterValues, final IConvertResultSetsToConcreteTypes<E> resultHandler, final int cacheSize) {
		this.statementFactory = statementFactory;
		this.sql = sql;
		this.parameterValues = parameterValues;
		this.resultHandler = resultHandler;
		this.cacheSize = cacheSize;
	}

	@Override
	public Iterator<E> iterator() {
		try {
			PreparedStatement statement = this.statementFactory.createPreparedStatement(this.sql);
			int parameterCount = statement.getParameterMetaData().getParameterCount();
			if (parameterCount == this.parameterValues.length + 2) {
				for (int i = 0; i < this.parameterValues.length; ++i) {
					statement.setString(i + 1, this.parameterValues[i]);
				}
				return new PreparedStatementIterator(statement, parameterCount - 1);
			}
		} catch (SQLException e) {
			LOGGER.debug("Couldn't create iterator. Thus return empty iterator:\n" + e);
		}
		return emptyIterator();
	}

	private class PreparedStatementIterator extends ReadOnlyIterator<E> {
		private final PreparedStatement statement;
		private final int limitParametersOffset;
		private boolean hasNext = false;
		private boolean nextWasUsed = false;
		private int nextWindowStart = 0;
		private ResultSet results = null;

		public PreparedStatementIterator(final PreparedStatement statement, final int limitParameterOffset) {
			this.statement = statement;
			this.limitParametersOffset = limitParameterOffset;
		}

		@Override
		public boolean hasNext() {
			try {
				if (this.results == null) {
					this.statement.setInt(this.limitParametersOffset, this.nextWindowStart);
					this.statement.setInt(this.limitParametersOffset + 1, ResultSetCursor.this.cacheSize);
					this.results = this.statement.executeQuery();
					this.hasNext = this.results.next();
					this.nextWasUsed = false;
				}

				if (this.nextWasUsed) {
					this.hasNext = this.results.next();
					this.nextWasUsed = false;

					if (!this.hasNext) {
						this.results.close();
						this.results = null;
						this.nextWindowStart += ResultSetCursor.this.cacheSize;
						hasNext();
					}
				}
				if (!this.hasNext) {
					closeStatement();
				}
			} catch (SQLException e) {
				LOGGER.fatal("Problems checking for next value:\n" + e);
				closeStatement();
				return false;
			}
			return this.hasNext;
		}

		@Override
		public E next() {
			this.nextWasUsed = true;
			return ResultSetCursor.this.resultHandler.valueOf(this.results);
		}

		private void closeStatement() {
			try {
				this.statement.close();
			} catch (SQLException e) {
				LOGGER.debug("Problems while closing statement:\n" + e);
			}
		}
	}
}
