package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

/**
 * A proto-expression capable of nesting other proto-expressions.
 * <p>
 * This interface is a specialised extension of {@link ProtoExpressionSpec} and encapsulates specifications
 * for expressions that can be nested within other expressions.
 */
public sealed interface ProtoNestableExpressionSpec extends ProtoExpressionSpec permits ProtoNestableBasicExprSpec, ProtoNestableTOExpr {

    /**
     * The nested target expression.
     * <p>
     * Target expressions are typically a column name to select via {@link SelectColumnSpec}
     * or {@link SelectFieldSpec}, but are not limited to these.
     *
     * @return the nested target expression.
     */
    ExpressionSpec target();

    /**
     * Retrieves the column name associated with the target expression.
     * <p>
     * The default implementation handles various types of target expressions and returns the appropriate column name.
     *
     * @return the column name retrieved from the target expression.
     * @throws IllegalArgumentException if the target expression is of an invalid type.
     */
    default String column() {
        return switch (target()) {
            case ProtoExpressionSpec protoExpression -> protoExpression.column();
            case ColumnExpressionSpec columnExpression -> columnExpression.column().name();
            default -> throw new IllegalArgumentException("Invalid target expression: " + target());
        };
    }
}
