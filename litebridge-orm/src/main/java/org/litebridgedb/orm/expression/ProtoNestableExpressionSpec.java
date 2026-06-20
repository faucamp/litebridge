package org.litebridgedb.orm.expression;

public sealed interface ProtoNestableExpressionSpec extends ProtoExpressionSpec permits ProtoNestableBasicExprSpec, ProtoNestableTOExpr {

    ExpressionSpec target();

    default String column() {
        return switch (target()) {
            case ProtoExpressionSpec protoExpression -> protoExpression.column();
            case ColumnExpressionSpec columnExpression -> columnExpression.column().name();
            default -> throw new IllegalArgumentException("Invalid target expression: " + target());
        };
    }
}
