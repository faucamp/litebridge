import org.jspecify.annotations.NullMarked;

/**
 * Oracle Database Provider
 */
@NullMarked
module litebridge.db.oracle {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires java.sql;
    requires org.slf4j;
    requires litebridge.commons;

    provides org.litebridge.db.spi.DatabaseProvider with org.litebridge.db.oracle.OracleDatabaseProvider;

    exports org.litebridge.db.oracle;
}