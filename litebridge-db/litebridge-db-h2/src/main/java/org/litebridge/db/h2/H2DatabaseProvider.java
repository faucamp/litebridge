package org.litebridge.db.h2;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysAuto;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2DatabaseProvider is a concrete implementation of AbstractDatabaseProvider
 * specifically designed to interact with H2 database instances. It handles the
 * creation of prepared statements and logging tailored for H2 database operations.
 * <p>
 * This class provides H2-specific implementations for database
 */
public final class H2DatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2DatabaseProvider.class);

    /**
     * Creates a new {@code H2DatabaseProvider}.
     */
    public H2DatabaseProvider() {
        super(new DefaultSqlGenerator(),
                new ExecutionEngineReturnedKeysAuto(
                        new DefaultTypeConverter(),
                        new UppercaseAliasTransformer()));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
