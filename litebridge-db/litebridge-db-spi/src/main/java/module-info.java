import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Database Service Provider Interface
 * <p>
 * SPI for Litebridge implementing vendor-specific database providers.
 */
@NullMarked
module litebridge.db.spi {
    requires java.sql;
    requires litebridge.commons;
    requires org.jspecify;
    requires org.slf4j;

    exports org.litebridgedb.db.spi;
    exports org.litebridgedb.db.spi.convert;
    exports org.litebridgedb.db.spi.expression;
    exports org.litebridgedb.db.spi.generator;
    exports org.litebridgedb.db.spi.math;
    exports org.litebridgedb.db.spi.query;
    exports org.litebridgedb.db.spi.tx;
    exports org.litebridgedb.db.spi.update;
    exports org.litebridgedb.db.spi.util;
    exports org.litebridgedb.db.spi.alias;
}