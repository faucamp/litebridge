import org.jspecify.annotations.NullMarked;

/**
 * H2 Database Provider
 */
@NullMarked
module litebridge.db.h2 {
    requires java.sql;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires org.jspecify;
}