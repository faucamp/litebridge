package org.litebridge.db.postgres;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PostgresqlDatabaseProvider is a concrete implementation of AbstractDatabaseProvider
 * specifically designed to interact with PostgreSQL database instances.
 */
public final class PostgresDatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabaseProvider.class);

    /**
     * Constructs a new {@code PostgresDatabaseProvider} using a default type converter.
     */
    public PostgresDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected PreparedStatement createPreparedStatementUsingConnection(final PreparedSql preparedSql,
                                                                       final boolean returnGeneratedKeys,
                                                                       final TableMetaData tableMetaData,
                                                                       final ManagedConnection connection) throws SQLException {
        if (returnGeneratedKeys) {
            return connection.prepareStatement(preparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(preparedSql.sql());
        }
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new PostgresSequenceColumnValueGenerator(sequence);
    }

    @Override
    protected AliasTransformer createAliasTransformer() {
        return new PostgresAliasTransformer();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
