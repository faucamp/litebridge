package org.litebridge.db.spi.convert;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The {@code TypeConverter} interface defines a mechanism for converting objects
 * between different types, commonly used for database data type conversions and
 * domain-specific representations. The generic conversion methods offer flexibility
 * for defining custom mappings.
 * <p>
 * Implementations of this interface should perform type-specific conversion logic
 * based on the parameters provided.
 * <p>
 * The methods in this interface support handling of nullable values,
 * allowing operation with {@code null} inputs and producing {@code null} results
 * when applicable.
 */
@NullMarked
public interface TypeConverter {

    /**
     * Converts the given value into a different object type based on the specified database data type.
     * This method is used for mapping application-level objects to database-specific representations
     * or vice versa.
     *
     * @param value      the input object to be converted, which can be {@code null}
     * @param dbDataType the database-specific data type as an integer code
     * @return the converted object, or {@code null} if the input value is {@code null}
     */
    @Nullable
    Object convert(@Nullable Object value, int dbDataType);

    /**
     * Converts the given input object to an instance of the specified type.
     * The conversion process depends on the provided target class and may return
     * a {@code null} value if the input is {@code null} or conversion is not possible.
     *
     * @param value     the input object to be converted, which can be {@code null}
     * @param fieldType the target class representing the desired type of the conversion result
     * @param <T>       the type parameter indicating the desired conversion output type
     * @return an instance of the specified type after conversion, or {@code null}
     */
    @Nullable
    <T> T convert(@Nullable Object value, Class<T> fieldType);
}
