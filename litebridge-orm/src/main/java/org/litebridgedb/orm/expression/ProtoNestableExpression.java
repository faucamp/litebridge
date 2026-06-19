package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

public sealed interface ProtoNestableExpression extends ProtoExpression permits ProtoNestableBasicExpr, ProtoNestableTOExpr {

    Expression target();

    default String column() {
        return switch (target()) {
            case ProtoExpression protoExpression -> protoExpression.column();
            case ColumnExpression columnExpression -> columnExpression.column().name();
            default -> throw new IllegalArgumentException("Invalid target expression: " + target());
        };
    }

    @Override
    default Expression resolve(final Table table) {
        return ProtoExpressionRegistry.resolveNestableExpression(this, new Column(table, column(), alias()));
    }

    @Override
    default Expression resolve(final Column column) {
        return ProtoExpressionRegistry.resolveNestableExpression(this, column);
    }
}
