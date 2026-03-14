import org.jspecify.annotations.NullMarked;

/**
 * H2 Database Provider
 */
@NullMarked
module litebridge.db.h2 {
    requires java.sql;
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi.impl;

    exports org.litebridge.db.h2;
}