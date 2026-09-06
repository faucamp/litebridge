package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.tx.ConnectionProvider;

/**
 * Contract for SQL generators used by {@link org.litebridge.db.spi.impl.AbstractDatabaseProvider} implementations.
 * <p>
 * Implementations of this interface are responsible for translating high-level
 * operation models into executable SQL statements and providing access to
 * supporting components such as metadata engines and specialised SQL generators.
 */
public interface SqlGenerator {

    /**
     * Generates SQL for the given operation using the provided connection provider.
     *
     * @param operation The operation to generate SQL for.
     * @param connectionProvider The connection provider to use.
     * @return The generated SQL.
     */
    String generateSql(final Operation operation, final ConnectionProvider connectionProvider);

    /**
     * Returns the metadata engine used by this SQL generator.
     *
     * @return The metadata engine.
     */
    MetaDataEngine metaDataEngine();

    /**
     * Returns the `SELECT`-query SQL generator used by this SQL generator.
     *
     * @return The select SQL generator.
     */
    SelectSqlGenerator selectSqlGenerator();
}
