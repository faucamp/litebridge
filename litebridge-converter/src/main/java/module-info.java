import org.jspecify.annotations.NullMarked;
import org.litebridge.convert.converter.Converter;
import org.litebridge.convert.converter.SqlConverter;
import org.litebridge.db.spi.convert.TypeConverter;

/**
 * Default Litebridge type converter
 * <p>
 * Provides general-purpose SQL data type conversion support for Litebridge.
 */
@NullMarked
module litebridge.converter {
    requires java.sql;
    requires org.jspecify;
    requires litebridge.commons;
    requires litebridge.db.spi;
    requires org.slf4j;

    provides TypeConverter with
            org.litebridge.convert.ConfigurableTypeConverter,
            org.litebridge.convert.DefaultTypeConverter;

    provides Converter with
            org.litebridge.convert.converter.BigDecimalConverter,
            org.litebridge.convert.converter.BigIntegerConverter,
            org.litebridge.convert.converter.BooleanConverter,
            org.litebridge.convert.converter.ByteArrayConverter,
            org.litebridge.convert.converter.ByteConverter,
            org.litebridge.convert.converter.CharacterConverter,
            org.litebridge.convert.converter.DoubleConverter,
            org.litebridge.convert.converter.FloatConverter,
            org.litebridge.convert.converter.IntegerConverter,
            org.litebridge.convert.converter.LongConverter,
            org.litebridge.convert.converter.ShortConverter,
            org.litebridge.convert.converter.SqlDateConverter,
            org.litebridge.convert.converter.SqlTimeConverter,
            org.litebridge.convert.converter.SqlTimestampConverter,
            org.litebridge.convert.converter.StringConverter;

    provides SqlConverter with
            org.litebridge.convert.converter.BigDecimalConverter,
            org.litebridge.convert.converter.BooleanConverter,
            org.litebridge.convert.converter.ByteArrayConverter,
            org.litebridge.convert.converter.ByteConverter,
            org.litebridge.convert.converter.DoubleConverter,
            org.litebridge.convert.converter.FloatConverter,
            org.litebridge.convert.converter.IntegerConverter,
            org.litebridge.convert.converter.LongConverter,
            org.litebridge.convert.converter.ShortConverter,
            org.litebridge.convert.converter.SqlDateConverter,
            org.litebridge.convert.converter.SqlTimeConverter,
            org.litebridge.convert.converter.SqlTimestampConverter,
            org.litebridge.convert.converter.StringConverter;

    uses Converter;

    exports org.litebridge.convert;
    exports org.litebridge.convert.converter;
}