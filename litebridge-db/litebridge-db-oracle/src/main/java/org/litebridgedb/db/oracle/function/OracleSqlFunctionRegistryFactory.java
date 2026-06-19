package org.litebridgedb.db.oracle.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.oracle.function.scalar.Substr;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SqlFunctionRegistryFactory;

/**
 * Oracle-specific {@link SqlFunctionRegistryFactory}.
 * <p>
 * This substitutes specific SQL functions for Oracle-specific ones.
 */
public final class OracleSqlFunctionRegistryFactory extends SqlFunctionRegistryFactory {

    /**
     * Constructs a new {@code OracleSqlFunctionRegistryFactory}.
     *
     * @param columnIdentifierGenerator The database provider's column identifier generator
     */
    public OracleSqlFunctionRegistryFactory(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(columnIdentifierGenerator);
    }

    /**
     * Creates a {@link Substr} expression instead of the default {@code SUBSTRING}.
     *
     * @param target Target expression to encapsulate.
     * @param start  Start index (first character is 1)
     * @param length Substring length; may be {@code null}
     * @return a {@link Substr} expression
     */
    @Override
    protected NestableExpression createSubstring(final ColumnExpression target, final int start, @Nullable final Integer length) {
        return new Substr(target, start, length, columnIdentifierGenerator);
    }
}
