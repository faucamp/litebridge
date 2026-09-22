package org.litebridge.db.spi.impl.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.function.FunctionExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

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
     * @param target         Target expression to extract characters from.
     * @param start          The starting position. The first character of a database string is always 1.
     * @param length         The number of characters to return. If {@code null}, the function extracts everything from the start position to the end of the text.
     * @param alias          The alias for the column expression.
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public Substring(final SelectExpression target,
                     final int start,
                     final @Nullable Integer length,
                     final @Nullable String alias,
                     final LabelGenerator labelGenerator) {
        super(target, alias, labelGenerator);
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
