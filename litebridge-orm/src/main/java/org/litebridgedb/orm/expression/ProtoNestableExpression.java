package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

public sealed interface ProtoNestableExpression extends ProtoExpression permits ProtoNestableBasicExpr, ProtoNestableTOExpr {

    ProtoExpression target();

    default String column() {
        return target().column();
    }

    @Override
    default Expression resolve(final Table table) {
        return ProtoExpressionRegistry.resolveNestableExpression(this, new Column(table, target().column(), alias()));
    }

    @Override
    default Expression resolve(final Column column) {
        return ProtoExpressionRegistry.resolveNestableExpression(this, column);
    }
}
