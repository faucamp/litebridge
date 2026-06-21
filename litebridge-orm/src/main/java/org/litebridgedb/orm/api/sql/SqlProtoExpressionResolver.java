package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.api.select.impl.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

public final class SqlProtoExpressionResolver extends ProtoExpressionResolver {

    private final SqlSelectSpec selectSpec;

    public SqlProtoExpressionResolver(final SqlSelectSpec selectSpec) {
        this.selectSpec = selectSpec;
    }

    @Override
    protected ExpressionSpec resolveConvertSpec(final ConvertSpec<?> convertSpec) {
        // Register the conversion
        selectSpec.setValueTypeOverride(convertSpec.returnType());
        return resolveExpression(convertSpec.target());
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable) {
        return new SelectColumnSpec(getColumn(resolvable));
    }

    @Override
    protected Column getColumn(final Resolvable resolvable) {
        if (resolvable instanceof ProtoExpressionSpec protoExpressionSpec) {
            return new Column(selectSpec.getTable(), resolvable.column(), protoExpressionSpec.alias());
        } else {
            return new Column(selectSpec.getTable(), resolvable.column());
        }
    }
}
