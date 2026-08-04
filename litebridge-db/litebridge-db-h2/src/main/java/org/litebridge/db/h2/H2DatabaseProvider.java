package org.litebridge.db.h2;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2DatabaseProvider is a concrete implementation of AbstractDatabaseProvider
 * specifically designed to interact with H2 database instances. It handles the
 * creation of prepared statements and logging tailored for H2 database operations.
 * <p>
 * This class provides H2-specific implementations for database
 */
public final class H2DatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(H2DatabaseProvider.class);

    /**
     * Creates a new {@code H2DatabaseProvider}.
     */
    public H2DatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected PreparedStatement createPreparedStatementUsingConnection(final PreparedSql preparedSql,
                                                                       final ManagedConnection connection) throws SQLException {
        final UpdateMetaData updateMetaData = preparedSql.updateMetaData();

        if (updateMetaData == null) {
            return connection.prepareStatement(preparedSql.sql());
        }

        if (updateMetaData.returnGeneratedKeys()) {
            return connection.prepareStatement(preparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(preparedSql.sql());
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
