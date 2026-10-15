import org.jspecify.annotations.NullMarked;

/**
 * H2 Database Provider
 */
@NullMarked
@SuppressWarnings("module")
module litebridge.db.h2 {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;

    provides org.litebridge.db.spi.DatabaseProvider with org.litebridge.db.h2.H2DatabaseProvider;

    exports org.litebridge.db.h2;
}