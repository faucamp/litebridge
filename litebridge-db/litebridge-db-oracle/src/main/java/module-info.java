import org.jspecify.annotations.NullMarked;

/**
 * Oracle Database Provider
 */
@NullMarked
module litebridge.db.oracle {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires java.sql;
    requires org.slf4j;

    exports org.litebridge.db.oracle;
}