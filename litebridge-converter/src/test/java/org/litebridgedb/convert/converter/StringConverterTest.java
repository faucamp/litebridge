package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringConverterTest {

    private final StringConverter converter = new StringConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_String() {
        // Given
        final String input = "abc";

        // When
        final String result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_ByteArray() {
        // Given
        final byte[] input = "abc".getBytes();

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("abc", result);
    }

    @Test
    void convert_CharArray() {
        // Given
        final char[] input = new char[]{'a', 'b', 'c'};

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("abc", result);
    }

    @Test
    void convert_ObjectArray() {
        // Given
        final Integer[] input = new Integer[]{1, 2, 3};

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals(input.toString(), result);
    }

    @Test
    void convert_OtherObject() {
        // Given
        final Integer input = 123;

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("123", result);
    }

    @Test
    void convert_BigDecimal_zeroFractionalPart() {
        // Given
        final BigDecimal input = new BigDecimal("123.0");

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("123", result);
    }

    @Test
    void convert_BigDecimal_nonZeroFractionalPart() {
        // Given
        final BigDecimal input = new BigDecimal("123.345");

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("123.345", result);
    }

    @Test
    void convert_Clob() {
        // Given
        final Clob input = new StringClob("abc");

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("abc", result);
    }

    @Test
    void convert_Clob_whenReadingFails() {
        // Given
        final SQLException cause = new SQLException("read failed");
        final Clob input = clobThrowingOnGetCharacterStream(cause);

        // When
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> converter.convert(input)
        );

        // Then
        assertEquals("Failed to read CLOB data", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void convert_Clob_whenFreeFails() {
        // Given
        final SQLException cause = new SQLException("free failed");
        final Clob input = clobThrowingOnFree(cause);

        // When
        final String result = converter.convert(input);

        // Then no exception is thrown
    }

    private static Clob clobThrowingOnGetCharacterStream(final SQLException exception) {
        return clobProxy((proxy, method, args) -> switch (method.getName()) {
            case "getCharacterStream" -> throw exception;
            case "free" -> null;
            case "toString" -> "clobThrowingOnGetCharacterStream";
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    private static Clob clobThrowingOnFree(final SQLException exception) {
        return clobProxy((proxy, method, args) -> switch (method.getName()) {
            case "getCharacterStream" -> Reader.nullReader();
            case "free" -> throw exception;
            case "toString" -> "clobThrowingOnFree";
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    private static Clob clobProxy(final java.lang.reflect.InvocationHandler invocationHandler) {
        return assertInstanceOf(
                Clob.class,
                Proxy.newProxyInstance(
                        StringConverterTest.class.getClassLoader(),
                        new Class<?>[]{Clob.class},
                        invocationHandler
                )
        );
    }

    @Test
    void type() {
        assertEquals(String.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB}, converter.sqlTypes());
    }

    private static final class StringClob implements Clob {

        private String string;

        public StringClob(final String string) {
            this.string = string;
        }

        @Override
        public String getSubString(final long pos, final int length) {
            return string.substring(Math.toIntExact(pos), Math.toIntExact(pos) + length);
        }

        @Override
        public Reader getCharacterStream() {
            return new StringReader(string);
        }

        @Override
        public InputStream getAsciiStream() {
            return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public long position(final String searchstr, final long start) {
            return string.indexOf(searchstr, Math.toIntExact(start));
        }

        @Override
        public long position(final Clob searchstr, final long start) throws SQLException {
            return string.indexOf(searchstr.getSubString(1, (int) searchstr.length()), Math.toIntExact(start));
        }

        @Override
        public int setString(final long pos, final String str) {
            this.string = string.substring(0, Math.toIntExact(pos)) + str + string.substring(Math.toIntExact(pos) + str.length());
            return string.length();
        }

        @Override
        public int setString(final long pos, final String str, final int offset, final int len) {
            return 0;
        }

        @Override
        public OutputStream setAsciiStream(final long pos) {
            return null;
        }

        @Override
        public Writer setCharacterStream(final long pos) {
            return null;
        }

        @Override
        public Reader getCharacterStream(final long pos, final long length) {
            return null;
        }

        @Override
        public void free() {
            string = null;
        }

        @Override
        public long length() {
            return string.length();
        }

        @Override
        public void truncate(final long len) {
            string = string.substring(0, Math.toIntExact(len));
        }
    }
}
