package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A converter for {@link String} values.
 * <p>
 * Handles {@link java.sql.Types#CHAR}, {@link java.sql.Types#VARCHAR}, and {@link java.sql.Types#LONGVARCHAR}.
 */
public class StringConverter implements SqlConverter<String> {

    private static final int[] SQL_TYPES = new int[]{Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB};

    /**
     * Converts the given rhs to a {@link String}.
     * <p>
     * If the rhs is a {@code byte[]} or {@code char[]}, it is converted to a string using the appropriate constructor.
     * Otherwise, {@link Object#toString()} is used.
     *
     * @param value the rhs to convert, may be {@code null}
     * @return the converted string, or {@code null}
     */
    @Override
    public @Nullable String convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String str) {
            return str;
        }

        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType() == Byte.TYPE) {
                return new String((byte[]) value);
            } else if (value.getClass().getComponentType() == Character.TYPE) {
                return new String((char[]) value);
            } else {
                return value.toString();
            }
        }

        return switch (value) {
            case Clob clob -> {
                try (Reader reader = clob.getCharacterStream(); final StringWriter writer = new StringWriter()) {
                    reader.transferTo(writer);
                    yield writer.toString();
                } catch (final Exception ex) {
                    throw new IllegalStateException("Failed to read CLOB data", ex);
                } finally {
                    try {
                        clob.free();
                    } catch (final SQLException ex) {
                        throw new IllegalStateException("Failed to free CLOB resources", ex);
                    }
                }
            }
            case BigDecimal bigDecimal -> {
                try {
                    yield bigDecimal.toBigIntegerExact().toString();
                } catch (final ArithmeticException ex) {
                    yield bigDecimal.toString();
                }
            }
            default -> value.toString();
        };
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link String}.class
     */
    @Override
    public Class<String> type() {
        return String.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#CHAR}, {@link java.sql.Types#VARCHAR}, and {@link java.sql.Types#LONGVARCHAR}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
