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
    requires jdk.jfr;

    exports org.litebridge.db.spi.impl;
    exports org.litebridge.db.spi.impl.alias;
    exports org.litebridge.db.spi.impl.expression;
    exports org.litebridge.db.spi.impl.expression.function;
    exports org.litebridge.db.spi.impl.expression.function.aggregate;
    exports org.litebridge.db.spi.impl.expression.function.date;
    exports org.litebridge.db.spi.impl.expression.function.scalar;
    exports org.litebridge.db.spi.impl.sql;
    exports org.litebridge.db.spi.impl.engine;
}