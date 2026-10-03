import org.jspecify.annotations.NullMarked;
import org.litebridge.db.postgres.PostgresDatabaseProvider;

/**
 * PostgreSQL Database Provider
 */
@NullMarked
module litebridge.db.postgres {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;

    provides org.litebridge.db.spi.DatabaseProvider with PostgresDatabaseProvider;

    exports org.litebridge.db.postgres;
}
