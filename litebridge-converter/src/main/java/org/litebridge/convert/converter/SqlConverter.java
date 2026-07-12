package org.litebridge.convert.converter;

/**
 * Extends {@link Converter} to add SQL-specific metadata.
 * <p>
 * A {@code SqlConverter} is associated with one or more {@link java.sql.Types} integer codes,
 * allowing it to be used for converting values to and from database-specific representations.
 *
 * @param <T> the target Java type this converter handles
 */
public interface SqlConverter<T> extends Converter<T> {

    /**
     * Returns an array of {@link java.sql.Types} integer codes that this converter is associated with.
     *
     * @return an array of SQL type codes
     */
    int[] sqlTypes();
}
