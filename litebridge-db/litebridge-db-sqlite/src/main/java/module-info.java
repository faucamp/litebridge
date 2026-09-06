import org.jspecify.annotations.NullMarked;

/**
 * SQLite Database Provider
 */
@NullMarked
module litebridge.db.sqlite {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    provides org.litebridge.db.spi.DatabaseProvider with org.litebridge.db.sqlite.SQLiteDatabaseProvider;

    exports org.litebridge.db.sqlite;
    exports org.litebridge.db.sqlite.engine;
}
