import org.jspecify.annotations.NullMarked;

/**
 * H2 Database Provider
 */
@NullMarked
@SuppressWarnings("module")
module litebridge.db.h2 {
    requires org.jspecify;
    requires litebridge.commons;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    provides org.litebridge.db.spi.DatabaseProvider with org.litebridge.db.h2.H2DatabaseProvider;

    exports org.litebridge.db.h2;
}