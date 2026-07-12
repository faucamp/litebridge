package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;

import java.util.Objects;

public final class SqlProtoExpressionResolver extends ProtoExpressionResolver {

    private @Nullable SelectSpec selectSpec;

    public SqlProtoExpressionResolver(@Nullable final SelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    public SqlProtoExpressionResolver() {
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable, final ClauseType clause) {
        return new SelectColumnSpec(getColumn(resolvable, clause));
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final QueryField queryField, final ClauseType clause) {
        throw new UnsupportedOperationException("QueryField not yet supported in SQL mode: " + queryField);
    }

    @Override
    protected Column getColumn(final Resolvable resolvable, final ClauseType clause) {
        Objects.requireNonNull(selectSpec, "SelectSpec table not set");

        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec) {
            return new Column(selectSpec.getTable(), resolvable.column(), protoExpressionSpec.alias());
        } else {
            return new Column(selectSpec.getTable(), resolvable.column());
        }
    }
}
