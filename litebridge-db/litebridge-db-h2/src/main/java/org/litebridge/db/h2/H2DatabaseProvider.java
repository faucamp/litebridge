package org.litebridge.db.h2;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.AbstractDatabaseProvider;

import java.sql.Connection;

public class H2DatabaseProvider extends AbstractDatabaseProvider {

    public H2DatabaseProvider(final Connection connection) {
        super(connection, new DefaultTypeConverter());
    }
}
