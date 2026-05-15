import org.jspecify.annotations.NullMarked;

/**
 * H2 Database Provider
 */
@NullMarked
module litebridge.db.h2 {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    provides org.litebridgedb.db.spi.DatabaseProvider with org.litebridgedb.db.h2.H2DatabaseProvider;

    exports org.litebridgedb.db.h2;
}