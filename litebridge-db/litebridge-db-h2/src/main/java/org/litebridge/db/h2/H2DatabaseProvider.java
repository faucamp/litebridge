package org.litebridge.db.h2;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;

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

    public H2DatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected @Nullable String transformAlias(final @Nullable String dbAlias) {
        if (dbAlias == null) {
            return null;
        } else {
            return dbAlias.toLowerCase();
        }
    }
}
