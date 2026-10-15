import org.jspecify.annotations.NullMarked;

/**
 * Common Spring Framework integration for Litebridge
 */
@NullMarked
module litebridge.spring.common {
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.data.commons;

    requires litebridge.annotations;
    requires litebridge.db.spi;
    requires litebridge.orm;
    requires litebridge.commons;

    exports org.litebridge.spring;
    exports org.litebridge.spring.repository;
    exports org.litebridge.spring.annotation;

    opens org.litebridge.spring to spring.core, spring.beans, spring.context;
    opens org.litebridge.spring.annotation to spring.core, spring.beans, spring.context;
}
