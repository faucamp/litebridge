package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.Select;

/**
 * Factory to create expressions encapsulation a sub-select.
 */
@FunctionalInterface
public interface SubselectExpressionFactory {

    /**
     * Creates a sub-select expression.
     *
     * @param subselect Target sub-select.
     * @return A new column expression.
     */
    SubselectExpression create(Select subselect);
}
