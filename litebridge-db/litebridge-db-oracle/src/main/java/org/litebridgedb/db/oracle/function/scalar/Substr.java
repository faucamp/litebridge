package org.litebridgedb.db.oracle.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.scalar.Substring;

public class Substr extends Substring {

    /**
     * Construct a {@code SUBSTR} function.
     *
     * @param target                    Target column expression to extract characters from.
     * @param start                     The starting position. The first character of a database string is always 1.
     * @param length                    The number of characters to return. If {@code null}, the function extracts everything from the start position to the end of the text.
     * @param columnIdentifierGenerator Database provider-specific column identifier generator.
     */
    public Substr(final ColumnExpression target, final int start, final @Nullable Integer length, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, start, length, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        if (length != null) {
            return "SUBSTR(%%s, %s, %s)".formatted(start, length);
        } else {
            return "SUBSTR(%%s, %s)".formatted(start);
        }
    }
}
