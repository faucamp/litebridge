import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring integration for Spring Framework 7 and Spring Boot 4
 */
@NullMarked
module litebridge.spring {
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.data.commons;

    requires transitive litebridge.spring.common;
    requires litebridge.annotations;
    requires litebridge.db.spi;
    requires litebridge.orm;

    exports org.litebridge.spring.impl;

    opens org.litebridge.spring.impl to spring.core, spring.beans, spring.context, litebridge.orm;
}
