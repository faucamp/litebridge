import org.jspecify.annotations.NullMarked;
import org.litebridge.db.oracle.convert.OracleOffsetDateTimeConverter;

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

    provides org.litebridge.db.spi.DatabaseProvider with org.litebridge.db.oracle.OracleDatabaseProvider;
    provides org.litebridge.convert.converter.Converter with OracleOffsetDateTimeConverter;
    provides org.litebridge.convert.converter.SqlConverter with OracleOffsetDateTimeConverter;

    exports org.litebridge.db.oracle;
    exports org.litebridge.db.oracle.convert;
}