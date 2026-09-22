package org.litebridge.db.oracle.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.oracle.function.scalar.Substr;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

/**
 * Oracle-specific {@link SqlFunctionRegistryFactory}.
 * <p>
 * This substitutes specific SQL functions for Oracle-specific ones.
 */
public final class OracleSqlFunctionRegistryFactory extends SqlFunctionRegistryFactory {

    /**
     * Constructs a new {@code OracleSqlFunctionRegistryFactory}.
     *
     * @param labelGenerator     the label generator for rendering aliases/identifiers
     * @param selectSqlGenerator The database provider's select SQL generator
     */
    public OracleSqlFunctionRegistryFactory(final LabelGenerator labelGenerator,
                                            final SelectSqlGenerator selectSqlGenerator) {
        super(labelGenerator, selectSqlGenerator);
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
    protected DelegateExpression createSubstring(final SelectExpression target, final int start, @Nullable final Integer length, final @Nullable String alias) {
        return new Substr(target, start, length, alias, labelGenerator);
    }
}
