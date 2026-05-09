import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring integration
 */
@NullMarked
module litebridge.spring {
    requires java.sql;
    requires litebridge.db.spi;
    requires spring.jdbc;
    requires spring.tx;
    requires org.jspecify;

    exports org.litebridge.spring;
}