package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A converter for {@code byte[]} values.
 * <p>
 * Handles {@link java.sql.Types#BINARY}, {@link java.sql.Types#VARBINARY}, and {@link java.sql.Types#LONGVARBINARY}.
 */
public class ByteArrayConverter implements SqlConverter<byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayConverter.class);
    private static final int[] SQL_TYPES = new int[]{Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB};

    /**
     * Converts the given value to a {@code byte[]}.
     * <p>
     * Supports {@code byte[]} and arrays of {@link Number} instances.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@code byte[]}, or {@code null}
     * @throws IllegalArgumentException if the value cannot be converted to {@code byte[]}
     */
    @Override
    public @Nullable byte[] convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof byte[] bytes) {
            return bytes;
        } else if (value instanceof String string) {
            return string.getBytes(StandardCharsets.UTF_8);
        } else if (Number[].class.isAssignableFrom(value.getClass())) {
            final Number[] numbers = (Number[]) value;
            final byte[] primitiveBytes = new byte[numbers.length];

            for (int i = 0; i < numbers.length; i++) {
                primitiveBytes[i] = numbers[i].byteValue();
            }

            return primitiveBytes;
        } else if (value instanceof Blob blob) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (InputStream is = blob.getBinaryStream()) {
                is.transferTo(baos);
            } catch (final Exception ex) {
                throw new IllegalStateException("Failed to read BLOB data", ex);
            } finally {
                try {
                    blob.free();
                } catch (final SQLException ex) {
                    LOGGER.error("Failed to free BLOB resources", ex);
                }
            }

            return baos.toByteArray();
        } else {
            throw new IllegalArgumentException("Cannot convert value of type " + value.getClass() + " to byte[]");
        }
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@code byte[].class}
     */
    @Override
    public Class<?> type() {
        return byte[].class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#BINARY}, {@link java.sql.Types#VARBINARY}, and {@link java.sql.Types#LONGVARBINARY}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
