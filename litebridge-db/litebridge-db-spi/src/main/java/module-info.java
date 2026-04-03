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

    exports org.litebridge.db.spi;
    exports org.litebridge.db.spi.convert;
    exports org.litebridge.db.spi.math;
    exports org.litebridge.db.spi.query;
    exports org.litebridge.db.spi.tx;
    exports org.litebridge.db.spi.update;
    exports org.litebridge.db.spi.util;
}