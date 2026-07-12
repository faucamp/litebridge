import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring Boot Auto-configuration
 */
@NullMarked
module litebridge.spring.boot.autoconfigure {
    requires java.sql;
    requires litebridge.orm;
    requires litebridge.spring;
    requires litebridge.db.spi;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.commons;

    opens org.litebridge.spring.boot.autoconfigure to spring.core, spring.beans, spring.context;
    exports org.litebridge.spring.boot.autoconfigure;
}
