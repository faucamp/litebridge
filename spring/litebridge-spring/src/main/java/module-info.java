import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring integration
 */
@NullMarked
module litebridge.spring {
    requires java.sql;
    requires litebridge.db.spi;
    requires org.jspecify;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires litebridge.annotations;
    requires litebridge.orm;
    requires org.slf4j;

    exports org.litebridge.spring;
}