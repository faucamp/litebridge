import org.jspecify.annotations.NullMarked;
import org.litebridgedb.db.oracle.convert.OracleOffsetDateTimeConverter;

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
    requires com.oracle.database.jdbc;

    provides org.litebridgedb.db.spi.DatabaseProvider with org.litebridgedb.db.oracle.OracleDatabaseProvider;
    provides org.litebridgedb.convert.converter.Converter with OracleOffsetDateTimeConverter;
    provides org.litebridgedb.convert.converter.SqlConverter with OracleOffsetDateTimeConverter;

    exports org.litebridgedb.db.oracle;
    exports org.litebridgedb.db.oracle.convert;
}