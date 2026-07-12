import org.jspecify.annotations.NullMarked;

/**
 * Abstract DatabaseProvider implementation.
 * <p>
 * This module provides base implementation classes for the Litebridge Database SPI,
 * simplifying the creation of new database providers.
 */
@NullMarked
module litebridge.db.spi.impl {
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.commons;
    requires litebridge.db.spi;

    exports org.litebridge.db.spi.impl;
    exports org.litebridge.db.spi.impl.alias;
    exports org.litebridge.db.spi.impl.function;
    exports org.litebridge.db.spi.impl.function.aggregate;
    exports org.litebridge.db.spi.impl.function.scalar;
    exports org.litebridge.db.spi.impl.sql;
}