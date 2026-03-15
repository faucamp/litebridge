package org.litebridge.db.h2;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;

/**
 * H2 Database Provider for Litebridge.
 * <p>
 * {@code }H2DatabaseProvider} is a concrete implementation of {@link AbstractDatabaseProvider}
 * designed to facilitate interactions with an H2 database. This class provides
 * database operations leveraging the connection specified during instantiation.
 * <p>
 * It uses a {@link DefaultTypeConverter} for handling type conversions between
 * database values and Java data types.
 * <p>
 * The {@code H2DatabaseProvider} requires an active database connection to be
 * provided at the time of instantiation.
 */
public class H2DatabaseProvider extends AbstractDatabaseProvider {

    public H2DatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    protected @Nullable String transformAlias(final @Nullable String dbAlias) {
        if (dbAlias == null) {
            return null;
        } else {
            return dbAlias.toLowerCase();
        }
    }
}
