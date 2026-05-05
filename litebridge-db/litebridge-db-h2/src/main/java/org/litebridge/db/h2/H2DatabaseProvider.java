package org.litebridge.db.h2;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 Database Provider for Litebridge.
 * <p>
 * {@code H2DatabaseProvider} is a concrete implementation of {@link AbstractDatabaseProvider}
 * designed to facilitate interactions with an H2 database.
 * <p>
 * It uses a {@link DefaultTypeConverter} for handling type conversions between
 * database values and Java data types.
 */
public class H2DatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(H2DatabaseProvider.class);

    public H2DatabaseProvider() {
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
    protected @Nullable String transformAlias(final @Nullable String dbAlias) {
        if (dbAlias == null) {
            return null;
        } else {
            return dbAlias.toLowerCase();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
