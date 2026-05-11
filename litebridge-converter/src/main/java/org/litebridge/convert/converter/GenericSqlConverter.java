package org.litebridge.convert.converter;

public class GenericSqlConverter<T> extends GenericConverter<T> implements SqlConverter<T> {

    private final int[] sqlTypes;

    public GenericSqlConverter(final Class<T> type, final int[] sqlTypes, final ConverterFunction<T> conversionFunction) {
        super(type, conversionFunction);
        this.sqlTypes = sqlTypes;
    }

    public GenericSqlConverter(final Class<T> type, final int sqlType, final ConverterFunction<T> conversionFunction) {
        this(type, new int[]{sqlType}, conversionFunction);
    }

    @Override
    public int[] sqlTypes() {
        return sqlTypes;
    }
}
