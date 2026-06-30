package org.litebridgedb.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.model.ProtoExpressionResolver;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ProtoExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.meta.QueryField;

import java.util.Objects;

public final class SqlProtoExpressionResolver extends ProtoExpressionResolver {

    private @Nullable SelectSpec selectSpec;

    public SqlProtoExpressionResolver(@Nullable final SelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    public SqlProtoExpressionResolver() {
    }

    public @Nullable SelectSpec getSelectSpec() {
        return selectSpec;
    }

    public void setSelectSpec(@Nullable final SelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable) {
        return new SelectColumnSpec(getColumn(resolvable));
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final QueryField queryField) {
        throw new UnsupportedOperationException("QueryField not yet supported in SQL mode: " + queryField);
    }

    @Override
    protected Column getColumn(final Resolvable resolvable) {
        Objects.requireNonNull(selectSpec, "SelectSpec table not set");

        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec) {
            return new Column(selectSpec.getTable(), resolvable.column(), protoExpressionSpec.alias());
        } else {
            return new Column(selectSpec.getTable(), resolvable.column());
        }
    }
}
