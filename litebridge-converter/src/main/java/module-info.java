import org.jspecify.annotations.NullMarked;

/**
 * Default Litebridge type converter
 * <p>
 * Provides general-purpose SQL data type conversion support for Litebridge.
 */
@NullMarked
module litebridge.converter {
    requires java.sql;
    requires org.jspecify;
    requires typeconverter;
    requires litebridge.commons;
    requires litebridge.db.spi;

    exports org.litebridge.convert;
}