package org.litebridge.db.spi.impl.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code SUBSTRING(column, start, length)} scalar function.
 */
public class Substring extends FunctionExpression {

    /**
     * Database index, 1-based.
     */
    protected final int start;

    /**
     * The number of characters to return.
     */
    protected final @Nullable Integer length;

    /**
     * Construct a {@code SUBSTRING} function.
     *
     * @param target                    Target expression to extract characters from.
     * @param start                     The starting position. The first character of a database string is always 1.
     * @param length                    The number of characters to return. If {@code null}, the function extracts everything from the start position to the end of the text.
     * @param columnIdentifierGenerator Database provider-specific column identifier generator.
     */
    public Substring(final ColumnExpression target, final int start, final @Nullable Integer length, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
        this.start = start;
        this.length = length;
    }

    @Override
    protected String template() {
        if (length != null) {
            return "SUBSTRING(%%s, %s, %s)".formatted(start, length);
        } else {
            return "SUBSTRING(%%s, %s)".formatted(start);
        }
    }
}
