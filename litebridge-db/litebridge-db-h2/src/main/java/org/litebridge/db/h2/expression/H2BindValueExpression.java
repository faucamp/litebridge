package org.litebridge.db.h2.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.BindValueExpressionImpl;
import org.litebridge.db.spi.impl.expression.Cast;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

public class H2BindValueExpression extends BindValueExpressionImpl {

    private final Cast cast;

    /**
     * Creates a new {@link H2BindValueExpression} instance.
     *
     * @param index          the index of the bind value
     * @param size           the size of the bind value (e.g. for collection expressions)
     * @param columnType     the data type of the bind value
     * @param alias          the alias for the expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public H2BindValueExpression(final int index, final int size, final ColumnType columnType, @Nullable final String alias, final LabelGenerator labelGenerator) {
        super(index, size, columnType, null, labelGenerator);
        cast = new Cast(this, alias, columnType.dataType(), columnType().size(), labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (clause == ClauseType.SELECT && parent == null) {
            return cast.toSql(operation, clause);
        } else {
            return super.toSql(operation, clause, parent);
        }
    }
}
