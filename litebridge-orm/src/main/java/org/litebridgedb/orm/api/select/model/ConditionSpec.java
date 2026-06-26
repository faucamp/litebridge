package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a lhs, an operator, and an optional rhs. The operator
 * dictates how the lhs will be compared to the provided rhs.
 */
public class ConditionSpec {

    private ExpressionSpec lhs;
    private Operator operator;
    private @Nullable Object value;

    public ExpressionSpec getLhs() {
        return lhs;
    }

    public void setLhs(final ExpressionSpec lhs) {
        this.lhs = lhs;
    }

    public void setLhs(final Column column) {
        this.lhs = new SelectColumnSpec(column);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(final @Nullable Object value) {
        this.value = value;
    }

    public Condition toCondition(final SelectExpressionMapper selectExpressionMapper) {
        final SelectExpression lhsSelectExpression = selectExpressionMapper.toSelectExpression(lhs);

        if (value instanceof SelectSpec selectSpec) {
            final Select select = selectSpec.toSelect();
            final SubselectExpression subselectExpression = selectExpressionMapper.sqlFunctionRegistry().select().subselect().create(select);
            return new Condition(lhsSelectExpression, operator, subselectExpression);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            return new Condition(lhsSelectExpression, operator, selectExpressionMapper.toSelectExpression(expressionSpec));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected lhs
            final SelectReference selectReference = selectExpressionMapper.sqlFunctionRegistry().select().reference().create(referencedColumn);
            return new Condition(lhsSelectExpression, operator, selectReference);
        }

        final LiteralExpression literalExpression = selectExpressionMapper.sqlFunctionRegistry().select().literal().create(value);
        return new Condition(lhsSelectExpression, operator, literalExpression);
    }
}
