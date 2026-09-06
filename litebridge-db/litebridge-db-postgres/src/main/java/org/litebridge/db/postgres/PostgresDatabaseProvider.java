package org.litebridge.db.postgres;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysAuto;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        super(new DefaultSqlGenerator(),
                new ExecutionEngineReturnedKeysAuto(
                        new DefaultTypeConverter(),
                        new PostgresAliasTransformer()));
    }

    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new PostgresSequenceColumnValueGenerator(sequence);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
