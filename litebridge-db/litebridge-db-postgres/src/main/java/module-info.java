import org.jspecify.annotations.NullMarked;
import org.litebridgedb.db.postgres.PostgresDatabaseProvider;

/**
 * PostgreSQL Database Provider
 */
@NullMarked
module litebridge.db.postgres {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    provides org.litebridgedb.db.spi.DatabaseProvider with PostgresDatabaseProvider;

    exports org.litebridgedb.db.postgres;
}
