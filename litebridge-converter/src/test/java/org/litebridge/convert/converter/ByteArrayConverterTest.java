package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteArrayConverterTest {

    private final ByteArrayConverter converter = new ByteArrayConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_ByteArray() {
        // Given
        final byte[] input = new byte[]{1, 2, 3};

        // When
        final byte[] result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "abç";

        // When
        final byte[] result = converter.convert(input);

        // Then
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    void convert_NumberArray() {
        // Given
        final Integer[] input = new Integer[]{1, 2, 3};

        // When
        final byte[] result = converter.convert(input);

        // Then
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void convert_Blob() throws Exception {
        // Given
        final byte[] input = new byte[]{1, 2, 3};
        final Blob blob = new ByteArrayBlob(input);

        // When
        final byte[] result = converter.convert(blob);

        // Then
        assertArrayEquals(input, result);
    }

    @Test
    void convert_BlobThrowsWhenReadingBinaryStreamFails() {
        // Given
        final Blob blob = new ThrowingBlob(true, false);

        // When
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> converter.convert(blob)
        );

        // Then
        assertEquals("Failed to read BLOB data", exception.getMessage());
    }

    @Test
    void convert_BlobThrowsWhenFreeFails() {
        // Given
        final Blob blob = new ThrowingBlob(false, true);

        // When
        final byte[] result = converter.convert(blob);

        // Then no exception is thrown
    }

    @Test
    void convert_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(new Object()));
    }

    @Test
    void type() {
        assertEquals(byte[].class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB}, converter.sqlTypes());
    }

    private static final class ByteArrayBlob implements Blob {

        private final byte[] bytes;

        private ByteArrayBlob(final byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public InputStream getBinaryStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void free() {
            // Nothing to release.
        }

        @Override
        public long length() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes(final long pos, final int length) {
            final int start = Math.toIntExact(pos - 1);
            final byte[] result = new byte[length];
            System.arraycopy(bytes, start, result, 0, length);
            return result;
        }

        @Override
        public long position(final byte[] pattern, final long start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long position(final Blob pattern, final long start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int setBytes(final long pos, final byte[] bytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int setBytes(final long pos, final byte[] bytes, final int offset, final int len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream setBinaryStream(final long pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void truncate(final long len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getBinaryStream(final long pos, final long length) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ThrowingBlob implements Blob {

        private final boolean throwOnGetBinaryStream;
        private final boolean throwOnFree;

        private ThrowingBlob(final boolean throwOnGetBinaryStream, final boolean throwOnFree) {
            this.throwOnGetBinaryStream = throwOnGetBinaryStream;
            this.throwOnFree = throwOnFree;
        }

        @Override
        public InputStream getBinaryStream() throws SQLException {
            if (throwOnGetBinaryStream) {
                throw new SQLException("Cannot read BLOB");
            }

            return InputStream.nullInputStream();
        }

        @Override
        public void free() throws SQLException {
            if (throwOnFree) {
                throw new SQLException("Cannot free BLOB");
            }
        }

        @Override
        public long length() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getBytes(final long pos, final int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long position(final byte[] pattern, final long start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long position(final Blob pattern, final long start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int setBytes(final long pos, final byte[] bytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int setBytes(final long pos, final byte[] bytes, final int offset, final int len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream setBinaryStream(final long pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void truncate(final long len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getBinaryStream(final long pos, final long length) {
            throw new UnsupportedOperationException();
        }
    }
}