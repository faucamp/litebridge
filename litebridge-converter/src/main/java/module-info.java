import org.jspecify.annotations.NullMarked;
import org.litebridgedb.convert.converter.Converter;
import org.litebridgedb.convert.converter.SqlConverter;
import org.litebridgedb.db.spi.convert.TypeConverter;

/**
 * Litebridge type converter
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
            org.litebridgedb.convert.ConfigurableTypeConverter,
            org.litebridgedb.convert.DefaultTypeConverter;

    provides Converter with
            org.litebridgedb.convert.converter.BigDecimalConverter,
            org.litebridgedb.convert.converter.BigIntegerConverter,
            org.litebridgedb.convert.converter.BooleanConverter,
            org.litebridgedb.convert.converter.ByteArrayConverter,
            org.litebridgedb.convert.converter.ByteConverter,
            org.litebridgedb.convert.converter.CharacterConverter,
            org.litebridgedb.convert.converter.DoubleConverter,
            org.litebridgedb.convert.converter.FloatConverter,
            org.litebridgedb.convert.converter.IntegerConverter,
            org.litebridgedb.convert.converter.LongConverter,
            org.litebridgedb.convert.converter.ShortConverter,
            org.litebridgedb.convert.converter.SqlDateConverter,
            org.litebridgedb.convert.converter.SqlTimeConverter,
            org.litebridgedb.convert.converter.SqlTimestampConverter,
            org.litebridgedb.convert.converter.StringConverter;

    provides SqlConverter with
            org.litebridgedb.convert.converter.BigDecimalConverter,
            org.litebridgedb.convert.converter.BooleanConverter,
            org.litebridgedb.convert.converter.ByteArrayConverter,
            org.litebridgedb.convert.converter.ByteConverter,
            org.litebridgedb.convert.converter.DoubleConverter,
            org.litebridgedb.convert.converter.FloatConverter,
            org.litebridgedb.convert.converter.IntegerConverter,
            org.litebridgedb.convert.converter.LongConverter,
            org.litebridgedb.convert.converter.ShortConverter,
            org.litebridgedb.convert.converter.SqlDateConverter,
            org.litebridgedb.convert.converter.SqlTimeConverter,
            org.litebridgedb.convert.converter.SqlTimestampConverter,
            org.litebridgedb.convert.converter.StringConverter;

    uses Converter;

    exports org.litebridgedb.convert;
    exports org.litebridgedb.convert.converter;
}