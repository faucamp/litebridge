package org.litebridge.convert.converter;

/**
 * A generic implementation of {@link SqlConverter} that uses a {@link ConverterFunction} for the conversion logic.
 *
 * @param <T> the target type
 */
public class GenericSqlConverter<T> extends GenericConverter<T> implements SqlConverter<T> {

    private final int[] sqlTypes;

    /**
     * Constructs a new {@code GenericSqlConverter} for the specified type, SQL types, and conversion function.
     *
     * @param type the target Java class
     * @param sqlTypes an array of {@link java.sql.Types} codes associated with this converter
     * @param conversionFunction the conversion logic
     */
    public GenericSqlConverter(final Class<T> type, final int[] sqlTypes, final ConverterFunction<T> conversionFunction) {
        super(type, conversionFunction);
        this.sqlTypes = sqlTypes;
    }

    /**
     * Constructs a new {@code GenericSqlConverter} for the specified type, single SQL type, and conversion function.
     *
     * @param type the target Java class
     * @param sqlType the {@link java.sql.Types} code associated with this converter
     * @param conversionFunction the conversion logic
     */
    public GenericSqlConverter(final Class<T> type, final int sqlType, final ConverterFunction<T> conversionFunction) {
        this(type, new int[]{sqlType}, conversionFunction);
    }

    /**
     * Returns an array of {@link java.sql.Types} integer codes that this converter is associated with.
     *
     * @return an array of SQL type codes
     */
    @Override
    public int[] sqlTypes() {
        return sqlTypes;
    }
}
