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

    provides org.litebridgedb.db.spi.DatabaseProvider with org.litebridgedb.db.oracle.OracleDatabaseProvider;

    exports org.litebridgedb.db.oracle;
}