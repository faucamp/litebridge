import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring integration
 */
@NullMarked
module litebridge.spring {
    requires java.sql;
    requires litebridge.db.spi;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.data.commons;
    requires litebridge.annotations;
    requires litebridge.orm;

    exports org.litebridge.spring;
    exports org.litebridge.spring.repository;

    opens org.litebridge.spring to spring.core, spring.beans, spring.context;
    opens org.litebridge.spring.repository to spring.core, spring.beans, spring.context;
}