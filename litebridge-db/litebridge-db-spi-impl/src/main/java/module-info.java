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

    exports org.litebridgedb.db.spi.impl;
    exports org.litebridgedb.db.spi.impl.function;
    exports org.litebridgedb.db.spi.impl.function.aggregate;
    exports org.litebridgedb.db.spi.impl.function.scalar;
    exports org.litebridgedb.db.spi.impl.alias;
}