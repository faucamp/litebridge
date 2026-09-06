package org.litebridge.db.sqlite;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.sqlite.engine.SQLiteExecutionEngine;
import org.litebridge.db.sqlite.engine.SQLiteMetaDataEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLite Database Provider for Litebridge.
 * <p>
 * This class provides SQLite-specific implementations for database interactions.
 */
public final class SQLiteDatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDatabaseProvider.class);

    public SQLiteDatabaseProvider() {
        super(new DefaultSqlGenerator(new SQLiteMetaDataEngine()),
                new SQLiteExecutionEngine(
                        new DefaultTypeConverter(),
                        new UppercaseAliasTransformer()));
    }

    /**
     * SQLite does not support sequences. Throws an {@code UnsupportedOperationException} if called.
     *
     * @param sequence the sequence name
     * @return N/A; throws an {@code UnsupportedOperationException}
     * @throws UnsupportedOperationException if called
     */
    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("SQLite does not support sequences");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
